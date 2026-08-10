package com.habitama.app.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateManifestTest {
    private val manifest = UpdateManifest(
        version = "0.4.0",
        versionCode = 4,
        apkUrl = "https://github.com/toshiwd/Habitama/releases/download/v0.4.0/Habitama-0.4.0.apk",
        sha256 = "A".repeat(64),
        releaseNotes = "更新",
    )

    @Test
    fun newerVersionCodeIsAvailable() {
        assertTrue(manifest.isNewerThan(3))
    }

    @Test
    fun sameOrNewerInstalledVersionIsLatest() {
        assertFalse(manifest.isNewerThan(4))
        assertFalse(manifest.isNewerThan(5))
    }
}
