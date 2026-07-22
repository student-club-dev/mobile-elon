package dev.feature.discounts.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.GlassTextField
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.component.ScreenTopBar
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.common_back
import dev.core.uikit.resources.common_retry
import dev.core.uikit.resources.discounts_map_branch_title
import dev.core.uikit.resources.discounts_map_confirm
import dev.core.uikit.resources.discounts_map_hint
import dev.core.uikit.resources.discounts_map_search_hint
import dev.core.uikit.resources.discounts_resolving_address
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppSize
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.discounts.presentation.components.MapSearchResults
import dev.feature.discounts.presentation.components.MyLocationButton
import dev.feature.discounts.presentation.form.TypeListingForm
import dev.core.uikit.map.MapCenterRequest
import dev.core.uikit.map.MapPicker
import dev.core.uikit.map.MapPoint
import dev.core.uikit.map.rememberUserLocation
import org.jetbrains.compose.resources.stringResource
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
                CircularProgressIndicator(color = palette.primary, strokeWidth = 3.dp)
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
                style = AppType.body.copy(color = palette.inkMuted),
                textAlign = TextAlign.Center,
            )
            if (onRetry != null) {
                PrimaryButton(stringResource(Res.string.common_retry), onRetry, palette = palette)
            }
            Text(
                stringResource(Res.string.common_back),
                modifier = Modifier.clickable(onClick = onBack).padding(AppSpacing.sm),
                style = AppType.label.copy(color = palette.primary),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Filial joyini xaritadan tanlash
// ---------------------------------------------------------------------------

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
        ScreenTopBar(
            title = stringResource(Res.string.discounts_map_branch_title),
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
                height = AppSize.fieldHeight,
                palette = palette,
            )
        }
        Spacer(Modifier.height(10.dp))

        Box(Modifier.fillMaxWidth().weight(1f)) {
            MapPicker(
                initial = userLocation,
                dark = palette.dark,
                onCenterChanged = { point -> pickedPoint = point },
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
                    // Pastdagi "tasdiqlash" tugmasi ustida turadi.
                    bottomPadding = 92.dp,
                )
            }
        }

        Box(Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg, vertical = 10.dp)) {
            PrimaryButton(
                stringResource(
                    if (state.resolvingAddress) Res.string.discounts_resolving_address
                    else Res.string.discounts_map_confirm,
                ),
                onClick = { pickedPoint?.let { vm.addBranchFromMap(it.lat, it.lng) } },
                enabled = pickedPoint != null && !state.resolvingAddress,
                palette = palette,
            )
        }
    }
}
