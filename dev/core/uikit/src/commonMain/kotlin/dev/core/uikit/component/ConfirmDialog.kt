package dev.core.uikit.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.common_cancel
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import org.jetbrains.compose.resources.stringResource

/**
 * "Rostdan ham?" oynasi — qaytarib bo'lmaydigan amallar uchun.
 *
 * Nega alohida komponent: o'chirish tugmalari ilova bo'ylab bir nechta ekranда va ularning
 * bir qismi **darhol** o'chirib yuborardi (e'lon kartasi). Bitta komponent har joyda bir xil
 * matn tuzilishini beradi: sarlavha + nima o'chirilayotgani (aynan **nomi bilan**) + ikki
 * tugma. Nomsiz "Rostdan ham o'chirasizmi?" savolida foydalanuvchi qaysi element haqida
 * gap ketayotganini bilmasdi.
 */
@Composable
fun ConfirmDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    /** Qaytarib bo'lmaydigan amal — tasdiq tugmasi qizil bo'ladi. */
    destructive: Boolean = true,
    palette: AppPalette = appPalette,
) {
    if (!visible) return
    val confirmColor: Color = if (destructive) palette.danger else palette.primary
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.card,
        shape = AppRadius.card,
        title = { Text(title, style = AppType.sectionTitle.copy(color = palette.ink)) },
        text = { Text(message, style = AppType.body.copy(color = palette.inkMuted)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, style = AppType.label.copy(color = confirmColor))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(Res.string.common_cancel),
                    style = AppType.body.copy(color = palette.inkMuted),
                )
            }
        },
    )
}
