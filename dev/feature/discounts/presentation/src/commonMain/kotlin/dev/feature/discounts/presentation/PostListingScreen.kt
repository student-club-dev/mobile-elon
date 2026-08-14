package dev.feature.discounts.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.uikit.component.PrimaryButton
import dev.core.uikit.component.ScreenTopBar
import dev.core.uikit.component.screenTopInset
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.common_back
import dev.core.uikit.resources.common_retry
import dev.core.uikit.resources.discounts_add_listing
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.discounts.presentation.form.TypeListingForm
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * E'lon qo'yish. Tur biznesdan **meros** olinadi (tur tanlash grid'i yo'q) — forma darrov
 * o'sha turga mos ochiladi (`form/TypeForms.kt`). Yozuvlar turga qarab farq qiladi:
 * kafeda "Taom nomi", game club'da "Sessiya".
 *
 * Filial bu ekranда yaratilmaydi — e'lon biznesning mavjud filiallaridan tanlaydi
 * (`branchIds`), yangi filial esa biznes profilida qo'shiladi.
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
    // Tahrirlash bo'lmasa biznesdan to'ldiramiz. `businessId` yo'q bo'lsa ham (nav argumenti
    // yo'qolган) bo'sh id bilan chaqiramiz — `prefillFromBusiness` xato holatini o'rnatadi va
    // ekran cheksiz spinner o'rniga "qayta urinish/orqaga" ko'rsatadi.
    LaunchedEffect(editListingId, businessId) {
        if (editListingId == null) vm.prefillFromBusiness(businessId.orEmpty())
    }
    LaunchedEffect(initialDiscount) {
        if (editListingId == null && initialDiscount != null) vm.setInitialMode(initialDiscount)
    }
    LaunchedEffect(state.published) { if (state.published) onPublished() }

    val type = state.businessType
    val loadError = state.loadError

    when {
        // Biznes yuklanmadi — foydalanuvchi spinnerда qamalib qolmasin: xato + qayta urinish/orqaga.
        type == null && loadError != null ->
            LoadFailed(
                message = loadError,
                palette = palette,
                onBack = onClose,
                // Tahrirlashда e'lonni, aks holда biznesni qayta yuklaymiz.
                onRetry = when {
                    editListingId != null -> { { vm.loadForEdit(editListingId) } }
                    else -> { { vm.prefillFromBusiness(businessId.orEmpty()) } }
                },
            )
        // Biznes turi hali yuklanmoqda.
        //
        // MUHIM — sahifa DARHOL ochiladi va yuklanish uning ICHIDA ko'rsatiladi: sarlavha va
        // orqaga tugmasi joyida qoladi, faqat kontent maydonida spinner aylanadi. Ilgari bu
        // yerда butun ekranni egallagan yalang'och spinner turardi — navigatsiya animatsiyasi
        // bo'sh ekranga o'tib, keyin birdan to'liq formaga sakrardi va o'tish "qotgan"day
        // ko'rinardi. Orqaga qaytish ham mumkin emas edi.
        type == null -> FormLoading(palette, onBack = onClose)
        else -> TypeListingForm(type, state, palette, vm, onBack = onClose)
    }
}

/** Forma yuklanayotgandagi ko'rinish — sarlavha joyida, spinner kontent maydonida. */
@Composable
private fun FormLoading(palette: AppPalette, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        ScreenTopBar(
            title = stringResource(Res.string.discounts_add_listing),
            onBack = onBack,
            backContentDescription = stringResource(Res.string.common_back),
            modifier = Modifier.screenTopInset(AppSpacing.md).padding(horizontal = AppSpacing.lg),
            palette = palette,
        )
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = palette.primary, strokeWidth = 3.dp)
        }
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
