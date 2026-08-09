package com.habitama.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val HabitamaBackground = Color(0xFFFFF9EE)
val HabitamaSurface = Color(0xFFFFFCF6)
val HabitamaText = Color(0xFF30251F)
val HabitamaPrimary = Color(0xFF2F968A)
val HabitamaPrimaryDark = Color(0xFF1F746A)
val HabitamaSuccess = Color(0xFF5D9B70)
val HabitamaAccent = Color(0xFFE8903B)
val HabitamaLine = Color(0xFFE4D8C5)
val HabitamaLeaf = Color(0xFFB9CFAB)
val HabitamaRose = Color(0xFFE87582)
val HabitamaBlue = Color(0xFF5D91C8)

private val HabitamaColors = lightColorScheme(
    primary = HabitamaPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEFEA),
    onPrimaryContainer = HabitamaPrimaryDark,
    secondary = HabitamaSuccess,
    tertiary = HabitamaAccent,
    background = HabitamaBackground,
    onBackground = HabitamaText,
    surface = HabitamaSurface,
    onSurface = HabitamaText,
    surfaceVariant = Color(0xFFF3EDDF),
    onSurfaceVariant = Color(0xFF75695E),
    outline = HabitamaLine,
)

private val HabitamaTypography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 29.sp, lineHeight = 38.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 29.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 29.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 22.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 14.sp),
)

@Composable
fun HabitamaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = HabitamaColors, typography = HabitamaTypography, content = content)
}
