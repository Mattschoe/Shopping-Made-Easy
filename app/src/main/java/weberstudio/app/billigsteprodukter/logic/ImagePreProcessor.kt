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

object ImagePreprocessor {

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
     * Adaptive thresholding via OpenCV
     */
    /*private fun adaptiveThreshold(src: Bitmap, blockSize: Int = 15, cValue: Double = 5.0): Bitmap {
        val mat = Mat()
        Utils.bitmapToMat(src, mat)
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2GRAY)
        Imgproc.adaptiveThreshold(
            mat,
            mat,
            255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY,
            if (blockSize % 2 == 0) blockSize + 1 else blockSize,
            cValue
        )
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_GRAY2RGBA)
        val outBmp = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, outBmp)
        mat.release()
        return outBmp
    } */

    /**
     * Full pipeline: load, preprocess, and wrap in InputImage
     */
    /* fun preprocessForMlKit(context: Context, imageUri: Uri): InputImage {
        // Load
        val original = loadBitmap(context, imageUri)
        // Preprocess
        val gray = toGrayscale(original)
        val blurred = blurBitmap(context, gray, radius = 2f)
        val binarized = adaptiveThreshold(blurred, blockSize = 15, cValue = 5.0)
        // Wrap for ML Kit
        return InputImage.fromBitmap(binarized, 0)
    } */
}
