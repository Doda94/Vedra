package hr.doda.vedra.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import hr.doda.vedra.R

// ---- Font families ----------------------------------------------------------
// Inter is shipped with three optical sizes (18/24/28 pt). We pick the
// optical size closest to the actual rendered size for each typography role:
//   18 pt  → 12-18 sp text
//   24 pt  → 18-26 sp text
//   28 pt  → 28+ sp text
//
// Manrope ships as a single optical size, so we use it for every weight
// of headlines and titles.

private val Manrope = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light,      FontWeight.Light),
    Font(R.font.manrope_regular,    FontWeight.Normal),
    Font(R.font.manrope_medium,     FontWeight.Medium),
    Font(R.font.manrope_semibold,   FontWeight.SemiBold),
    Font(R.font.manrope_bold,       FontWeight.Bold),
    Font(R.font.manrope_extrabold,  FontWeight.ExtraBold),
)

// Body / label scale (12-18 sp) — Inter at the 18 pt optical size.
private val Inter18 = FontFamily(
    Font(R.font.inter_18pt_thin,       FontWeight.Thin),
    Font(R.font.inter_18pt_extralight, FontWeight.ExtraLight),
    Font(R.font.inter_18pt_light,      FontWeight.Light),
    Font(R.font.inter_18pt_regular,    FontWeight.Normal),
    Font(R.font.inter_18pt_medium,     FontWeight.Medium),
    Font(R.font.inter_18pt_semibold,   FontWeight.SemiBold),
    Font(R.font.inter_18pt_bold,       FontWeight.Bold),
)

// ---- Typography role mapping -----------------------------------------------
// Display & headline & title  → Manrope (per design spec).
// Body & label                → Inter   (per design spec).
//
// Sizes follow Material 3 type scale (https://m3.material.io/styles/typography).
val VedraTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Light,
        fontSize = 96.sp,
        lineHeight = 104.sp,
        letterSpacing = (-1.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Light,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter18,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter18,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Inter18,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Inter18,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Inter18,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Inter18,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)
