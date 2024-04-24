package com.FEUP.nfcreader.ui

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.example.nfcreader.R
import com.FEUP.nfcreader.controllers.Controller
import com.FEUP.nfcreader.models.TagInfo
import org.json.JSONException

class QRActivity : AppCompatActivity(), Controller.RequestCallback {
    private lateinit var codeScanner: CodeScanner
    private val REQUEST_CAMERA_PERMISSION = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qractivity)

        val scannerView = findViewById<CodeScannerView>(R.id.codeScannerView)

        codeScanner = CodeScanner(this, scannerView)


        if (checkPermission()) {
            codeScanner.startPreview()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback { result ->
            runOnUiThread {

                val rawScannedData  = result.rawBytes.toString(Charsets.UTF_8)
                Log.d("ScannedInfo", rawScannedData )

                val startIndex = rawScannedData.indexOf("{")
                val endIndex = rawScannedData.lastIndexOf("}") + 1

                if (startIndex != -1 && endIndex != -1) {
                    val jsonScannedData = rawScannedData.substring(startIndex, endIndex)
                    Log.d("JSONScannedData", jsonScannedData)
                    try{
                        val tagInfo = TagInfo.fromJson(jsonScannedData)
                        if (tagInfo != null) {
                            if(tagInfo.tagType == "Ticket") {
                                Controller().sendRequestValidateTicket(tagInfo, this)

                            } else if(tagInfo.tagType == "Cafeteria"){
                                val response = Controller().sendRequestMakeCafeteriaOrder(tagInfo)
                                if (response != null) {
                                    //show response
                                    Toast.makeText(this, "Order Placed", Toast.LENGTH_LONG).show()
                                    Toast.makeText(this, response.toString(), Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(this, "Invalid Order", Toast.LENGTH_LONG).show()
                                }
                            }
                            else {
                                Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: JSONException){
                        Log.e("JSONError", "Error parsing JSON", e)
                    }
                }


            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }
    override fun onSuccess(message: String) {
        runOnUiThread {
            Toast.makeText(this@QRActivity, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onFailure(message: String) {
        runOnUiThread {
            Toast.makeText(this@QRActivity, message, Toast.LENGTH_LONG).show()
        }
    }
}
