package com.habitama.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SystemUpdateAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.habitama.app.BuildConfig
import com.habitama.app.ui.theme.HabitamaPrimary
import com.habitama.app.update.AppUpdateManager
import com.habitama.app.update.InstallResult
import com.habitama.app.update.UpdateCheckResult
import com.habitama.app.update.UpdateDownloadState
import com.habitama.app.update.UpdateManifest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun AppUpdateCard() {
    val context = LocalContext.current
    val manager = remember { AppUpdateManager(context) }
    val scope = rememberCoroutineScope()
    var checking by remember { mutableStateOf(false) }
    var checkResult by remember { mutableStateOf<UpdateCheckResult?>(null) }
    var manifest by remember { mutableStateOf<UpdateManifest?>(null) }
    var downloadState by remember { mutableStateOf<UpdateDownloadState>(UpdateDownloadState.Idle) }
    var pollingKey by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        scope.launch {
            checking = true
            message = null
            val result = manager.checkForUpdate(BuildConfig.VERSION_CODE)
            checkResult = result
            manifest = when (result) {
                is UpdateCheckResult.Available -> result.manifest
                is UpdateCheckResult.Latest -> result.manifest
                is UpdateCheckResult.Failed -> null
            }
            downloadState = if (result is UpdateCheckResult.Available) {
                manager.getDownloadState(result.manifest)
            } else {
                UpdateDownloadState.Idle
            }
            checking = false
            pollingKey++
        }
    }

    fun startInstall(downloadId: Long) {
        when (val result = manager.install(downloadId)) {
            InstallResult.Started -> message = "インストール画面を開きました。"
            is InstallResult.Failed -> message = result.message
            is InstallResult.PermissionRequired -> Unit
        }
    }

    val installPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        val ready = downloadState as? UpdateDownloadState.Ready
        if (ready != null) startInstall(ready.downloadId)
    }

    fun installOrRequestPermission(downloadId: Long) {
        when (val result = manager.install(downloadId)) {
            InstallResult.Started -> message = "インストール画面を開きました。"
            is InstallResult.Failed -> message = result.message
            is InstallResult.PermissionRequired -> {
                message = "「この提供元のアプリを許可」をオンにしてください。"
                installPermissionLauncher.launch(result.intent)
            }
        }
    }

    fun beginDownload() {
        val target = manifest ?: return
        manager.enqueue(target)
        downloadState = UpdateDownloadState.Pending
        message = "Androidのダウンロード管理へ登録しました。"
        pollingKey++
    }

    LaunchedEffect(Unit) { refresh() }
    LaunchedEffect(manifest?.versionCode, pollingKey) {
        val target = manifest ?: return@LaunchedEffect
        if (checkResult !is UpdateCheckResult.Available) return@LaunchedEffect
        while (true) {
            val state = manager.getDownloadState(target)
            downloadState = state
            if (state !is UpdateDownloadState.Pending &&
                state !is UpdateDownloadState.Running
            ) break
            delay(1_000)
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.SystemUpdateAlt, contentDescription = null, tint = HabitamaPrimary)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("アプリの更新", fontWeight = FontWeight.Bold)
                    Text(
                        "現在のバージョン ${BuildConfig.VERSION_NAME.removeSuffix("-debug")}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (checking) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
            }

            Text(
                text = updateStatusText(checkResult, downloadState),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )

            val running = downloadState as? UpdateDownloadState.Running
            if (running != null) {
                if (running.progress != null) {
                    LinearProgressIndicator(
                        progress = { running.progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("${(running.progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                } else {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }

            message?.let {
                Text(it, color = HabitamaPrimary, style = MaterialTheme.typography.bodyMedium)
            }

            when {
                checking -> Unit
                downloadState is UpdateDownloadState.Ready -> {
                    Button(
                        onClick = { installOrRequestPermission((downloadState as UpdateDownloadState.Ready).downloadId) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) {
                        Icon(Icons.Rounded.InstallMobile, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("APKをインストール")
                    }
                }
                downloadState is UpdateDownloadState.Pending || downloadState is UpdateDownloadState.Running -> {
                    OutlinedButton(
                        onClick = ::beginDownload,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("ダウンロードをやり直す")
                    }
                }
                checkResult is UpdateCheckResult.Available &&
                    downloadState !is UpdateDownloadState.Pending &&
                    downloadState !is UpdateDownloadState.Running -> {
                    Button(
                        onClick = ::beginDownload,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) {
                        Icon(Icons.Rounded.Download, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (downloadState is UpdateDownloadState.Paused || downloadState is UpdateDownloadState.Failed) {
                                "ダウンロードをやり直す"
                            } else {
                                "v${manifest?.version} をダウンロード"
                            },
                        )
                    }
                }
                else -> {
                    OutlinedButton(
                        onClick = ::refresh,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("もう一度確認")
                    }
                }
            }
        }
    }
}

private fun updateStatusText(
    result: UpdateCheckResult?,
    downloadState: UpdateDownloadState,
): String = when {
    downloadState is UpdateDownloadState.Pending -> "ダウンロードの開始を待っています。"
    downloadState is UpdateDownloadState.Running -> "APKをダウンロードしています。画面を閉じても続行します。"
    downloadState is UpdateDownloadState.Paused -> downloadState.message
    downloadState is UpdateDownloadState.Failed -> downloadState.message
    downloadState is UpdateDownloadState.Ready -> "APKのSHA-256検証が完了しました。インストールできます。"
    result is UpdateCheckResult.Available -> "新しいバージョン ${result.manifest.version} があります。${result.manifest.releaseNotes}"
    result is UpdateCheckResult.Latest -> "最新版を使用しています。"
    result is UpdateCheckResult.Failed -> result.message
    else -> "最新版があるか確認しています。"
}
