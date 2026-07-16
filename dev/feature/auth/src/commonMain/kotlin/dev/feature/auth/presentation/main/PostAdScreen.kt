package dev.feature.auth.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.domain.model.AdType
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.components.AppIcons
import dev.core.designsystem.components.GlassTextField
import dev.core.designsystem.components.PrimaryButton
import dev.core.designsystem.theme.AppPalette
import dev.core.designsystem.theme.appPalette
import org.koin.compose.viewmodel.koinViewModel

private data class AdTypeInfo(val type: AdType, val title: String, val subtitle: String, val icon: ImageVector)

private val adTypes = listOf(
    AdTypeInfo(AdType.JOB, "Ish e'loni", "Xodim yoki part-time izlash", AppIcons.Briefcase),
    AdTypeInfo(AdType.RENTAL, "Ijara / Turar joy", "Kvartira, hostel, room-mate", AppIcons.Home),
    AdTypeInfo(AdType.SALE, "Sotuv (bozor)", "Kitob, texnika, buyum sotish", AppIcons.Store),
    AdTypeInfo(AdType.SERVICE, "Xizmat / Repetitor", "Dars berish, dizayn, tarjima", AppIcons.GraduationCap),
    AdTypeInfo(AdType.OTHER, "Boshqa e'lon", "Tadbir, jamoa, yo'qoldi-topildi", AppIcons.MessageSquare),
)

@Composable
fun PostAdScreen(onClose: () -> Unit, editAdId: String? = null, vm: PostAdViewModel = koinViewModel()) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()

    // Tahrirlash rejimi — mavjud e'lonni forma'ga yuklaymiz.
    LaunchedEffect(editAdId) { if (editAdId != null) vm.loadForEdit(editAdId) }
    LaunchedEffect(state.posted) { if (state.posted) onClose() }

    if (state.type == null) {
        AdTypePicker(palette, onClose = onClose, onPick = vm::selectType)
    } else {
        AdForm(state, palette, onBack = vm::backToTypes, vm = vm)
    }
}

// 1s — tur tanlash
@Composable
private fun AdTypePicker(palette: AppPalette, onClose: () -> Unit, onPick: (AdType) -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(top = 54.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            IconBtn(AppIcons.Close, palette, onClose)
            Text("Elon berish", style = TextStyle(fontFamily = AppFontFamily, fontSize = 20.sp, fontWeight = FontWeight.Black, color = palette.ink))
        }
        Spacer(Modifier.height(6.dp))
        Text("Qanday e'lon joylamoqchisiz?", style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, color = palette.inkMuted))
        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            adTypes.forEach { info ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(palette.glass).border(1.dp, palette.border, RoundedCornerShape(16.dp))
                        .clickable { onPick(info.type) }.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(palette.primary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Icon(info.icon, null, tint = palette.primary, modifier = Modifier.size(20.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(info.title, style = TextStyle(fontFamily = AppFontFamily, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink))
                        Text(info.subtitle, style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.5f.sp, color = palette.inkFaint))
                    }
                    Icon(AppIcons.ChevronRight, null, tint = palette.inkFaint, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// 1v — forma
@Composable
private fun AdForm(state: PostAdUiState, palette: AppPalette, onBack: () -> Unit, vm: PostAdViewModel) {
    val info = adTypes.first { it.type == state.type }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(top = 54.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                IconBtn(AppIcons.ArrowLeft, palette, onBack)
                Text(info.title, style = TextStyle(fontFamily = AppFontFamily, fontSize = 19.sp, fontWeight = FontWeight.Black, color = palette.ink))
            }
            Spacer(Modifier.height(16.dp))

            // Rasm qo'shish (dashed placeholder)
            Box(
                Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(16.dp)).background(palette.primary.copy(alpha = 0.05f)).border(1.dp, palette.border, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(AppIcons.ImageIcon, null, tint = palette.inkFaint, modifier = Modifier.size(26.dp))
                    Text("Rasm qo'shish", style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.inkFaint))
                }
            }
            Spacer(Modifier.height(14.dp))

            FieldTitle("E'lon sarlavhasi", palette)
            GlassTextField(state.title, vm::onTitle, "SMM menejer kerak", height = 48)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f)) {
                    FieldTitle("Kategoriya", palette)
                    GlassTextField(state.category, vm::onCategory, "IT", height = 48)
                }
                Column(Modifier.weight(1f)) {
                    FieldTitle("Narx / Maosh", palette)
                    GlassTextField(state.price, vm::onPrice, "3–5 mln", height = 48)
                }
            }
            Spacer(Modifier.height(12.dp))
            FieldTitle("Tavsif", palette)
            GlassTextField(state.description, vm::onDescription, "Batafsil ma'lumot...", height = 96)
            Spacer(Modifier.height(16.dp))
        }
        // Sticky tugma
        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            PrimaryButton("E'lonni joylash", vm::submit, enabled = state.canSubmit)
        }
    }
}

@Composable
private fun FieldTitle(text: String, palette: AppPalette) {
    Text(text, style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.label))
    Spacer(Modifier.height(7.dp))
}

@Composable
private fun IconBtn(icon: ImageVector, palette: AppPalette, onClick: () -> Unit) {
    Box(
        Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(palette.glass).border(1.dp, palette.border, RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, "Orqaga", tint = palette.ink, modifier = Modifier.size(18.dp)) }
}
