package dev.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.designsystem.theme.AppPalette
import dev.core.designsystem.theme.appPalette

/** Barcha auth ekranlari uchun umumiy shrift oilasi (Figtree/Google Sans o'rniga). */
val AppFontFamily = FontFamily.Default

// ---------------------------------------------------------------------------
// Fon — gradient + dekorativ bloblar + ekran paddingi
// ---------------------------------------------------------------------------

@Composable
fun AppScreenScaffold(
    modifier: Modifier = Modifier,
    scroll: Boolean = false,
    horizontalPadding: Int = 22,
    topPadding: Int = 56,
    palette: AppPalette = appPalette,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize().background(palette.bgBrush)) {
        // Yuqori-o'ng primary blob
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(0.dp)
                .size(210.dp)
                .graphicsLayer { translationX = 60f; translationY = -80f }
                .background(
                    Brush.radialGradient(listOf(palette.blobPrimary, Color.Transparent)),
                    RoundedCornerShape(999.dp),
                ),
        )
        // Pastki-chap cyan blob
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .size(200.dp)
                .graphicsLayer { translationX = -70f; translationY = 40f }
                .background(
                    Brush.radialGradient(listOf(palette.blobCyan, Color.Transparent)),
                    RoundedCornerShape(999.dp),
                ),
        )
        val col = Modifier
            .fillMaxSize()
            .padding(
                start = horizontalPadding.dp,
                end = horizontalPadding.dp,
                top = topPadding.dp,
                bottom = 26.dp,
            )
            .then(if (scroll) Modifier.verticalScroll(rememberScrollState()) else Modifier)
        Column(modifier = col, content = content)
    }
}

// ---------------------------------------------------------------------------
// Matn uslublari
// ---------------------------------------------------------------------------

@Composable
fun ScreenTitle(text: String, size: Int = 24, palette: AppPalette = appPalette) {
    Text(
        text,
        style = TextStyle(
            fontFamily = AppFontFamily,
            fontSize = size.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp,
            color = palette.ink,
        ),
    )
}

@Composable
fun ScreenSubtitle(text: String, palette: AppPalette = appPalette) {
    Text(
        text,
        style = TextStyle(
            fontFamily = AppFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 19.sp,
            color = palette.inkMuted,
        ),
    )
}

@Composable
fun FieldLabel(text: String, palette: AppPalette = appPalette) {
    Text(
        text,
        style = TextStyle(
            fontFamily = AppFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = palette.label,
        ),
    )
}

@Composable
fun HintText(text: String, palette: AppPalette = appPalette) {
    Text(
        text,
        style = TextStyle(
            fontFamily = AppFontFamily,
            fontSize = 11.5f.sp,
            color = palette.inkFaint,
            lineHeight = 15.sp,
        ),
    )
}

// ---------------------------------------------------------------------------
// Logo tile
// ---------------------------------------------------------------------------

@Composable
fun LogoTile(
    size: Int = 52,
    radius: Int = 16,
    iconSize: Int = 28,
    palette: AppPalette = appPalette,
) {
    Box(
        Modifier
            .size(size.dp)
            .shadow(18.dp, RoundedCornerShape(radius.dp), spotColor = palette.primary, ambientColor = palette.primary)
            .background(palette.primaryBrush, RoundedCornerShape(radius.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(AppIcons.GraduationCap, null, tint = Color.White, modifier = Modifier.size(iconSize.dp))
    }
}

// ---------------------------------------------------------------------------
// Back / icon buttons
// ---------------------------------------------------------------------------

@Composable
fun BackButton(onClick: () -> Unit, icon: ImageVector = AppIcons.ArrowLeft, palette: AppPalette = appPalette) {
    Box(
        Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(palette.glass)
            .border(1.dp, palette.border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = palette.ink, modifier = Modifier.size(19.dp))
    }
}

// ---------------------------------------------------------------------------
// Primary / outline tugmalar
// ---------------------------------------------------------------------------

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingIcon: ImageVector? = null,
    palette: AppPalette = appPalette,
) {
    val shape = RoundedCornerShape(15.dp)
    Box(
        modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(if (enabled) 20.dp else 0.dp, shape, spotColor = palette.primary, ambientColor = palette.primary)
            .clip(shape)
            .background(if (enabled) palette.primaryBrush else SolidColor(palette.primary.copy(alpha = 0.4f)))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text,
                style = TextStyle(
                    fontFamily = AppFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.onPrimary,
                ),
            )
            if (trailingIcon != null) {
                Icon(trailingIcon, null, tint = palette.onPrimary, modifier = Modifier.size(17.dp))
            }
        }
    }
}

@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    palette: AppPalette = appPalette,
) {
    val shape = RoundedCornerShape(15.dp)
    val contentAlpha = if (enabled) 1f else 0.4f
    Row(
        modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(shape)
            .background(palette.glass)
            .border(1.dp, palette.border, shape)
            .clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, null, tint = palette.primary.copy(alpha = contentAlpha), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(9.dp))
        }
        Text(
            text,
            style = TextStyle(
                fontFamily = AppFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = (if (palette.dark) palette.ink else Color(0xFF4A3F86)).copy(alpha = contentAlpha),
            ),
        )
    }
}

