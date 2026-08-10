package com.habitama.app.update

import org.json.JSONObject
import java.net.URI

data class UpdateManifest(
    val version: String,
    val versionCode: Int,
    val apkUrl: String,
    val sha256: String,
    val releaseNotes: String,
)

object UpdateManifestParser {
    private val sha256Pattern = Regex("^[0-9a-fA-F]{64}$")

    fun parse(rawJson: String): UpdateManifest {
        val json = JSONObject(rawJson.trimStart('\uFEFF').trim())
        val version = json.getString("version").trim()
        val versionCode = json.getInt("versionCode")
        val apkUrl = json.getString("apkUrl").trim()
        val sha256 = json.getString("sha256").trim().uppercase()
        val releaseNotes = json.optString("releaseNotes", "安定性と使いやすさを改善しました。").trim()
        val uri = URI(apkUrl)

        require(version.matches(Regex("^\\d+\\.\\d+\\.\\d+$"))) { "version is invalid" }
        require(versionCode > 0) { "versionCode is invalid" }
        require(uri.scheme == "https" && uri.host.equals("github.com", ignoreCase = true)) {
            "apkUrl must be an HTTPS GitHub URL"
        }
        require(sha256Pattern.matches(sha256)) { "sha256 is invalid" }

        return UpdateManifest(version, versionCode, apkUrl, sha256, releaseNotes)
    }
}

fun UpdateManifest.isNewerThan(currentVersionCode: Int): Boolean = versionCode > currentVersionCode
