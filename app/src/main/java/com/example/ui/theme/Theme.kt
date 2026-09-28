package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = Emerald500,
  onPrimary = Color.Black,
  primaryContainer = Emerald500.copy(alpha = 0.2f),
  onPrimaryContainer = Emerald400,
  secondary = Blue500,
  onSecondary = Color.White,
  secondaryContainer = Blue500.copy(alpha = 0.2f),
  onSecondaryContainer = Blue500,
  tertiary = Amber500,
  onTertiary = Color.Black,
  tertiaryContainer = Amber500.copy(alpha = 0.2f),
  onTertiaryContainer = Amber400,
  background = Slate950,
  onBackground = Slate100,
  surface = Slate900,
  onSurface = Slate100,
  surfaceVariant = Slate800,
  onSurfaceVariant = Slate300,
  error = Rose500,
  onError = Color.White,
  errorContainer = Rose500.copy(alpha = 0.2f),
  onErrorContainer = Rose400,
  outline = Slate700,
  outlineVariant = Slate800
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force sleek trading terminal dark theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
