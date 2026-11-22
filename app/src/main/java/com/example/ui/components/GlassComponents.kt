package com.example.ui.components

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BakeryDining
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Cookie
import androidx.compose.material.icons.rounded.EmojiFoodBeverage
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.LocalBar
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.TableStatus
import com.example.data.model.WallpaperOption
import com.example.ui.theme.LocalGlassStyle
import com.example.ui.theme.StatusAvailableGreen
import com.example.ui.theme.StatusBillRed
import com.example.ui.theme.StatusOccupiedAmber
import kotlinx.coroutines.delay

data class IconCatalogOption(
    val key: String,
    val labelAr: String,
    val icon: ImageVector,
    val defaultColorHex: Long
)

object CafeIconCatalog {
    val options = listOf(
        IconCatalogOption("hookah", "أركيلة", Icons.Rounded.Spa, 0xFFFF9F0A),
        IconCatalogOption("flame", "جمر / نكهة", Icons.Rounded.LocalFireDepartment, 0xFFFF6B35),
        IconCatalogOption("cloud", "دخان / سحبة", Icons.Rounded.Cloud, 0xFF64D2FF),
        IconCatalogOption("hot_drink", "مشروب ساخن", Icons.Rounded.LocalCafe, 0xFFBF5AF2),
        IconCatalogOption("coffee", "قهوة", Icons.Rounded.Coffee, 0xFFC58940),
        IconCatalogOption("tea", "شاي", Icons.Rounded.EmojiFoodBeverage, 0xFF30D158),
        IconCatalogOption("cold_drink", "مشروب بارد", Icons.Rounded.LocalBar, 0xFF32ADE6),
        IconCatalogOption("juice", "عصير", Icons.Rounded.AcUnit, 0xFF0A84FF),
        IconCatalogOption("dessert", "حلويات", Icons.Rounded.BakeryDining, 0xFFFF375F),
        IconCatalogOption("cake", "كيك", Icons.Rounded.Cake, 0xFFFF6482),
        IconCatalogOption("snack", "مقبلات", Icons.Rounded.Cookie, 0xFFFFD60A),
        IconCatalogOption("food", "وجبات", Icons.Rounded.Fastfood, 0xFFFF9F0A),
        IconCatalogOption("sparkles", "مميز", Icons.Rounded.AutoAwesome, 0xFF5E5CE6),
        IconCatalogOption("star", "الأكثر طلباً", Icons.Rounded.Star, 0xFFFFCC00),
        IconCatalogOption("restaurant", "مطبخ", Icons.Rounded.Restaurant, 0xFF34C759)
    )

    val colorPresets = listOf(
        0xFFFF9F0A,
        0xFFBF5AF2,
        0xFF32ADE6,
        0xFFFF375F,
        0xFF30D158,
        0xFF5E5CE6,
        0xFFFF6B35,
        0xFF0A84FF
    )

    fun resolveIcon(key: String): ImageVector =
        options.firstOrNull { it.key == key }?.icon ?: Icons.Rounded.LocalCafe

    fun resolveColor(key: String): Color =
        Color(options.firstOrNull { it.key == key }?.defaultColorHex ?: 0xFFFF9F0A)
}

