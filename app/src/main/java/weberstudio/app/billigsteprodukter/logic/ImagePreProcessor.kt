package weberstudio.app.billigsteprodukter.logic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import weberstudio.app.billigsteprodukter.logic.Formatter.normalizeText
import weberstudio.app.billigsteprodukter.logic.Store.Companion.bottomAnchors
import weberstudio.app.billigsteprodukter.logic.Store.Companion.topAnchors
import weberstudio.app.billigsteprodukter.logic.components.FuzzyMatcher
import kotlin.math.max
import kotlin.math.min


//OBS: AI-Generated SLOP, source: https://claude.ai/chat/7c4df045-144f-4a8c-ba12-7ba6d1b7bf56
object ImagePreprocessor {

    private const val TOP_BUFFER_PERCENTAGE = 0.25f
    private const val BOTTOM_BUFFER_PERCENTAGE = 0.15f
    private const val MIN_CROP_HEIGHT_PERCENTAGE = 0.3f // Minimum of image height

    /**
     * Store-specific anchor configurations for cropping
     */


    /**
     * Load a Bitmap from the given URI
     */
    private fun loadBitmap(context: Context, uri: Uri): Bitmap {
        context.contentResolver.openInputStream(uri).use { stream ->
            return BitmapFactory.decodeStream(stream)
                ?: throw IllegalArgumentException("Unable to decode bitmap from URI: $uri")
        }
    }

