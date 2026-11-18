package com.example.soul_android.ui.screens
import com.example.soul_android.ui.theme.SoulColors

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 图片大屏预览：点击聊天图片后全屏查看，支持双指缩放/双击放大、保存到相册。
 */
@Composable
fun ImageViewerScreen(
    imageUrl: String,
    onClose: () -> Unit
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        AsyncImage(
            model = imageUrl,
            contentDescription = "大图预览",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (scale > 1f) {
                                scale = 1f
                                offset = Offset.Zero
                            } else {
                                scale = 2.5f
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(1f, 5f)
                        scale = newScale
                        offset += pan
                        if (scale <= 1f) {
                            offset = Offset.Zero
                        }
                    }
                },
            contentScale = ContentScale.Fit
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = SoulColors.textPrimary
                )
            }

            IconButton(
                onClick = {
                    scope.launch {
                        val ok = saveImageToGallery(context, imageUrl)
                        Toast.makeText(
                            context,
                            if (ok) "已保存到相册" else "保存失败",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.SaveAlt,
                    contentDescription = "保存",
                    tint = SoulColors.textPrimary
                )
            }
        }
    }
}

private suspend fun saveImageToGallery(context: Context, url: String): Boolean {
    return try {
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false)
            .build()
        val result = context.imageLoader.execute(request)
        val bitmap = (result as? SuccessResult)?.drawable?.toBitmap()
            ?: return false
        withContext(Dispatchers.IO) {
            writeBitmapToGallery(context, bitmap)
        }
    } catch (e: Exception) {
        false
    }
}

private fun writeBitmapToGallery(context: Context, bitmap: Bitmap): Boolean {
    return try {
        val filename = "SOUL_${System.currentTimeMillis()}.jpg"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SOUL")
            }
        }
        val uri: Uri? = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )
        if (uri == null) {
            false
        } else {
            context.contentResolver.openOutputStream(uri)?.use { os ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, os)
            }
            true
        }
    } catch (e: Exception) {
        false
    }
}
