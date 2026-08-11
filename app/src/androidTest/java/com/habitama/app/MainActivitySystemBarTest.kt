package com.habitama.app

import androidx.core.view.WindowCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivitySystemBarTest {
    @Test
    fun lightBackgroundUsesDarkStatusBarIcons() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
                assertTrue(controller.isAppearanceLightStatusBars)
                assertTrue(controller.isAppearanceLightNavigationBars)
            }
        }
    }
}
