package dev.feature.auth.presentation.main

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.domain.model.Job
import dev.core.domain.model.JobFilter
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.BackButton
import dev.core.uikit.component.FilterPill
import dev.core.uikit.component.MonogramTile
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.component.SoftPill
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.auth_bookmark
import dev.core.uikit.resources.auth_filter
import dev.core.uikit.resources.auth_filter_all
import dev.core.uikit.resources.auth_jobs_applied
import dev.core.uikit.resources.auth_jobs_apply
import dev.core.uikit.resources.auth_jobs_detail_note
import dev.core.uikit.resources.auth_jobs_filter_it
import dev.core.uikit.resources.auth_jobs_filter_part_time
import dev.core.uikit.resources.auth_jobs_filter_remote
import dev.core.uikit.resources.auth_jobs_meta
import dev.core.uikit.resources.auth_jobs_salary
import dev.core.uikit.resources.auth_jobs_title
import dev.core.uikit.resources.common_back
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Ish e'lonlari filtri va uning yorlig'i (matn resursdan). */
private val filterLabels: List<Pair<JobFilter, StringResource>> = listOf(
    JobFilter.ALL to Res.string.auth_filter_all,
    JobFilter.IT to Res.string.auth_jobs_filter_it,
    JobFilter.REMOTE to Res.string.auth_jobs_filter_remote,
    JobFilter.PART_TIME to Res.string.auth_jobs_filter_part_time,
)

@Composable
fun JobsScreen(vm: JobsViewModel = koinViewModel()) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()
    var selectedId by remember { mutableStateOf<String?>(null) }
    val selected = state.jobs.firstOrNull { it.id == selectedId }

    if (selected != null) {
        JobDetail(
            selected,
            palette,
            onBack = { selectedId = null },
            onApply = { vm.apply(selected) },
            onBookmark = { vm.toggleBookmark(selected) },
        )
        return
    }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg).padding(top = 54.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(Res.string.auth_jobs_title),
                    style = AppType.screenTitle.copy(letterSpacing = 0.sp, color = palette.ink),
                    modifier = Modifier.weight(1f),
                )
                Box(
                    Modifier.size(40.dp).clip(AppRadius.md).background(palette.glass)
                        .border(1.dp, palette.border, AppRadius.md),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        AppIcons.Filter,
                        stringResource(Res.string.auth_filter),
                        tint = palette.ink,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                filterLabels.forEach { (filter, label) ->
                    FilterPill(
                        stringResource(label),
                        state.filter == filter,
                        { vm.setFilter(filter) },
                        palette = palette,
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        LazyColumn(
            Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(start = AppSpacing.lg, end = AppSpacing.lg, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(state.jobs, key = { it.id }) { job ->
                JobCard(job, palette, onOpen = { selectedId = job.id }, onBookmark = { vm.toggleBookmark(job) })
            }
        }
    }
}

@Composable
private fun JobCard(job: Job, palette: AppPalette, onOpen: () -> Unit, onBookmark: () -> Unit) {
    val shape = AppRadius.card
    Column(
        Modifier.fillMaxWidth().clip(shape).background(palette.glass).border(1.dp, palette.border, shape)
            .clickable(onClick = onOpen).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            MonogramTile(
                job.companyMonogram,
                size = 46.dp,
                shape = RoundedCornerShape(13.dp),
                fontSize = 17.sp,
                backgroundAlpha = 0.12f,
            )
            Column(Modifier.weight(1f)) {
                Text(
                    job.title,
                    style = AppType.body.copy(fontSize = 14.5f.sp, fontWeight = AppType.button.fontWeight, color = palette.ink),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    stringResource(Res.string.auth_jobs_meta, job.company, job.location),
                    style = AppType.hint.copy(color = palette.inkFaint),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                AppIcons.Bookmark,
                stringResource(Res.string.auth_bookmark),
                tint = if (job.bookmarked) palette.primary else palette.inkFaint,
                modifier = Modifier.size(20.dp).clickable(onClick = onBookmark),
            )
        }

        if (job.tags.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                job.tags.forEach { SoftPill(it) }
            }
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                job.salary,
                style = AppType.subtitle.copy(fontWeight = AppType.button.fontWeight, color = palette.successDeep),
                modifier = Modifier.weight(1f),
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Icon(AppIcons.Clock, null, tint = palette.inkFaint, modifier = Modifier.size(13.dp))
                Text(job.postedAgo, style = AppType.caption.copy(color = palette.inkFaint))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Ish detali
// ---------------------------------------------------------------------------
@Composable
private fun JobDetail(job: Job, palette: AppPalette, onBack: () -> Unit, onApply: () -> Unit, onBookmark: () -> Unit) {
    var applied by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.lg).padding(top = 54.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BackButton(onBack, contentDescription = stringResource(Res.string.common_back), iconSize = 18.dp)
                Spacer(Modifier.weight(1f))
                Icon(
                    AppIcons.Bookmark,
                    stringResource(Res.string.auth_bookmark),
                    tint = if (job.bookmarked) palette.primary else palette.inkFaint,
                    modifier = Modifier.size(22.dp).clickable(onClick = onBookmark),
                )
            }
            Spacer(Modifier.height(AppSpacing.lg))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                MonogramTile(
                    job.companyMonogram,
                    size = 58.dp,
                    shape = RoundedCornerShape(16.dp),
                    fontSize = 22.sp,
                    backgroundAlpha = 0.12f,
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        job.title,
                        style = AppType.screenTitle.copy(fontSize = 18.sp, letterSpacing = 0.sp, color = palette.ink),
                    )
                    Text(
                        stringResource(Res.string.auth_jobs_meta, job.company, job.location),
                        style = AppType.fieldLabel.copy(fontWeight = AppType.subtitle.fontWeight, color = palette.inkFaint),
                    )
                }
            }
            Spacer(Modifier.height(AppSpacing.lg))
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                job.tags.forEach { SoftPill(it) }
            }
            Spacer(Modifier.height(AppSpacing.lg))
            Box(Modifier.fillMaxWidth().clip(AppRadius.lg).background(palette.successBg).padding(14.dp)) {
                Column {
                    Text(stringResource(Res.string.auth_jobs_salary), style = AppType.caption.copy(color = palette.inkFaint))
                    Text(
                        job.salary,
                        style = AppType.screenTitle.copy(fontSize = 18.sp, letterSpacing = 0.sp, color = palette.successDeep),
                    )
                }
            }
            Spacer(Modifier.height(AppSpacing.lg))
            Text(
                stringResource(Res.string.auth_jobs_detail_note, job.field),
                style = AppType.subtitle.copy(color = palette.inkMuted),
            )
            Spacer(Modifier.height(AppSpacing.lg))
        }
        Box(Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)) {
            PrimaryButton(
                if (applied) stringResource(Res.string.auth_jobs_applied) else stringResource(Res.string.auth_jobs_apply),
                onClick = { if (!applied) { onApply(); applied = true } },
                enabled = !applied,
            )
        }
    }
}
