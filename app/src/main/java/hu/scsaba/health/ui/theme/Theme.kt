package hu.scsaba.health.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(

    primary = darkPrimary,
    onPrimary = onDarkPrimary,
    secondary = darkSecondary,
    secondaryVariant = darkSecondaryVariant,
    background = darkBase,
    surface = darkSurface,
    onSurface = onDarkSurface,
    onSecondary = onDarkSecondary
)

private val LightColorPalette = lightColors(
    primary = lightPrimary,
    onPrimary = onLightPrimary,
    secondary = lightSecondary,
    secondaryVariant = lightSecondaryVariant,
    background = lightBase,
    surface = lightSurface,
    onSurface = onLightSurface,
    onSecondary = onLightSecondary
)

@Composable
fun HealthTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}