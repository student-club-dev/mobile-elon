package dev.feature.discounts.presentation.form

import dev.core.uikit.resources.Res
import dev.core.uikit.resources.discounts_copy_barber_about
import dev.core.uikit.resources.discounts_copy_barber_business
import dev.core.uikit.resources.discounts_copy_barber_business_hint
import dev.core.uikit.resources.discounts_copy_barber_category_hint
import dev.core.uikit.resources.discounts_copy_barber_description_hint
import dev.core.uikit.resources.discounts_copy_barber_images
import dev.core.uikit.resources.discounts_copy_barber_images_hint
import dev.core.uikit.resources.discounts_copy_barber_subtitle
import dev.core.uikit.resources.discounts_copy_barber_subtitle_regular
import dev.core.uikit.resources.discounts_copy_barber_title
import dev.core.uikit.resources.discounts_copy_barber_title_hint
import dev.core.uikit.resources.discounts_copy_barber_title_regular
import dev.core.uikit.resources.discounts_copy_beauty_about
import dev.core.uikit.resources.discounts_copy_beauty_business
import dev.core.uikit.resources.discounts_copy_beauty_business_hint
import dev.core.uikit.resources.discounts_copy_beauty_category_hint
import dev.core.uikit.resources.discounts_copy_beauty_description_hint
import dev.core.uikit.resources.discounts_copy_beauty_images
import dev.core.uikit.resources.discounts_copy_beauty_images_hint
import dev.core.uikit.resources.discounts_copy_beauty_subtitle
import dev.core.uikit.resources.discounts_copy_beauty_subtitle_regular
import dev.core.uikit.resources.discounts_copy_beauty_title
import dev.core.uikit.resources.discounts_copy_beauty_title_hint
import dev.core.uikit.resources.discounts_copy_beauty_title_regular
import dev.core.uikit.resources.discounts_copy_cafe_about
import dev.core.uikit.resources.discounts_copy_cafe_business
import dev.core.uikit.resources.discounts_copy_cafe_business_hint
import dev.core.uikit.resources.discounts_copy_cafe_category_hint
import dev.core.uikit.resources.discounts_copy_cafe_description_hint
import dev.core.uikit.resources.discounts_copy_cafe_images
import dev.core.uikit.resources.discounts_copy_cafe_images_hint
import dev.core.uikit.resources.discounts_copy_cafe_subtitle
import dev.core.uikit.resources.discounts_copy_cafe_subtitle_regular
import dev.core.uikit.resources.discounts_copy_cafe_title
import dev.core.uikit.resources.discounts_copy_cafe_title_hint
import dev.core.uikit.resources.discounts_copy_cafe_title_regular
import dev.core.uikit.resources.discounts_copy_cloth_about
import dev.core.uikit.resources.discounts_copy_cloth_business
import dev.core.uikit.resources.discounts_copy_cloth_business_hint
import dev.core.uikit.resources.discounts_copy_cloth_category_hint
import dev.core.uikit.resources.discounts_copy_cloth_description_hint
import dev.core.uikit.resources.discounts_copy_cloth_images
import dev.core.uikit.resources.discounts_copy_cloth_images_hint
import dev.core.uikit.resources.discounts_copy_cloth_subtitle
import dev.core.uikit.resources.discounts_copy_cloth_subtitle_regular
import dev.core.uikit.resources.discounts_copy_cloth_title
import dev.core.uikit.resources.discounts_copy_cloth_title_hint
import dev.core.uikit.resources.discounts_copy_cloth_title_regular
import dev.core.uikit.resources.discounts_copy_edu_about
import dev.core.uikit.resources.discounts_copy_edu_business
import dev.core.uikit.resources.discounts_copy_edu_business_hint
import dev.core.uikit.resources.discounts_copy_edu_category_hint
import dev.core.uikit.resources.discounts_copy_edu_description_hint
import dev.core.uikit.resources.discounts_copy_edu_images
import dev.core.uikit.resources.discounts_copy_edu_images_hint
import dev.core.uikit.resources.discounts_copy_edu_subtitle
import dev.core.uikit.resources.discounts_copy_edu_subtitle_regular
import dev.core.uikit.resources.discounts_copy_edu_title
import dev.core.uikit.resources.discounts_copy_edu_title_hint
import dev.core.uikit.resources.discounts_copy_edu_title_regular
import dev.core.uikit.resources.discounts_copy_ent_about
import dev.core.uikit.resources.discounts_copy_ent_business
import dev.core.uikit.resources.discounts_copy_ent_business_hint
import dev.core.uikit.resources.discounts_copy_ent_category_hint
import dev.core.uikit.resources.discounts_copy_ent_description_hint
import dev.core.uikit.resources.discounts_copy_ent_images
import dev.core.uikit.resources.discounts_copy_ent_images_hint
import dev.core.uikit.resources.discounts_copy_ent_subtitle
import dev.core.uikit.resources.discounts_copy_ent_subtitle_regular
import dev.core.uikit.resources.discounts_copy_ent_title
import dev.core.uikit.resources.discounts_copy_ent_title_hint
import dev.core.uikit.resources.discounts_copy_ent_title_regular
import dev.core.uikit.resources.discounts_copy_game_about
import dev.core.uikit.resources.discounts_copy_game_business
import dev.core.uikit.resources.discounts_copy_game_business_hint
import dev.core.uikit.resources.discounts_copy_game_category_hint
import dev.core.uikit.resources.discounts_copy_game_description_hint
import dev.core.uikit.resources.discounts_copy_game_images
import dev.core.uikit.resources.discounts_copy_game_images_hint
import dev.core.uikit.resources.discounts_copy_game_subtitle
import dev.core.uikit.resources.discounts_copy_game_subtitle_regular
import dev.core.uikit.resources.discounts_copy_game_title
import dev.core.uikit.resources.discounts_copy_game_title_hint
import dev.core.uikit.resources.discounts_copy_game_title_regular
import dev.feature.discounts.domain.model.BusinessType
import org.jetbrains.compose.resources.StringResource

