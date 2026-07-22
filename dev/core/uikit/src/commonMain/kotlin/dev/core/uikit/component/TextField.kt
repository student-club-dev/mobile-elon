package dev.core.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette

/**
 * Shisha uslubidagi matn maydoni.
 *
 * [focused] `true` bo'lganda chegara qalinlashadi va brend rangiga o'tadi — fokus holati
 * `BasicTextField` ichida emas, tashqaridan boshqariladi (ekran o'z fokus holatini biladi).
 */
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
    height: Dp = AppSize.fieldHeight,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    textLetterSpacing: Float = 0f,
    palette: AppPalette = appPalette,
) {
    val shape = AppRadius.lg
    val borderColor = if (focused) palette.borderStrong else palette.border
    val borderWidth = if (focused) 1.5.dp else 1.dp
    var box = modifier
        .fillMaxWidth()
        .height(height)
    if (focused) box = box.shadow(0.dp, shape, spotColor = palette.fieldFocusGlow)
    Row(
        box
            .clip(shape)
            .background(palette.fieldBg)
            .border(borderWidth, borderColor, shape)
            .padding(horizontal = AppSpacing.md),
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
                Text(placeholder, style = AppType.body.copy(color = palette.inkFaint))
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = AppType.bodyStrong.copy(
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
