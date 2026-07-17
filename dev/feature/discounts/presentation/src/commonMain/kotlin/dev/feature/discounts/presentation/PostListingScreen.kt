package dev.feature.discounts.presentation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.designsystem.components.AppFontFamily
import dev.core.designsystem.components.AppIcons
import dev.core.designsystem.components.GlassTextField
import dev.core.designsystem.components.PrimaryButton
import dev.core.designsystem.theme.AppPalette
import dev.core.designsystem.theme.appPalette
import dev.feature.discounts.domain.model.BusinessType
import dev.feature.discounts.domain.model.ListingField
import dev.feature.discounts.presentation.components.FormSection
import dev.feature.discounts.presentation.components.IconSquareButton
import dev.feature.discounts.presentation.form.TypeListingForm
import dev.feature.discounts.presentation.map.MapCenterRequest
import dev.feature.discounts.presentation.map.MapPicker
import dev.feature.discounts.presentation.map.MapPoint
import dev.feature.discounts.presentation.map.rememberUserLocation
import org.koin.compose.viewmodel.koinViewModel

/**
 * E'lon qo'yish. Tur biznesdan **meros** olinadi (tur tanlash grid'i yo'q) — forma darrov
 * o'sha turga mos ochiladi (`form/TypeForms.kt`), kerak bo'lganda xarita. Yozuvlar turga
 * qarab farq qiladi: kafeda "Taom nomi", game club'da "Sessiya".
 *
 * Chegirma/oddiy — forma ichидаgi toggle: chegirma bo'lsa 2 narx (oldingi + yangi), aks holda 1.
 */
@Composable
fun PostListingScreen(
    onClose: () -> Unit,
    onPublished: () -> Unit,
    editListingId: String? = null,
    // `true` — Chegirma tab'idan, `false` — E'lon tab'idan (rejim qulflanadi).
    initialDiscount: Boolean? = null,
    // E'lon shu biznesga tegishli — nom/lokatsiya/tur biznesdan meros olinadi.
    businessId: String? = null,
    vm: PostListingViewModel = koinViewModel(),
) {
    val palette = appPalette
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(editListingId) { if (editListingId != null) vm.loadForEdit(editListingId) }
    LaunchedEffect(businessId) {
        if (editListingId == null && businessId != null) vm.prefillFromBusiness(businessId)
    }
    LaunchedEffect(initialDiscount) {
        if (editListingId == null && initialDiscount != null) vm.setInitialMode(initialDiscount)
    }
    LaunchedEffect(state.published) { if (state.published) onPublished() }

    val type = state.businessType
    val loadError = state.loadError

    when {
        // Xarita hamma narsadan ustun — joy tanlanmaguncha forma ko'rinmaydi.
        state.pickingOnMap -> BranchMapScreen(state, palette, vm)
        // Biznes yuklanmadi — foydalanuvchi spinnerда qamalib qolmasin: xato + qayta urinish/orqaga.
        type == null && loadError != null ->
            LoadFailed(
                message = loadError,
                palette = palette,
                onBack = onClose,
                onRetry = businessId?.let { id -> { vm.prefillFromBusiness(id) } },
            )
        // Biznes turi hali yuklanmoqda — qisqa spinner. "Chegirma e'loni" tur tanlash grid'i YO'Q:
        // e'lon turi biznesdan meros olinadi.
        type == null ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator(color = palette.primary, strokeWidth = 3.dp)
            }
        else -> TypeListingForm(type, state, palette, vm, onBack = onClose)
    }
}

/**
 * Biznes ma'lumoti kelmaganда ko'rsatiladi — cheksiz spinner o'rniga aniq xato va **ikkita
 * chiqish yo'li**: qayta urinish yoki orqaga.
 */
@Composable
private fun LoadFailed(
    message: String,
    palette: AppPalette,
    onBack: () -> Unit,
    onRetry: (() -> Unit)?,
) {
    Box(Modifier.fillMaxSize().padding(28.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                message,
                style = TextStyle(
                    fontFamily = AppFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = palette.inkMuted,
                ),
                textAlign = TextAlign.Center,
            )
            if (onRetry != null) PrimaryButton("Qayta urinish", onRetry)
            Text(
                "Orqaga",
                modifier = Modifier.clickable(onClick = onBack).padding(8.dp),
                style = TextStyle(
                    fontFamily = AppFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.primary,
                ),
            )
        }
    }
}


// ---------------------------------------------------------------------------
// Filiallar — xaritadan
// ---------------------------------------------------------------------------

/**
 * Filiallar bo'limi. Manzil qo'lda yozilmaydi: "+" bosiladi, xaritadan joy tanlanadi,
 * manzil teskari geokodlash bilan o'zi to'ladi.
 */
