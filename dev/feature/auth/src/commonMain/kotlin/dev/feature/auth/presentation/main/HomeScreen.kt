package dev.feature.auth.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import dev.core.domain.model.DiscountCategory
import dev.core.domain.model.DiscountOffer
import dev.core.domain.model.DiscountTag
import dev.core.domain.model.FriendStatus
import dev.core.domain.model.Job
import dev.core.domain.model.Student
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.components.AppIcons
import dev.core.designsystem.theme.AppPalette
import dev.core.designsystem.theme.appPalette
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onOpenProfile: () -> Unit = {},
    onOpenChat: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenDiscounts: () -> Unit = {},
    onOpenJobs: () -> Unit = {},
    onOpenStudents: () -> Unit = {},
    vm: HomeViewModel = koinViewModel(),
) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        HomeHeader(state, palette, onOpenProfile, onOpenChat, onOpenNotifications)
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 18.dp)) {
            DiscountsSection(state.categories, state.featured, palette, onOpenDiscounts)
            Spacer(Modifier.height(24.dp))
            JobsSection(state.jobs, palette, onOpenJobs) { vm.toggleBookmark(it) }
            Spacer(Modifier.height(24.dp))
            StudentsSection(state.students, palette, onOpenStudents) { vm.toggleFriend(it) }
            Spacer(Modifier.height(110.dp)) // pastki navigatsiya uchun joy
        }
    }
}

// ---------------------------------------------------------------------------
// Header
// ---------------------------------------------------------------------------
@Composable
private fun HomeHeader(state: HomeUiState, palette: AppPalette, onOpenProfile: () -> Unit, onOpenChat: () -> Unit, onOpenNotifications: () -> Unit) {
    val gradient = Brush.linearGradient(listOf(Color(0xFF6C47FF), Color(0xFF7C4DFF), Color(0xFF5B34D6)))
    Box(
        Modifier.fillMaxWidth()
            .background(gradient, RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .padding(start = 20.dp, end = 20.dp, top = 54.dp, bottom = 24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(46.dp).clip(RoundedCornerShape(999.dp)).background(Color.White.copy(alpha = 0.20f)).clickable(onClick = onOpenProfile),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    state.userName.take(1).uppercase(),
                    style = TextStyle(fontFamily = AppFontFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Assalomu alaykum 👋", style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, color = Color.White.copy(alpha = 0.85f)))
                Spacer(Modifier.height(2.dp))
                Text(state.userName, style = TextStyle(fontFamily = AppFontFamily, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White), maxLines = 1, overflow = TextOverflow.Ellipsis)
                val badge = listOfNotNull(state.universityMonogram, state.courseLabel).joinToString(" · ")
                if (badge.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Row(
                        Modifier.clip(RoundedCornerShape(999.dp)).background(Color.White.copy(alpha = 0.18f)).padding(horizontal = 9.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(AppIcons.GraduationCap, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(badge, style = TextStyle(fontFamily = AppFontFamily, fontSize = 10.5f.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(Color.White.copy(alpha = 0.18f)).clickable(onClick = onOpenChat),
                    contentAlignment = Alignment.Center,
                ) { Icon(AppIcons.MessageSquare, "Xabarlar", tint = Color.White, modifier = Modifier.size(18.dp)) }
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(Color.White.copy(alpha = 0.18f)).clickable(onClick = onOpenNotifications),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(AppIcons.Bell, "Bildirishnomalar", tint = Color.White, modifier = Modifier.size(18.dp))
                    if (state.hasUnreadNotifications) {
                        Box(Modifier.align(Alignment.TopEnd).padding(10.dp).size(7.dp).clip(RoundedCornerShape(999.dp)).background(Color(0xFFFF5A5A)))
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Umumiy bo'lim sarlavhasi
// ---------------------------------------------------------------------------
@Composable
private fun SectionHeader(title: String, action: String? = null, subtitle: String? = null, palette: AppPalette, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = TextStyle(fontFamily = AppFontFamily, fontSize = 17.sp, fontWeight = FontWeight.Black, color = palette.ink))
            if (subtitle != null) {
                Text(subtitle, style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.5f.sp, color = palette.inkFaint))
            }
        }
        if (action != null) {
            Row(
                Modifier.then(if (onAction != null) Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onAction).padding(horizontal = 4.dp, vertical = 2.dp) else Modifier),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(action, style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.primary))
                Icon(AppIcons.ChevronRight, null, tint = palette.primary, modifier = Modifier.size(15.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Chegirmalar
// ---------------------------------------------------------------------------
@Composable
private fun DiscountsSection(categories: List<DiscountCategory>, featured: DiscountOffer?, palette: AppPalette, onSeeAll: () -> Unit) {
    SectionHeader("Chegirmalar", action = "Barchasi", palette = palette, onAction = onSeeAll)
    Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        categories.take(4).forEach { CategoryChip(it, palette, onSeeAll) }
        MoreChip(palette, onSeeAll)
    }
    if (featured != null) {
        Spacer(Modifier.height(14.dp))
        FeaturedOfferCard(featured, palette, onSeeAll)
    }
}

@Composable
private fun CategoryChip(category: DiscountCategory, palette: AppPalette, onClick: () -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(999.dp)).background(palette.glass).border(1.dp, palette.border, RoundedCornerShape(999.dp)).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(category.emoji, style = TextStyle(fontSize = 14.sp))
        Text(category.name, style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.ink))
    }
}

@Composable
private fun MoreChip(palette: AppPalette, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(999.dp)).background(palette.primary.copy(alpha = 0.10f)).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("Yana", style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.primary))
    }
}

@Composable
private fun FeaturedOfferCard(offer: DiscountOffer, palette: AppPalette, onClick: () -> Unit) {
    val accent = Color(offer.bannerAccent)
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(palette.glassStrong).border(1.dp, palette.border, RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) { Text(offer.emoji, style = TextStyle(fontSize = 24.sp)) }
        Column(Modifier.weight(1f)) {
            Text(
                "${offer.merchant} — ${offer.discountPercent}% chegirma",
                style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.5f.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink),
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            val tagText = if (offer.tag == DiscountTag.STUDENT_ID) "Talaba ID bilan" else "Promokod"
            val meta = listOfNotNull(tagText, offer.expiry).joinToString(" · ")
            Text(meta, style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.sp, color = palette.inkFaint))
        }
        Box(
            Modifier.clip(RoundedCornerShape(10.dp)).background(accent).padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("−${offer.discountPercent}%", style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White))
        }
    }
}

// ---------------------------------------------------------------------------
// Ishlar
// ---------------------------------------------------------------------------
@Composable
private fun JobsSection(jobs: List<Job>, palette: AppPalette, onSeeAll: () -> Unit, onBookmark: (Job) -> Unit) {
    SectionHeader("Ishlar", action = "Barchasi", subtitle = "Sizning bo'limlaringiz bo'yicha", palette = palette, onAction = onSeeAll)
    Spacer(Modifier.height(12.dp))
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        jobs.take(3).forEach { JobRow(it, palette, onBookmark) }
    }
}

