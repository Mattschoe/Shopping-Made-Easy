package weberstudio.app.billigsteprodukter.logic

import android.util.Log
import weberstudio.app.billigsteprodukter.ReceiptApp
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.getValue


object Logger {
    private val logFile by lazy { File(ReceiptApp.instance.filesDir, "app_debug.log") }
    private const val MAX_LOG_SIZE = 5 * 1024 * 1024 //5MB

    fun log(tag: String, message: String) {
        try {
            if (logFile.exists() && logFile.length() > MAX_LOG_SIZE) {
                logFile.writeText("") //Clears old log
            }

            val timestamp = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US).format(Date())
            logFile.appendText("$timestamp [$tag] $message\n")
        } catch (e: Exception) {
            Log.e("Logger", "Failed to write log:", e)
        }
    }

    fun getLogFile() = logFile
}

