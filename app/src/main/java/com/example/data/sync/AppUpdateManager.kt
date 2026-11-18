package com.example.data.sync

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
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
        val isOffline: Boolean,
        val isNotFound404: Boolean = false,
        val fallbackWebUrl: String? = null
    ) : UpdateCheckResult()
}

/**
 * Play Creator Program compliant update checker:
 * Compares installed BuildConfig.VERSION_CODE/VERSION_NAME against an optional HTTPS JSON update
 * manifest, GitHub Releases API, or direct HTTPS APK link configured by the instructor.
 * Download is launched strictly via zero-permission Intent(Intent.ACTION_VIEW).
 */
object AppUpdateManager {

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .followRedirects(true)
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

    fun extractGitHubRepo(rawUrl: String): Pair<String, String>? {
        val trimmed = rawUrl.trim()
        val match = Regex("""^https://(?:www\.)?github\.com/([^/]+)/([^/]+)(?:/.*)?$""", RegexOption.IGNORE_CASE)
            .find(trimmed) ?: return null
        val owner = match.groupValues[1].trim()
        val repo = match.groupValues[2].removeSuffix(".git").trim()
        if (owner.isBlank() || repo.isBlank()) return null
        return owner to repo
    }

    fun githubReleasesPageUrl(rawUrl: String): String? {
        val (owner, repo) = extractGitHubRepo(rawUrl) ?: return null
        return "https://github.com/$owner/$repo/releases"
    }

    fun isReleaseTagNewer(remoteTag: String, installedVersionName: String): Boolean {
        val remoteParts = parseSemverParts(remoteTag)
        val installedParts = parseSemverParts(installedVersionName)
        if (remoteParts.isEmpty() || installedParts.isEmpty()) return false
        val maxLen = maxOf(remoteParts.size, installedParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val inst = installedParts.getOrElse(i) { 0 }
            if (r > inst) return true
            if (r < inst) return false
        }
        return false
    }

