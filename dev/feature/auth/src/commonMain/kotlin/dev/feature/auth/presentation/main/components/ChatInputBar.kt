package dev.feature.auth.presentation.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.IconTile
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_chat_attach
import dev.core.uikit.resources.auth_chat_input_placeholder
import dev.core.uikit.resources.auth_chat_send
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.ctaShadow
import org.jetbrains.compose.resources.stringResource

/** Kiritish panelidagi dumaloq tugmalar (handoff, 6-ekran: 38px). */
private val InputButtonSize = 38.dp

/**
 * Suhbat ostidagi kiritish paneli — oq fon, tepasida ajratuvchi chiziq, chapda biriktirish
 * nishoni, o'rtada yumaloq maydon va o'ngda gradient yuborish tugmasi.
 */
@Composable
internal fun ChatInputBar(
    draft: String,
    palette: AppPalette,
    onDraft: (String) -> Unit,
    onSend: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().background(palette.card)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(palette.divider))
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp).padding(top = 10.dp, bottom = AppSpacing.xl),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            // Fayl biriktirish backend'ga hali ulanmagan — nishon handoff shakli uchun turadi
            // va shu sababli bosilmaydi (bosiladigan, lekin javob bermaydigan tugma bo'lmasin).
            IconTile(
                AppIcons.Attach,
                contentDescription = stringResource(Res.string.auth_chat_attach),
                size = InputButtonSize,
                iconSize = 18.dp,
                shape = AppRadius.pill,
            )
            Box(
                Modifier.weight(1f).clip(AppRadius.lg).background(palette.fieldTrack)
                    .padding(horizontal = AppSpacing.lg, vertical = 10.dp),
            ) {
                if (draft.isEmpty()) {
                    Text(
                        stringResource(Res.string.auth_chat_input_placeholder),
                        style = AppType.subtitle.copy(fontSize = 14.5.sp, color = palette.inkFaint),
                    )
                }
                BasicTextField(
                    value = draft,
                    onValueChange = onDraft,
                    singleLine = true,
                    textStyle = AppType.subtitle.copy(fontSize = 14.5.sp, color = palette.ink),
                    cursorBrush = SolidColor(palette.primary),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Box(
                Modifier.size(InputButtonSize).ctaShadow(AppRadius.pill).clip(AppRadius.pill)
                    .background(palette.primaryBrush).clickable(onClick = onSend),
                contentAlignment = Alignment.Center,
            ) {
                // Gradient USTIDAGI ikonka — har ikkala rejimda oq.
                Icon(
                    AppIcons.Send,
                    stringResource(Res.string.auth_chat_send),
                    tint = Color.White,
                    modifier = Modifier.size(17.dp),
                )
            }
        }
    }
}
