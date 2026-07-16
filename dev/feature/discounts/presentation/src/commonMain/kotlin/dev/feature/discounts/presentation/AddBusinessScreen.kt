package dev.feature.discounts.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.components.AppIcons
import dev.core.designsystem.components.AppScreenScaffold
import dev.core.designsystem.components.GlassTextField
import dev.core.designsystem.components.PrimaryButton
import dev.core.designsystem.theme.AppPalette
import dev.core.designsystem.theme.appPalette
import dev.feature.discounts.domain.model.Business
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.presentation.components.IconSquareButton
import dev.feature.discounts.presentation.map.MapCenterRequest
import dev.feature.discounts.presentation.map.MapPicker
import dev.feature.discounts.presentation.map.MapPoint
import dev.feature.discounts.presentation.map.rememberUserLocation
import org.koin.compose.viewmodel.koinViewModel

// ===========================================================================
// Bosh ekran — Mening bizneslarim (ro'yxat + "+" tugma)
// ===========================================================================

@Composable
fun MyBusinessesScreen(
    onOpenBusiness: (Business) -> Unit,
    onEditBusiness: (Business) -> Unit,
    onAddBusiness: () -> Unit,
    vm: MyBusinessesViewModel = koinViewModel(),
) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()
    var toDelete by remember { mutableStateOf<Business?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 54.dp, bottom = 8.dp)) {
                Text("BIZNES MARKAZI", style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = palette.primary))
                Text("Bizneslarim", style = TextStyle(fontFamily = AppFontFamily, fontSize = 26.sp, fontWeight = FontWeight.Black, color = palette.ink))
            }

            when {
                state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = palette.primary, strokeWidth = 3.dp)
                }
                state.businesses.isEmpty() -> EmptyBusinesses(palette, onAddBusiness)
                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp),
                    verticalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    items(state.businesses, key = { it.id }) { biz ->
                        BusinessCard(
                            biz, palette,
                            onClick = { onOpenBusiness(biz) },
                            onEdit = { onEditBusiness(biz) },
                            onDelete = { toDelete = biz },
                        )
                    }
                }
            }
        }

        // Pastdagi "+" tugma — yangi biznes qo'shish.
        Row(
            Modifier.align(Alignment.BottomEnd).padding(20.dp)
                .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = palette.primary.copy(alpha = 0.5f))
                .clip(RoundedCornerShape(18.dp)).background(palette.primaryBrush)
                .clickable(onClick = onAddBusiness)
                .padding(horizontal = 20.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(AppIcons.Plus, null, tint = Color.White, modifier = Modifier.size(20.dp))
            Text("Biznes", style = TextStyle(fontFamily = AppFontFamily, fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.White))
        }

        // O'chirishни tasdiqlash dialogи.
        toDelete?.let { biz ->
            AlertDialog(
                onDismissRequest = { toDelete = null },
                title = { Text("Biznesni o'chirish", style = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Black, color = palette.ink)) },
                text = { Text("\"${biz.name}\" o'chirilsinmi? Bu amalни qaytarib bo'lmaydi.", style = TextStyle(fontFamily = AppFontFamily, color = palette.inkMuted)) },
                confirmButton = {
                    TextButton(onClick = { vm.delete(biz.id); toDelete = null }) {
                        Text("O'chirish", style = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626)))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { toDelete = null }) {
                        Text("Bekor qilish", style = TextStyle(fontFamily = AppFontFamily, color = palette.inkMuted))
                    }
                },
            )
        }
    }
}

