package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val SynvexDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = ObsidianVoid,
    primaryContainer = CyanDeep,
    onPrimaryContainer = ElectricCyan,
    secondary = HotMagenta,
    onSecondary = IceWhite,
    secondaryContainer = MagentaDeep,
    onSecondaryContainer = HotMagenta,
    tertiary = AcidLime,
    onTertiary = ObsidianVoid,
    background = ObsidianVoid,
    onBackground = IceWhite,
    surface = MidnightStage,
    onSurface = IceWhite,
    surfaceVariant = NeonPanel,
    onSurfaceVariant = MistLavender,
    outline = NeonBorder,
    error = DangerRed,
    onError = IceWhite
)

private val SynvexShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp)
)

@Composable
fun SynvexTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SynvexDarkColorScheme,
        typography = SynvexTypography,
        shapes = SynvexShapes,
        content = content
    )
}
