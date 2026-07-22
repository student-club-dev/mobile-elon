package dev.feature.profile.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import coil3.compose.AsyncImage
import dev.core.uikit.resources.Res
import dev.core.uikit.resources.profile_avatar_content_description
import dev.core.uikit.theme.AppPalette
import dev.core.uikit.theme.AppRadius
import dev.core.uikit.theme.AppType
import org.jetbrains.compose.resources.stringResource

/**
 * Profil rasmi — uchta holat, shu tartibda:
 * 1. [localPreview] — endigina galereyadan tanlangan rasm (hali yuklanmagan) — darrov ko'rinadi,
 * 2. [avatarUrl] — serverdagi rasm (Coil orqali yuklanadi),
 * 3. ikkalasi ham yo'q — [name] ning bosh harfi.
 */
@Composable
fun ProfileAvatar(
    name: String,
    size: Dp,
    fontSize: TextUnit,
    palette: AppPalette,
    avatarUrl: String? = null,
    localPreview: ImageBitmap? = null,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(size)
            .clip(AppRadius.pill)
            .background(palette.primary.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        val description = stringResource(Res.string.profile_avatar_content_description)
        when {
            localPreview != null -> Image(
                bitmap = localPreview,
                contentDescription = description,
                modifier = Modifier.size(size),
                contentScale = ContentScale.Crop,
            )

            !avatarUrl.isNullOrBlank() -> AsyncImage(
                model = avatarUrl,
                contentDescription = description,
                modifier = Modifier.size(size),
                contentScale = ContentScale.Crop,
            )

            else -> Text(
                name.take(1).uppercase(),
                style = AppType.screenTitle.copy(fontSize = fontSize, color = palette.primary),
            )
        }
    }
}
