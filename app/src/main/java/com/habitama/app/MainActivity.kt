package com.habitama.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.habitama.app.ui.HabitamaRoot
import com.habitama.app.ui.theme.HabitamaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitamaTheme {
                HabitamaRoot()
            }
        }
    }
}
