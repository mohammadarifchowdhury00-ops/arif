package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = ToriiCrimson,
    onPrimary = Color.White,
    primaryContainer = ToriiCrimsonContainer,
    onPrimaryContainer = OnToriiCrimsonContainer,
    secondary = IndigoScholar,
    onSecondary = Color.White,
    secondaryContainer = IndigoScholarContainer,
    onSecondaryContainer = OnIndigoScholarContainer,
    tertiary = MatchaJade,
    onTertiary = Color.White,
    tertiaryContainer = MatchaJadeContainer,
    onTertiaryContainer = OnMatchaJadeContainer,
    background = WashiBackground,
    onBackground = SumiTextPrimary,
    surface = WashiSurface,
    onSurface = SumiTextPrimary,
    surfaceVariant = WashiSurfaceVariant,
    onSurfaceVariant = SumiTextSecondary,
    surfaceContainerLowest = WashiCard,
    surfaceContainerLow = Color(0xFFFCF6F0),
    surfaceContainer = Color(0xFFF6EFE8),
    surfaceContainerHigh = Color(0xFFEFE6DD),
    outline = WashiOutline,
    outlineVariant = Color(0xFFEDE4DC)
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkCrimson,
    onPrimary = Color(0xFF690005),
    primaryContainer = DarkCrimsonContainer,
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = DarkIndigo,
    onSecondary = Color(0xFF002E69),
    secondaryContainer = DarkIndigoContainer,
    onSecondaryContainer = Color(0xFFD8E2FF),
    tertiary = DarkJade,
    onTertiary = Color(0xFF003731),
    tertiaryContainer = DarkJadeContainer,
    onTertiaryContainer = Color(0xFFCCFBF1),
    background = SumiNightBackground,
    onBackground = SumiNightTextPrimary,
    surface = SumiNightSurface,
    onSurface = SumiNightTextPrimary,
    surfaceVariant = SumiNightSurfaceVariant,
    onSurfaceVariant = SumiNightTextSecondary,
    surfaceContainerLowest = Color(0xFF14171D),
    surfaceContainerLow = Color(0xFF1A1E26),
    surfaceContainer = Color(0xFF20242E),
    surfaceContainerHigh = Color(0xFF282D3A),
    outline = SumiNightOutline,
    outlineVariant = Color(0xFF2B303D)
)

val ArifJournalShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ArifJournalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = ArifJournalShapes,
        content = content
    )
}
