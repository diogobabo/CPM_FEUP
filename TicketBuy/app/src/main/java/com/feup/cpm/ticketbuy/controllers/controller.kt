package com.feup.cpm.ticketbuy.controllers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.feup.cpm.ticketbuy.controllers.cypher.KeyManager
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import com.feup.cpm.ticketbuy.MainActivity
import com.feup.cpm.ticketbuy.controllers.cypher.KeyManager.singData
import com.feup.cpm.ticketbuy.models.*
import kotlinx.coroutines.DelicateCoroutinesApi
import org.json.JSONArray

//Server URL 10.227.157.133
//val serverURL = "http://192.168.1.87:3000"
val serverURL = "http://192.168.176.45:3000"



object Controller {
    // Initialize database collections
    val performances = MutableLiveData<List<Performance>>()
    val allTickets = MutableLiveData<List<Ticket>>()
    val vouchers = MutableLiveData<List<Voucher>>()
    val transactions = MutableLiveData<List<Transaction>>()
    val orders = MutableLiveData<List<Order>>()
    val items = MutableLiveData<List<Item>>()
    private var userID = String()

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
        val editor = getSharedPreferences(context).edit()
        editor.putString(CUSTOMER_PREF_KEY, json.toString())
        editor.apply()
    }

    private fun readStream(input: InputStream): String {
        var reader: BufferedReader? = null
        var line: String?
        val response = StringBuilder()
        try {
            reader = BufferedReader(InputStreamReader(input))
            while (reader.readLine().also { line = it } != null)
                response.append(line)
        } catch (e: IOException) {
            response.clear()
            response.append("readStream: ${e.message}")
        }
        reader?.close()
        return response.toString()
    }

    // Function to register a customer
    @OptIn(DelicateCoroutinesApi::class)
    fun registerCustomer(
        context: Context,
        name: String,
        nif: String,
        creditCardType: String,
        creditCardNumber: String,
        creditCardValidity: String,
    ) {

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
        GlobalScope.launch(Dispatchers.IO) {
            try {
                with(urlObj.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("charset", "utf-8")
                    setRequestProperty(
                        "Content-Length",
                        json.toString().toByteArray().size.toString()
                    )
                    outputStream.write(json.toString().toByteArray())
                    outputStream.close()

                    val responseCode = responseCode
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        println("registerCustomer: $responseCode")
                        return@launch
                    }
                    val response = readStream(inputStream)
                    // Parse the response
                    val responseJson = JSONObject(response)
                    if (responseJson.has("error")) {
                        println("registerCustomer: ${responseJson.getString("error")}")
                    } else {
                        println("registerCustomer: ${responseJson.getString("user_id")}")

                        val customer = Customer(
                            responseJson.getString("user_id"),
                            name,
                            nif,
                            creditCardType,
                            creditCardNumber,
                            creditCardValidity
                        )
                        saveCustomerLocally(context, customer)
                        userID = responseJson.getString("user_id")

                        // Restart the app
                        restartApp(context)

                    }
                }
            } catch (e: Exception) {
                println("registerCustomer: $e")
            }
        }
    }

    private fun restartApp(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }

    fun getLocalCustomer(context: Context): Customer? {
        val jsonString = getSharedPreferences(context).getString(CUSTOMER_PREF_KEY, null)
        return jsonString?.let { json ->
            val jsonObject = JSONObject(json)
            this.userID = jsonObject.getString("userId")
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
    @OptIn(DelicateCoroutinesApi::class)
    fun getNextPerformances() {
        // Get from the server
        val url = "$serverURL/performances"

        val urlObj = URL(url)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                with(urlObj.openConnection() as HttpURLConnection) {
                    requestMethod = "GET"
                    val response = readStream(inputStream)

                    val responseCode = responseCode
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        println("getNextPerformances error: $responseCode")
                        return@launch
                    }

                    // Parse the response
                    val performancesJson = JSONArray(response)
                    val performanceList = mutableListOf<Performance>()
                    for (i in 0 until performancesJson.length()) {
                        val performanceJson = performancesJson.getJSONObject(i)
                        val performance = Performance(
                            performanceJson.getInt("performance_id"),
                            performanceJson.getString("name"),
                            performanceJson.getString("date"),
                            performanceJson.getString("price").toDouble()
                        )
                        performanceList.add(performance)
                    }
                    performances.postValue(performanceList.toList())
                    println("getNextPerformances: $performances")
                }
            } catch (e: Exception) {
                println("getNextPerformances error: $e")
            }
        }
    }

    // Function to purchase tickets
    @OptIn(DelicateCoroutinesApi::class)
    fun purchaseTickets(
        performanceId: Int,
        performanceDate: String,
        numTickets: Int
    ) {

        // Post to the server
        val url = "$serverURL/purchase-tickets"
        val urlObj = URL(url)

        val json = JSONObject()
        json.put("user_id", userID)
        json.put("performance_id", performanceId)
        json.put("performance_date", performanceDate)
        json.put("number_of_tickets", numTickets)

        val signature = singData(json.toString().toByteArray())
        json.put("signature", signature)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                with(urlObj.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("charset", "utf-8")
                    setRequestProperty(
                        "Content-Length",
                        json.toString().toByteArray().size.toString()
                    )
                    outputStream.write(json.toString().toByteArray())
                    val response = readStream(inputStream)

                    // Parse the response
                    val responseJson = JSONObject(response)
                    if (responseJson.has("error")) {
                        println("purchaseTickets: ${responseJson.getString("error")}")
                    } else {
                        val ticketsJson = responseJson.getJSONArray("tickets")
                        val ticketsList = mutableListOf<Ticket>()
                        for (i in 0 until ticketsJson.length()) {
                            val ticketJson = ticketsJson.getJSONObject(i)
                            val ticket = Ticket(
                                ticketJson.getString("ticket_id"),
                                ticketJson.getString("performance_id").toInt(),
                                ticketJson.getString("user_id"),
                                ticketJson.getString("place_in_room"),
                                false
                            )
                            ticketsList.add(ticket)
                        }
                        allTickets.postValue(ticketsList.toList())
                        println("purchaseTickets: ${allTickets.value}")

                    }
                }
            } catch (e: Exception) {
                println("purchaseTickets: ${e.message}")
            }
        }
    }

    // Function to validate tickets
    fun validateTickets(ticketIds: List<String>, performanceId: Int): Boolean {
        val url = "$serverURL/validate-tickets"

        val urlObj = URL(url)

        val json = JSONObject()
        json.put("user_id", userID)
        json.put("performance_id", performanceId)
        json.put("ticket_ids", ticketIds)

        val signature = singData(json.toString().toByteArray())
        json.put("signature", signature)

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
                    println("validateTickets: ${responseJson.getString("error")}")
                    return false
                } else {
                    return responseJson.getBoolean("validated")
                }
            }
        } catch (e: Exception) {
            println("validateTickets: ${e.message}")
            return false
        }
    }
    /*
    // Function to make cafeteria order
    fun makeCafeteriaOrder(items: List<Item>, vouchers: List<Voucher>): Int {
        val url = "$serverURL/make-cafeteria-order"

        val urlObj = URL(url)

        val json = JSONObject()
        json.put("user_id", userID)
        json.put("items", items)
        json.put("vouchers", vouchers)

        val signature = singData(json.toString().toByteArray())
        json.put("signature", signature)

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
                    println("makeCafeteriaOrder: ${responseJson.getString("error")}")
                    return -1
                } else {
                    if (responseJson.has("order_id")) {
                        val order = Order(
                            responseJson.getInt("order_id"),
                            responseJson.getString("user_id"),
                            responseJson.getString("order_date"),
                            items
                        )
                        orders.add(order)
                    }
                    return responseJson.getInt("order_id")
                }
            }
        } catch (e: Exception) {
            println("makeCafeteriaOrder: ${e.message}")
            return -1
        }
    }

    // Function to validate vouchers and pay an order
    fun validateVouchersAndPayOrder(ordered_products: List<String>, vouchers: List<String>): Double {
        return 0.0
    }

    // Function to consult transactions
    fun consultTransactions(): List<Transaction>? {
        val url = "$serverURL/consult-transactions"

        val urlObj = URL(url)

        val json = JSONObject()
        json.put("user_id", userID)

        val signature = singData(json.toString().toByteArray())
        json.put("signature", signature)

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
                    println("consultTransactions: ${responseJson.getString("error")}")
                    return emptyList()
                } else {
                    val transactionsJson = responseJson.getJSONArray("transactions")

                    transactions.clear()
                    for (i in 0 until transactionsJson.length()) {
                        val transactionJson = transactionsJson.getJSONObject(i)
                        val transaction = Transaction(
                            transactionJson.getInt("transaction_id"),
                            transactionJson.getString("user_id"),
                            transactionJson.getString("transaction_type"),
                            transactionJson.getString("transaction_date"),
                            transactionJson.getDouble("transaction_value")
                        )
                        transactions.add(transaction)
                    }

                    val ordersJson = responseJson.getJSONArray("orders")
                    orders.clear()
                    for (i in 0 until ordersJson.length()) {
                        val orderJson = ordersJson.getJSONObject(i)
                        val order = Order(
                            orderJson.getInt("order_id"),
                            orderJson.getString("user_id"),
                            orderJson.getString("order_date"),
                            emptyList()
                        )
                        orders.add(order)
                    }

                    val itemsJson = responseJson.getJSONArray("items")
                    items.clear()
                    for (i in 0 until itemsJson.length()) {
                        val itemJson = itemsJson.getJSONObject(i)
                        val item = Item(
                            itemJson.getInt("item_id"),
                            itemJson.getString("name"),
                            itemJson.getInt("quantity"),
                            itemJson.getDouble("price")
                        )
                        items.add(item)
                    }

                    val vouchersJson = responseJson.getJSONArray("vouchers")
                    vouchers.clear()
                    for (i in 0 until vouchersJson.length()) {
                        val voucherJson = vouchersJson.getJSONObject(i)
                        val voucher = Voucher(
                            voucherJson.getString("voucher_id"),
                            voucherJson.getString("user_id"),
                            voucherJson.getString("type_code"),
                            voucherJson.getBoolean("is_used")
                        )
                        vouchers.add(voucher)
                    }

                    return transactions
                }
            }
        } catch (e: Exception) {
            println("consultTransactions: ${e.message}")
            return null
        }
    }
*/
}