@Composable
fun BranchesSection(state: PostListingUiState, palette: AppPalette, vm: PostListingViewModel) {
    FormSection(
        title = "Filiallar",
        subtitle = "Talabaga eng yaqini masofasi bilan ko'rsatiladi",
        error = state.errorFor(ListingField.LOCATION),
    ) {
        state.branches.forEachIndexed { index, branch ->
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp))
                    .background(palette.fieldBg).padding(11.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier.size(28.dp).clip(RoundedCornerShape(9.dp)).background(palette.primary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${index + 1}",
                            style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Black, color = palette.primary),
                        )
                    }
                    Text(
                        branch.address,
                        style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.ink),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        AppIcons.Close,
                        "Filialni o'chirish",
                        tint = palette.inkFaint,
                        modifier = Modifier.size(15.dp).clickable { vm.removeBranch(index) },
                    )
                }

                GlassTextField(
                    branch.name.orEmpty(),
                    { vm.onBranchName(index, it) },
                    "Filial nomi (ixtiyoriy): Chilonzor filiali",
                    height = 44,
                )
            }
        }

        if (state.resolvingAddress) {
            Text(
                "Manzil aniqlanmoqda...",
                style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.5f.sp, fontWeight = FontWeight.Bold, color = palette.primary),
            )
        }

        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp))
                .background(palette.primary.copy(alpha = 0.08f))
                .clickable(onClick = vm::openMap)
                .padding(vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(AppIcons.Plus, null, tint = palette.primary, modifier = Modifier.size(17.dp))
            Spacer(Modifier.size(7.dp))
            Text(
                if (state.branches.isEmpty()) "Xaritadan filial belgilash" else "Yana bitta filial",
                style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.5f.sp, fontWeight = FontWeight.ExtraBold, color = palette.primary),
            )
        }
    }
}

/**
 * Xaritadan joy tanlash — Yandex uslubi: belgi ekran markazida turadi, xarita suriladi,
 * belgi ostidagi joy tanlanadi. Qidiruv ham bor.
 */
@Composable
private fun BranchMapScreen(state: PostListingUiState, palette: AppPalette, vm: PostListingViewModel) {
    val userLocation = rememberUserLocation()

    var centerRequest by remember { mutableStateOf<MapCenterRequest?>(null) }
    var requestId by remember { mutableStateOf(0) }
    var pickedPoint by remember { mutableStateOf<MapPoint?>(null) }

    // Joylashuv birinchi marta aniqlanganda xaritani avtomatik o'sha yerga olib boramiz.
    LaunchedEffect(userLocation) {
        if (userLocation != null && centerRequest == null) {
            requestId++
            centerRequest = MapCenterRequest(userLocation, requestId)
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 54.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            IconSquareButton(vm::closeMap, AppIcons.ArrowLeft, palette)
            Column(Modifier.weight(1f)) {
                Text(
                    "Filial joyi",
                    style = TextStyle(fontFamily = AppFontFamily, fontSize = 18.sp, fontWeight = FontWeight.Black, color = palette.ink),
                )
                Text(
                    if (state.resolvingAddress) "Manzil aniqlanmoqda..." else "Xaritani suring — belgi joyni ko'rsatadi",
                    style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.5f.sp, color = palette.inkFaint),
                )
            }
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            GlassTextField(
                state.searchQuery,
                vm::onSearchQuery,
                "Qidirish: Mega Planet, Amir Temur ko'chasi...",
                leading = AppIcons.Search,
                height = 46,
            )
        }
        Spacer(Modifier.height(10.dp))

        Box(Modifier.fillMaxWidth().weight(1f)) {
            MapPicker(
                initial = userLocation,
                onCenterChanged = { point -> pickedPoint = point },
                modifier = Modifier.fillMaxSize(),
                centerRequest = centerRequest,
            )

            if (state.searching || state.searchResults.isNotEmpty()) {
                Column(
                    Modifier.align(Alignment.TopCenter)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White),
                ) {
                    if (state.searching) {
                        Text(
                            "Qidirilmoqda...",
                            style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B6880)),
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                    state.searchResults.forEach { place ->
                        Column(
                            Modifier.fillMaxWidth()
                                .clickable {
                                    requestId++
                                    centerRequest = MapCenterRequest(MapPoint(place.lat, place.lng), requestId)
                                    vm.clearSearch()
                                }
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                place.title,
                                style = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF14102D)),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (place.subtitle.isNotBlank()) {
                                Text(
                                    place.subtitle,
                                    style = TextStyle(fontFamily = AppFontFamily, fontSize = 11.sp, color = Color(0xFF8A87A0)),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }

            if (userLocation != null) {
                Row(
                    Modifier.align(Alignment.BottomStart)
                        .padding(start = 12.dp, bottom = 92.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .clickable {
                            requestId++
                            centerRequest = MapCenterRequest(userLocation, requestId)
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("📍", style = TextStyle(fontSize = 13.sp))
                    Text(
                        "Mening joylashuvim",
                        style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF14102D)),
                    )
                }
            }
        }

        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
            PrimaryButton(
                if (state.resolvingAddress) "Manzil aniqlanmoqda..." else "Shu yerni tanlash",
                onClick = { pickedPoint?.let { vm.addBranchFromMap(it.lat, it.lng) } },
                enabled = pickedPoint != null && !state.resolvingAddress,
            )
        }
    }
}

/** Bir martalik xabar (rasm xatosi, "Qoralama saqlandi"). */
@Composable
fun MessageBar(message: String, palette: AppPalette, onDismiss: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(palette.primary.copy(alpha = 0.10f))
            .clickable(onClick = onDismiss)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            message,
            style = TextStyle(fontFamily = AppFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.primary),
            modifier = Modifier.weight(1f),
        )
        Icon(AppIcons.Close, "Yopish", tint = palette.primary, modifier = Modifier.size(14.dp))
    }
}
