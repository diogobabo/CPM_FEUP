package com.feup.cpm.ticketbuy.controllers

import com.feup.cpm.ticketbuy.controllers.cypher.KeyManager
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import android.content.Context
import androidx.core.content.edit
import com.feup.cpm.ticketbuy.models.*

//Server URL
val serverURL = "192.168.1.87:3000"


object Controller {
    // Initialize database collections
    val performances = mutableListOf<Performance>()
    val tickets = mutableListOf<Ticket>()
    val vouchers = mutableListOf<Voucher>()
    val transactions = mutableListOf<Transaction>()
    val orders = mutableListOf<Order>()
    val items = mutableListOf<Item>()

    private const val CUSTOMER_PREF_KEY = "TicketBuyCustomer"

    private fun getSharedPreferences(context: Context) =
        context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    private fun saveCustomerLocally(context: Context, customer: Customer) {
        val json = JSONObject().apply {
            put("userId", customer.userId)
            put("name", customer.name)
            put("nif", customer.nif)
            put("creditCardType", customer.creditCardType)
            put("creditCardNumber", customer.creditCardNumber)
            put("creditCardValidity", customer.creditCardValidity)
        }
        getSharedPreferences(context).edit {
            putString(CUSTOMER_PREF_KEY, json.toString())
        }
    }

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

    // Function to register a customer
    fun registerCustomer(
        context: Context,
        name: String,
        nif: String,
        creditCardType: String,
        creditCardNumber: String,
        creditCardValidity: String,
    ): Customer? {
        
        // Post to the server
        val url = "$serverURL/register"
        val urlObj = URL(url)

        KeyManager.generateAndStoreKeys()
        val publicKey = KeyManager.getPublicKey()

        val json = JSONObject()
        json.put("name", name)
        json.put("nif", nif)
        json.put("creditCardType", creditCardType)
        json.put("creditCardNumber", creditCardNumber)
        json.put("creditCardValidity", creditCardValidity)
        json.put("publicKey", publicKey)

        try {
            with(urlObj.openConnection() as HttpURLConnection) {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("charset", "utf-8")
                setRequestProperty("Content-Length", json.toString().toByteArray().size.toString())
                outputStream.write(json.toString().toByteArray())
                val response = readStream(inputStream)

                // Parse the response
                val responseJson = JSONObject(response)
                if (responseJson.has("error")) {
                    println("registerCustomer: ${responseJson.getString("error")}")
                    return null
                } else {
                    println("registerCustomer: ${responseJson.getString("userId")}")

                    val customer = Customer(
                        responseJson.getString("userId"),
                        name,
                        nif,
                        creditCardType,
                        creditCardNumber,
                        creditCardValidity
                    )
                    saveCustomerLocally(context, customer)

                    return customer
                }
            }
        } catch (e: Exception) {
            println("registerCustomer: ${e.message}")
            return null
        }
    }

    fun getLocalCustomer(context: Context): Customer? {
        val jsonString = getSharedPreferences(context).getString(CUSTOMER_PREF_KEY, null)
        return jsonString?.let { json ->
            val jsonObject = JSONObject(json)
            Customer(
                jsonObject.getString("userId"),
                jsonObject.getString("name"),
                jsonObject.getString("nif"),
                jsonObject.getString("creditCardType"),
                jsonObject.getString("creditCardNumber"),
                jsonObject.getString("creditCardValidity")
            )
        }
    }

    // Function to get next performances
    fun getNextPerformances(): List<Performance> {
        return emptyList()
    }

    // Function to purchase tickets
    fun purchaseTickets(userId: String, performanceDate: String, numTickets: Int): List<Ticket> {
        return emptyList()
    }

    // Function to validate tickets
    fun validateTickets(ticketIds: List<String>, userId: String): Boolean {
        return false
    }

    // Function to make cafeteria order
    fun makeCafeteriaOrder(userId: String, orderedProducts: List<String>, vouchers: List<String>): Int {
        return 0
    }

    // Function to validate vouchers and pay an order
    fun validateVouchersAndPayOrder(userId: String, orderedProducts: List<String>, vouchers: List<String>): Double {
        return 0.0
    }

    // Function to consult transactions
    fun consultTransactions(userId: String): List<Transaction> {
        return emptyList()
    }
}