@Composable
fun CafeteriaWallpaperBackground(
    wallpaper: WallpaperOption,
    isDarkMode: Boolean,
    blurIntensity: Float,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurIntensity > 0.5f) {
            Modifier.blur((blurIntensity * 0.75f).dp)
        } else {
            Modifier
        }

        val drawableRes = when (wallpaper) {
            WallpaperOption.ESPRESSO -> R.drawable.img_bg_espresso_lounge
            WallpaperOption.OCEAN -> R.drawable.img_bg_ocean_glass
            WallpaperOption.EMERALD -> R.drawable.img_bg_emerald_mist
            WallpaperOption.CLASSIC -> null
        }

        if (drawableRes != null) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .then(blurModifier)
            )
        } else {
            val gradientColors = if (isDarkMode) {
                listOf(
                    Color(0xFF16131A),
                    Color(0xFF231D2B),
                    Color(0xFF141E28)
                )
            } else {
                listOf(
                    Color(0xFFF3EFEA),
                    Color(0xFFE7EEF6),
                    Color(0xFFF6ECE2)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(gradientColors))
            )
        }

        // Atmospheric contrast scrim for legibility in Dark and Light modes across all wallpapers
        val scrimBrush = if (isDarkMode) {
            Brush.verticalGradient(
                listOf(
                    Color(0xFF0A0B0F).copy(alpha = 0.42f),
                    Color(0xFF0A0B0F).copy(alpha = 0.58f)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    Color(0xFFF6F4F0).copy(alpha = 0.72f),
                    Color(0xFFEEEAE3).copy(alpha = 0.80f)
                )
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrimBrush)
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    accentTint: Color? = null,
    borderColor: Color? = null,
    opacityMultiplier: Float = 1f,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val glassStyle = LocalGlassStyle.current
    val effectiveOpacity = (glassStyle.opacity * opacityMultiplier).coerceIn(0.24f, 0.94f)

    val baseFill = if (glassStyle.isDarkMode) {
        Color(0xFF161820).copy(alpha = (effectiveOpacity + 0.14f).coerceIn(0.36f, 0.94f))
    } else {
        Color(0xFFFCFAF7).copy(alpha = (effectiveOpacity + 0.34f).coerceIn(0.58f, 0.96f))
    }

    val tintedFill = if (accentTint != null) {
        Brush.linearGradient(
            listOf(
                accentTint.copy(alpha = if (glassStyle.isDarkMode) 0.18f else 0.12f),
                baseFill
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                baseFill.copy(alpha = (baseFill.alpha + 0.08f).coerceAtMost(0.95f)),
                baseFill
            )
        )
    }

    val resolvedBorder = borderColor?.let {
        Brush.linearGradient(listOf(it.copy(alpha = 0.72f), it.copy(alpha = 0.22f)))
    } ?: Brush.linearGradient(
        if (glassStyle.isDarkMode) {
            listOf(
                Color.White.copy(alpha = 0.28f),
                Color.White.copy(alpha = 0.06f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.85f),
                Color.Black.copy(alpha = 0.10f)
            )
        }
    )

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (glassStyle.isDarkMode) 8.dp else 6.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.18f),
                spotColor = Color.Black.copy(alpha = 0.22f)
            )
            .clip(shape)
            .background(tintedFill)
            .border(width = 1.dp, brush = resolvedBorder, shape = shape)
            .then(clickableModifier)
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onSurface
        ) {
            content()
        }
    }
}

@Composable
fun SquircleIconBadge(
    iconKey: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp
) {
    val icon = CafeIconCatalog.resolveIcon(iconKey)
    val glassStyle = LocalGlassStyle.current
    val squircleShape = RoundedCornerShape(size * 0.30f)

    Box(
        modifier = modifier
            .size(size)
            .clip(squircleShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        accentColor.copy(alpha = (0.78f + glassStyle.opacity * 0.2f).coerceAtMost(0.96f)),
                        accentColor.copy(alpha = 0.58f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.65f),
                        Color.White.copy(alpha = 0.12f)
                    )
                ),
                shape = squircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun StatusGlassBadge(
    status: TableStatus,
    modifier: Modifier = Modifier
) {
    val badgeColor by animateColorAsState(
        targetValue = when (status) {
            TableStatus.AVAILABLE -> StatusAvailableGreen
            TableStatus.OCCUPIED -> StatusOccupiedAmber
            TableStatus.BILL_REQUESTED -> StatusBillRed
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "status_color"
    )

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = badgeColor.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
            )
            Text(
                text = status.labelAr,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = badgeColor
            )
        }
    }
}

@Composable
fun LiveElapsedTimerText(
    sinceMillis: Long?,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    if (sinceMillis == null) return

    var nowMillis by remember(sinceMillis) { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(sinceMillis) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val totalSeconds = ((nowMillis - sinceMillis) / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val formatted = if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }

    Text(
        text = formatted,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        ),
        color = color,
        modifier = modifier
    )
}
