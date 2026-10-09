package com.example.telemetry

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class JunkCategory {
    CACHE,
    TEMPORARY,
    DUPLICATE_IMAGE
}

data class JunkItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val category: JunkCategory,
    val lastModified: Long = System.currentTimeMillis(),
    val isDuplicateCopy: Boolean = false,
    val duplicateGroupId: String? = null,
    val originalPath: String? = null,
    val isSelected: Boolean = true,
    val fileRef: File? = null
) {
    val formattedSize: String
        get() = formatBytes(sizeBytes)

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(lastModified))
        }

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
            val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
            return String.format(Locale.US, "%.1f %s", value, units[digitGroups])
        }
    }
}

data class JunkScanState(
    val isScanning: Boolean = false,
    val isCleaning: Boolean = false,
    val scanProgress: Float = 0f,
    val currentScannedPath: String = "",
    val items: List<JunkItem> = emptyList(),
    val selectedCategory: JunkCategory? = null,
    val lastCleanedBytes: Long = 0L,
    val lastCleanedTime: Long = 0L,
    val lastScanCompleted: Boolean = false
) {
    val totalJunkBytes: Long
        get() = items.sumOf { it.sizeBytes }

    val selectedJunkBytes: Long
        get() = items.filter { it.isSelected }.sumOf { it.sizeBytes }

    val selectedCount: Int
        get() = items.count { it.isSelected }

    val cacheBytes: Long
        get() = items.filter { it.category == JunkCategory.CACHE }.sumOf { it.sizeBytes }

    val tempBytes: Long
        get() = items.filter { it.category == JunkCategory.TEMPORARY }.sumOf { it.sizeBytes }

    val duplicateBytes: Long
        get() = items.filter { it.category == JunkCategory.DUPLICATE_IMAGE && it.isDuplicateCopy }.sumOf { it.sizeBytes }
}

class JunkCleanerManager(private val context: Context) {

    private val TAG = "JunkCleanerManager"

    private val _state = MutableStateFlow(JunkScanState())
    val state: StateFlow<JunkScanState> = _state.asStateFlow()

    init {
        // Prepare demo test junk in cache if the application environment is fresh
        seedDemoJunkIfEmpty()
    }

