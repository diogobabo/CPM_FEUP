package com.feup.cpm.ticketbuy.controllers

import java.util.UUID

// Define data classes for database entities
data class Customer(
    val userId: String,
    val name: String,
    val nif: String,
    val creditCardType: String,
    val creditCardNumber: String,
    val creditCardValidity: String,
    val publicKey: String
)

data class Performance(
    val performanceId: Int,
    val name: String,
    val date: String,
    val price: Double
)

data class Ticket(
    val ticketId: String,
    val performanceId: Int,
    val userId: String,
    val placeInRoom: String,
    var isUsed: Boolean = false
)

data class Voucher(
    val voucherId: String,
    val userId: String,
    val typeCode: String,
    var isUsed: Boolean = false
)

data class Transaction(
    val transactionId: Int,
    val userId: String,
    val transactionType: String,
    val transactionDate: String,
    val transactionValue: Double
)

object Controller {
    // Initialize database collections
    val performances = mutableListOf<Performance>()
    val tickets = mutableListOf<Ticket>()
    val vouchers = mutableListOf<Voucher>()
    val transactions = mutableListOf<Transaction>()

    // Function to register a customer
    fun registerCustomer(
        name: String,
        nif: String,
        creditCardType: String,
        creditCardNumber: String,
        creditCardValidity: String,
        publicKey: String
    ): String {
        val userId = UUID.randomUUID().toString()
        val customer = Customer(userId, name, nif, creditCardType, creditCardNumber, creditCardValidity, publicKey)
        return userId
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

    // Function to present tickets
    fun presentTickets(ticketIds: List<String>, userId: String): Boolean {
        return false
    }
}
