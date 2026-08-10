package com.habitama.app.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

private const val MANIFEST_URL =
    "https://github.com/toshiwd/Habitama/releases/latest/download/version.json"
private const val PREFS_NAME = "app_update_download"
private const val KEY_DOWNLOAD_ID = "download_id"
private const val KEY_VERSION_CODE = "version_code"

sealed interface UpdateCheckResult {
    data class Available(val manifest: UpdateManifest) : UpdateCheckResult
    data class Latest(val manifest: UpdateManifest) : UpdateCheckResult
    data class Failed(val message: String) : UpdateCheckResult
}

sealed interface UpdateDownloadState {
    data object Idle : UpdateDownloadState
    data object Pending : UpdateDownloadState
    data class Running(val progress: Float?) : UpdateDownloadState
    data class Paused(val message: String) : UpdateDownloadState
    data class Failed(val message: String) : UpdateDownloadState
    data class Ready(val downloadId: Long) : UpdateDownloadState
}

sealed interface InstallResult {
    data object Started : InstallResult
    data class PermissionRequired(val intent: Intent) : InstallResult
    data class Failed(val message: String) : InstallResult
}

class AppUpdateManager(private val context: Context) {
    private val appContext = context.applicationContext
    private val downloadManager = appContext.getSystemService(DownloadManager::class.java)
    private val preferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun checkForUpdate(currentVersionCode: Int): UpdateCheckResult = withContext(Dispatchers.IO) {
        try {
            val separator = if (MANIFEST_URL.contains('?')) '&' else '?'
            val connection = URL("$MANIFEST_URL${separator}t=${System.currentTimeMillis()}")
                .openConnection() as HttpURLConnection
            try {
                connection.instanceFollowRedirects = true
                connection.connectTimeout = 15_000
                connection.readTimeout = 20_000
                connection.useCaches = false
                connection.setRequestProperty("Cache-Control", "no-cache")
                connection.setRequestProperty("User-Agent", "Habitama-Android")
                val status = connection.responseCode
                if (status !in 200..299) {
                    return@withContext UpdateCheckResult.Failed("更新情報を取得できませんでした（HTTP $status）")
                }
                val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val manifest = UpdateManifestParser.parse(body)
                if (manifest.isNewerThan(currentVersionCode)) {
                    UpdateCheckResult.Available(manifest)
                } else {
                    UpdateCheckResult.Latest(manifest)
                }
            } finally {
                connection.disconnect()
            }
        } catch (_: java.net.SocketTimeoutException) {
            UpdateCheckResult.Failed("通信がタイムアウトしました。接続を確認してもう一度お試しください。")
        } catch (error: Exception) {
            UpdateCheckResult.Failed("更新情報を確認できませんでした：${error.message ?: "通信エラー"}")
        }
    }

    fun enqueue(manifest: UpdateManifest): Long {
        clearPreviousDownload()
        val fileName = "Habitama-${manifest.version}.apk"
        val destination = appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?.resolve(fileName)
        if (destination?.exists() == true) destination.delete()

        val request = DownloadManager.Request(Uri.parse(manifest.apkUrl))
            .setTitle("Habitama ${manifest.version}")
            .setDescription("更新APKをダウンロードしています")
            .setMimeType(APK_MIME_TYPE)
            .setAllowedOverRoaming(false)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(appContext, Environment.DIRECTORY_DOWNLOADS, fileName)

        val downloadId = downloadManager.enqueue(request)
        preferences.edit {
            putLong(KEY_DOWNLOAD_ID, downloadId)
            putInt(KEY_VERSION_CODE, manifest.versionCode)
        }
        return downloadId
    }

    suspend fun getDownloadState(manifest: UpdateManifest): UpdateDownloadState = withContext(Dispatchers.IO) {
        val downloadId = storedDownloadId(manifest) ?: return@withContext UpdateDownloadState.Idle
        val query = DownloadManager.Query().setFilterById(downloadId)
        downloadManager.query(query)?.use { cursor ->
            if (!cursor.moveToFirst()) return@withContext UpdateDownloadState.Idle
            val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            when (status) {
                DownloadManager.STATUS_PENDING -> UpdateDownloadState.Pending
                DownloadManager.STATUS_RUNNING -> {
                    val downloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    UpdateDownloadState.Running(if (total > 0) downloaded.toFloat() / total else null)
                }
                DownloadManager.STATUS_PAUSED -> UpdateDownloadState.Paused("ダウンロードが一時停止しています。通信状態を確認してください。")
                DownloadManager.STATUS_FAILED -> UpdateDownloadState.Failed("ダウンロードに失敗しました。もう一度お試しください。")
                DownloadManager.STATUS_SUCCESSFUL -> verifyDownloadedApk(downloadId, manifest.sha256)
                else -> UpdateDownloadState.Idle
            }
        } ?: UpdateDownloadState.Idle
    }

    fun install(downloadId: Long): InstallResult {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !appContext.packageManager.canRequestPackageInstalls()) {
            return InstallResult.PermissionRequired(
                Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${appContext.packageName}")),
            )
        }
        val apkUri = downloadManager.getUriForDownloadedFile(downloadId)
            ?: return InstallResult.Failed("ダウンロードしたAPKを開けませんでした。再ダウンロードしてください。")
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(apkUri, APK_MIME_TYPE)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return try {
            appContext.startActivity(intent)
            InstallResult.Started
        } catch (_: Exception) {
            InstallResult.Failed("APKインストーラーを開けませんでした。端末の設定を確認してください。")
        }
    }

    fun discardDownload() {
        clearPreviousDownload()
    }

    private fun storedDownloadId(manifest: UpdateManifest): Long? {
        if (preferences.getInt(KEY_VERSION_CODE, -1) != manifest.versionCode) return null
        return preferences.getLong(KEY_DOWNLOAD_ID, -1L).takeIf { it >= 0 }
    }

    private fun clearPreviousDownload() {
        val previousId = preferences.getLong(KEY_DOWNLOAD_ID, -1L)
        if (previousId >= 0) downloadManager.remove(previousId)
        preferences.edit { clear() }
    }

    private fun verifyDownloadedApk(downloadId: Long, expectedSha256: String): UpdateDownloadState {
        val uri = downloadManager.getUriForDownloadedFile(downloadId)
            ?: return UpdateDownloadState.Failed("ダウンロードしたAPKを読み取れませんでした。")
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            appContext.contentResolver.openInputStream(uri)?.use { input ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    digest.update(buffer, 0, count)
                }
            } ?: return UpdateDownloadState.Failed("ダウンロードしたAPKを読み取れませんでした。")
            val actual = digest.digest().joinToString("") { "%02X".format(it) }
            if (actual.equals(expectedSha256, ignoreCase = true)) {
                UpdateDownloadState.Ready(downloadId)
            } else {
                UpdateDownloadState.Failed("APKの検証に失敗しました。安全のためインストールしません。")
            }
        } catch (_: Exception) {
            UpdateDownloadState.Failed("APKの安全性を確認できませんでした。再ダウンロードしてください。")
        }
    }

    private companion object {
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    }
}