    /**
     * Creates realistic cache, temp, and duplicate image files in the application's cache directory
     * so user scans always have realistic, cleanable junk files immediately.
     */
    fun seedDemoJunkIfEmpty() {
        try {
            val junkDir = File(context.cacheDir, "andro_junk_staging")
            if (!junkDir.exists()) {
                junkDir.mkdirs()
            }

            val existingFiles = junkDir.listFiles()
            if (existingFiles.isNullOrEmpty() || existingFiles.size < 4) {
                // 1. Temporary log files and backups
                File(junkDir, "system_crash_dump_091026.log").writeBytes(ByteArray(1024 * 380) { (it % 90).toByte() })
                File(junkDir, "network_sync_trace.tmp").writeBytes(ByteArray(1024 * 720) { (it % 110).toByte() })
                File(junkDir, "analytics_batch_report.bak").writeBytes(ByteArray(1024 * 250) { (it % 70).toByte() })
                File(junkDir, "staging_update_chunk_1.temp").writeBytes(ByteArray(1024 * 1024 * 2) { (it % 50).toByte() })

                // 2. Application cache files
                val cacheSubdir = File(junkDir, "thumbnails_cache").apply { mkdirs() }
                File(cacheSubdir, "thumb_preview_hdr_449.cache").writeBytes(ByteArray(1024 * 512) { (it % 120).toByte() })
                File(cacheSubdir, "okhttp_response_body_58.bin").writeBytes(ByteArray(1024 * 840) { (it % 80).toByte() })
                File(cacheSubdir, "webview_dom_cache_02.tmp").writeBytes(ByteArray(1024 * 1024 * 3) { (it % 60).toByte() })

                // 3. Duplicate images (Create realistic sample bitmap images)
                val imagesSubdir = File(junkDir, "scanned_media").apply { mkdirs() }
                createDuplicateImagePair(
                    dir = imagesSubdir,
                    baseName = "cyber_photo_hdr_01.jpg",
                    copyName = "cyber_photo_hdr_01_copia.jpg",
                    color = AndroidColor.CYAN,
                    label = "Cyber Neon 1"
                )
                createDuplicateImagePair(
                    dir = imagesSubdir,
                    baseName = "screenshot_system_status.png",
                    copyName = "screenshot_system_status(1).png",
                    color = AndroidColor.MAGENTA,
                    label = "System Screenshot"
                )
                createDuplicateImagePair(
                    dir = imagesSubdir,
                    baseName = "telemetry_chart_export.jpg",
                    copyName = "telemetry_chart_export_dup.jpg",
                    color = AndroidColor.GREEN,
                    label = "Telemetry Chart"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding demo junk: ${e.message}")
        }
    }

    private fun createDuplicateImagePair(dir: File, baseName: String, copyName: String, color: Int, label: String) {
        try {
            val bitmap = Bitmap.createBitmap(320, 240, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(color)
            val paint = Paint().apply {
                this.color = AndroidColor.WHITE
                this.textSize = 28f
                this.isAntiAlias = true
            }
            canvas.drawText(label, 30f, 130f, paint)

            val originalFile = File(dir, baseName)
            FileOutputStream(originalFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            // Create identical byte-for-byte copy
            val copyFile = File(dir, copyName)
            originalFile.copyTo(copyFile, overwrite = true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create duplicate pair: ${e.message}")
        }
    }

    suspend fun scanJunk(): JunkScanState = withContext(Dispatchers.IO) {
        if (_state.value.isScanning) return@withContext _state.value

        _state.value = _state.value.copy(
            isScanning = true,
            scanProgress = 0.05f,
            currentScannedPath = "Iniciando escaneo de directorios..."
        )

        val scannedItems = mutableListOf<JunkItem>()
        val imageFiles = mutableListOf<File>()

        // 1. Scan app internal cache
        delay(150)
        _state.value = _state.value.copy(scanProgress = 0.20f, currentScannedPath = context.cacheDir.path)
        scanDirectory(context.cacheDir, scannedItems, imageFiles)

        // 2. Scan code cache & external cache
        delay(150)
        _state.value = _state.value.copy(scanProgress = 0.40f, currentScannedPath = context.codeCacheDir.path)
        scanDirectory(context.codeCacheDir, scannedItems, imageFiles)

        context.externalCacheDir?.let { extCache ->
            delay(150)
            _state.value = _state.value.copy(scanProgress = 0.55f, currentScannedPath = extCache.path)
            scanDirectory(extCache, scannedItems, imageFiles)
        }

        // 3. Scan external files dir
        context.getExternalFilesDir(null)?.let { extFiles ->
            delay(150)
            _state.value = _state.value.copy(scanProgress = 0.70f, currentScannedPath = extFiles.path)
            scanDirectory(extFiles, scannedItems, imageFiles)
        }

        // 4. Duplicate Image Analysis
        _state.value = _state.value.copy(scanProgress = 0.85f, currentScannedPath = "Analizando firmas hash de imágenes...")
        delay(200)

        val duplicateItems = identifyDuplicateImages(imageFiles)
        scannedItems.addAll(duplicateItems)

        // Filter out duplicate path references if any
        val distinctItems = scannedItems.distinctBy { it.path }

        _state.value = _state.value.copy(
            isScanning = false,
            scanProgress = 1.0f,
            currentScannedPath = "Escaneo finalizado con éxito",
            items = distinctItems,
            lastScanCompleted = true
        )

        _state.value
    }

    private fun scanDirectory(dir: File, outItems: MutableList<JunkItem>, outImages: MutableList<File>) {
        if (!dir.exists() || !dir.isDirectory) return

        val files = dir.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                // Recursive descent (limit depth by avoiding huge system trees)
                scanDirectory(file, outItems, outImages)
            } else {
                val name = file.name.lowercase(Locale.ROOT)
                val size = file.length()
                if (size <= 0) continue

                // Check for image files
                if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") ||
                    name.endsWith(".webp") || name.endsWith(".bmp")
                ) {
                    outImages.add(file)
                    continue
                }

                // Check for Cache files
                if (name.endsWith(".cache") || name.endsWith(".bin") || dir.path.contains("cache") ||
                    name.startsWith("cache_") || name.endsWith(".dex")
                ) {
                    outItems.add(
                        JunkItem(
                            name = file.name,
                            path = file.absolutePath,
                            sizeBytes = size,
                            category = JunkCategory.CACHE,
                            lastModified = file.lastModified(),
                            isSelected = true,
                            fileRef = file
                        )
                    )
                    continue
                }

                // Check for Temporary files
                if (name.endsWith(".tmp") || name.endsWith(".temp") || name.endsWith(".bak") ||
                    name.endsWith(".log") || name.endsWith(".old") || name.endsWith(".part") ||
                    name.startsWith("temp_")
                ) {
                    outItems.add(
                        JunkItem(
                            name = file.name,
                            path = file.absolutePath,
                            sizeBytes = size,
                            category = JunkCategory.TEMPORARY,
                            lastModified = file.lastModified(),
                            isSelected = true,
                            fileRef = file
                        )
                    )
                }
            }
        }
    }

    private fun identifyDuplicateImages(imageFiles: List<File>): List<JunkItem> {
        val results = mutableListOf<JunkItem>()
        // Group by size first for fast filtering
        val bySize = imageFiles.groupBy { it.length() }

        for ((size, filesWithSize) in bySize) {
            if (filesWithSize.size <= 1) continue

            // Compute MD5 hash for candidate duplicates
            val byHash = mutableMapOf<String, MutableList<File>>()
            for (file in filesWithSize) {
                try {
                    val hash = computeFileHash(file)
                    byHash.getOrPut(hash) { mutableListOf() }.add(file)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to hash ${file.name}: ${e.message}")
                }
            }

            for ((hash, filesWithHash) in byHash) {
                if (filesWithHash.size > 1) {
                    val groupId = UUID.randomUUID().toString()
                    val original = filesWithHash.first()

                    // Add original (unselected by default to protect user data)
                    results.add(
                        JunkItem(
                            name = original.name,
                            path = original.absolutePath,
                            sizeBytes = original.length(),
                            category = JunkCategory.DUPLICATE_IMAGE,
                            lastModified = original.lastModified(),
                            isDuplicateCopy = false,
                            duplicateGroupId = groupId,
                            originalPath = null,
                            isSelected = false,
                            fileRef = original
                        )
                    )

                    // Add duplicate copies (selected by default for clean-up)
                    for (i in 1 until filesWithHash.size) {
                        val dup = filesWithHash[i]
                        results.add(
                            JunkItem(
                                name = dup.name,
                                path = dup.absolutePath,
                                sizeBytes = dup.length(),
                                category = JunkCategory.DUPLICATE_IMAGE,
                                lastModified = dup.lastModified(),
                                isDuplicateCopy = true,
                                duplicateGroupId = groupId,
                                originalPath = original.absolutePath,
                                isSelected = true,
                                fileRef = dup
                            )
                        )
                    }
                }
            }
        }
        return results
    }

    private fun computeFileHash(file: File): String {
        val digest = MessageDigest.getInstance("MD5")
        file.inputStream().use { stream ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (stream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun toggleItemSelection(itemId: String) {
        val updated = _state.value.items.map { item ->
            if (item.id == itemId) item.copy(isSelected = !item.isSelected) else item
        }
        _state.value = _state.value.copy(items = updated)
    }

    fun selectAll(select: Boolean, category: JunkCategory? = null) {
        val updated = _state.value.items.map { item ->
            if (category == null || item.category == category) {
                // If it's an original image, don't automatically select when selecting all, to protect the master copy
                if (item.category == JunkCategory.DUPLICATE_IMAGE && !item.isDuplicateCopy) {
                    item.copy(isSelected = false)
                } else {
                    item.copy(isSelected = select)
                }
            } else {
                item
            }
        }
        _state.value = _state.value.copy(items = updated)
    }

    fun filterByCategory(category: JunkCategory?) {
        _state.value = _state.value.copy(selectedCategory = category)
    }

    suspend fun cleanSelectedJunk(): Long = withContext(Dispatchers.IO) {
        if (_state.value.isCleaning) return@withContext 0L

        _state.value = _state.value.copy(isCleaning = true)
        delay(400) // Brief animation

        var freedBytes = 0L
        val remainingItems = mutableListOf<JunkItem>()

        for (item in _state.value.items) {
            if (item.isSelected) {
                try {
                    val file = item.fileRef ?: File(item.path)
                    if (file.exists()) {
                        val length = file.length()
                        if (file.delete()) {
                            freedBytes += length
                        } else {
                            // If delete failed, keep in list
                            remainingItems.add(item)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error deleting ${item.name}: ${e.message}")
                    remainingItems.add(item)
                }
            } else {
                remainingItems.add(item)
            }
        }

        _state.value = _state.value.copy(
            isCleaning = false,
            items = remainingItems,
            lastCleanedBytes = freedBytes,
            lastCleanedTime = System.currentTimeMillis()
        )

        freedBytes
    }
}
