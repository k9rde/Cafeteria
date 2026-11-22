package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalGlassStyle

private const val DEVELOPER_WHATSAPP_URL = "https://wa.me/9647746088000"
private const val DEVELOPER_INSTAGRAM_URL = "https://www.instagram.com/0nusn/"

@Composable
fun DeveloperContactCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val glassStyle = LocalGlassStyle.current

    fun openExternalUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            context.startActivity(intent)
        }.onFailure {
            Toast.makeText(context, "تعذر فتح الرابط", Toast.LENGTH_SHORT).show()
        }
    }

    GlassCard(
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Top-right in RTL ("للتواصل مع المطور")
            Text(
                text = "للتواصل مع المطور",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (glassStyle.isDarkMode) Color(0xFF1CA0F2) else Color(0xFF0284C7)
            )

            // Centered side-by-side glass squircles (RTL: Instagram on Right, WhatsApp on Left)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(22.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Instagram Glass Squircle (Right)
                SocialGlassButton(
                    glowColor = Color(0xFFE1306C),
                    secondaryGlowColor = Color(0xFFF56040),
                    contentDescription = "إنستغرام المطور 0nusn",
                    onClick = { openExternalUrl(DEVELOPER_INSTAGRAM_URL) },
                    modifier = Modifier.testTag("developer_instagram_button")
                ) {
                    InstagramLogoIcon(modifier = Modifier.size(46.dp))
                }

                // WhatsApp Glass Squircle (Left)
                SocialGlassButton(
                    glowColor = Color(0xFF25D366),
                    secondaryGlowColor = Color(0xFF128C7E),
                    contentDescription = "واتساب المطور +9647746088000",
                    onClick = { openExternalUrl(DEVELOPER_WHATSAPP_URL) },
                    modifier = Modifier.testTag("developer_whatsapp_button")
                ) {
                    WhatsAppLogoIcon(modifier = Modifier.size(46.dp))
                }
            }
        }
    }
}

@Composable
private fun SocialGlassButton(
    glowColor: Color,
    secondaryGlowColor: Color,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconContent: @Composable () -> Unit
) {
    val glassStyle = LocalGlassStyle.current
    val shape = RoundedCornerShape(24.dp)

    val fillBrush = Brush.radialGradient(
        colors = listOf(
            glowColor.copy(alpha = if (glassStyle.isDarkMode) 0.24f else 0.16f),
            secondaryGlowColor.copy(alpha = if (glassStyle.isDarkMode) 0.12f else 0.08f),
            if (glassStyle.isDarkMode) Color(0xFF1D1E26).copy(alpha = 0.72f) else Color.White.copy(alpha = 0.75f)
        )
    )

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            glowColor.copy(alpha = 0.70f),
            secondaryGlowColor.copy(alpha = 0.35f),
            Color.White.copy(alpha = if (glassStyle.isDarkMode) 0.20f else 0.65f)
        )
    )

    Box(
        modifier = modifier
            .size(86.dp)
            .minimumInteractiveComponentSize()
            .clip(shape)
            .background(fillBrush)
            .border(width = 1.2.dp, brush = borderBrush, shape = shape)
            .semantics { this.contentDescription = contentDescription }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        iconContent()
    }
}

@Composable
private fun WhatsAppLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h * 0.48f
        val radius = w * 0.44f

        // Green speech bubble circle + bottom-left tail
        val bubblePath = Path().apply {
            addOval(
                androidx.compose.ui.geometry.Rect(
                    left = cx - radius,
                    top = cy - radius,
                    right = cx + radius,
                    bottom = cy + radius
                )
            )
        }
        drawPath(bubblePath, color = Color(0xFF25D366))

        val tailPath = Path().apply {
            moveTo(cx - radius * 0.62f, cy + radius * 0.56f)
            lineTo(cx - radius * 0.96f, cy + radius * 1.02f)
            lineTo(cx - radius * 0.25f, cy + radius * 0.86f)
            close()
        }
        drawPath(tailPath, color = Color(0xFF25D366))

        // White handset silhouette inside bubble
        val handset = Path().apply {
            moveTo(cx - radius * 0.36f, cy - radius * 0.38f)
            cubicTo(
                cx - radius * 0.48f, cy - radius * 0.24f,
                cx - radius * 0.46f, cy + radius * 0.04f,
                cx - radius * 0.20f, cy + radius * 0.28f
            )
            cubicTo(
                cx + radius * 0.04f, cy + radius * 0.48f,
                cx + radius * 0.30f, cy + radius * 0.48f,
                cx + radius * 0.42f, cy + radius * 0.32f
            )
        }
        drawPath(
            path = handset,
            color = Color.White,
            style = Stroke(width = w * 0.115f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun InstagramLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val corner = w * 0.28f

        // Instagram gradient rounded square
        val igBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFCAF45),
                Color(0xFFF56040),
                Color(0xFFE1306C),
                Color(0xFFC13584),
                Color(0xFF833AB4)
            ),
            start = Offset(0f, h),
            end = Offset(w, 0f)
        )
        drawRoundRect(
            brush = igBrush,
            topLeft = Offset.Zero,
            size = Size(w, h),
            cornerRadius = CornerRadius(corner, corner)
        )

        // White rounded camera outline
        val strokeW = w * 0.085f
        val inset = w * 0.19f
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(inset, inset),
            size = Size(w - inset * 2f, h - inset * 2f),
            cornerRadius = CornerRadius(w * 0.18f, w * 0.18f),
            style = Stroke(width = strokeW)
        )

        // Center lens ring
        drawCircle(
            color = Color.White,
            radius = w * 0.165f,
            center = Offset(w / 2f, h / 2f),
            style = Stroke(width = strokeW)
        )

        // Top-right flash dot
        drawCircle(
            color = Color.White,
            radius = w * 0.052f,
            center = Offset(w * 0.68f, h * 0.32f)
        )
    }
}
