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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.domain.model.Job
import dev.core.domain.model.JobFilter
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.components.AppIcons
import dev.core.designsystem.components.PrimaryButton
import dev.core.designsystem.theme.AppPalette
import dev.core.designsystem.theme.appPalette
import org.koin.compose.viewmodel.koinViewModel

private val filterLabels = listOf(
    JobFilter.ALL to "Barchasi",
    JobFilter.IT to "IT",
    JobFilter.REMOTE to "Masofaviy",
    JobFilter.PART_TIME to "Part-time",
)

@Composable
fun JobsScreen(vm: JobsViewModel = koinViewModel()) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()
    var selectedId by remember { mutableStateOf<String?>(null) }
    val selected = state.jobs.firstOrNull { it.id == selectedId }

    if (selected != null) {
        JobDetail(selected, palette, onBack = { selectedId = null }, onApply = { vm.apply(selected) }, onBookmark = { vm.toggleBookmark(selected) })
        return
    }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 54.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Ishlar", style = TextStyle(fontFamily = AppFontFamily, fontSize = 24.sp, fontWeight = FontWeight.Black, color = palette.ink), modifier = Modifier.weight(1f))
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(palette.glass).border(1.dp, palette.border, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) { Icon(AppIcons.Filter, "Filtr", tint = palette.ink, modifier = Modifier.size(18.dp)) }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filterLabels.forEach { (filter, label) ->
                    FilterChip(label, state.filter == filter, { vm.setFilter(filter) }, palette)
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        LazyColumn(
            Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(state.jobs, key = { it.id }) { job ->
                JobCard(job, palette, onOpen = { selectedId = job.id }, onBookmark = { vm.toggleBookmark(job) })
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, active: Boolean, onClick: () -> Unit, palette: AppPalette) {
    Box(
        Modifier.clip(RoundedCornerShape(999.dp))
            .background(if (active) palette.primary else palette.glass)
            .then(if (active) Modifier else Modifier.border(1.dp, palette.border, RoundedCornerShape(999.dp)))
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, fontWeight = FontWeight.Bold, color = if (active) Color.White else palette.inkMuted))
    }
}

@Composable
private fun JobCard(job: Job, palette: AppPalette, onOpen: () -> Unit, onBookmark: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(palette.glass).border(1.dp, palette.border, RoundedCornerShape(18.dp)).clickable(onClick = onOpen).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            Box(
                Modifier.size(46.dp).clip(RoundedCornerShape(13.dp)).background(palette.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(job.companyMonogram, style = TextStyle(fontFamily = AppFontFamily, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = palette.primary))
            }
            Column(Modifier.weight(1f)) {
                Text(job.title, style = TextStyle(fontFamily = AppFontFamily, fontSize = 14.5f.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text("${job.company} · ${job.location}", style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.5f.sp, color = palette.inkFaint), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(
                AppIcons.Bookmark, "Saqlash",
                tint = if (job.bookmarked) palette.primary else palette.inkFaint,
                modifier = Modifier.size(20.dp).clickable(onClick = onBookmark),
            )
        }

        if (job.tags.isNotEmpty()) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                job.tags.forEach { TagPill(it, palette) }
            }
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(job.salary, style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = palette.successDeep), modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(AppIcons.Clock, null, tint = palette.inkFaint, modifier = Modifier.size(13.dp))
                Text(job.postedAgo, style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.sp, color = palette.inkFaint))
            }
        }
    }
}

@Composable
private fun TagPill(tag: String, palette: AppPalette) {
    Box(
        Modifier.clip(RoundedCornerShape(8.dp)).background(palette.primary.copy(alpha = 0.08f)).padding(horizontal = 9.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(tag, style = TextStyle(fontFamily = AppFontFamily, fontSize = 10.5f.sp, fontWeight = FontWeight.Bold, color = palette.primary))
    }
}

// ---------------------------------------------------------------------------
// Ish detali
// ---------------------------------------------------------------------------
@Composable
private fun JobDetail(job: Job, palette: AppPalette, onBack: () -> Unit, onApply: () -> Unit, onBookmark: () -> Unit) {
    var applied by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(top = 54.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(palette.glass).border(1.dp, palette.border, RoundedCornerShape(12.dp)).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
                    Icon(AppIcons.ArrowLeft, "Orqaga", tint = palette.ink, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.weight(1f))
                Icon(AppIcons.Bookmark, "Saqlash", tint = if (job.bookmarked) palette.primary else palette.inkFaint, modifier = Modifier.size(22.dp).clickable(onClick = onBookmark))
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(58.dp).clip(RoundedCornerShape(16.dp)).background(palette.primary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                    Text(job.companyMonogram, style = TextStyle(fontFamily = AppFontFamily, fontSize = 22.sp, fontWeight = FontWeight.Black, color = palette.primary))
                }
                Column(Modifier.weight(1f)) {
                    Text(job.title, style = TextStyle(fontFamily = AppFontFamily, fontSize = 18.sp, fontWeight = FontWeight.Black, color = palette.ink))
                    Text("${job.company} · ${job.location}", style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, color = palette.inkFaint))
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                job.tags.forEach { TagPill(it, palette) }
            }
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(palette.successBg).padding(14.dp)) {
                Column {
                    Text("Maosh", style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.sp, color = palette.inkFaint))
                    Text(job.salary, style = TextStyle(fontFamily = AppFontFamily, fontSize = 18.sp, fontWeight = FontWeight.Black, color = palette.successDeep))
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Bu ish sizning bo'limingizga (${job.field}) mos keladi. To'liq tavsif e'lon egasidan.", style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp, color = palette.inkMuted, lineHeight = 19.sp))
            Spacer(Modifier.height(16.dp))
        }
        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            PrimaryButton(
                if (applied) "Ariza yuborildi ✓" else "Ariza berish",
                onClick = { if (!applied) { onApply(); applied = true } },
                enabled = !applied,
            )
        }
    }
}
