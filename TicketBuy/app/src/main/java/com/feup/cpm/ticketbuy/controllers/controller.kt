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
val serverURL = "http://192.168.1.87:3000"
//val serverURL = "http://192.168.176.45:3000"



object Controller {
    // Initialize database collections
    val performances = MutableLiveData<List<Performance>>()
    val allTickets = MutableLiveData<List<Ticket>>()
    val vouchers = MutableLiveData<List<Voucher>>()
    val transactions = MutableLiveData<List<Transaction>>()
    val orders = MutableLiveData<List<Order>>()
    val items = MutableLiveData<List<Item>>()
    var userID = String()

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

        /*
        val performance = Performance(0, "performance1", "24/05", 8.0)
        val performanceList = mutableListOf<Performance>()
        performanceList.add(performance)
        performances.postValue(performanceList.toList())
*/

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
        performance: Performance,
        numTickets: Int
    ) {

        // Post to the server
        val url = "$serverURL/purchase-tickets"
        val urlObj = URL(url)

        val json = JSONObject()
        json.put("user_id", userID)
        json.put("performance_id", performance.performanceId)
        json.put("performance_date", performance.date)
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
                    val responseCode = responseCode
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        println("purchaseTickets error: $responseCode")
                        return@launch
                    }

                    val response = readStream(inputStream)

                    // Parse the response
                    val responseJson = JSONObject(response)

                    val ticketsJson = responseJson.getJSONArray("tickets")
                    val ticketsList = mutableListOf<Ticket>()
                    ticketsList.addAll(allTickets.value ?: emptyList())

                    for (i in 0 until ticketsJson.length()) {
                        val ticketJson = ticketsJson.getJSONObject(i)
                        val ticket = Ticket(
                            ticketJson.getString("ticket_id"),
                            performance,
                            ticketJson.getString("user_id"),
                            ticketJson.getString("place_in_room"),
                            false
                        )
                        ticketsList.add(ticket)
                    }
                    allTickets.postValue(ticketsList.toList())
                    println("purchaseTickets: ${allTickets.value}")


                }
            } catch (e: Exception) {
                println("purchaseTickets: ${e.message}")
            }
        }
    }


    // Function to get cafeteria items
    @OptIn(DelicateCoroutinesApi::class)
    fun getCafeteriaItems() {
        // Get from the server
        val url = "$serverURL/items"

        /*
        val item1 = Item(0, "pao", 50, 0.15)
        val item2 = Item(1, "bolo", 20, 2.00)
        val item3 = Item(2, "sumo", 30, 1.20)
        val item4 = Item(3, "7up", 2, 1.20)

        val itemsList = mutableListOf<Item>()
        itemsList.add(item1)
        itemsList.add(item2)
        itemsList.add(item3)
        itemsList.add(item4)
        items.postValue(itemsList.toList())
*/
        val urlObj = URL(url)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                with(urlObj.openConnection() as HttpURLConnection) {
                    requestMethod = "GET"
                    val response = readStream(inputStream)

                    val responseCode = responseCode
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        println("getCafeteriaItems error: $responseCode")
                        return@launch
                    }

                    // Parse the response
                    val itemsJson = JSONArray(response)
                    val itemsList = mutableListOf<Item>()
                    for (i in 0 until itemsJson.length()) {
                        val itemJson = itemsJson.getJSONObject(i)
                        val item = Item(
                            itemJson.getInt("item_id"),
                            itemJson.getString("name"),
                            itemJson.getString("quantity").toInt(),
                            itemJson.getString("price").toDouble()
                        )
                        itemsList.add(item)
                    }
                    items.postValue(itemsList.toList())
                    println("getCafeteriaItems: $items")
                }
            } catch (e: Exception) {
                println("getCafeteriaItems error: $e")
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun getTickets(){

        val url = "$serverURL/tickets"

        val urlObj = URL(url)
        val json = JSONObject()
        json.put("user_id", userID)
        val signature = singData(json.toString().toByteArray())
        json.put("signature", signature)

        GlobalScope.launch(Dispatchers.IO){
            try {
                with(urlObj.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("charset", "utf-8")
                    setRequestProperty("Content-Length", json.toString().toByteArray().size.toString())
                    outputStream.write(json.toString().toByteArray())
                    val response = readStream(inputStream)

                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        println("getTickets error: $responseCode")
                        return@launch
                    }
                    val responseJson = JSONObject(response)
                    val ticketsJson = responseJson.getJSONArray("tickets")
                    val performancesJson = responseJson.getJSONArray("performances")
                    val performancesList = mutableListOf<Performance>()
                    for (i in 0 until performancesJson.length()) {
                        val performanceJson = performancesJson.getJSONObject(i)
                        val performance = Performance(
                            performanceJson.getInt("performance_id"),
                            performanceJson.getString("name"),
                            performanceJson.getString("date"),
                            performanceJson.getDouble("price")
                        )
                        performancesList.add(performance)
                    }
                    val ticketsList = mutableListOf<Ticket>()
                    for (i in 0 until ticketsJson.length()) {
                        val ticketJson = ticketsJson.getJSONObject(i)
                        val performance = performancesList.find { it.performanceId == ticketJson.getInt("performance_id") }!!
                        val ticket = Ticket(
                            ticketJson.getString("ticket_id"),
                            performance,
                            ticketJson.getString("user_id"),
                            ticketJson.getString("place_in_room"),
                            ticketJson.getInt("is_used") == 1
                        )
                        ticketsList.add(ticket)
                    }
                    allTickets.postValue(ticketsList.toList())
                }
            } catch (e: Exception) {
                println("getTickets error: $e")
            }
        }
    }

    // Function to consult transactions
    @OptIn(DelicateCoroutinesApi::class)
    fun consultTransactions() {
        val url = "$serverURL/consult-transactions"

        val urlObj = URL(url)

        val json = JSONObject()
        json.put("user_id", userID)

        val signature = singData(json.toString().toByteArray())
        json.put("signature", signature)

        GlobalScope.launch(Dispatchers.IO){
            try {
                with(urlObj.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("charset", "utf-8")
                    setRequestProperty("Content-Length", json.toString().toByteArray().size.toString())
                    outputStream.write(json.toString().toByteArray())
                    val response = readStream(inputStream)

                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        println("consultTransactions error: $responseCode")
                        return@launch
                    }
                    val responseJson = JSONObject(response)
                    val transactionsJson = responseJson.getJSONArray("transactions")

                   val transactionsList = mutableListOf<Transaction>()
                    for (i in 0 until transactionsJson.length()) {
                        val transactionJson = transactionsJson.getJSONObject(i)
                        val transaction = Transaction(
                            transactionJson.getInt("transaction_id"),
                            transactionJson.getString("user_id"),
                            transactionJson.getString("transaction_type"),
                            transactionJson.getString("transaction_date"),
                            transactionJson.getDouble("transaction_value")
                        )
                        transactionsList.add(transaction)
                    }
                    transactions.postValue(transactionsList.toList())

                    val ordersJson = responseJson.getJSONArray("orders")
                    val ordersList = mutableListOf<Order>()
                    //orders.clear()
                    for (i in 0 until ordersJson.length()) {
                        val orderJson = ordersJson.getJSONObject(i)
                        val order = Order(
                            orderJson.getInt("order_id"),
                            orderJson.getString("user_id"),
                            orderJson.getString("order_date"),
                            emptyList()
                        )
                        ordersList.add(order)
                    }
                    orders.postValue(ordersList.toList())

                    val itemsJson = responseJson.getJSONArray("items")
                    val itemsList = mutableListOf<Item>()
                    for (i in 0 until itemsJson.length()) {
                        val itemJson = itemsJson.getJSONObject(i)
                        val item = Item(
                            itemJson.getInt("item_id"),
                            itemJson.getString("name"),
                            itemJson.getInt("quantity"),
                            itemJson.getDouble("price")
                        )
                        itemsList.add(item)
                    }
                    items.postValue(itemsList.toList())

                    val vouchersJson = responseJson.getJSONArray("vouchers")
                    val vouchersList = mutableListOf<Voucher>()
                    for (i in 0 until vouchersJson.length()) {
                        val voucherJson = vouchersJson.getJSONObject(i)
                        val voucher = Voucher(
                            voucherJson.getString("voucher_id"),
                            voucherJson.getString("user_id"),
                            voucherJson.getString("type_code"),
                            voucherJson.getInt("is_used") == 1
                        )
                        vouchersList.add(voucher)
                    }
                    vouchers.postValue(vouchersList.toList())

                }
            } catch (e: Exception) {
                println("consultTransactions error: $e")
            }
        }
    }
}
