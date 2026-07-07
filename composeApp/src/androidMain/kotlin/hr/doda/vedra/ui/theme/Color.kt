package hr.doda.vedra.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ---- Brand seed colors (from the design tool) -------------------------------
internal val Primary = Color(0xFF2196F3) // Sky / clear weather
internal val Secondary = Color(0xFF607D8B) // Cloud / overcast
internal val Tertiary = Color(0xFFDB7900) // Sun / warm accent
internal val Neutral = Color(0xFF73777E) // Surfaces / dividers

// ---- Tonal derivatives (Material 3 palette tones, hand-tuned for our seeds) -
// Light mode uses tones ~40 (key) / 90 (container).
// Dark  mode uses tones ~80 (key) / 30 (container) so contrast stays AA.
private val PrimaryLight = Color(0xFFD1E4FF)
private val OnPrimaryContainerLight = Color(0xFF001D36)
private val PrimaryDark = Color(0xFF9CCAFF)
private val OnPrimaryDark = Color(0xFF003258)
private val PrimaryContainerDark = Color(0xFF00497D)

private val SecondaryLight = Color(0xFFD3E3EC)
private val OnSecondaryContainerLight = Color(0xFF1A2A33)
private val SecondaryDark = Color(0xFFB6C8D2)
private val OnSecondaryDark = Color(0xFF223038)
private val SecondaryContainerDark = Color(0xFF38464F)

private val TertiaryLight = Color(0xFFFFDDB3)
private val OnTertiaryContainerLight = Color(0xFF291800)
private val TertiaryDark = Color(0xFFFFB86F)
private val OnTertiaryDark = Color(0xFF3F2700)
private val TertiaryContainerDark = Color(0xFF5B3900)

private val Background = Color(0xFFFAFCFF)
private val OnBackground = Color(0xFF1A1C1E)
private val SurfaceVariant = Color(0xFFDFE2EB)
private val OnSurfaceVariant = Color(0xFF43474E)
private val OutlineVariant = Color(0xFFC3C6CF)

private val BackgroundDark = Color(0xFF101418)
private val OnBackgroundDark = Color(0xFFE2E2E6)
private val SurfaceVariantDark = Color(0xFF43474E)
private val OnSurfaceVariantDark = Color(0xFFC3C6CF)
private val OutlineVariantDark = Color(0xFF43474E)

private val ErrorLight = Color(0xFFBA1A1A)
private val ErrorContainerL = Color(0xFFFFDAD6)
private val OnErrorContainerL = Color(0xFF410002)
private val ErrorDark = Color(0xFFFFB4AB)
private val OnErrorDark = Color(0xFF690005)
private val ErrorContainerD = Color(0xFF93000A)
private val OnErrorContainerD = Color(0xFFFFDAD6)

// ---- Material 3 schemes -----------------------------------------------------
val LightVedraColors =
    lightColorScheme(
        primary = Primary,
        onPrimary = Color.White,
        primaryContainer = PrimaryLight,
        onPrimaryContainer = OnPrimaryContainerLight,
        secondary = Secondary,
        onSecondary = Color.White,
        secondaryContainer = SecondaryLight,
        onSecondaryContainer = OnSecondaryContainerLight,
        tertiary = Tertiary,
        onTertiary = Color.White,
        tertiaryContainer = TertiaryLight,
        onTertiaryContainer = OnTertiaryContainerLight,
        background = Background,
        onBackground = OnBackground,
        surface = Background,
        onSurface = OnBackground,
        surfaceVariant = SurfaceVariant,
        onSurfaceVariant = OnSurfaceVariant,
        outline = Neutral,
        outlineVariant = OutlineVariant,
        error = ErrorLight,
        onError = Color.White,
        errorContainer = ErrorContainerL,
        onErrorContainer = OnErrorContainerL,
        inversePrimary = PrimaryDark,
        inverseSurface = Color(0xFF2F3033),
        inverseOnSurface = Color(0xFFF1F0F4),
        scrim = Color.Black,
    )

val DarkVedraColors =
    darkColorScheme(
        primary = PrimaryDark,
        onPrimary = OnPrimaryDark,
        primaryContainer = PrimaryContainerDark,
        onPrimaryContainer = PrimaryLight,
        secondary = SecondaryDark,
        onSecondary = OnSecondaryDark,
        secondaryContainer = SecondaryContainerDark,
        onSecondaryContainer = SecondaryLight,
        tertiary = TertiaryDark,
        onTertiary = OnTertiaryDark,
        tertiaryContainer = TertiaryContainerDark,
        onTertiaryContainer = TertiaryLight,
        background = BackgroundDark,
        onBackground = OnBackgroundDark,
        surface = BackgroundDark,
        onSurface = OnBackgroundDark,
        surfaceVariant = SurfaceVariantDark,
        onSurfaceVariant = OnSurfaceVariantDark,
        outline = Color(0xFF8C9197),
        outlineVariant = OutlineVariantDark,
        error = ErrorDark,
        onError = OnErrorDark,
        errorContainer = ErrorContainerD,
        onErrorContainer = OnErrorContainerD,
        inversePrimary = Primary,
        inverseSurface = Color(0xFFE2E2E6),
        inverseOnSurface = Color(0xFF2F3033),
        scrim = Color.Black,
    )