    private fun parseSemverParts(version: String): List<Int> {
        val clean = version.trim().removePrefix("v").removePrefix("V")
        return Regex("""\d+""").findAll(clean).mapNotNull { it.value.toIntOrNull() }.take(3).toList()
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

        // 1. JSON manifest feed
        if (cleanUrl.endsWith(".json", ignoreCase = true) || cleanUrl.contains("manifest", ignoreCase = true)) {
            return@withContext try {
                val request = Request.Builder().url(cleanUrl).get().build()
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext UpdateCheckResult.Unavailable(
                            installedVersionCode = installedCode,
                            installedVersionName = installedName,
                            reason = "به‌روزرسانی در دسترس نیست (کد وضعیت سرور: ${response.code}).",
                            isOffline = false,
                            isNotFound404 = response.code == 404,
                            fallbackWebUrl = githubReleasesPageUrl(cleanUrl)
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

        // 2. GitHub Releases live discovery (finds real published .apk asset and avoids S3 HEAD 403)
        val ghRepo = extractGitHubRepo(cleanUrl)
        if (ghRepo != null && cleanUrl.contains("/releases", ignoreCase = true)) {
            val (owner, repo) = ghRepo
            val apiResult = discoverViaGitHubReleasesApi(
                owner = owner,
                repo = repo,
                installedCode = installedCode,
                installedName = installedName
            )
            if (apiResult != null) {
                return@withContext apiResult
            }
        }

        // 3. Direct HTTPS APK URL probe using 1-byte Range GET (preserves CDN/S3 GET-signed links)
        return@withContext try {
            val probeRequest = Request.Builder()
                .url(cleanUrl)
                .get()
                .header("Range", "bytes=0-0")
                .build()
            httpClient.newCall(probeRequest).execute().use { response ->
                when {
                    response.isSuccessful || response.code == 206 -> UpdateCheckResult.UpToDate(
                        installedVersionCode = installedCode,
                        installedVersionName = installedName,
                        directApkUrl = cleanUrl,
                        message = "فایل نصبی رسمی آماده دریافت مستقیم است (نسخه فعلی نصب‌شده: $installedName — کد $installedCode)."
                    )
                    response.code == 404 -> UpdateCheckResult.Unavailable(
                        installedVersionCode = installedCode,
                        installedVersionName = installedName,
                        reason = "فایل APK نسخه جدید هنوز در گیت‌هاب منتشر نشده است (کد ۴۰۴).\nبرای رفع این مشکل، در گوگل ای‌آی استودیو از منوی تنظیمات (بالای صفحه) گزینه Generate APK یا Push to GitHub کامل پروژه را اجرا کنید، یا فایل APK را در بخش Releases گیت‌هاب آپلود نمایید.",
                        isOffline = false,
                        isNotFound404 = true,
                        fallbackWebUrl = githubReleasesPageUrl(cleanUrl)
                    )
                    else -> UpdateCheckResult.Unavailable(
                        installedVersionCode = installedCode,
                        installedVersionName = installedName,
                        reason = "پاسخ سرور برای نشانی APK آماده نیست (کد وضعیت: ${response.code}).",
                        isOffline = false,
                        fallbackWebUrl = githubReleasesPageUrl(cleanUrl)
                    )
                }
            }
        } catch (_: Exception) {
            UpdateCheckResult.Unavailable(
                installedVersionCode = installedCode,
                installedVersionName = installedName,
                reason = "برقراری ارتباط با سرور دریافت APK ممکن نشد. اتصال اینترنت یا نشانی ثبت‌شده در اتاق مدرس را بررسی کنید.",
                isOffline = true
            )
        }
    }

    private fun discoverViaGitHubReleasesApi(
        owner: String,
        repo: String,
        installedCode: Int,
        installedName: String
    ): UpdateCheckResult? {
        return try {
            val apiUrl = "https://api.github.com/repos/$owner/$repo/releases"
            val request = Request.Builder()
                .url(apiUrl)
                .get()
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "MindConnectionAndroid/$installedName")
                .build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    if (response.code == 404) {
                        return UpdateCheckResult.Unavailable(
                            installedVersionCode = installedCode,
                            installedVersionName = installedName,
                            reason = "مخزن یا بخش Releases در گیت‌هاب هنوز فایل نصبی منتشرشده‌ای ندارد (کد ۴۰۴).\nاگر فقط ۱ یا ۲ فایل را دستی آپلود کرده‌اید، کل پروژه از گیت‌هاب ساخته نمی‌شود؛ در Google AI Studio گزینه Generate APK یا Push to GitHub را بزنید.",
                            isOffline = false,
                            isNotFound404 = true,
                            fallbackWebUrl = "https://github.com/$owner/$repo/releases"
                        )
                    }
                    return null
                }

                val body = response.body?.string().orEmpty()
                val releases = JSONArray(body)
                for (i in 0 until releases.length()) {
                    val release = releases.optJSONObject(i) ?: continue
                    if (release.optBoolean("draft", false)) continue
                    val assets = release.optJSONArray("assets") ?: continue
                    var matchedApkUrl = ""
                    var matchedApkSize = 0L
                    for (j in 0 until assets.length()) {
                        val asset = assets.optJSONObject(j) ?: continue
                        val assetName = asset.optString("name", "")
                        val dlUrl = asset.optString("browser_download_url", "").trim()
                        if (assetName.endsWith(".apk", ignoreCase = true) && isValidPublicHttpsUrl(dlUrl)) {
                            matchedApkUrl = dlUrl
                            matchedApkSize = asset.optLong("size", 0L)
                            break
                        }
                    }
                    if (matchedApkUrl.isNotBlank()) {
                        val tag = release.optString("tag_name", "").trim()
                        val publishedAt = release.optString("published_at", "").take(10)
                        val changelog = release.optString("body", "").trim()
                            .ifBlank { "نسخه رسمی «هنر ارتباط ذهن‌ها» آماده نصب و به‌روزرسانی است." }
                        val sizeMb = if (matchedApkSize > 0L) {
                            String.format(java.util.Locale.US, "%.1f MB", matchedApkSize / (1024.0 * 1024.0))
                        } else ""

                        return if (isReleaseTagNewer(tag, installedName)) {
                            UpdateCheckResult.UpdateAvailable(
                                installedVersionCode = installedCode,
                                installedVersionName = installedName,
                                manifest = RemoteUpdateManifest(
                                    versionCode = installedCode + 1,
                                    versionName = tag.removePrefix("v").removePrefix("V").ifBlank { tag },
                                    releaseDate = publishedAt,
                                    changelog = changelog,
                                    apkUrl = matchedApkUrl,
                                    sizeLabel = sizeMb
                                )
                            )
                        } else {
                            UpdateCheckResult.UpToDate(
                                installedVersionCode = installedCode,
                                installedVersionName = installedName,
                                directApkUrl = matchedApkUrl,
                                message = "فایل نصبی رسمی ($tag${if (sizeMb.isNotBlank()) " • $sizeMb" else ""}) آماده دریافت مستقیم است. نسخه نصب‌شده روی گوشی شما: $installedName."
                            )
                        }
                    }
                }

                // Repo exists, but zero releases with .apk assets exist yet
                UpdateCheckResult.Unavailable(
                    installedVersionCode = installedCode,
                    installedVersionName = installedName,
                    reason = "در مخزن گیت‌هاب شما ($owner/$repo) هنوز هیچ نسخه APK منتشرشده‌ای در بخش Releases وجود ندارد (کد ۴۰۴).\nبرای دریافت سریع APK: در Google AI Studio منوی تنظیمات (⚙️) ← Generate APK را بزنید یا کل پروژه را یکجا Push to GitHub کنید.",
                    isOffline = false,
                    isNotFound404 = true,
                    fallbackWebUrl = "https://github.com/$owner/$repo/releases"
                )
            }
        } catch (_: Exception) {
            null
        }
    }
}
