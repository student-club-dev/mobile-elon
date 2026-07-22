package dev.feature.auth.presentation.main

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.domain.model.FriendStatus
import dev.core.domain.model.Student
import dev.core.domain.model.StudentSort
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.FilterPill
import dev.core.uikit.component.GlassRow
import dev.core.uikit.component.MonogramTile
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_filter
import dev.core.uikit.resources.auth_friend_add
import dev.core.uikit.resources.auth_friend_add_short
import dev.core.uikit.resources.auth_friend_friends
import dev.core.uikit.resources.auth_friend_pending
import dev.core.uikit.resources.auth_students_meta
import dev.core.uikit.resources.auth_students_sort_all
import dev.core.uikit.resources.auth_students_sort_my_university
import dev.core.uikit.resources.auth_students_title
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.core.uikit.theme.rowShadow
import dev.feature.auth.presentation.main.components.StudentProfile
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun StudentsScreen(vm: StudentsViewModel = koinViewModel()) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()
    var selectedId by remember { mutableStateOf<String?>(null) }
    val selected = state.students.firstOrNull { it.id == selectedId }

    if (selected != null) {
        StudentProfile(
            student = selected,
            palette = palette,
            meta = studentMeta(selected),
            friendLabel = friendLabel(selected.friendStatus, short = false),
            onBack = { selectedId = null },
            onFriend = { vm.toggleFriend(selected) },
        )
        return
    }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg).padding(top = 54.dp)) {
            Text(
                stringResource(Res.string.auth_students_title),
                style = AppType.screenTitle.copy(color = palette.ink),
            )
            Spacer(Modifier.height(14.dp))
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                FilterPill(
                    stringResource(Res.string.auth_students_sort_my_university),
                    state.sort == StudentSort.MY_UNIVERSITY,
                    { vm.setSort(StudentSort.MY_UNIVERSITY) },
                    palette = palette,
                )
                FilterPill(
                    stringResource(Res.string.auth_students_sort_all),
                    state.sort == StudentSort.ALL,
                    { vm.setSort(StudentSort.ALL) },
                    palette = palette,
                )
                FilterChipButton(palette)
            }
        }

        Spacer(Modifier.height(14.dp))

        LazyColumn(
            Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(start = AppSpacing.lg, end = AppSpacing.lg, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            items(state.students, key = { it.id }) { student ->
                StudentRow(
                    student,
                    palette,
                    onOpen = { selectedId = student.id },
                    onFriend = { vm.toggleFriend(student) },
                )
            }
        }
    }
}

/** Hozircha faqat ko'rinish — filtr oynasi keyingi bosqichda ulanadi. */
@Composable
private fun FilterChipButton(palette: AppPalette) {
    val shape = AppRadius.pill
    Row(
        Modifier.rowShadow(shape).clip(shape).background(palette.card)
            .padding(horizontal = 13.dp, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(AppIcons.Filter, null, tint = palette.inkMuted, modifier = Modifier.size(14.dp))
        Text(
            stringResource(Res.string.auth_filter),
            style = AppType.groupLabel.copy(color = palette.inkMuted),
        )
    }
}

@Composable
private fun StudentRow(student: Student, palette: AppPalette, onOpen: () -> Unit, onFriend: () -> Unit) {
    GlassRow(
        shape = AppRadius.md,
        contentPadding = PaddingValues(AppSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        onClick = onOpen,
        palette = palette,
    ) {
        MonogramTile(student.initial, size = 46.dp)
        Column(Modifier.weight(1f)) {
            Text(
                student.fullName,
                style = AppType.subtitle.copy(fontWeight = AppType.button.fontWeight, color = palette.ink),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                studentMeta(student),
                style = AppType.caption.copy(color = palette.inkFaint),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        FriendPill(student.friendStatus, onFriend, palette)
    }
}

@Composable
private fun FriendPill(status: FriendStatus, onClick: () -> Unit, palette: AppPalette) {
    val filled = status == FriendStatus.NONE
    Box(
        Modifier.clip(AppRadius.sm)
            .background(if (filled) palette.primary else palette.primary.copy(alpha = 0.12f))
            .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = AppSpacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            friendLabel(status, short = true),
            style = AppType.hint.copy(
                fontWeight = AppType.button.fontWeight,
                color = if (filled) palette.onPrimary else palette.primary,
            ),
            maxLines = 1,
        )
    }
}

/** "TATU · 3-kurs · Dasturiy injiniring" ko'rinishidagi meta qator. */
@Composable
private fun studentMeta(student: Student): String = stringResource(
    Res.string.auth_students_meta,
    student.universityMonogram,
    "${student.course}",
    student.faculty,
)

/** Do'stlik holati yorlig'i — ro'yxatda qisqa ("+ Do'st"), profilda to'liq. */
@Composable
private fun friendLabel(status: FriendStatus, short: Boolean): String = when (status) {
    FriendStatus.NONE -> if (short) {
        stringResource(Res.string.auth_friend_add_short)
    } else {
        stringResource(Res.string.auth_friend_add)
    }
    FriendStatus.PENDING -> stringResource(Res.string.auth_friend_pending)
    FriendStatus.FRIENDS -> stringResource(Res.string.auth_friend_friends)
}
