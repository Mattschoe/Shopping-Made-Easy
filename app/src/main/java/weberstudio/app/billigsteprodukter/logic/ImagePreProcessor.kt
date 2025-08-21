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
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

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
     * Apply CLAHE on the grayscale channel to enhance local contrast
     */
    private fun applyClaheGray(src: Bitmap): Bitmap {
        // Convert Bitmap to Mat
        val mat = Mat()
        Utils.bitmapToMat(src, mat)

        // Convert RGBA -> Gray
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2GRAY)

        // Create CLAHE and apply
        val clahe = Imgproc.createCLAHE(4.0, org.opencv.core.Size(8.0, 8.0))
        clahe.apply(mat, mat)

        // Convert back to RGBA for InputImage compatibility
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_GRAY2RGBA)
        val resultBmp = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, resultBmp)

        mat.release()
        return resultBmp
    }

    /**
     * Full pipeline: load, preprocess, and wrap in InputImage
     */
    fun preprocessForMlKit(context: Context, imageUri: Uri): InputImage {
        // Load
        val original = loadBitmap(context, imageUri)
        // Preprocess
        val gray = toGrayscale(original)
        val blurred = blurBitmap(context, gray, radius = 2f)
        val enhanced = applyClaheGray(blurred)
        // Wrap for ML Kit
        return InputImage.fromBitmap(enhanced, 0)
    }
}