@Composable
private fun BusinessCard(biz: Business, palette: AppPalette, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val accent = biz.businessType?.let { Color(it.accent) } ?: palette.primary
    Row(
        Modifier.fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(20.dp), spotColor = accent.copy(alpha = 0.22f))
            .clip(RoundedCornerShape(20.dp)).background(palette.glass)
            .border(1.dp, palette.border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(52.dp).clip(RoundedCornerShape(15.dp)).background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(biz.businessType?.emoji ?: "🏢", style = TextStyle(fontSize = 24.sp))
        }
        Column(Modifier.weight(1f)) {
            Text(biz.name, style = TextStyle(fontFamily = AppFontFamily, fontSize = 15.sp, fontWeight = FontWeight.Black, color = palette.ink), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                biz.businessType?.label ?: "Biznes",
                style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.5f.sp, fontWeight = FontWeight.Bold, color = accent),
            )
            biz.primaryBranch?.address?.takeIf { it.isNotBlank() }?.let {
                Text("📍 $it", style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.sp, color = palette.inkFaint), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        // Tahrirlash tugmasi.
        Box(
            Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(palette.primary.copy(alpha = 0.12f))
                .clickable(onClick = onEdit),
            contentAlignment = Alignment.Center,
        ) {
            Icon(AppIcons.Pencil, "Tahrirlash", tint = palette.primary, modifier = Modifier.size(16.dp))
        }
        // O'chirish tugmasi.
        Box(
            Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFFEE2E2))
                .clickable(onClick = onDelete),
            contentAlignment = Alignment.Center,
        ) {
            Icon(AppIcons.Close, "O'chirish", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun EmptyBusinesses(palette: AppPalette, onAdd: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(84.dp).shadow(18.dp, RoundedCornerShape(26.dp), spotColor = palette.primary.copy(alpha = 0.5f))
                .clip(RoundedCornerShape(26.dp)).background(palette.primaryBrush),
            contentAlignment = Alignment.Center,
        ) { Icon(AppIcons.Store, null, tint = Color.White, modifier = Modifier.size(38.dp)) }
        Spacer(Modifier.height(18.dp))
        Text("Hali biznes yo'q", style = TextStyle(fontFamily = AppFontFamily, fontSize = 17.sp, fontWeight = FontWeight.Black, color = palette.ink))
        Spacer(Modifier.height(6.dp))
        Text(
            "Birinchi biznesingizni qo'shing — keyin unga chegirma va e'lonlar joylaysiz.",
            style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, color = palette.inkMuted, lineHeight = 18.sp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        PrimaryButton("Biznes qo'shish", onAdd, trailingIcon = AppIcons.Plus)
    }
}

// ===========================================================================
// Biznes qo'shish — nom, telefon, TUR (majburiy), lokatsiya
// ===========================================================================

@Composable
fun AddBusinessScreen(
    onClose: () -> Unit,
    onSaved: () -> Unit,
    // `null` — yangi biznes; aks holda mavjud biznesni tahrirlash.
    businessId: String? = null,
    vm: AddBusinessViewModel = koinViewModel(),
) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(businessId) { if (businessId != null) vm.loadForEdit(businessId) }
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    if (state.pickingOnMap) {
        BusinessMapScreen(state, palette, vm)
        return
    }

    AppScreenScaffold(scroll = true, topPadding = 54) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            IconSquareButton(onClose, AppIcons.ArrowLeft, palette)
            Text(if (state.editing) "Biznesni tahrirlash" else "Biznes qo'shish", style = TextStyle(fontFamily = AppFontFamily, fontSize = 20.sp, fontWeight = FontWeight.Black, color = palette.ink))
        }
        Spacer(Modifier.height(18.dp))

        FieldLabelLocal("Biznes turi", palette)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.availableTypes) { type ->
                TypeChip(type, state.businessType == type, { vm.onType(type) }, palette)
            }
        }

        Spacer(Modifier.height(16.dp))
        FieldLabelLocal("Biznes nomi", palette)
        Spacer(Modifier.height(7.dp))
        GlassTextField(state.name, vm::onName, "Masalan: Kafe Aurora")

        Spacer(Modifier.height(14.dp))
        FieldLabelLocal("Telefon raqami", palette)
        Spacer(Modifier.height(7.dp))
        GlassTextField(
            value = state.phone,
            onValueChange = vm::onPhone,
            placeholder = "90 123 45 67",
            leadingContent = { Text("🇺🇿 +998", style = TextStyle(fontFamily = AppFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = palette.ink)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )

        Spacer(Modifier.height(14.dp))
        FieldLabelLocal("Joylashuv", palette)
        Spacer(Modifier.height(7.dp))
        LocationCard(state.branch?.address, palette, onPick = vm::openMap)

        Spacer(Modifier.height(20.dp))
        state.error?.let {
            Text(it, style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, color = Color(0xFFDC2626)))
            Spacer(Modifier.height(10.dp))
        }
        PrimaryButton(if (state.saving) "Saqlanmoqda..." else "Biznesni saqlash", onClick = vm::save, enabled = state.canSave)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun TypeChip(type: BusinessType, active: Boolean, onClick: () -> Unit, palette: AppPalette) {
    val shape = RoundedCornerShape(13.dp)
    Row(
        Modifier.clip(shape)
            .background(if (active) palette.primary else palette.glass)
            .then(if (active) Modifier else Modifier.border(1.dp, palette.border, shape))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(type.emoji, style = TextStyle(fontSize = 15.sp))
        Text(type.label, style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, fontWeight = FontWeight.Bold, color = if (active) Color.White else palette.inkMuted), maxLines = 1)
    }
}