    /**
     * Convert to grayscale using a ColorMatrix
     */
    private fun toGrayscale(src: Bitmap): Bitmap {
        val gray = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(gray)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        }
        canvas.drawBitmap(src, null, Rect(0, 0, src.width, src.height), paint)
        return gray
    }

    /**
     * Apply Gaussian blur via RenderScript
     */
    private fun blurBitmap(context: Context, src: Bitmap, radius: Float = 2f): Bitmap {
        val rs = RenderScript.create(context)
        val input = Allocation.createFromBitmap(rs, src)
        val output = Allocation.createTyped(rs, input.type)
        val blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs)).apply {
            setRadius(radius.coerceIn(1f, 25f))
            setInput(input)
        }
        blur.forEach(output)
        output.copyTo(src)
        rs.destroy()
        return src
    }

    /**
     * Apply CLAHE on the grayscale channel to enhance local contrast
     */
    private fun applyClaheGray(src: Bitmap): Bitmap {
        val mat = Mat()
        Utils.bitmapToMat(src, mat)

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2GRAY)
        val clahe = Imgproc.createCLAHE(4.0, org.opencv.core.Size(8.0, 8.0))
        clahe.apply(mat, mat)
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_GRAY2RGBA)

        val resultBmp = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, resultBmp)

        mat.release()
        return resultBmp
    }

    /**
     * Full preprocessing pipeline
     */
    private fun preprocessBitmap(context: Context, bitmap: Bitmap): Bitmap {
        val gray = toGrayscale(bitmap)
        val blurred = blurBitmap(context, gray, radius = 2f)
        return applyClaheGray(blurred)
    }

    /**
     * Original method: preprocess for initial OCR pass (full image)
     */
    fun preprocessForMlKit(context: Context, imageUri: Uri): InputImage {
        val original = loadBitmap(context, imageUri)
        val enhanced = preprocessBitmap(context, original)
        return InputImage.fromBitmap(enhanced, 0)
    }

    /**
     * NEW: Two-pass preprocessing with intelligent cropping
     *
     * 1. Preprocesses full image for first OCR pass
     * 2. After OCR, crops to product section based on detected store
     * 3. Returns cropped and preprocessed image for second OCR pass
     */
    fun preprocessAndCropForMlKit(
        context: Context,
        imageUri: Uri,
        firstPassOcrResult: Text,
        detectedStore: Store
    ): CropResult {
        // Load original bitmap
        val original = loadBitmap(context, imageUri)

        // Find anchors in the OCR result
        val config = StoreAnchorConfig(topKeywords = detectedStore.topAnchors, bottomKeywords = detectedStore.bottomAnchors)

        val topAnchor = findTopAnchor(firstPassOcrResult, config)
        val bottomAnchor = findBottomAnchor(firstPassOcrResult, config)

        if (topAnchor == null || bottomAnchor == null) {
            return CropResult.Failed(
                "Could not find anchors. Top: ${topAnchor != null}, Bottom: ${bottomAnchor != null}"
            )
        }

        // Calculate crop bounds
        val cropBounds = calculateCropBounds(
            topAnchor,
            bottomAnchor,
            original.width,
            original.height
        )

        // Validate crop bounds
        if (!isValidCropBounds(cropBounds, original)) {
            return CropResult.Failed("Invalid crop bounds calculated")
        }

        // Crop the original bitmap
        val croppedBitmap = Bitmap.createBitmap(
            original,
            cropBounds.left,
            cropBounds.top,
            cropBounds.width(),
            cropBounds.height()
        )

        // Preprocess the cropped image
        val enhancedCropped = preprocessBitmap(context, croppedBitmap)

        // Determine orientation
        val isFlipped = topAnchor.centerY > bottomAnchor.centerY
        val rotation = if (isFlipped) 180 else 0

        return CropResult.Success(
            inputImage = InputImage.fromBitmap(enhancedCropped, rotation),
            isFlipped = isFlipped,
            originalBounds = cropBounds
        )
    }

    /**
     * Find the topmost anchor (store name/logo text)
     */
    private fun findTopAnchor(text: Text, config: StoreAnchorConfig): Rect? {
        var bestMatch: Pair<Rect, Int>? = null // Rect and match count

        for (block in text.textBlocks) {
            for (line in block.lines) {
                val normalizedText = normalizeText(line.text)
                val words = normalizedText.split(" ")

                var matchCount = 0
                for (word in words) {
                    for (keyword in config.topKeywords) {
                        if (FuzzyMatcher().match(word, listOf(keyword), 0.80f, 0.3f)) {
                            matchCount++
                        }
                    }
                }

                if (matchCount > 0) {
                    val currentBounds = line.boundingBox ?: continue

                    // Keep the match with most keyword matches
                    if (bestMatch == null || matchCount > bestMatch.second) {
                        bestMatch = currentBounds to matchCount
                    }
                }
            }
        }

        return bestMatch?.first
    }

    /**
     * Find the bottom anchor (total/sum text)
     */
    @Throws(IllegalStateException::class)
    private fun findBottomAnchor(text: Text, config: StoreAnchorConfig): Rect? {
        // Try primary keywords first
        var anchor = findAnchorWithKeywords(text, config.bottomKeywords)

        if (anchor == null) {
            throw IllegalStateException("Couldn't find a bottom anchor!")
        }
        return anchor
    }

    private fun findAnchorWithKeywords(text: Text, keywords: List<String>): Rect? {
        val matches = mutableListOf<Pair<Rect, Int>>()

        for (block in text.textBlocks) {
            for (line in block.lines) {
                val normalizedText = normalizeText(line.text)

                var matchCount = 0
                for (keyword in keywords) {
                    if (FuzzyMatcher().match(normalizedText, listOf(keyword), 0.80f, 0.3f)) {
                        matchCount++
                    }
                }

                if (matchCount > 0) {
                    line.boundingBox?.let { bounds ->
                        matches.add(bounds to matchCount)
                    }
                }
            }
        }

        // Return the match with most keyword matches
        return matches.maxByOrNull { it.second }?.first
    }

    /**
     * Calculate crop boundaries with buffer zones and orientation handling
     */
    private fun calculateCropBounds(
        topAnchor: Rect,
        bottomAnchor: Rect,
        imageWidth: Int,
        imageHeight: Int
    ): Rect {
        // Determine actual top and bottom (handle flipped receipts)
        val actualTop = min(topAnchor.centerY, bottomAnchor.centerY)
        val actualBottom = max(topAnchor.centerY, bottomAnchor.centerY)

        val contentHeight = actualBottom - actualTop

        // Use different buffer sizes for top (larger to include logo) and bottom
        val topBufferSize = (contentHeight * TOP_BUFFER_PERCENTAGE).toInt()
        val bottomBufferSize = (contentHeight * BOTTOM_BUFFER_PERCENTAGE).toInt()

        // Calculate vertical bounds with asymmetric buffers
        val cropTop = max(0, actualTop - topBufferSize)
        val cropBottom = min(imageHeight, actualBottom + bottomBufferSize)

        // Use full width (products can span the entire width)
        val cropLeft = 0
        val cropRight = imageWidth

        return Rect(cropLeft, cropTop, cropRight, cropBottom)
    }

    /**
     * Validate that crop bounds are reasonable
     */
    private fun isValidCropBounds(bounds: Rect, bitmap: Bitmap): Boolean {
        // Check bounds are within image
        if (bounds.left < 0 || bounds.top < 0 ||
            bounds.right > bitmap.width || bounds.bottom > bitmap.height) {
            return false
        }

        // Check minimum height
        val cropHeight = bounds.height()
        val minHeight = (bitmap.height * MIN_CROP_HEIGHT_PERCENTAGE).toInt()
        if (cropHeight < minHeight) {
            return false
        }

        // Check crop is not entire image (anchors should reduce size)
        val cropArea = bounds.width() * bounds.height()
        val imageArea = bitmap.width * bitmap.height
        if (cropArea > imageArea * 0.95) {
            return false
        }

        return true
    }

    /**
     * Configuration for store-specific anchor words
     */
    data class StoreAnchorConfig(
        val topKeywords: List<String>,
        val bottomKeywords: List<String>,
    )

    /**
     * Result of the crop and preprocess operation
     */
    sealed class CropResult {
        data class Success(
            val inputImage: InputImage,
            val isFlipped: Boolean,
            val originalBounds: Rect
        ) : CropResult()

        data class Failed(val reason: String) : CropResult()
    }

    // Extension property for Rect
    private val Rect.centerY: Int
        get() = (top + bottom) / 2
}