package dev.feature.auth.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.AppScreenScaffold
import dev.core.uikit.component.IconTile
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_role_business
import dev.core.uikit.resources.auth_role_business_desc
import dev.core.uikit.resources.auth_role_student
import dev.core.uikit.resources.auth_role_student_desc
import dev.core.uikit.resources.auth_role_subtitle
import dev.core.uikit.resources.auth_role_title
import dev.core.uikit.resources.common_back
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.auth.presentation.flow.Role
import org.jetbrains.compose.resources.stringResource

/**
 * Login'dan OLDIN chiqadigan rol tanlash — foydalanuvchi Talaba yoki Biznesmen sifatida davom etadi.
 * Tanlangan rol [Role] sifatida qaytadi (AuthFlowViewModel'ga yoziladi) va ro'yxatdan o'tishda
 * ishlatiladi; login'dan keyin RootShell shu rol bo'yicha mos shell'ni ochadi.
 */
@Composable
fun RoleChoiceScreen(
    onPick: (Role) -> Unit,
    onBack: () -> Unit = {},
) {
    val palette = appPalette

    AppScreenScaffold {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(top = 48.dp, bottom = AppSpacing.xl),
        ) {
            IconTile(
                AppIcons.ArrowLeft,
                contentDescription = stringResource(Res.string.common_back),
                tint = palette.primary,
                background = palette.primary.copy(alpha = 0.10f),
                size = 40.dp,
                iconSize = 19.dp,
                shape = RoundedCornerShape(13.dp),
                onClick = onBack,
            )

            Spacer(Modifier.height(28.dp))
            Text(
                stringResource(Res.string.auth_role_title),
                style = AppType.screenTitle.copy(fontSize = 26.sp, letterSpacing = 0.sp, color = palette.ink),
            )
            Spacer(Modifier.height(AppSpacing.sm))
            Text(
                stringResource(Res.string.auth_role_subtitle),
                style = AppType.subtitle.copy(color = palette.inkMuted),
            )

            Spacer(Modifier.height(28.dp))
            RoleCard(
                title = stringResource(Res.string.auth_role_student),
                desc = stringResource(Res.string.auth_role_student_desc),
                icon = AppIcons.GraduationCap,
                accent = palette.primary,
                onClick = { onPick(Role.STUDENT) },
                palette = palette,
            )
            Spacer(Modifier.height(14.dp))
            RoleCard(
                title = stringResource(Res.string.auth_role_business),
                desc = stringResource(Res.string.auth_role_business_desc),
                icon = AppIcons.Store,
                accent = palette.moduleFood,
                onClick = { onPick(Role.BUSINESS) },
                palette = palette,
            )
        }
    }
}

@Composable
private fun RoleCard(
    title: String,
    desc: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    palette: AppPalette,
) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        Modifier.fillMaxWidth()
            .clip(shape)
            .background(accent.copy(alpha = 0.08f))
            .border(1.5.dp, accent.copy(alpha = 0.28f), shape)
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        IconTile(
            icon,
            contentDescription = title,
            tint = accent,
            background = accent.copy(alpha = 0.16f),
            size = 52.dp,
            iconSize = 26.dp,
            shape = RoundedCornerShape(16.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(title, style = AppType.sectionTitle.copy(fontWeight = AppType.screenTitle.fontWeight, color = palette.ink))
            Spacer(Modifier.height(3.dp))
            Text(desc, style = AppType.hint.copy(fontSize = 12.sp, color = palette.inkFaint))
        }
        Icon(AppIcons.ChevronRight, null, tint = accent, modifier = Modifier.size(20.dp))
    }
}
