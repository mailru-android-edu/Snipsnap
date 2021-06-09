package com.wndenis.snipsnap.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.wndenis.snipsnap.R

@Composable
fun SnipsnapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val b400 = colorResource(R.color.B400)
    val dn20 = colorResource(R.color.DN20)
    val dn40 = colorResource(R.color.DN40)
    val dn800 = colorResource(R.color.DN800)
    val g500 = colorResource(R.color.G500)
    val n0 = colorResource(R.color.N0)
    val n20 = colorResource(R.color.N20)
    val n30 = colorResource(R.color.N30)
    val n800 = colorResource(R.color.N800)

    val darkColorPalette = darkColors(
        primary = b400,
        primaryVariant = b400,
        secondary = n800,
        background = dn20,
        surface = dn40,
        onPrimary = n20,
        onSecondary = n0,
        onBackground = dn800,
    )

    val lightColorPalette = lightColors(
        primary = b400,
        primaryVariant = b400,
        secondary = n800,
        background = n30,
        surface = n0,
        onPrimary = n20,
        onSecondary = n0,
        onBackground = n800,
    )

    val colors = if (darkTheme) {
        darkColorPalette
    } else {
        lightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
