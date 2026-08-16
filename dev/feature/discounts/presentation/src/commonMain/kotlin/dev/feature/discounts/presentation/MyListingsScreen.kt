package dev.feature.discounts.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.clickable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.core.common.error.AppException
import dev.core.uikit.component.AppBottomSheet
import dev.core.uikit.component.AppIcons
import dev.core.uikit.component.BottomSheetOption
import dev.core.uikit.component.BackButton
import dev.core.uikit.component.BannerTone
import dev.core.uikit.component.ConfirmDialog
import dev.core.uikit.component.EmptyState
import dev.core.uikit.component.ErrorState
import dev.core.uikit.component.CompactPrimaryButton
import dev.core.uikit.component.IconTile
import dev.core.uikit.component.ScreenTopBar
import dev.core.uikit.component.screenTopInset
import dev.core.uikit.component.SoftPill
import dev.core.uikit.component.StatusBanner
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.common_back
import dev.core.uikit.resources.common_delete
import dev.core.uikit.resources.common_edit
import dev.core.uikit.resources.common_retry
import dev.core.uikit.resources.discounts_listing_delete_confirm
import dev.core.uikit.resources.discounts_listing_delete_title
import dev.core.uikit.resources.discounts_action_duplicate
import dev.core.uikit.resources.discounts_action_more
import dev.core.uikit.resources.discounts_action_stats
import dev.core.uikit.resources.discounts_action_withdraw
import dev.core.uikit.resources.discounts_add_discount
import dev.core.uikit.resources.discounts_add_listing
import dev.core.uikit.resources.discounts_empty_discount_message
import dev.core.uikit.resources.discounts_empty_discount_title
import dev.core.uikit.resources.discounts_empty_listing_message
import dev.core.uikit.resources.discounts_empty_listing_title
import dev.core.uikit.resources.discounts_listings_count
import dev.core.uikit.resources.discounts_my_listings_title
import dev.core.uikit.resources.discounts_offline
import dev.core.uikit.resources.discounts_price_sum
import dev.core.uikit.resources.discounts_redeem_title
import dev.core.uikit.resources.discounts_redemptions_amount
import dev.core.uikit.resources.discounts_redemptions_empty
import dev.core.uikit.resources.discounts_redemptions_title
import dev.core.uikit.resources.discounts_stats_conversion
import dev.core.uikit.resources.discounts_stats_favorites
import dev.core.uikit.resources.discounts_stats_redemptions
import dev.core.uikit.resources.discounts_stats_revenue
import dev.core.uikit.resources.discounts_stats_title
import dev.core.uikit.resources.discounts_stats_views
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppSpacing
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import dev.feature.discounts.domain.model.Business
import dev.feature.discounts.domain.model.BusinessStatus
import dev.feature.discounts.domain.model.Listing
import dev.feature.discounts.domain.model.ListingStatus
import dev.feature.discounts.domain.model.formatSum
import dev.feature.discounts.presentation.components.ListingImage
import dev.feature.discounts.presentation.components.MyListingCard
import dev.feature.discounts.presentation.components.RedeemSheet
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Ro'yxat oxiridan shu qadar element qolgaнда keyingi sahifa oldindan yuklanadi. */
private const val LOAD_MORE_THRESHOLD = 3

