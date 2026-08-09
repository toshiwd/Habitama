package com.habitama.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val HabitamaBackground = Color(0xFFF7F8FA)
val HabitamaText = Color(0xFF202124)
val HabitamaPrimary = Color(0xFF2374E1)
val HabitamaSuccess = Color(0xFF36A269)
val HabitamaAccent = Color(0xFFF3A712)

private val HabitamaColors = lightColorScheme(
    primary = HabitamaPrimary,
    onPrimary = Color.White,
    secondary = HabitamaSuccess,
    tertiary = HabitamaAccent,
    background = HabitamaBackground,
    onBackground = HabitamaText,
    surface = Color.White,
    onSurface = HabitamaText,
    surfaceVariant = Color(0xFFECEFF3),
)

@Composable
fun HabitamaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HabitamaColors,
        content = content,
    )
}
