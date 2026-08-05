package dev.feature.profile.data.remote

import dev.core.common.Resource
import dev.core.common.error.AppException
import dev.core.common.errorOf
import dev.feature.profile.domain.model.UserProfile
import dev.feature.profile.domain.repository.ProfileExistence
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Zaxira manba qachon ishlaydi.
 *
 * Qoida: local zaxira faqat backend **gapirmaganда** (internet yo'q / vaqt tugadi). Server
 * javob bergan bo'lsa — uning xatosi va matni foydalanuvchigacha yetib borishi shart.
 */
class FallbackProfileRemoteDataSourceTest {

    @Test
    fun `server rad etsa xato uzatiladi — soxta muvaffaqiyat emas`() = runTest {
        val denied = errorOf(
            AppException.PermissionDenied("PHONE_NOT_VERIFIED", "Avval raqamingizni tasdiqlang"),
        )
        val source = FallbackProfileRemoteDataSource(FakeProfileRemote(saveResult = denied))

        val result = source.save(UserProfile(firstName = "Ali"))

        val error = assertIs<Resource.Error>(result)
        assertEquals("Avval raqamingizni tasdiqlang", error.message)
    }

    @Test
    fun `validatsiya xatosi ham uzatiladi`() = runTest {
        val invalid = errorOf(AppException.Validation("Telefon noto'g'ri"))
        val source = FallbackProfileRemoteDataSource(FakeProfileRemote(saveResult = invalid))

        assertIs<Resource.Error>(source.save(UserProfile()))
    }

    @Test
    fun `internet yo'q bo'lsa profil local saqlanadi`() = runTest {
        val offline = errorOf(AppException.NoInternet())
        val source = FallbackProfileRemoteDataSource(FakeProfileRemote(saveResult = offline))

        val result = source.save(UserProfile(firstName = "Ali"))

        assertEquals("Ali", assertIs<Resource.Success<UserProfile>>(result).data.firstName)
    }

    @Test
    fun `rasm serverда rad etilsa xato ko'rsatiladi, data URI emas`() = runTest {
        val tooLarge = errorOf(AppException.Validation("Rasm hajmi 5 MB dan oshmasin"))
        val source = FallbackProfileRemoteDataSource(FakeProfileRemote(avatarResult = tooLarge))

        val error = assertIs<Resource.Error>(source.uploadAvatar(byteArrayOf(1), "a.jpg"))
        assertEquals("Rasm hajmi 5 MB dan oshmasin", error.message)
    }

    @Test
    fun `rasm — internet yo'q bo'lsa data URI ga tushadi`() = runTest {
        val offline = errorOf(AppException.Timeout())
        val source = FallbackProfileRemoteDataSource(FakeProfileRemote(avatarResult = offline))

        val ok = assertIs<Resource.Success<String>>(source.uploadAvatar(byteArrayOf(1), "a.jpg"))
        assertTrue(ok.data.startsWith("data:image/jpeg;base64,"))
    }
}

/** Faqat kerakli javoblarni qaytaradigan soxta masofaviy manba. */
private class FakeProfileRemote(
    private val saveResult: Resource<UserProfile> = Resource.Success(UserProfile()),
    private val avatarResult: Resource<String> = Resource.Success("https://cdn/a.jpg"),
) : ProfileRemoteDataSource {
    override suspend fun fetch(): Resource<UserProfile?> = Resource.Success(null)
    override suspend fun save(profile: UserProfile): Resource<UserProfile> = saveResult
    override suspend fun checkExistence(): ProfileExistence = ProfileExistence.ERROR
    override suspend fun uploadAvatar(bytes: ByteArray, fileName: String): Resource<String> = avatarResult
}
