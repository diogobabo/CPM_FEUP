package com.FEUP.nfcreader.controllers

import com.FEUP.nfcreader.models.TagInfo
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

val serverURL = "http://192.168.1.87:3000"

class Controller {
    private fun readStream(input: InputStream): String {
        var reader: BufferedReader? = null
        var line: String?
        val response = StringBuilder()
        try {
            reader = BufferedReader(InputStreamReader(input))
            while (reader.readLine().also{ line = it } != null)
                response.append(line)
        }
        catch (e: IOException) {
            response.clear()
            response.append("readStream: ${e.message}")
        }
        reader?.close()
        return response.toString()
    }
    @OptIn(DelicateCoroutinesApi::class)
    fun sendRequestValidateTicket(tag: TagInfo){
        // Send request to server
        val url = "$serverURL/validate-tickets"

        val jsonObject = tag.payLoad
        jsonObject.put("user_id",tag.userId)
        GlobalScope.launch {
            try {
                val urlObj = URL(url)
                with(urlObj.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("charset", "utf-8")
                    setRequestProperty(
                        "Content-Length",
                        jsonObject.toString().toByteArray().size.toString()
                    )
                    outputStream.write(jsonObject.toString().toByteArray())
                    val response = readStream(inputStream)

                    val jsonResponse = JSONObject(response)
                }
            } catch (e: Exception) {
                println("sendRequestValidateTicket: ${e}")
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun sendRequestMakeCafeteriaOrder(tag: TagInfo){
        // Send request to server
        val url = "$serverURL/make-cafeteria-order"

        val jsonObject = tag.payLoad
        jsonObject.put("user_id",tag.userId)
        GlobalScope.launch {
            try {
                val urlObj = URL(url)
                with(urlObj.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("charset", "utf-8")
                    setRequestProperty(
                        "Content-Length",
                        jsonObject.toString().toByteArray().size.toString()
                    )
                    outputStream.write(jsonObject.toString().toByteArray())
                    val response = readStream(inputStream)
                    println("sendRequestMakeCafeteriaOrder: $response")
                }
            } catch (e: Exception) {
                println("sendRequestMakeCafeteriaOrder: ${e}")
            }
        }
    }

}