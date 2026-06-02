package hr.doda.vedra.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Wrap your top-level Composable in [VedraTheme]. By default it follows
 * the system's light/dark setting and uses Android 12+ wallpaper-based
 * dynamic color when available — falling back to the brand palette
 * defined in [LightVedraColors] / [DarkVedraColors].
 *
 * @param darkTheme    override the system dark setting (e.g. for previews / screenshot tests).
 * @param dynamicColor disable to lock the app to the brand palette.
 */
@Composable
fun VedraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkVedraColors
        else      -> LightVedraColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = VedraTypography,
        content = content,
    )
}
