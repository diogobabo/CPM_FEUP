package com.feup.cpm.ticketbuy.controllers.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.util.*

object QRCodeGenerator {

    fun generateQRCode(content: String, width: Int, height: Int): Bitmap? {
        val hints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"

        try {
            val matrix: BitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints)
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            return bmp
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
