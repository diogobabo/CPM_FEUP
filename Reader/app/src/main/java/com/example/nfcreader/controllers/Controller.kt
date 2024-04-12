package com.example.nfcreader.controllers

import com.example.nfcreader.models.TagInfo
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

val serverURL = "192.168.1.87:3000"

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
    fun sendRequestValidateTicket(tag: TagInfo) : Boolean{
        // Send request to server
        val url = "$serverURL/validate-tickets"

        val jsonObject = tag.payLoad
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
                return jsonResponse.getBoolean("validated")
            }
        } catch (e: Exception) {
            println("sendRequestValidateTicket: ${e.message}")
        }
        return false
    }

    fun sendRequestMakeCafeteriaOrder(tag: TagInfo) : JSONObject?{
        // Send request to server
        val url = "$serverURL/make-cafeteria-order"

        val jsonObject = tag.payLoad
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

                return JSONObject(response)
            }
        } catch (e: Exception) {
            println("sendRequestMakeCafeteriaOrder: ${e.message}")
        }
        return null
    }
}