/** Biznes egasining chegirma e'lonlari — status, to'xtatish, tahrirlash, o'chirish. */
@Composable
fun MyListingsScreen(
    onCreate: () -> Unit,
    onEdit: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    // Umumiy sarlavha ("E'lonlarim"). Biznes ochilganда uning o'rniga biznes sarlavhasi
    // (logo + nom + holat + tahrirlash) chiziladi.
    showHeader: Boolean = true,
    /**
     * Biznesni tahrirlash — sarlavhadagi qalam. `null` bo'lsa tugma umuman chizilmaydi
     * (masalan umumiy e'lonlar ro'yxatida ochilgan biznes yo'q).
     */
    onEditBusiness: (() -> Unit)? = null,
    // `true` — faqat chegirma, `false` — faqat oddiy e'lon, `null` — hammasi.
    filterDiscount: Boolean? = null,
    // Faqat shu biznesning e'lonlari (`null` — barcha e'lonlar).
    businessId: String? = null,
    vm: MyListingsViewModel = koinViewModel(),
    // Kassir oqimi o'z holatiga ega (kod, tekshiruv, tasdiqlash) va ro'yxat holatidan
    // mustaqil — shuning uchun alohida ViewModel.
    redeemVm: RedeemViewModel = koinViewModel(),
) {
    val palette = appPalette
    // Biznes ochilganда e'lonlarni serverdan (`GET /business/{id}/listings`) paginatsiyalab,
    // sarlavha uchun biznesni (`GET /business/{id}`) yuklaydi.
    LaunchedEffect(businessId) { vm.load(businessId) }
    // Ekran qайta ko'ringanда (e'lon qo'shib/tahrirlab qaytganда) ro'yxatni jimgina yangilaydi.
    LifecycleResumeEffect(businessId) {
        vm.onResumed()
        onPauseOrDispose { }
    }
    val state by vm.state.collectAsStateWithLifecycle()
    val online by vm.online.collectAsStateWithLifecycle()
    val redeemState by redeemVm.state.collectAsStateWithLifecycle()
    val listings = state.listings
        .filter { filterDiscount == null || it.isDiscount == filterDiscount }
        .filter { businessId == null || it.businessId == businessId }

    /** "Boshqa amallar" menyusi ochilgan e'lon (`null` — yopiq). */
    var moreFor by remember { mutableStateOf<Listing?>(null) }

    /** O'chirish tasdig'i kutayotgan e'lon — savat bosilishi darhol o'chirmaydi. */
    var toDelete by remember { mutableStateOf<Listing?>(null) }

    // Sheet'lar butun ekranni egallaydi, shuning uchun ular ildizdagi `Box` ichida, ro'yxatdan
    // KEYIN chaqiriladi — aks holda ro'yxat ustidan chiqmaydi (`AddBusinessScreen` bilan bir xil).
    Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize()) {
        if (showHeader) {
            ScreenTopBar(
                title = stringResource(Res.string.discounts_my_listings_title),
                subtitle = stringResource(Res.string.discounts_listings_count, "${listings.size}"),
                onBack = onBack,
                backContentDescription = stringResource(Res.string.common_back),
                modifier = Modifier.screenTopInset().padding(horizontal = AppSpacing.lg),
                palette = palette,
            )
            Spacer(Modifier.height(14.dp))
        }

        // Ochilgan biznes sarlavhasi — logo, nomi va moderatsiya holati.
        // Biznes shell'ida umumiy header yashirin (`showHeader = false`), shu qator uning
        // o'rnini bosadi va orqaga/tahrirlash tugmalarini o'zi ko'taradi.
        if (!showHeader) {
            BusinessHeaderBar(
                business = state.business,
                palette = palette,
                onBack = onBack,
                onEdit = onEditBusiness,
            )
        } else {
            state.business?.let { business ->
                BusinessHeaderBar(business, palette, onBack = null, onEdit = null, topInset = false)
            }
        }

        // Internet yo'q — ustki banner (offline'да e'lonlar local bazadan ko'rsatiladi).
        // Ranglar palitradan: ilgari qattiq `0xFFFEF3C7` fon qorong'i rejimda o'qilmasdi.
        if (!online) {
            StatusBanner(
                text = stringResource(Res.string.discounts_offline),
                tone = BannerTone.WARNING,
                modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = 6.dp),
                palette = palette,
            )
        }

        // Karta amali rad etildi (masalan muddati o'tgan e'lonni yoqishga urinish) — ro'yxat
        // joyida qoladi, sabab shu yerda ko'rinadi va bosilganda yopiladi.
        state.actionMessage?.let { message ->
            StatusBanner(
                text = message,
                tone = BannerTone.DANGER,
                modifier = Modifier
                    .padding(horizontal = AppSpacing.lg, vertical = 6.dp)
                    .clickable(onClick = vm::consumeActionMessage),
                palette = palette,
            )
        }

        when {
            // 1) Yuklanmoqda — spinner.
            state.loading -> LoadingView(palette)
            // 2) Xato — sabab + "Qayta urinish".
            state.error != null -> ErrorView(state.error!!, palette, onRetry = vm::retry)
            // 3) Bo'sh — birinchi e'longa chorlov.
            listings.isEmpty() -> ListingsEmptyState(palette, onCreate, filterDiscount)
            // 4) Kontent.
            else -> {
                val listState = rememberLazyListState()
                // Cheksiz scroll — ro'yxat oxiriga yaqinlashganда keyingi sahifani so'raymiz.
                // Client-side filtr (chegirma/oddiy) qo'llanadi, shuning uchun ko'rinadigan
                // ro'yxat bo'yicha hisoblaymiz; VM `hasNext`/yuklanish holatini o'zi tekshiradi.
                val shouldLoadMore by remember(listings.size) {
                    derivedStateOf {
                        val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        last >= listings.size - LOAD_MORE_THRESHOLD
                    }
                }
                LaunchedEffect(shouldLoadMore, state.hasNext) {
                    if (shouldLoadMore && state.hasNext) vm.loadMore()
                }
                // E'lon qo'shib qaytганда (jimgina qayta yuklash) ro'yxat tepaga suriladi —
                // yangi e'lon "eng yangi birinchi" tartibда 0-o'rinда turadi.
                LaunchedEffect(state.reloadTick) {
                    if (state.reloadTick > 0) listState.animateScrollToItem(0)
                }
                LazyColumn(
                    Modifier.fillMaxWidth().weight(1f),
                    state = listState,
                    contentPadding = PaddingValues(
                        start = AppSpacing.lg,
                        end = AppSpacing.lg,
                        top = AppSpacing.sm,
                        bottom = 110.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    items(listings, key = { it.id }) { listing ->
                        MyListingCard(
                            listing = listing,
                            palette = palette,
                            onEdit = { onEdit(listing.id) },
                            onTogglePaused = { vm.togglePaused(listing) },
                            // O'chirish darrov bajarilmaydi — tasdiq oynasi ochiladi.
                            onDelete = { toDelete = listing },
                            // Qo'shimcha amallar faqat biznes ochilganda mazmunli: ular
                            // serverdagi e'lon ustida ishlaydi, umumiy (local) ro'yxatда esa
                            // e'lon hali serverга yetib bormagan bo'lishi mumkin.
                            onMore = if (businessId == null) null else ({ moreFor = listing }),
                        )
                    }
                    // Keyingi sahifa yuklanayotganда ro'yxat ostida spinner.
                    if (state.loadingMore) {
                        item(key = "loading_more") {
                            Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = palette.primary, strokeWidth = 2.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }

        // "+ E'lon" — o'ng-past burchakda suzuvchi CTA, "Bizneslarim" ekranidagi "+ Biznes"
        // bilan bir xil naqsh. Ilgari u sarlavha ostidagi to'liq kenglikdagi tugma edi va
        // ro'yxatning eng qimmatli joyini — birinchi ekranni — egallab turardi.
        //
        // Modal oynalardan (amallar, statistika, kassir, o'chirish tasdig'i) OLDIN chiziladi,
        // shu sabab ular ochilganда tugma ularning ostida qoladi.
        if (listings.isNotEmpty()) {
            CompactPrimaryButton(
                text = stringResource(
                    if (filterDiscount == true) Res.string.discounts_add_discount
                    else Res.string.discounts_add_listing,
                ),
                onClick = onCreate,
                modifier = Modifier.align(Alignment.BottomEnd)
                    .padding(end = AppSpacing.screenHorizontal, bottom = AppSpacing.screenBottom),
                palette = palette,
            )
        }

        ListingActionsSheet(
            listing = moreFor,
            palette = palette,
            onDismiss = { moreFor = null },
            onStats = { listing -> moreFor = null; vm.openStats(listing) },
            onDuplicate = { listing -> moreFor = null; vm.duplicate(listing) },
            onWithdraw = { listing -> moreFor = null; vm.withdraw(listing) },
            onRedeem = { listing -> moreFor = null; redeemVm.open(listing) },
        )

        ListingStatsSheet(state, palette, onDismiss = vm::closeStats)

        RedeemSheet(
            state = redeemState,
            palette = palette,
            onCode = redeemVm::onCode,
            onBranch = redeemVm::onBranch,
            onAmount = redeemVm::onAmount,
            onVerify = redeemVm::verify,
            onConfirm = redeemVm::confirm,
            onDismiss = redeemVm::close,
        )

        toDelete?.let { listing ->
            ConfirmDialog(
                visible = true,
                title = stringResource(Res.string.discounts_listing_delete_title),
                message = stringResource(Res.string.discounts_listing_delete_confirm, listing.title),
                confirmText = stringResource(Res.string.common_delete),
                onConfirm = { vm.delete(listing); toDelete = null },
                onDismiss = { toDelete = null },
                palette = palette,
            )
        }
    }
}

/**
 * "Boshqa amallar" menyusi — statistika, nusxa olish, tekshiruvdan qaytarish.
 *
 * Har bir amal faqat mazmunli holatda chiqadi: qaytarib olish `PENDING_REVIEW` da (backend
 * boshqasidan `409` qaytaradi), statistika esa hali chop etilmagan qoralamada emas — u yerда
 * hamma son nol bo'lardi va bu ma'lumot emas, shovqin.
 */
@Composable
private fun ListingActionsSheet(
    listing: Listing?,
    palette: AppPalette,
    onDismiss: () -> Unit,
    onStats: (Listing) -> Unit,
    onDuplicate: (Listing) -> Unit,
    onWithdraw: (Listing) -> Unit,
    onRedeem: (Listing) -> Unit,
) {
    AppBottomSheet(
        visible = listing != null,
        onDismiss = onDismiss,
        title = stringResource(Res.string.discounts_action_more),
        palette = palette,
    ) {
        // Sheet yopilish animatsiyasi davomida `listing` `null` bo'lishi mumkin — shunda
        // hech narsa chizilmaydi (bo'sh sheet bir zum ko'rinib ketmasin).
        val target = listing ?: return@AppBottomSheet
        // Kassir oqimi faqat FAOL e'londa: to'xtatilgan yoki muddati o'tgan chegirmani
        // qo'llab bo'lmaydi va server baribir rad etardi.
        if (target.status == ListingStatus.ACTIVE) {
            BottomSheetOption(
                label = stringResource(Res.string.discounts_redeem_title),
                selected = false,
                onClick = { onRedeem(target) },
                icon = AppIcons.ShieldCheck,
                palette = palette,
            )
        }
        if (target.status != ListingStatus.DRAFT) {
            BottomSheetOption(
                label = stringResource(Res.string.discounts_action_stats),
                selected = false,
                onClick = { onStats(target) },
                icon = AppIcons.Star,
                palette = palette,
            )
        }
        BottomSheetOption(
            label = stringResource(Res.string.discounts_action_duplicate),
            selected = false,
            onClick = { onDuplicate(target) },
            icon = AppIcons.FileText,
            palette = palette,
        )
        if (target.status == ListingStatus.PENDING_REVIEW) {
            BottomSheetOption(
                label = stringResource(Res.string.discounts_action_withdraw),
                selected = false,
                onClick = { onWithdraw(target) },
                icon = AppIcons.ArrowLeft,
                palette = palette,
            )
        }
    }
}

/**
 * Statistika oynasi — sonlar (`GET /listings/{id}/stats`) va oxirgi foydalanishlar
 * (`GET /listings/{id}/redemptions`) birga.
 *
 * Foydalanishlar bo'sh bo'lishi **normal** (hali hech kim kelmagan), shuning uchun u yerда
 * xato emas, tushuntiruvchi matn ko'rsatiladi.
 */
@Composable
private fun ListingStatsSheet(
    state: MyListingsUiState,
    palette: AppPalette,
    onDismiss: () -> Unit,
) {
    AppBottomSheet(
        visible = state.statsListing != null,
        onDismiss = onDismiss,
        title = stringResource(Res.string.discounts_stats_title),
        palette = palette,
    ) {
        when {
            state.statsLoading -> Box(
                Modifier.fillMaxWidth().padding(vertical = AppSpacing.xl),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = palette.primary, strokeWidth = 2.5.dp)
            }
            state.statsError != null -> Text(
                state.statsError,
                style = AppType.body.copy(color = palette.danger),
                modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
            )
            state.stats != null -> {
                val stats = state.stats
                StatRow(stringResource(Res.string.discounts_stats_views), "${stats.views}", palette)
                StatRow(stringResource(Res.string.discounts_stats_favorites), "${stats.favorites}", palette)
                StatRow(stringResource(Res.string.discounts_stats_redemptions), "${stats.redemptions}", palette)
                StatRow(
                    stringResource(Res.string.discounts_stats_conversion),
                    "${stats.conversionPercent}%",
                    palette,
                )
                StatRow(
                    stringResource(Res.string.discounts_stats_revenue),
                    stringResource(Res.string.discounts_price_sum, stats.totalRevenue.formatSum()),
                    palette,
                )

                Spacer(Modifier.height(AppSpacing.md))
                Text(
                    stringResource(Res.string.discounts_redemptions_title),
                    style = AppType.label.copy(color = palette.ink),
                    modifier = Modifier.padding(horizontal = AppSpacing.lg),
                )
                if (state.recentRedemptions.isEmpty()) {
                    Text(
                        stringResource(Res.string.discounts_redemptions_empty),
                        style = AppType.caption.copy(color = palette.inkFaint),
                        modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
                    )
                } else {
                    state.recentRedemptions.forEach { redemption ->
                        StatRow(
                            // Ism ham, username ham bo'lmasa umumiy yozuv — bo'sh qator emas.
                            redemption.displayName() ?: stringResource(Res.string.discounts_redemptions_title),
                            redemption.amount
                                ?.let { stringResource(Res.string.discounts_redemptions_amount, it.formatSum()) }
                                .orEmpty(),
                            palette,
                        )
                    }
                }
                Spacer(Modifier.height(AppSpacing.md))
            }
        }
    }
}

/** Statistika oynasidagi bitta qator — chapda nom, o'ngda qiymat. */
@Composable
private fun StatRow(label: String, value: String, palette: AppPalette) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = AppType.body.copy(color = palette.inkMuted), maxLines = 1)
        Text(value, style = AppType.bodyStrong.copy(color = palette.ink), maxLines = 1)
    }
}