@Composable
private fun LocationCard(address: String?, palette: AppPalette, onPick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(palette.glass)
            .border(1.dp, palette.border, RoundedCornerShape(14.dp))
            .clickable(onClick = onPick).padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("📍", style = TextStyle(fontSize = 18.sp))
        Text(
            address?.takeIf { it.isNotBlank() } ?: "Xaritadan joyni tanlang",
            style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (address != null) palette.ink else palette.inkFaint),
            modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis,
        )
        Icon(AppIcons.ChevronRight, null, tint = palette.inkFaint, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun FieldLabelLocal(text: String, palette: AppPalette) {
    Text(text, style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, fontWeight = FontWeight.Bold, color = palette.label))
}

@Composable
private fun BusinessMapScreen(state: AddBusinessUiState, palette: AppPalette, vm: AddBusinessViewModel) {
    val userLocation = rememberUserLocation()
    var centerRequest by remember { mutableStateOf<MapCenterRequest?>(null) }
    var requestId by remember { mutableStateOf(0) }
    var pickedPoint by remember { mutableStateOf<MapPoint?>(null) }

    LaunchedEffect(userLocation) {
        if (userLocation != null && centerRequest == null) {
            requestId++
            centerRequest = MapCenterRequest(userLocation, requestId)
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 54.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            IconSquareButton(vm::closeMap, AppIcons.ArrowLeft, palette)
            Column(Modifier.weight(1f)) {
                Text("Biznes joyi", style = TextStyle(fontFamily = AppFontFamily, fontSize = 18.sp, fontWeight = FontWeight.Black, color = palette.ink))
                Text(
                    if (state.resolvingAddress) "Manzil aniqlanmoqda..." else "Xaritani suring — belgi joyni ko'rsatadi",
                    style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.5f.sp, color = palette.inkFaint),
                )
            }
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            GlassTextField(state.searchQuery, vm::onSearchQuery, "Qidirish: Mega Planet, Amir Temur ko'chasi...", leading = AppIcons.Search, height = 46)
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().weight(1f)) {
            MapPicker(initial = userLocation, onCenterChanged = { pickedPoint = it }, modifier = Modifier.fillMaxSize(), centerRequest = centerRequest)
            if (state.searchResults.isNotEmpty()) {
                Column(
                    Modifier.align(Alignment.TopCenter).padding(horizontal = 12.dp, vertical = 8.dp)
                        .fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White),
                ) {
                    state.searchResults.forEach { place ->
                        Text(
                            place.title,
                            style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF14102D)),
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth().clickable {
                                requestId++
                                centerRequest = MapCenterRequest(MapPoint(place.lat, place.lng), requestId)
                                vm.clearSearch()
                            }.padding(horizontal = 12.dp, vertical = 9.dp),
                        )
                    }
                }
            }
        }
        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
            PrimaryButton(
                if (state.resolvingAddress) "Manzil aniqlanmoqda..." else "Shu yerni tanlash",
                onClick = { pickedPoint?.let { vm.setLocationFromMap(it.lat, it.lng) } },
                enabled = pickedPoint != null && !state.resolvingAddress,
            )
        }
    }
}
