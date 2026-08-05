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
import dev.core.uikit.theme.rowShadow

/**
 * Matn maydoni — oq karta yuzasi, yumshoq soya bilan (`design_handoff_studentclub_elonuz`).
 *
 * [focused] `true` bo'lganda brend rangli nozik halqa qo'shiladi. Fokus holati
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
    /**
     * Maydon turi — klaviatura, niqob va kiritishni tozalashni birga beradi.
     * Quyidagi ikki parametr undan kelib chiqadi, kerak bo'lsa alohida bekor qilinadi.
     */
    type: AppFieldType = AppFieldType.Text,
    keyboardOptions: KeyboardOptions = type.keyboardOptions,
    visualTransformation: VisualTransformation = type.visualTransformation,
    textLetterSpacing: Float = 0f,
    palette: AppPalette = appPalette,
) {
    val shape = AppRadius.lg
    // Yangi dizaynda maydonni chegara emas, SOYA ajratadi — u oq karta bilan bir xil yuzada.
    // Fokusda esa brend rangli nozik halqa qo'shiladi, shunda faol maydon aniq ko'rinadi.
    val box = modifier
        // Fokusga kelganda o'zini klaviatura ustiga suradi — ilovadagi HAMMA matn maydoni
        // shu komponentdan foydalanadi, shuning uchun qoida shu yerda, bir joyda turadi.
        .keyboardAware()
        .fillMaxWidth()
        .height(height)
        .rowShadow(shape)
        .clip(shape)
        .background(palette.card)
        .then(
            if (focused) Modifier.border(1.5.dp, palette.primary, shape) else Modifier,
        )
    Row(
        box.padding(horizontal = AppSpacing.lg),
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
                // Tur qoidasi kiritishning O'ZIDA qo'llanadi — ruxsatsiz belgi holatga
                // umuman tushmaydi, ekran uni alohida tozalashi shart emas.
                onValueChange = { onValueChange(type.sanitize(it)) },
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