/**
 * Ochilgan biznesning sarlavha qatori: orqaga, **logo**, nomi + holati, tahrirlash.
 *
 * Ilgari bu yerда ikki qavatli "BUSINESS CENTER / My listings" yozuvi va o'ng tepada profilga
 * olib boradigan tugma turardi. Ikkalasi ham foydali emas edi: sarlavha qaysi biznes ochilganini
 * aytmasdi (nom pastda alohida qatorда edi), profil tugmasi esa e'lonlarga aloqasiz. Endi
 * sarlavha aynan biznesni ko'rsatadi va o'ngdagi qalam o'sha biznesni tahrirlaydi.
 *
 * MUHIM — bu qator [MyListingsScreen] ning O'ZIDA chiziladi, karkasda emas: statistika/amallar
 * oynalari shu ekranning ildiz `Box` ida turadi va faqat shundagina sarlavha ustidan
 * qoplanadi. Karkasда bo'lganда oyna ochilsa header uning ustiga chiqib qolardi.
 */
@Composable
private fun BusinessHeaderBar(
    business: Business?,
    palette: AppPalette,
    onBack: (() -> Unit)?,
    onEdit: (() -> Unit)?,
    /**
     * Status bar paddingini shu qator olsinmi. Umumiy sarlavha ([ScreenTopBar]) ko'rsatilganda
     * `false`: inset u yerда allaqachon qo'llangan va ikki marta qo'shilsa qator pastga tushib
     * ketardi.
     */
    topInset: Boolean = true,
) {
    Row(
        Modifier.fillMaxWidth()
            .then(if (topInset) Modifier.screenTopInset() else Modifier)
            .padding(horizontal = AppSpacing.lg)
            .padding(bottom = AppSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            BackButton(
                onClick = onBack,
                contentDescription = stringResource(Res.string.common_back),
                palette = palette,
            )
        }

        // Biznes logotipi — yo'q bo'lsa tur ikonkasi. Nom bilan bitta qatorда turadi.
        val logo = business?.logoUrl
        if (logo != null) {
            ListingImage(logo, Modifier.size(BusinessLogoSize).clip(AppRadius.md))
        } else {
            IconTile(
                icon = business?.businessType?.icon ?: AppIcons.Store,
                tint = palette.primary,
                background = palette.accentBg,
                size = BusinessLogoSize,
                iconSize = 22.dp,
                shape = AppRadius.md,
            )
        }

        Text(
            business?.name.orEmpty(),
            modifier = Modifier.weight(1f),
            style = AppType.topBarTitle.copy(color = palette.ink),
            // Uzun nom sarlavhani ikki qatorga bo'lib yubormaydi — kesiladi.
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        business?.status?.let { status ->
            SoftPill(
                status.localizedLabel(),
                accent = status.color(palette),
                backgroundAlpha = 0.14f,
                shape = AppRadius.sm,
                textStyle = AppType.caption.copy(fontWeight = AppType.label.fontWeight),
                contentPadding = PaddingValues(horizontal = AppSpacing.sm, vertical = 3.dp),
            )
        }

        if (onEdit != null) {
            IconTile(
                icon = AppIcons.Pencil,
                contentDescription = stringResource(Res.string.common_edit),
                tint = palette.primary,
                background = palette.accentBg,
                size = 40.dp,
                iconSize = 18.dp,
                shape = AppRadius.sm,
                onClick = onEdit,
            )
        }
    }
}

/** Sarlavhadagi logo o'lchami — orqaga tugmasi bilan bir balandlikda. */
private val BusinessLogoSize = 42.dp

/** Biznes holati rangi — palitradan (qorong'i rejimda ham o'qiladi). */
private fun BusinessStatus.color(palette: AppPalette): Color = when (this) {
    BusinessStatus.APPROVED -> palette.success
    BusinessStatus.PENDING_REVIEW, BusinessStatus.DRAFT -> palette.primary
    BusinessStatus.REJECTED, BusinessStatus.BLOCKED -> palette.danger
    BusinessStatus.ARCHIVED -> palette.inkMuted
}

/** Markazda aylanuvchi yuklash indikatori. */
@Composable
private fun ColumnScope.LoadingView(palette: AppPalette) {
    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = palette.primary, strokeWidth = 3.dp)
    }
}

