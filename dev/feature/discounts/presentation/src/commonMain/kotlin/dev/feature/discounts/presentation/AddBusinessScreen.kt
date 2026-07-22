package dev.feature.discounts.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.AppScreenScaffold
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.component.ScreenTopBar
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.common_back
import dev.core.uikit.resources.discounts_business_add
import dev.core.uikit.resources.discounts_business_edit
import dev.core.uikit.resources.discounts_business_name_hint
import dev.core.uikit.resources.discounts_business_name_label
import dev.core.uikit.resources.discounts_business_save
import dev.core.uikit.resources.discounts_business_type_label
import dev.core.uikit.resources.discounts_location_label
import dev.core.uikit.resources.discounts_map_business_title
import dev.core.uikit.resources.discounts_map_confirm
import dev.core.uikit.resources.discounts_map_hint
import dev.core.uikit.resources.discounts_map_search_hint
import dev.core.uikit.resources.discounts_name_latin_only
import dev.core.uikit.resources.discounts_phone_hint
import dev.core.uikit.resources.discounts_phone_label
import dev.core.uikit.resources.discounts_phone_prefix
import dev.core.uikit.resources.discounts_region_label
import dev.core.uikit.resources.discounts_resolving_address
import dev.core.uikit.resources.discounts_saving
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.discounts.presentation.components.BusinessTypeChip
import dev.feature.discounts.presentation.components.FormFieldLabel
import dev.feature.discounts.presentation.components.LocationCard
import dev.feature.discounts.presentation.components.MapSearchResults
import dev.feature.discounts.presentation.components.RegionSelector
import dev.feature.discounts.presentation.components.MyLocationButton
import dev.core.uikit.map.MapCenterRequest
import dev.core.uikit.map.MapPicker
import dev.core.uikit.map.MapPoint
import dev.core.uikit.map.rememberUserLocation
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import dev.core.uikit.component.AppFieldType
import dev.core.uikit.component.HintText

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

    AppScreenScaffold(scroll = true, topPadding = 54.dp, palette = palette) {
        ScreenTopBar(
            title = stringResource(
                if (state.editing) Res.string.discounts_business_edit else Res.string.discounts_business_add,
            ),
            onBack = onClose,
            backContentDescription = stringResource(Res.string.common_back),
            titleFontSize = 20.sp,
            palette = palette,
        )
        Spacer(Modifier.height(18.dp))

        FormFieldLabel(stringResource(Res.string.discounts_business_type_label), palette)
        Spacer(Modifier.height(AppSpacing.sm))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            items(state.availableTypes) { info ->
                BusinessTypeChip(info, state.businessType == info.type, { vm.onType(info.type) }, palette)
            }
        }

        Spacer(Modifier.height(AppSpacing.lg))
        FormFieldLabel(stringResource(Res.string.discounts_business_name_label), palette)
        Spacer(Modifier.height(7.dp))
        GlassTextField(
            state.name,
            vm::onName,
            stringResource(Res.string.discounts_business_name_hint),
            type = AppFieldType.LatinText,
            palette = palette,
        )
        // Qoida OLDINDAN ko'rsatiladi. Ilgari u faqat kirill yozilgandan keyin chiqardi, lekin
        // endi `AppFieldType.LatinText` harfni maydonga umuman kiritmaydi — foydalanuvchi
        // "nega yozilmayapti?" deb qolmasligi uchun sabab doim ko'rinib turadi.
        Spacer(Modifier.height(5.dp))
        HintText(stringResource(Res.string.discounts_name_latin_only), palette = palette)

        Spacer(Modifier.height(14.dp))
        FormFieldLabel(stringResource(Res.string.discounts_phone_label), palette)
        Spacer(Modifier.height(7.dp))
        GlassTextField(
            value = state.phone,
            onValueChange = vm::onPhone,
            placeholder = stringResource(Res.string.discounts_phone_hint),
            leadingContent = {
                Text(
                    stringResource(Res.string.discounts_phone_prefix),
                    style = AppType.body.copy(fontWeight = AppType.label.fontWeight, color = palette.ink),
                )
            },
            type = AppFieldType.UzPhone,
            palette = palette,
        )

        Spacer(Modifier.height(14.dp))
        FormFieldLabel(stringResource(Res.string.discounts_region_label), palette)
        Spacer(Modifier.height(7.dp))
        RegionSelector(
            regionName = state.regionName,
            expanded = state.regionPickerOpen,
            onToggle = { if (state.regionPickerOpen) vm.closeRegionPicker() else vm.openRegionPicker() },
            onSelect = vm::onRegion,
            palette = palette,
        )

        Spacer(Modifier.height(14.dp))
        FormFieldLabel(stringResource(Res.string.discounts_location_label), palette)
        Spacer(Modifier.height(7.dp))
        LocationCard(state.branch?.address, palette, onPick = vm::openMap)

        Spacer(Modifier.height(20.dp))
        state.error?.let {
            Text(it, style = AppType.error.copy(color = palette.danger))
            Spacer(Modifier.height(10.dp))
        }
        PrimaryButton(
            stringResource(
                if (state.saving) Res.string.discounts_saving else Res.string.discounts_business_save,
            ),
            onClick = vm::save,
            enabled = state.canSave,
            palette = palette,
        )
        Spacer(Modifier.height(AppSpacing.xl))
    }
}

// ---------------------------------------------------------------------------
// Biznes joyini xaritadan tanlash
// ---------------------------------------------------------------------------

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
        ScreenTopBar(
            title = stringResource(Res.string.discounts_map_business_title),
            subtitle = stringResource(
                if (state.resolvingAddress) Res.string.discounts_resolving_address
                else Res.string.discounts_map_hint,
            ),
            onBack = vm::closeMap,
            backContentDescription = stringResource(Res.string.common_back),
            modifier = Modifier.padding(horizontal = AppSpacing.lg).padding(top = 54.dp, bottom = AppSpacing.md),
            palette = palette,
        )
        Column(Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg)) {
            GlassTextField(
                state.searchQuery,
                // Qidiruv xaritaning ko'rinib turgan markazidan boshlab yaqinlashtiriladi.
                { query -> vm.onSearchQuery(query, pickedPoint?.lat, pickedPoint?.lng) },
                stringResource(Res.string.discounts_map_search_hint),
                leading = AppIcons.Search,
                height = 46.dp,
                palette = palette,
            )
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().weight(1f)) {
            MapPicker(
                initial = userLocation,
                dark = palette.dark,
                onCenterChanged = { pickedPoint = it },
                modifier = Modifier.fillMaxSize(),
                centerRequest = centerRequest,
            )
            MapSearchResults(
                results = state.searchResults,
                searching = state.searching,
                searched = state.searched,
                onSelect = { place ->
                    requestId++
                    centerRequest = MapCenterRequest(MapPoint(place.lat, place.lng), requestId)
                    vm.clearSearch()
                },
                palette = palette,
            )
            if (userLocation != null) {
                MyLocationButton(
                    onClick = {
                        requestId++
                        centerRequest = MapCenterRequest(userLocation, requestId)
                    },
                    palette = palette,
                )
            }
        }
        Box(Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg, vertical = 10.dp)) {
            PrimaryButton(
                stringResource(
                    if (state.resolvingAddress) Res.string.discounts_resolving_address
                    else Res.string.discounts_map_confirm,
                ),
                onClick = { pickedPoint?.let { vm.setLocationFromMap(it.lat, it.lng) } },
                enabled = pickedPoint != null && !state.resolvingAddress,
                palette = palette,
            )
        }
    }
}
