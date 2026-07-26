package dev.core.uikit.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.common_loading
import dev.core.uikit.resources.legal_privacy_title
import dev.core.uikit.resources.legal_terms_title
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppType
import dev.core.uikit.theme.appPalette
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

/** Ilova ichida ko'rsatiladigan huquqiy hujjatlar. */
enum class LegalDocument { TERMS, PRIVACY }

/**
 * Foydalanish shartlari / Maxfiylik siyosatini pastdan chiqadigan oynada ko'rsatadi.
 *
 * Matn brauzerga o'tmasdan ilova ichida o'qiladi: hujjatlar `composeResources/files/legal/`
 * da Markdown ko'rinishida yotadi va shu yerda oddiy formatlash bilan chiziladi. Til joriy
 * lokalga qarab tanlanadi (ru → ruscha, qolganida o'zbekcha).
 *
 * [document] `null` bo'lsa oyna yopiladi; yopilish animatsiyasi davomida oxirgi hujjat
 * ko'rinib turishi uchun u eslab qolinadi.
 */
// `Res.readBytes` hali eksperimental (compose-resources 1.7) — xarita assetlarida ham
// shunday ochib ishlatilgan (`MapAssets.kt`).
@OptIn(ExperimentalResourceApi::class)
@Composable
fun LegalSheet(
    document: LegalDocument?,
    onDismiss: () -> Unit,
    palette: AppPalette = appPalette,
) {
    // Yopilish animatsiyasi paytida sarlavha/matn yo'qolib qolmasligi uchun oxirgi qiymat.
    var shown by remember { mutableStateOf(LegalDocument.TERMS) }
    if (document != null) shown = document

    val language = Locale.current.language
    var text by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(shown, language, document != null) {
        if (document == null) return@LaunchedEffect
        text = null
        text = runCatching { Res.readBytes(resourcePath(shown, language)).decodeToString() }
            .getOrElse { "" }
    }

    val title = when (shown) {
        LegalDocument.TERMS -> stringResource(Res.string.legal_terms_title)
        LegalDocument.PRIVACY -> stringResource(Res.string.legal_privacy_title)
    }

    AppBottomSheet(visible = document != null, onDismiss = onDismiss, title = title, palette = palette) {
        val body = text
        if (body == null) {
            Text(stringResource(Res.string.common_loading), style = AppType.body.copy(color = palette.inkMuted))
        } else {
            MarkdownBlocks(body, palette)
        }
    }
}

/** Til bo'yicha fayl yo'li — ruscha lokalda ruscha nusxa, qolgan hollarda o'zbekcha. */
private fun resourcePath(document: LegalDocument, language: String): String {
    val suffix = if (language.lowercase() == "ru") "ru" else "uz"
    val name = if (document == LegalDocument.TERMS) "terms" else "privacy"
    return "files/legal/${name}_$suffix.md"
}

// ---------------------------------------------------------------------------
// Markdown'ning kichik qismini chizish
//
// To'liq Markdown kutubxonasi olib kelinmadi: hujjatlarda faqat sarlavhalar, ro'yxatlar,
// jadvallar, izoh bloki va `**qalin**` matn ishlatiladi — shularning o'zi yetadi.
// ---------------------------------------------------------------------------

@Composable
private fun MarkdownBlocks(markdown: String, palette: AppPalette) {
    val blocks = remember(markdown) { parseMarkdown(markdown) }
    blocks.forEach { block ->
        when (block) {
            is Block.Heading -> {
                Spacer(Modifier.height(if (block.level >= 3) 8.dp else 14.dp))
                Text(
                    inline(block.text),
                    style = if (block.level >= 3) {
                        AppType.rowTitle.copy(color = palette.ink)
                    } else {
                        AppType.cardTitle.copy(color = palette.ink)
                    },
                )
                Spacer(Modifier.height(4.dp))
            }

            is Block.Paragraph -> Text(
                inline(block.text),
                style = AppType.body.copy(color = palette.inkMuted, lineHeight = AppType.body.lineHeight),
            )

            is Block.Bullet -> Row {
                Text("•", style = AppType.body.copy(color = palette.primary))
                Spacer(Modifier.width(8.dp))
                Text(inline(block.text), style = AppType.body.copy(color = palette.inkMuted))
            }

            is Block.Note -> Text(
                inline(block.text),
                style = AppType.hint.copy(color = palette.inkFaint),
            )
        }
        Spacer(Modifier.height(6.dp))
    }
    Spacer(Modifier.height(AppType.body.lineHeight.value.dp))
}

private sealed interface Block {
    data class Heading(val text: String, val level: Int) : Block
    data class Paragraph(val text: String) : Block
    data class Bullet(val text: String) : Block
    data class Note(val text: String) : Block
}

/**
 * Markdown matnini bloklarga ajratadi. Bir xatboshiga tegishli qatorlar (manba faylda ular
 * qo'lda o'ralgan) bitta matnga birlashtiriladi, aks holda ekranda uzuq-yuluq ko'rinardi.
 */
private fun parseMarkdown(markdown: String): List<Block> {
    val blocks = mutableListOf<Block>()
    val paragraph = StringBuilder()

    fun flush() {
        if (paragraph.isNotBlank()) blocks += Block.Paragraph(paragraph.toString().trim())
        paragraph.clear()
    }

    for (raw in markdown.lines()) {
        val line = raw.trim()
        when {
            line.isEmpty() -> flush()

            // Hujjat sarlavhasi — oynaning o'z sarlavhasi bor, takrorlanmasin.
            line.startsWith("# ") -> flush()

            line.startsWith("---") -> flush()

            line.startsWith("### ") -> {
                flush()
                blocks += Block.Heading(line.removePrefix("### "), level = 3)
            }

            line.startsWith("## ") -> {
                flush()
                blocks += Block.Heading(line.removePrefix("## "), level = 2)
            }

            line.startsWith("> ") -> {
                flush()
                blocks += Block.Note(line.removePrefix("> "))
            }

            line.startsWith("- ") -> {
                flush()
                blocks += Block.Bullet(line.removePrefix("- "))
            }

            // Jadval: ajratuvchi qatorni tashlab, kataklarni bitta qatorga qo'shamiz.
            line.startsWith("|") -> {
                flush()
                val cells = line.trim('|').split('|').map { it.trim() }
                if (cells.none { it.isNotEmpty() && it.all { c -> c == '-' || c == ':' } }) {
                    blocks += Block.Bullet(cells.filter { it.isNotEmpty() }.joinToString(" — "))
                }
            }

            else -> {
                if (paragraph.isNotEmpty()) paragraph.append(' ')
                paragraph.append(line)
            }
        }
    }
    flush()
    return blocks
}

/** `**qalin**` bo'laklarini ajratadi, qolgan belgilarni o'zgarishsiz qoldiradi. */
private fun inline(text: String): AnnotatedString = buildAnnotatedString {
    var rest = text
    while (true) {
        val start = rest.indexOf("**")
        if (start < 0) break
        val end = rest.indexOf("**", start + 2)
        if (end < 0) break
        append(rest.substring(0, start))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(rest.substring(start + 2, end)) }
        rest = rest.substring(end + 2)
    }
    append(rest)
}