/**
 * Bitta biznes turining ekrandagi **matnlari**.
 *
 * Nega alohida: kafe "taom", game club "sessiya", o'quv markaz "kurs" sotadi — bir xil
 * "Sarlavha / Narx / Tavsif" yozuvlari ularning hech biriga to'g'ri kelmaydi. Har turning
 * o'z ekrani bor ([TypeForms]), ekranlar esa umumiy bloklardan yig'iladi va shu yerdagi
 * matnlarni oladi. Shunday qilib takrorlanish ham yo'q, yozuvlar ham har xil.
 *
 * Maydonlar `String` emas, [StringResource] — matnlar `strings_discounts.xml` da (uz/ru/en)
 * yashaydi va ekranda `stringResource(copy.titleHint)` bilan olinadi.
 */
data class ListingFormCopy(
    /** Chegirma e'loni sarlavhasi. */
    val screenTitle: StringResource,
    val screenSubtitle: StringResource,
    /** Oddiy (chegirmasiz) e'lon sarlavhasi — `isDiscount = false` bo'lganда. */
    val screenTitleRegular: StringResource,
    val screenSubtitleRegular: StringResource,

    val businessSection: StringResource,
    val businessHint: StringResource,
    val categoryHint: StringResource,

    val imagesSection: StringResource,
    val imagesHint: StringResource,

    val aboutSection: StringResource,
    val titleHint: StringResource,
    val descriptionHint: StringResource,
) {
    companion object {
        fun of(type: BusinessType): ListingFormCopy = when (type) {

            BusinessType.CAFE_RESTAURANT -> ListingFormCopy(
                screenTitle = Res.string.discounts_copy_cafe_title,
                screenSubtitle = Res.string.discounts_copy_cafe_subtitle,
                screenTitleRegular = Res.string.discounts_copy_cafe_title_regular,
                screenSubtitleRegular = Res.string.discounts_copy_cafe_subtitle_regular,
                businessSection = Res.string.discounts_copy_cafe_business,
                businessHint = Res.string.discounts_copy_cafe_business_hint,
                categoryHint = Res.string.discounts_copy_cafe_category_hint,
                imagesSection = Res.string.discounts_copy_cafe_images,
                imagesHint = Res.string.discounts_copy_cafe_images_hint,
                aboutSection = Res.string.discounts_copy_cafe_about,
                titleHint = Res.string.discounts_copy_cafe_title_hint,
                descriptionHint = Res.string.discounts_copy_cafe_description_hint,
            )

            BusinessType.GAME_CLUB -> ListingFormCopy(
                screenTitle = Res.string.discounts_copy_game_title,
                screenSubtitle = Res.string.discounts_copy_game_subtitle,
                screenTitleRegular = Res.string.discounts_copy_game_title_regular,
                screenSubtitleRegular = Res.string.discounts_copy_game_subtitle_regular,
                businessSection = Res.string.discounts_copy_game_business,
                businessHint = Res.string.discounts_copy_game_business_hint,
                categoryHint = Res.string.discounts_copy_game_category_hint,
                imagesSection = Res.string.discounts_copy_game_images,
                imagesHint = Res.string.discounts_copy_game_images_hint,
                aboutSection = Res.string.discounts_copy_game_about,
                titleHint = Res.string.discounts_copy_game_title_hint,
                descriptionHint = Res.string.discounts_copy_game_description_hint,
            )

            BusinessType.CLOTHING -> ListingFormCopy(
                screenTitle = Res.string.discounts_copy_cloth_title,
                screenSubtitle = Res.string.discounts_copy_cloth_subtitle,
                screenTitleRegular = Res.string.discounts_copy_cloth_title_regular,
                screenSubtitleRegular = Res.string.discounts_copy_cloth_subtitle_regular,
                businessSection = Res.string.discounts_copy_cloth_business,
                businessHint = Res.string.discounts_copy_cloth_business_hint,
                categoryHint = Res.string.discounts_copy_cloth_category_hint,
                imagesSection = Res.string.discounts_copy_cloth_images,
                imagesHint = Res.string.discounts_copy_cloth_images_hint,
                aboutSection = Res.string.discounts_copy_cloth_about,
                titleHint = Res.string.discounts_copy_cloth_title_hint,
                descriptionHint = Res.string.discounts_copy_cloth_description_hint,
            )

            BusinessType.EDUCATION_CENTER -> ListingFormCopy(
                screenTitle = Res.string.discounts_copy_edu_title,
                screenSubtitle = Res.string.discounts_copy_edu_subtitle,
                screenTitleRegular = Res.string.discounts_copy_edu_title_regular,
                screenSubtitleRegular = Res.string.discounts_copy_edu_subtitle_regular,
                businessSection = Res.string.discounts_copy_edu_business,
                businessHint = Res.string.discounts_copy_edu_business_hint,
                categoryHint = Res.string.discounts_copy_edu_category_hint,
                imagesSection = Res.string.discounts_copy_edu_images,
                imagesHint = Res.string.discounts_copy_edu_images_hint,
                aboutSection = Res.string.discounts_copy_edu_about,
                titleHint = Res.string.discounts_copy_edu_title_hint,
                descriptionHint = Res.string.discounts_copy_edu_description_hint,
            )

            BusinessType.ENTERTAINMENT -> ListingFormCopy(
                screenTitle = Res.string.discounts_copy_ent_title,
                screenSubtitle = Res.string.discounts_copy_ent_subtitle,
                screenTitleRegular = Res.string.discounts_copy_ent_title_regular,
                screenSubtitleRegular = Res.string.discounts_copy_ent_subtitle_regular,
                businessSection = Res.string.discounts_copy_ent_business,
                businessHint = Res.string.discounts_copy_ent_business_hint,
                categoryHint = Res.string.discounts_copy_ent_category_hint,
                imagesSection = Res.string.discounts_copy_ent_images,
                imagesHint = Res.string.discounts_copy_ent_images_hint,
                aboutSection = Res.string.discounts_copy_ent_about,
                titleHint = Res.string.discounts_copy_ent_title_hint,
                descriptionHint = Res.string.discounts_copy_ent_description_hint,
            )

            BusinessType.BARBERSHOP -> ListingFormCopy(
                screenTitle = Res.string.discounts_copy_barber_title,
                screenSubtitle = Res.string.discounts_copy_barber_subtitle,
                screenTitleRegular = Res.string.discounts_copy_barber_title_regular,
                screenSubtitleRegular = Res.string.discounts_copy_barber_subtitle_regular,
                businessSection = Res.string.discounts_copy_barber_business,
                businessHint = Res.string.discounts_copy_barber_business_hint,
                categoryHint = Res.string.discounts_copy_barber_category_hint,
                imagesSection = Res.string.discounts_copy_barber_images,
                imagesHint = Res.string.discounts_copy_barber_images_hint,
                aboutSection = Res.string.discounts_copy_barber_about,
                titleHint = Res.string.discounts_copy_barber_title_hint,
                descriptionHint = Res.string.discounts_copy_barber_description_hint,
            )

            BusinessType.BEAUTY_SALON -> ListingFormCopy(
                screenTitle = Res.string.discounts_copy_beauty_title,
                screenSubtitle = Res.string.discounts_copy_beauty_subtitle,
                screenTitleRegular = Res.string.discounts_copy_beauty_title_regular,
                screenSubtitleRegular = Res.string.discounts_copy_beauty_subtitle_regular,
                businessSection = Res.string.discounts_copy_beauty_business,
                businessHint = Res.string.discounts_copy_beauty_business_hint,
                categoryHint = Res.string.discounts_copy_beauty_category_hint,
                imagesSection = Res.string.discounts_copy_beauty_images,
                imagesHint = Res.string.discounts_copy_beauty_images_hint,
                aboutSection = Res.string.discounts_copy_beauty_about,
                titleHint = Res.string.discounts_copy_beauty_title_hint,
                descriptionHint = Res.string.discounts_copy_beauty_description_hint,
            )
        }
    }
}
