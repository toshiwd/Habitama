package com.habitama.app.update

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppUpdateManagerTest {
    @Test
    fun publicManifestApkDownloadsAndPassesSha256Verification() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = AppUpdateManager(context)
        manager.discardDownload()

        val check = manager.checkForUpdate(currentVersionCode = 0)
        assertTrue("公開version.jsonを取得できませんでした: $check", check is UpdateCheckResult.Available)
        val manifest = (check as UpdateCheckResult.Available).manifest
        manager.enqueue(manifest)

        var state: UpdateDownloadState = UpdateDownloadState.Pending
        for (attempt in 0 until 180) {
            state = manager.getDownloadState(manifest)
            if (state is UpdateDownloadState.Ready || state is UpdateDownloadState.Failed) break
            delay(1_000)
        }

        try {
            assertTrue("APKの取得・SHA-256検証が完了しませんでした: $state", state is UpdateDownloadState.Ready)
        } finally {
            manager.discardDownload()
        }
    }
}
