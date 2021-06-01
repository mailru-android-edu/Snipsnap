package com.wndenis.snipsnap.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = B400,
    primaryVariant = B400,
    secondary = N800,
    background = DN20,
    surface = DN40,
    onPrimary = N20,
    onSecondary = N0,
    onBackground = DN800,
)

private val LightColorPalette = lightColors(
    primary = B400,
    primaryVariant = B400,
    secondary = N800,
    background = N30,
    surface = N0,
    onPrimary = N20,
    onSecondary = N0,
    onBackground = N800,
)


@Composable
fun SnipsnapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
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