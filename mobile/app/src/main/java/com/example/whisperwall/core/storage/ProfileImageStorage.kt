package com.example.whisperwall.core.storage

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.whisperwall.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.UUID

data class ProfileImageUploadResult(
    val storedValue: String,
    val previewBitmap: Bitmap,
    val storageMode: StorageMode
)

enum class StorageMode {
    SUPABASE,
    BASE64
}

object ProfileImageStorage {
    private const val MAX_SIDE = 800
    private const val MAX_BYTES = 500 * 1024
    private const val BUCKET_NAME = "profile-pictures"

    private val httpClient = OkHttpClient()

    suspend fun prepareProfileImage(
        context: Context,
        uri: Uri,
        username: String
    ): ProfileImageUploadResult = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val decodedBitmap = decodeBitmap(resolver, uri)
            ?: throw IllegalStateException("Unable to read the selected image.")

        val resizedBitmap = resizeBitmap(decodedBitmap)
        val jpegBytes = compressToTarget(resizedBitmap)
        val publicUrl = uploadToSupabase(jpegBytes, username)

        if (!publicUrl.isNullOrBlank()) {
            ProfileImageUploadResult(publicUrl, resizedBitmap, StorageMode.SUPABASE)
        } else {
            val dataUrl = "data:image/jpeg;base64,${Base64.encodeToString(jpegBytes, Base64.NO_WRAP)}"
            ProfileImageUploadResult(dataUrl, resizedBitmap, StorageMode.BASE64)
        }
    }

    private fun decodeBitmap(resolver: ContentResolver, uri: Uri): Bitmap? {
        return resolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    }

    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val maxDimension = maxOf(width, height)

        if (maxDimension <= MAX_SIDE) {
            return bitmap
        }

        val scale = MAX_SIDE.toFloat() / maxDimension.toFloat()
        val targetWidth = (width * scale).toInt().coerceAtLeast(1)
        val targetHeight = (height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    private fun compressToTarget(bitmap: Bitmap): ByteArray {
        var quality = 80
        var output: ByteArray

        do {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            output = stream.toByteArray()
            quality -= 10
        } while (output.size > MAX_BYTES && quality >= 40)

        return output
    }

    private fun uploadToSupabase(imageBytes: ByteArray, username: String): String? {
        val supabaseUrl = BuildConfig.SUPABASE_URL.trim().trimEnd('/')
        val supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY.trim()

        if (supabaseUrl.isBlank() || supabaseAnonKey.isBlank()) {
            return null
        }

        val filePath = "$username/${UUID.randomUUID()}_${System.currentTimeMillis()}.jpg"
        val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaType())
        val request = Request.Builder()
            .url("$supabaseUrl/storage/v1/object/$BUCKET_NAME/$filePath")
            .addHeader("apikey", supabaseAnonKey)
            .addHeader("Authorization", "Bearer $supabaseAnonKey")
            .addHeader("x-upsert", "false")
            .post(requestBody)
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return null
            }

            return "$supabaseUrl/storage/v1/object/public/$BUCKET_NAME/$filePath"
        }
    }
}