// ---------------------------------------------------------------------------
// Segmented tabs (Telefon | Email)
// ---------------------------------------------------------------------------

enum class AuthTab { PHONE, EMAIL }

@Composable
fun SegmentedTabs(
    selected: AuthTab,
    onSelect: (AuthTab) -> Unit,
    modifier: Modifier = Modifier,
    palette: AppPalette = appPalette,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.tabTrack)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TabPill(AppIcons.Phone, "Telefon", selected == AuthTab.PHONE, { onSelect(AuthTab.PHONE) }, Modifier.weight(1f), palette)
        TabPill(AppIcons.Mail, "Email", selected == AuthTab.EMAIL, { onSelect(AuthTab.EMAIL) }, Modifier.weight(1f), palette)
    }
}

@Composable
private fun TabPill(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    palette: AppPalette,
) {
    val shape = RoundedCornerShape(10.dp)
    val bg = when {
        active && palette.dark -> Modifier.background(palette.primaryBrush, shape)
        active -> Modifier
            .shadow(6.dp, shape, spotColor = palette.primary.copy(alpha = 0.4f))
            .background(Color.White, shape)
        else -> Modifier
    }
    val content = if (active) (if (palette.dark) Color.White else palette.primary) else palette.inkFaint
    Row(
        modifier
            .height(38.dp)
            .clip(shape)
            .then(bg)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = content, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = content))
    }
}

// ---------------------------------------------------------------------------
// Glass text field
// ---------------------------------------------------------------------------

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leading: ImageVector? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    focused: Boolean = false,
    height: Int = 50,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    textLetterSpacing: Float = 0f,
    palette: AppPalette = appPalette,
) {
    val shape = RoundedCornerShape(14.dp)
    val borderColor = if (focused) palette.borderStrong else palette.border
    val borderWidth = if (focused) 1.5.dp else 1.dp
    var box = modifier
        .fillMaxWidth()
        .height(height.dp)
    if (focused) box = box.shadow(0.dp, shape, spotColor = palette.fieldFocusGlow)
    Row(
        box
            .clip(shape)
            .background(palette.fieldBg)
            .border(borderWidth, borderColor, shape)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        if (leadingContent != null) {
            leadingContent()
        } else if (leading != null) {
            Icon(leading, null, tint = palette.inkFaint, modifier = Modifier.size(17.dp))
        }
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    placeholder,
                    style = TextStyle(fontFamily = AppFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = palette.inkFaint),
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = AppFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.ink,
                    letterSpacing = textLetterSpacing.sp,
                ),
                cursorBrush = SolidColor(palette.primary),
                keyboardOptions = keyboardOptions,
                visualTransformation = visualTransformation,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (trailing != null) trailing()
    }
}

// ---------------------------------------------------------------------------
// Divider "yoki"
// ---------------------------------------------------------------------------

@Composable
fun OrDivider(text: String = "yoki", modifier: Modifier = Modifier, palette: AppPalette = appPalette) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.weight(1f).height(1.dp).background(palette.border))
        Text(text, style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.5f.sp, fontWeight = FontWeight.Medium, color = palette.inkFaint))
        Box(Modifier.weight(1f).height(1.dp).background(palette.border))
    }
}

// ---------------------------------------------------------------------------
// Social row (Google / Apple / Telegram)
// ---------------------------------------------------------------------------

@Composable
fun SocialRow(
    onGoogle: () -> Unit,
    onApple: () -> Unit,
    onTelegram: () -> Unit,
    modifier: Modifier = Modifier,
    palette: AppPalette = appPalette,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
        SocialButton(Modifier.weight(1f), onGoogle, palette) {
            androidx.compose.foundation.Image(AppIcons.Google, null, modifier = Modifier.size(21.dp))
        }
        SocialButton(Modifier.weight(1f), onApple, palette) {
            Icon(AppIcons.Apple, null, tint = palette.ink, modifier = Modifier.size(19.dp))
        }
        SocialButton(Modifier.weight(1f), onTelegram, palette) {
            androidx.compose.foundation.Image(AppIcons.Telegram, null, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun SocialButton(modifier: Modifier, onClick: () -> Unit, palette: AppPalette, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier
            .height(50.dp)
            .clip(shape)
            .background(palette.glass)
            .border(1.dp, palette.border, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

// ---------------------------------------------------------------------------
// Footer link ("Hisobingiz yo'qmi? Ro'yxatdan o'tish")
// ---------------------------------------------------------------------------

/** Xato xabari — bo'sh bo'lmasa qizil matn ko'rsatadi. */
@Composable
fun ColumnScope.ErrorText(message: String?) {
    if (message.isNullOrBlank()) return
    Spacer(Modifier.height(12.dp))
    Text(
        message,
        style = TextStyle(
            fontFamily = AppFontFamily,
            fontSize = 12.5f.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFDC2626),
            lineHeight = 17.sp,
        ),
    )
}

@Composable
fun FooterLink(prefix: String, action: String, onClick: () -> Unit, palette: AppPalette = appPalette) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            "$prefix ",
            style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, color = palette.inkMuted),
        )
        Text(
            action,
            style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, fontWeight = FontWeight.ExtraBold, color = palette.primary),
        )
    }
}