/** Xato holati — typed sabab matni va "Qayta urinish" tugmasi. */
@Composable
private fun ColumnScope.ErrorView(error: AppException, palette: AppPalette, onRetry: () -> Unit) {
    ErrorState(
        message = error.userMessage,
        modifier = Modifier.weight(1f),
        actionText = stringResource(Res.string.common_retry),
        onAction = onRetry,
        palette = palette,
    )
}

/**
 * Bo'sh holat. Tab bo'yicha farqli matn — Chegirma yoki E'lon.
 * `null` (birlashgan ro'yxat) → umumiy "e'lon" matni; `true` → chegirma, `false` → oddiy.
 */
@Composable
private fun ListingsEmptyState(palette: AppPalette, onCreate: () -> Unit, discount: Boolean?) {
    val isDiscount = discount == true
    EmptyState(
        icon = if (isDiscount) AppIcons.Tag else AppIcons.FileText,
        title = stringResource(
            if (isDiscount) Res.string.discounts_empty_discount_title else Res.string.discounts_empty_listing_title,
        ),
        message = stringResource(
            if (isDiscount) Res.string.discounts_empty_discount_message else Res.string.discounts_empty_listing_message,
        ),
        modifier = Modifier.fillMaxSize(),
        actionText = stringResource(
            if (isDiscount) Res.string.discounts_add_discount else Res.string.discounts_add_listing,
        ),
        // Chapdan "+" — ro'yxat to'lganda ko'rinadigan tugma bilan bir xil.
        actionIcon = AppIcons.Plus,
        onAction = onCreate,
        palette = palette,
    )
}
