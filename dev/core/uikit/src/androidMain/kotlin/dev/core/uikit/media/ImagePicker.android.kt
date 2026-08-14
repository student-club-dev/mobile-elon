package dev.core.uikit.media

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun rememberImagePicker(onResult: (PickedImage?) -> Unit): ImagePicker {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) {
            onResult(null) // bekor qilindi
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val picked = withContext(Dispatchers.IO) { context.readForUpload(uri) }
            onResult(picked)
        }
    }

    return remember(launcher) {
        ImagePicker {
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        }
    }
}

@Composable
actual fun rememberMultiImagePicker(
    maxItems: Int,
    onResult: (List<PickedImage>) -> Unit,
): ImagePicker {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // `PickMultipleVisualMedia` kamida 2 ta talab qiladi va 1 berilsa `IllegalArgumentException`
    // tashlaydi. Bo'sh joy bittagina qolganda ham tanlagich ochilishi kerak, shuning uchun
    // chegara pastdan qisiladi; ortiqchasini ViewModel baribir qabul qilmaydi.
    val limit = maxItems.coerceAtLeast(2)
    // Kontrakt `remember` da — busiz u har rekompozitsiyada yangi obyekt bo'lib, launcher
    // qayta ro'yxatdan o'tar va tanlagich javobi yo'qolishi mumkin edi.
    val contract = remember(limit) { ActivityResultContracts.PickMultipleVisualMedia(limit) }

    val launcher = rememberLauncherForActivityResult(contract) { uris ->
        if (uris.isEmpty()) {
            onResult(emptyList()) // bekor qilindi
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            // Fayllarni o'qish va siqish — IO oqimida: 5 ta kamera rasmi 40 MB gacha
            // bo'lishi mumkin va bu asosiy oqimni qotirib qo'yardi.
            val picked = withContext(Dispatchers.IO) { uris.mapNotNull { context.readForUpload(it) } }
            onResult(picked)
        }
    }

    return remember(launcher) {
        ImagePicker {
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        }
    }
}

/**
 * URI'ni o'qib, yuklashga tayyorlaydi. `null` — faylni ochib bo'lmadi (foydalanuvchi ruxsatni
 * bekor qilgan yoki fayl o'chirilgan).
 *
 * Yuklashdan oldin kichiklashtirib siqamiz: kamera rasmi odatda 3-8 MB, backend chegarasi esa
 * 5 MB (`POST /v1/media/upload`).
 */
private fun Context.readForUpload(uri: Uri): PickedImage? = runCatching {
    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@runCatching null
    val ext = if (contentResolver.getType(uri) == "image/png") "png" else "jpg"
    prepareImageForUpload(bytes, "image.$ext")
}.getOrNull()

actual fun ByteArray.toImageBitmapOrNull(): ImageBitmap? =
    BitmapFactory.decodeByteArray(this, 0, size)?.asImageBitmap()
