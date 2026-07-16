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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.domain.model.FriendStatus
import dev.core.domain.model.Student
import dev.core.domain.model.StudentSort
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.components.AppIcons
import dev.core.designsystem.theme.AppPalette
import dev.core.designsystem.theme.appPalette
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun StudentsScreen(vm: StudentsViewModel = koinViewModel()) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()
    var selectedId by remember { mutableStateOf<String?>(null) }
    val selected = state.students.firstOrNull { it.id == selectedId }

    if (selected != null) {
        StudentProfile(selected, palette, onBack = { selectedId = null }, onFriend = { vm.toggleFriend(selected) })
        return
    }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 54.dp)) {
            Text("Studentlar", style = TextStyle(fontFamily = AppFontFamily, fontSize = 24.sp, fontWeight = FontWeight.Black, color = palette.ink))
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SortChip("Mening univer", state.sort == StudentSort.MY_UNIVERSITY, { vm.setSort(StudentSort.MY_UNIVERSITY) }, palette)
                SortChip("Barcha univer", state.sort == StudentSort.ALL, { vm.setSort(StudentSort.ALL) }, palette)
                FilterChipButton(palette)
            }
        }

        Spacer(Modifier.height(14.dp))

        LazyColumn(
            Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            items(state.students, key = { it.id }) { student ->
                StudentRow(student, palette, onOpen = { selectedId = student.id }, onFriend = { vm.toggleFriend(student) })
            }
        }
    }
}

@Composable
private fun SortChip(label: String, active: Boolean, onClick: () -> Unit, palette: AppPalette) {
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
private fun FilterChipButton(palette: AppPalette) {
    Row(
        Modifier.clip(RoundedCornerShape(999.dp)).background(palette.glass).border(1.dp, palette.border, RoundedCornerShape(999.dp)).padding(horizontal = 13.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(AppIcons.Filter, null, tint = palette.inkMuted, modifier = Modifier.size(14.dp))
        Text("Filtr", style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, fontWeight = FontWeight.Bold, color = palette.inkMuted))
    }
}

@Composable
private fun StudentRow(student: Student, palette: AppPalette, onOpen: () -> Unit, onFriend: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(palette.glass).border(1.dp, palette.border, RoundedCornerShape(16.dp)).clickable(onClick = onOpen).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box(
            Modifier.size(46.dp).clip(RoundedCornerShape(999.dp)).background(palette.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(student.initial, style = TextStyle(fontFamily = AppFontFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = palette.primary))
        }
        Column(Modifier.weight(1f)) {
            Text(student.fullName, style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.5f.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(
                "${student.universityMonogram} · ${student.course}-kurs · ${student.faculty}",
                style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.sp, color = palette.inkFaint),
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
        FriendPill(student.friendStatus, onFriend, palette)
    }
}

@Composable
private fun FriendPill(status: FriendStatus, onClick: () -> Unit, palette: AppPalette) {
    val label = when (status) {
        FriendStatus.NONE -> "+ Do'st"
        FriendStatus.PENDING -> "Kutilmoqda"
        FriendStatus.FRIENDS -> "Do'stlar"
    }
    val filled = status == FriendStatus.NONE
    val bg = if (filled) palette.primary else palette.primary.copy(alpha = 0.12f)
    val fg = if (filled) Color.White else palette.primary
    Box(
        Modifier.clip(RoundedCornerShape(10.dp)).background(bg).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.5f.sp, fontWeight = FontWeight.ExtraBold, color = fg), maxLines = 1)
    }
}

// ---------------------------------------------------------------------------
// 1u — Student profili
// ---------------------------------------------------------------------------
@Composable
private fun StudentProfile(student: Student, palette: AppPalette, onBack: () -> Unit, onFriend: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(
            Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF6C47FF), Color(0xFF7C4DFF), Color(0xFF5B34D6))), RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .padding(start = 20.dp, end = 20.dp, top = 54.dp, bottom = 26.dp),
        ) {
            Column {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.18f)).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
                    Icon(AppIcons.ArrowLeft, "Orqaga", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(Modifier.size(72.dp).clip(RoundedCornerShape(24.dp)).background(Color.White.copy(alpha = 0.20f)), contentAlignment = Alignment.Center) {
                        Text(student.initial, style = TextStyle(fontFamily = AppFontFamily, fontSize = 30.sp, fontWeight = FontWeight.Black, color = Color.White))
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(student.fullName, style = TextStyle(fontFamily = AppFontFamily, fontSize = 19.sp, fontWeight = FontWeight.Black, color = Color.White))
                            Icon(AppIcons.ShieldCheck, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("${student.universityMonogram} · ${student.course}-kurs · ${student.faculty}", style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f)))
                    }
                }
            }
        }

        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            // Stats
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(palette.glass).border(1.dp, palette.border, RoundedCornerShape(16.dp)).padding(vertical = 14.dp)) {
                StatCell("${student.friendsCount}", "Do'stlar", palette, Modifier.weight(1f))
                StatCell("${student.adsCount}", "E'lonlar", palette, Modifier.weight(1f))
                StatCell(formatRating(student.rating), "Reyting", palette, Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))

            if (student.interests.isNotEmpty()) {
                Text("Qiziqishlar", style = TextStyle(fontFamily = AppFontFamily, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink))
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    student.interests.forEach { interest ->
                        Box(Modifier.clip(RoundedCornerShape(999.dp)).background(palette.primary.copy(alpha = 0.08f)).padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(interest, style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.primary))
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val friend = student.friendStatus
                val label = when (friend) {
                    FriendStatus.NONE -> "Do'st qo'shish"
                    FriendStatus.PENDING -> "Kutilmoqda"
                    FriendStatus.FRIENDS -> "Do'stlar"
                }
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                        .background(if (friend == FriendStatus.NONE) palette.primary else palette.primary.copy(alpha = 0.12f))
                        .clickable(onClick = onFriend).padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(label, style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.5f.sp, fontWeight = FontWeight.ExtraBold, color = if (friend == FriendStatus.NONE) Color.White else palette.primary))
                }
                Box(
                    Modifier.size(50.dp).clip(RoundedCornerShape(14.dp)).background(palette.glass).border(1.dp, palette.border, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) { Icon(AppIcons.MessageSquare, "Xabar", tint = palette.primary, modifier = Modifier.size(20.dp)) }
            }
        }
    }
}

@Composable
private fun StatCell(value: String, label: String, palette: AppPalette, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = TextStyle(fontFamily = AppFontFamily, fontSize = 17.sp, fontWeight = FontWeight.Black, color = palette.ink))
        Text(label, style = TextStyle(fontFamily = AppFontFamily, fontSize = 10.5f.sp, color = palette.inkFaint))
    }
}

private fun formatRating(rating: Double): String {
    val rounded = (rating * 10).toLong()
    return "${rounded / 10}.${rounded % 10}"
}
