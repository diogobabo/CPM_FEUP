package com.example.nfcreader.ui

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.example.nfcreader.controllers.Controller
import com.example.nfcreader.models.TagInfo

class QRActivity : AppCompatActivity() {
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

                val scannedInfo = result.rawBytes.toString(Charsets.UTF_8)
                val tagInfo = TagInfo.fromJson(scannedInfo)
                if (tagInfo != null) {
                    if(tagInfo.tagType == "Ticket") {
                        val bool = Controller().sendRequestValidateTicket(tagInfo)
                        if (bool) {
                            Toast.makeText(this, "Ticket Validated", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Invalid Ticket", Toast.LENGTH_LONG).show()
                        }
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

    }
