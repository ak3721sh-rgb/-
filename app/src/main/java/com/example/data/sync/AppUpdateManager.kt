package com.example.data.sync

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class RemoteUpdateManifest(
    val versionCode: Int,
    val versionName: String,
    val releaseDate: String = "",
    val changelog: String,
    val apkUrl: String,
    val sha256: String = "",
    val sizeLabel: String = ""
)

sealed class UpdateCheckResult {
    data class UpdateAvailable(
        val installedVersionCode: Int,
        val installedVersionName: String,
        val manifest: RemoteUpdateManifest
    ) : UpdateCheckResult()

    data class UpToDate(
        val installedVersionCode: Int,
        val installedVersionName: String,
        val directApkUrl: String? = null,
        val message: String
    ) : UpdateCheckResult()

    data class Unavailable(
        val installedVersionCode: Int,
        val installedVersionName: String,
        val reason: String,
        val isOffline: Boolean
    ) : UpdateCheckResult()
}

/**
 * Play Creator Program compliant update checker:
 * Compares installed BuildConfig.VERSION_CODE/VERSION_NAME against an optional HTTPS JSON update
 * manifest or direct HTTPS APK link configured by the instructor. Download is launched strictly via
 * zero-permission Intent(Intent.ACTION_VIEW) — zero REQUEST_INSTALL_PACKAGES or dynamic loading.
 */
object AppUpdateManager {

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .build()
    }

    fun isValidPublicHttpsUrl(rawUrl: String): Boolean {
        val trimmed = rawUrl.trim()
        if (!trimmed.startsWith("https://", ignoreCase = true)) return false
        val lower = trimmed.lowercase()
        if (lower.contains("example.com") ||
            lower.contains("localhost") ||
            lower.contains("127.0.0.1") ||
            lower.contains("ais-dev-") ||
            lower.contains("ais-pre-") ||
            lower.contains("aistudio.google.com") ||
            lower.contains(".internal")
        ) {
            return false
        }
        return trimmed.length > 12
    }

    fun parseManifestJson(
        jsonText: String,
        installedVersionCode: Int = BuildConfig.VERSION_CODE,
        installedVersionName: String = BuildConfig.VERSION_NAME
    ): UpdateCheckResult {
        return try {
            val obj = JSONObject(jsonText.trim())
            val remoteCode = obj.optInt("versionCode", 0)
            val remoteName = obj.optString("versionName", "").trim()
            val releaseDate = obj.optString("releaseDate", "").trim()
                .ifBlank { obj.optString("publishedAt", "").trim() }
            val apkUrl = obj.optString("apkUrl", "").trim()
            val changelog = obj.optString("changelog", "بهبود رابط کاربری، پایداری و بهینه‌سازی مطالعه").trim()
            val sha256 = obj.optString("sha256", "").trim()
            val sizeLabel = obj.optString("sizeLabel", "").trim()

            if (remoteCode <= 0 || remoteName.isBlank() || !isValidPublicHttpsUrl(apkUrl)) {
                return UpdateCheckResult.Unavailable(
                    installedVersionCode = installedVersionCode,
                    installedVersionName = installedVersionName,
                    reason = "به‌روزرسانی در دسترس نیست — پرونده اطلاعات نسخه در سرور معتبر یا کامل نیست.",
                    isOffline = false
                )
            }

            if (remoteCode > installedVersionCode) {
                UpdateCheckResult.UpdateAvailable(
                    installedVersionCode = installedVersionCode,
                    installedVersionName = installedVersionName,
                    manifest = RemoteUpdateManifest(
                        versionCode = remoteCode,
                        versionName = remoteName,
                        releaseDate = releaseDate,
                        changelog = changelog,
                        apkUrl = apkUrl,
                        sha256 = sha256,
                        sizeLabel = sizeLabel
                    )
                )
            } else {
                UpdateCheckResult.UpToDate(
                    installedVersionCode = installedVersionCode,
                    installedVersionName = installedVersionName,
                    directApkUrl = apkUrl,
                    message = "شما در حال استفاده از آخرین نسخه رسمی ($installedVersionName — کد $installedVersionCode) هستید."
                )
            }
        } catch (_: Exception) {
            UpdateCheckResult.Unavailable(
                installedVersionCode = installedVersionCode,
                installedVersionName = installedVersionName,
                reason = "به‌روزرسانی در دسترس نیست — پاسخ سرور در قالب استاندارد JSON نیست.",
                isOffline = false
            )
        }
    }

    suspend fun checkForUpdate(
        isNetworkConnected: Boolean,
        configuredApkOrManifestUrl: String
    ): UpdateCheckResult = withContext(Dispatchers.IO) {
        val installedCode = BuildConfig.VERSION_CODE
        val installedName = BuildConfig.VERSION_NAME
        val cleanUrl = configuredApkOrManifestUrl.trim()

        if (!isValidPublicHttpsUrl(cleanUrl)) {
            return@withContext UpdateCheckResult.Unavailable(
                installedVersionCode = installedCode,
                installedVersionName = installedName,
                reason = "به‌روزرسانی در دسترس نیست — لینک دانلود عمومی هنوز منتشر نشده است.",
                isOffline = !isNetworkConnected
            )
        }

        if (!isNetworkConnected) {
            return@withContext UpdateCheckResult.Unavailable(
                installedVersionCode = installedCode,
                installedVersionName = installedName,
                reason = "اتصال اینترنت دستگاه برقرار نیست. برای بررسی به‌روزرسانی آنلاین لطفاً به اینترنت متصل شوید.",
                isOffline = true
            )
        }

        // If configured URL points to a manifest JSON feed, fetch & compare versionCode cleanly.
        if (cleanUrl.endsWith(".json", ignoreCase = true) || cleanUrl.contains("manifest", ignoreCase = true)) {
            return@withContext try {
                val request = Request.Builder().url(cleanUrl).get().build()
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext UpdateCheckResult.Unavailable(
                            installedVersionCode = installedCode,
                            installedVersionName = installedName,
                            reason = "به‌روزرسانی در دسترس نیست (کد وضعیت سرور: ${response.code}).",
                            isOffline = false
                        )
                    }
                    val body = response.body?.string().orEmpty()
                    parseManifestJson(body, installedCode, installedName)
                }
            } catch (_: Exception) {
                UpdateCheckResult.Unavailable(
                    installedVersionCode = installedCode,
                    installedVersionName = installedName,
                    reason = "برقراری ارتباط با سرور به‌روزرسانی ممکن نشد. اتصال اینترنت یا نشانی سرور را بررسی کنید.",
                    isOffline = true
                )
            }
        }

        // Direct APK URL registered by instructor
        UpdateCheckResult.UpToDate(
            installedVersionCode = installedCode,
            installedVersionName = installedName,
            directApkUrl = cleanUrl,
            message = "نشانی رسمی دریافت نسخه برنامه آماده است (نسخه فعلی نصب‌شده: $installedName — کد $installedCode)."
        )
    }
}
