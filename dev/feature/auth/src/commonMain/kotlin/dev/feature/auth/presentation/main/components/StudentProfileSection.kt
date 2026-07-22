package dev.feature.auth.presentation.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.core.domain.model.FriendStatus
import dev.core.domain.model.Student
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.GradientHeader
import dev.core.uikit.component.SoftPill
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_message
import dev.core.uikit.resources.auth_students_interests
import dev.core.uikit.resources.auth_students_stat_ads
import dev.core.uikit.resources.auth_students_stat_friends
import dev.core.uikit.resources.auth_students_stat_rating
import dev.core.uikit.resources.common_back
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import org.jetbrains.compose.resources.stringResource

/**
 * 1u — Student profili. Tepasida gradientli sarlavha, ostida statistika,
 * qiziqishlar va do'stlik amallari.
 *
 * Gradient USTIDAGI kontent palitraga bog'liq emas — binafsha fon har ikkala
 * rejimda quyuq, shuning uchun matn/ikonka oq bo'lib qoladi.
 */
@Composable
internal fun StudentProfile(
    student: Student,
    palette: AppPalette,
    meta: String,
    friendLabel: String,
    onBack: () -> Unit,
    onFriend: () -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        GradientHeader(palette = palette) {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 54.dp, bottom = 26.dp)) {
                Box(
                    Modifier.size(40.dp).clip(AppRadius.md)
                        .background(Color.White.copy(alpha = 0.18f))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        AppIcons.ArrowLeft,
                        stringResource(Res.string.common_back),
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        Modifier.size(72.dp).clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(student.initial, style = AppType.screenTitle.copy(fontSize = 30.sp, color = Color.White))
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                student.fullName,
                                style = AppType.screenTitle.copy(fontSize = 19.sp, letterSpacing = 0.sp, color = Color.White),
                            )
                            Icon(AppIcons.ShieldCheck, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.height(AppSpacing.xs))
                        Text(
                            meta,
                            style = AppType.fieldLabel.copy(
                                fontWeight = AppType.subtitle.fontWeight,
                                color = Color.White.copy(alpha = 0.85f),
                            ),
                        )
                    }
                }
            }
        }

        Column(Modifier.fillMaxWidth().padding(AppSpacing.lg)) {
            // Statistika
            val shape = RoundedCornerShape(16.dp)
            Row(
                Modifier.fillMaxWidth().clip(shape).background(palette.glass)
                    .border(1.dp, palette.border, shape).padding(vertical = 14.dp),
            ) {
                StatCell("${student.friendsCount}", stringResource(Res.string.auth_students_stat_friends), palette, Modifier.weight(1f))
                StatCell("${student.adsCount}", stringResource(Res.string.auth_students_stat_ads), palette, Modifier.weight(1f))
                StatCell(formatRating(student.rating), stringResource(Res.string.auth_students_stat_rating), palette, Modifier.weight(1f))
            }
            Spacer(Modifier.height(AppSpacing.lg))

            if (student.interests.isNotEmpty()) {
                Text(
                    stringResource(Res.string.auth_students_interests),
                    style = AppType.body.copy(fontWeight = AppType.button.fontWeight, color = palette.ink),
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    student.interests.forEach { interest ->
                        SoftPill(
                            text = interest,
                            shape = AppRadius.pill,
                            textStyle = AppType.fieldLabel,
                            contentPadding = PaddingValues(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val filled = student.friendStatus == FriendStatus.NONE
                Box(
                    Modifier.weight(1f).clip(AppRadius.lg)
                        .background(if (filled) palette.primary else palette.primary.copy(alpha = 0.12f))
                        .clickable(onClick = onFriend).padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        friendLabel,
                        style = AppType.label.copy(
                            fontSize = 13.5f.sp,
                            fontWeight = AppType.button.fontWeight,
                            color = if (filled) palette.onPrimary else palette.primary,
                        ),
                    )
                }
                Box(
                    Modifier.size(50.dp).clip(AppRadius.lg).background(palette.glass)
                        .border(1.dp, palette.border, AppRadius.lg),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        AppIcons.MessageSquare,
                        stringResource(Res.string.auth_message),
                        tint = palette.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCell(value: String, label: String, palette: AppPalette, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = AppType.screenTitle.copy(fontSize = 17.sp, letterSpacing = 0.sp, color = palette.ink))
        Text(label, style = AppType.caption.copy(fontSize = 10.5f.sp, color = palette.inkFaint))
    }
}

/** Reytingni bitta kasr xonasi bilan ko'rsatadi ("4.8"). */
private fun formatRating(rating: Double): String {
    val rounded = (rating * 10).toLong()
    return "${rounded / 10}.${rounded % 10}"
}
