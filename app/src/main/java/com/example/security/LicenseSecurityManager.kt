package com.example.security

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import com.example.data.local.AppDatabase
import com.example.data.model.AccessRequestEntity
import com.example.data.model.LicenseEntity
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

sealed class ActivationResult {
    data object Success : ActivationResult()
    data class Error(val message: String) : ActivationResult()
}

sealed class PinVerifyStatus {
    data object Success : PinVerifyStatus()
    data class Invalid(val attemptsRemaining: Int) : PinVerifyStatus()
    data class LockedOut(val remainingSeconds: Long) : PinVerifyStatus()
}

sealed class OtpIssueResult {
    data class Issued(val code: String, val ttlSeconds: Int) : OtpIssueResult()
    data class LockedOut(val remainingSeconds: Long) : OtpIssueResult()
}

sealed class OtpVerifyResult {
    data object VerifiedAndConsumed : OtpVerifyResult()
    data object NotIssuedOrUsed : OtpVerifyResult()
    data object Expired : OtpVerifyResult()
    data class Invalid(val attemptsRemaining: Int) : OtpVerifyResult()
    data class LockedOut(val remainingSeconds: Long) : OtpVerifyResult()
}

class LicenseSecurityManager(
    private val context: Context,
    private val database: AppDatabase
) {
    private val prefs = context.getSharedPreferences("mind_sec_store", Context.MODE_PRIVATE)
    private val random = SecureRandom()

    companion object {
        private const val PREF_DEVICE_ID = "device_fingerprint_id"
        private const val PREF_PIN_HASH = "instructor_pin_hash"
        private const val PREF_PIN_SALT = "instructor_pin_salt"
        private const val PREF_PIN_FAILED_COUNT = "instructor_pin_failed_count"
        private const val PREF_PIN_LOCKOUT_UNTIL = "instructor_pin_lockout_until"

        private const val PREF_OTP_HASH = "local_otp_hash"
        private const val PREF_OTP_SALT = "local_otp_salt"
        private const val PREF_OTP_EXPIRES_AT = "local_otp_expires_at"
        private const val PREF_OTP_CONSUMED = "local_otp_consumed"
        private const val PREF_OTP_FAILED_COUNT = "local_otp_failed_count"
        private const val PREF_OTP_LOCKOUT_UNTIL = "local_otp_lockout_until"

        private const val MAX_PIN_ATTEMPTS = 5
        private const val MAX_OTP_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MS = 60_000L
        private const val OTP_TTL_SECONDS = 120

        private const val CHAR_POOL = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        private const val SIGN_ALGO = "HmacSHA256"
        private val INTERNAL_VERIFIER_TAG = byteArrayOf(
            0x4D, 0x69, 0x6E, 0x64, 0x43, 0x6F, 0x6E, 0x6E,
            0x41, 0x6B, 0x62, 0x61, 0x72, 0x39, 0x36, 0x38
        )
    }

    fun getOrCreateDeviceId(): String {
        var id = prefs.getString(PREF_DEVICE_ID, null)
        if (id.isNullOrBlank()) {
            val token = UUID.randomUUID().toString().replace("-", "").take(12).uppercase()
            id = "DEV-$token"
            prefs.edit().putString(PREF_DEVICE_ID, id).apply()
        }
        return id
    }

    // =====================================================
    // Salted SHA-256 Instructor Authentication + Rate Limit
    // =====================================================
    fun hasInstructorPinSet(): Boolean {
        return !prefs.getString(PREF_PIN_HASH, null).isNullOrBlank() &&
            !prefs.getString(PREF_PIN_SALT, null).isNullOrBlank()
    }

    fun getPinLockoutRemainingSeconds(): Long {
        val lockoutUntil = prefs.getLong(PREF_PIN_LOCKOUT_UNTIL, 0L)
        val remainingMs = lockoutUntil - System.currentTimeMillis()
        return if (remainingMs > 0L) ((remainingMs + 999L) / 1000L) else 0L
    }

    fun setupInitialInstructorPin(pin: String): Boolean {
        val clean = pin.trim()
        if (clean.length < 6) return false
        val salt = ByteArray(16).also { random.nextBytes(it) }
        val hash = hashPinWithSalt(clean, salt)
        prefs.edit()
            .putString(PREF_PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(PREF_PIN_HASH, hash)
            .putInt(PREF_PIN_FAILED_COUNT, 0)
            .putLong(PREF_PIN_LOCKOUT_UNTIL, 0L)
            .apply()
        return true
    }

    fun verifyInstructorPinDetailed(pin: String): PinVerifyStatus {
        val lockSeconds = getPinLockoutRemainingSeconds()
        if (lockSeconds > 0L) {
            return PinVerifyStatus.LockedOut(lockSeconds)
        }

        val savedSalt = prefs.getString(PREF_PIN_SALT, null)
            ?: return PinVerifyStatus.Invalid(MAX_PIN_ATTEMPTS)
        val savedHash = prefs.getString(PREF_PIN_HASH, null)
            ?: return PinVerifyStatus.Invalid(MAX_PIN_ATTEMPTS)

        val saltBytes = Base64.decode(savedSalt, Base64.NO_WRAP)
        val candidateHash = hashPinWithSalt(pin.trim(), saltBytes)
        val isMatch = MessageDigest.isEqual(
            candidateHash.toByteArray(Charsets.UTF_8),
            savedHash.toByteArray(Charsets.UTF_8)
        )

        return if (isMatch) {
            prefs.edit()
                .putInt(PREF_PIN_FAILED_COUNT, 0)
                .putLong(PREF_PIN_LOCKOUT_UNTIL, 0L)
                .apply()
            PinVerifyStatus.Success
        } else {
            val fails = prefs.getInt(PREF_PIN_FAILED_COUNT, 0) + 1
            if (fails >= MAX_PIN_ATTEMPTS) {
                val lockoutUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS
                prefs.edit()
                    .putInt(PREF_PIN_FAILED_COUNT, 0)
                    .putLong(PREF_PIN_LOCKOUT_UNTIL, lockoutUntil)
                    .apply()
                PinVerifyStatus.LockedOut(LOCKOUT_DURATION_MS / 1000L)
            } else {
                prefs.edit().putInt(PREF_PIN_FAILED_COUNT, fails).apply()
                PinVerifyStatus.Invalid(MAX_PIN_ATTEMPTS - fails)
            }
        }
    }

    fun verifyInstructorPin(pin: String): Boolean {
        return verifyInstructorPinDetailed(pin) is PinVerifyStatus.Success
    }

    fun changeInstructorPinDetailed(currentPin: String, newPin: String): PinVerifyStatus {
        val verifyStatus = verifyInstructorPinDetailed(currentPin)
        if (verifyStatus !is PinVerifyStatus.Success) return verifyStatus
        return if (setupInitialInstructorPin(newPin)) {
            PinVerifyStatus.Success
        } else {
            PinVerifyStatus.Invalid(MAX_PIN_ATTEMPTS)
        }
    }

    fun changeInstructorPin(currentPin: String, newPin: String): Boolean {
        return changeInstructorPinDetailed(currentPin, newPin) is PinVerifyStatus.Success
    }

    // =====================================================
    // Genuine Local CSPRNG Single-Use Expiring OTP
    // (For sensitive on-device instructor actions; remote SMS OTP
    // requires external backend + SMS gateway infrastructure.)
    // =====================================================
    fun issueLocalConfirmationOtp(): OtpIssueResult {
        val lockUntil = prefs.getLong(PREF_OTP_LOCKOUT_UNTIL, 0L)
        val remainingMs = lockUntil - System.currentTimeMillis()
        if (remainingMs > 0L) {
            return OtpIssueResult.LockedOut((remainingMs + 999L) / 1000L)
        }

        val tokenInt = 100_000 + random.nextInt(900_000)
        val tokenStr = tokenInt.toString()
        val salt = ByteArray(16).also { random.nextBytes(it) }
        val hash = hashPinWithSalt(tokenStr, salt)
        val expiresAt = System.currentTimeMillis() + (OTP_TTL_SECONDS * 1000L)

        prefs.edit()
            .putString(PREF_OTP_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(PREF_OTP_HASH, hash)
            .putLong(PREF_OTP_EXPIRES_AT, expiresAt)
            .putBoolean(PREF_OTP_CONSUMED, false)
            .putInt(PREF_OTP_FAILED_COUNT, 0)
            .apply()

        return OtpIssueResult.Issued(code = tokenStr, ttlSeconds = OTP_TTL_SECONDS)
    }

    fun verifyAndConsumeConfirmationOtp(inputCode: String, nowMs: Long = System.currentTimeMillis()): OtpVerifyResult {
        val lockUntil = prefs.getLong(PREF_OTP_LOCKOUT_UNTIL, 0L)
        val lockRemaining = lockUntil - nowMs
        if (lockRemaining > 0L) {
            return OtpVerifyResult.LockedOut((lockRemaining + 999L) / 1000L)
        }

        val consumed = prefs.getBoolean(PREF_OTP_CONSUMED, true)
        val savedSalt = prefs.getString(PREF_OTP_SALT, null)
        val savedHash = prefs.getString(PREF_OTP_HASH, null)
        if (consumed || savedSalt.isNullOrBlank() || savedHash.isNullOrBlank()) {
            return OtpVerifyResult.NotIssuedOrUsed
        }

        val expiresAt = prefs.getLong(PREF_OTP_EXPIRES_AT, 0L)
        if (nowMs > expiresAt) {
            prefs.edit()
                .putBoolean(PREF_OTP_CONSUMED, true)
                .remove(PREF_OTP_HASH)
                .remove(PREF_OTP_SALT)
                .apply()
            return OtpVerifyResult.Expired
        }

        val saltBytes = Base64.decode(savedSalt, Base64.NO_WRAP)
        val candidateHash = hashPinWithSalt(inputCode.trim(), saltBytes)
        val isMatch = MessageDigest.isEqual(
            candidateHash.toByteArray(Charsets.UTF_8),
            savedHash.toByteArray(Charsets.UTF_8)
        )

        return if (isMatch) {
            // Immediately invalidate so token can never be replayed
            prefs.edit()
                .putBoolean(PREF_OTP_CONSUMED, true)
                .remove(PREF_OTP_HASH)
                .remove(PREF_OTP_SALT)
                .putInt(PREF_OTP_FAILED_COUNT, 0)
                .putLong(PREF_OTP_LOCKOUT_UNTIL, 0L)
                .apply()
            OtpVerifyResult.VerifiedAndConsumed
        } else {
            val fails = prefs.getInt(PREF_OTP_FAILED_COUNT, 0) + 1
            if (fails >= MAX_OTP_ATTEMPTS) {
                prefs.edit()
                    .putInt(PREF_OTP_FAILED_COUNT, 0)
                    .putLong(PREF_OTP_LOCKOUT_UNTIL, nowMs + LOCKOUT_DURATION_MS)
                    .putBoolean(PREF_OTP_CONSUMED, true)
                    .remove(PREF_OTP_HASH)
                    .remove(PREF_OTP_SALT)
                    .apply()
                OtpVerifyResult.LockedOut(LOCKOUT_DURATION_MS / 1000L)
            } else {
                prefs.edit().putInt(PREF_OTP_FAILED_COUNT, fails).apply()
                OtpVerifyResult.Invalid(MAX_OTP_ATTEMPTS - fails)
            }
        }
    }

    private fun hashPinWithSalt(pin: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        var current = salt + pin.toByteArray(Charsets.UTF_8)
        repeat(1000) {
            digest.reset()
            digest.update(salt)
            current = digest.digest(current)
        }
        return Base64.encodeToString(current, Base64.NO_WRAP)
    }

    // =====================================================
    // Signed Non-Guessable One-Time License Codes
    // Format: MIND-<payload8>-<sig4>
    // payload8[0] = duration code: W(7d), M(30d), Q(90d), Y(365d), U(unlimited)
    // payload8[1] = maxDevices char: 1, 2, 3, 5
    // payload8[2..7] = 6 CSPRNG chars
    // =====================================================
    private fun durationCharToDays(c: Char): Int? = when (c) {
        'W' -> 7
        'M' -> 30
        'Q' -> 90
        'Y' -> 365
        'U' -> null
        else -> null
    }

    private fun daysToDurationChar(days: Int?): Char = when (days) {
        7 -> 'W'
        30 -> 'M'
        90 -> 'Q'
        365 -> 'Y'
        else -> 'U'
    }

    private fun computeSignatureTag(payload8: String): String {
        val mac = Mac.getInstance(SIGN_ALGO)
        mac.init(SecretKeySpec(INTERNAL_VERIFIER_TAG, SIGN_ALGO))
        val bytes = mac.doFinal(payload8.toByteArray(Charsets.UTF_8))
        return (0 until 4).joinToString("") { idx ->
            val unsigned = bytes[idx].toInt() and 0xFF
            CHAR_POOL[unsigned % CHAR_POOL.length].toString()
        }
    }

    fun isValidSignedKeyStructure(cleanKey: String): Boolean {
        val parts = cleanKey.split("-")
        if (parts.size != 3 || parts[0] != "MIND") return false
        val payload = parts[1]
        val sig = parts[2]
        if (payload.length != 8 || sig.length != 4) return false
        if (payload[0] !in listOf('W', 'M', 'Q', 'Y', 'U')) return false
        if (payload[1] !in listOf('1', '2', '3', '5')) return false
        if (!payload.drop(2).all { it in CHAR_POOL }) return false
        return computeSignatureTag(payload) == sig
    }

    suspend fun generateNewLicenseKey(
        studentName: String? = null,
        studentContact: String? = null,
        validityDays: Int? = 90,
        maxDevices: Int = 1
    ): String {
        val durChar = daysToDurationChar(validityDays)
        val devChar = when {
            maxDevices <= 1 -> '1'
            maxDevices == 2 -> '2'
            maxDevices == 3 -> '3'
            else -> '5'
        }
        val randPart = (1..6).map { CHAR_POOL[random.nextInt(CHAR_POOL.length)] }.joinToString("")
        val payload = "$durChar$devChar$randPart"
        val sig = computeSignatureTag(payload)
        val key = "MIND-$payload-$sig"

        val now = System.currentTimeMillis()
        val expiresAt = validityDays?.let { now + it * 86_400_000L }

        val entity = LicenseEntity(
            licenseKey = key,
            status = "ACTIVE",
            maxDevices = devChar.digitToInt(),
            usedCount = 0,
            issuedAt = now,
            expiresAt = expiresAt,
            studentName = studentName?.takeIf { it.isNotBlank() } ?: "هنرجوی جدید",
            studentContact = studentContact?.takeIf { it.isNotBlank() }
        )
        database.licenseDao().insertLicense(entity)
        return key
    }

    suspend fun activateKey(inputKey: String): ActivationResult {
        val cleanKey = inputKey.trim().uppercase()
        if (cleanKey.isEmpty()) {
            return ActivationResult.Error("لطفاً کلید فعال‌سازی را وارد کنید.")
        }

        val deviceId = getOrCreateDeviceId()
        val now = System.currentTimeMillis()
        var license = database.licenseDao().getLicenseByKey(cleanKey)

        // If key is cryptographically valid (e.g. sent via SMS by instructor from another device)
        // and not yet recorded in this device's local Room DB, register local record with embedded expiry/maxDevices.
        if (license == null) {
            if (!isValidSignedKeyStructure(cleanKey)) {
                return ActivationResult.Error("کلید واردشده معتبر نیست یا ساختار امنیتی آن اشتباه است. لطفاً کلید صحیح را از مدرس (اکبر شیرزادخانی) دریافت کنید.")
            }
            val payload = cleanKey.split("-")[1]
            val days = durationCharToDays(payload[0])
            val maxDev = payload[1].digitToIntOrNull() ?: 1
            license = LicenseEntity(
                licenseKey = cleanKey,
                status = "ACTIVE",
                maxDevices = maxDev,
                usedCount = 0,
                issuedAt = now,
                expiresAt = days?.let { now + it * 86_400_000L },
                studentName = "هنرجوی دوره"
            )
            database.licenseDao().insertLicense(license)
        }

        if (license.status == "REVOKED") {
            return ActivationResult.Error("این کلید توسط مدرس ابطال شده و قابل استفاده نیست.")
        }

        if (license.expiresAt != null && now > license.expiresAt) {
            database.licenseDao().updateLicense(license.copy(status = "EXPIRED"))
            return ActivationResult.Error("مهلت اعتبار این کلید به پایان رسیده است. لطفاً کلید جدید دریافت کنید.")
        }

        val boundList = license.boundDeviceIds
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toMutableList()

        if (boundList.contains(deviceId)) {
            database.userProfileDao().setCourseUnlocked(cleanKey, deviceId)
            database.mentorBookDao().unlockAllBooks()
            database.chapterDao().unlockAllChapters()
            return ActivationResult.Success
        }

        if (license.usedCount >= license.maxDevices || license.status == "USED") {
            return ActivationResult.Error("ظرفیت استفاده از این کد (${license.maxDevices} دستگاه) تکمیل شده است.")
        }

        boundList.add(deviceId)
        val newUsedCount = boundList.size
        val newStatus = if (newUsedCount >= license.maxDevices) "USED" else "ACTIVE"

        val updatedLicense = license.copy(
            status = newStatus,
            boundDeviceId = license.boundDeviceId ?: deviceId,
            boundDeviceIds = boundList.joinToString(","),
            usedCount = newUsedCount,
            activatedAt = license.activatedAt ?: now
        )
        database.licenseDao().updateLicense(updatedLicense)
        database.userProfileDao().setCourseUnlocked(cleanKey, deviceId)
        database.mentorBookDao().unlockAllBooks()
        database.chapterDao().unlockAllChapters()

        return ActivationResult.Success
    }

    suspend fun revokeLicense(key: String) {
        database.licenseDao().updateStatus(key, "REVOKED")
    }

    suspend fun extendLicenseDays(key: String, extraDays: Int) {
        val current = database.licenseDao().getLicenseByKey(key) ?: return
        val base = maxOf(System.currentTimeMillis(), current.expiresAt ?: System.currentTimeMillis())
        val updated = current.copy(
            expiresAt = base + extraDays * 86_400_000L,
            status = if (current.status == "EXPIRED") {
                if (current.usedCount >= current.maxDevices) "USED" else "ACTIVE"
            } else current.status
        )
        database.licenseDao().updateLicense(updated)
    }

    suspend fun recordAccessRequest(
        studentName: String,
        studentContact: String,
        channel: String
    ) {
        val request = AccessRequestEntity(
            studentName = studentName.ifBlank { "هنرجو" },
            studentContact = studentContact,
            deviceId = getOrCreateDeviceId(),
            channel = channel,
            status = "PENDING",
            requestedAt = System.currentTimeMillis()
        )
        database.accessRequestDao().insertRequest(request)
    }

    fun requestKeyViaSms(studentName: String) {
        val deviceId = getOrCreateDeviceId()
        val message = "سلام و احترام، مدرس محترم جناب اکبر شیرزادخانی؛\nاینجانب $studentName درخواست کلید فعال‌سازی دوره هنر ارتباط ذهن‌ها را دارم.\nشناسه دستگاه من:\n$deviceId"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("sms:09138310889")
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun requestKeyViaEmail(studentName: String) {
        val deviceId = getOrCreateDeviceId()
        val message = "سلام و احترام، مدرس محترم جناب اکبر شیرزادخانی؛\nاینجانب $studentName درخواست کلید فعال‌سازی دوره هنر ارتباط ذهن‌ها را دارم.\n\nشناسه سخت‌افزاری دستگاه من:\n$deviceId\n\nبا تشکر"
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:aram96822@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, "درخواست کلید فعال‌سازی هنر ارتباط ذهن‌ها - $studentName")
            putExtra(Intent.EXTRA_TEXT, message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }
}