@Composable
private fun JobRow(job: Job, palette: AppPalette, onBookmark: (Job) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(palette.glass).border(1.dp, palette.border, RoundedCornerShape(16.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box(
            Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(palette.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(job.companyMonogram, style = TextStyle(fontFamily = AppFontFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = palette.primary))
        }
        Column(Modifier.weight(1f)) {
            Text(job.title, style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.5f.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(
                "${job.company} · ${job.location} · ${job.category}",
                style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.sp, color = palette.inkFaint),
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(job.salary, style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, fontWeight = FontWeight.Bold, color = palette.successDeep))
        }
        Icon(
            AppIcons.Bookmark, "Saqlash",
            tint = if (job.bookmarked) palette.primary else palette.inkFaint,
            modifier = Modifier.size(20.dp).clickable { onBookmark(job) },
        )
    }
}

// ---------------------------------------------------------------------------
// Studentlar
// ---------------------------------------------------------------------------
@Composable
private fun StudentsSection(students: List<Student>, palette: AppPalette, onSeeAll: () -> Unit, onFriend: (Student) -> Unit) {
    SectionHeader("Studentlar", action = "Ko'proq", subtitle = "Universitet bo'yicha do'st toping", palette = palette, onAction = onSeeAll)
    Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
        students.take(6).forEach { StudentConnectCard(it, palette, onFriend) }
    }
}

@Composable
private fun StudentConnectCard(student: Student, palette: AppPalette, onFriend: (Student) -> Unit) {
    Column(
        Modifier.width(128.dp).clip(RoundedCornerShape(16.dp)).background(palette.glass).border(1.dp, palette.border, RoundedCornerShape(16.dp)).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(
            Modifier.size(48.dp).clip(RoundedCornerShape(999.dp)).background(palette.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(student.initial, style = TextStyle(fontFamily = AppFontFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = palette.primary))
        }
        Text(student.firstName, style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = palette.ink), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(student.universityMonogram, style = TextStyle(fontFamily = AppFontFamily, fontSize = 10.5f.sp, color = palette.inkFaint))
        FriendButton(student, palette, onFriend)
    }
}

@Composable
private fun FriendButton(student: Student, palette: AppPalette, onFriend: (Student) -> Unit) {
    val pending = student.friendStatus == FriendStatus.PENDING
    val friends = student.friendStatus == FriendStatus.FRIENDS
    val label = when {
        friends -> "Do'st"
        pending -> "Kutilmoqda"
        else -> "+ Do'st"
    }
    val bg = if (pending || friends) palette.primary.copy(alpha = 0.12f) else palette.primary
    val fg = if (pending || friends) palette.primary else Color.White
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(bg).clickable { onFriend(student) }.padding(vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = fg), maxLines = 1)
    }
}
