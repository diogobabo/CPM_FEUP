package com.feup.cpm.ticketbuy.models

data class Customer(
    val userId: String,
    val name: String,
    val nif: String,
    val creditCardType: String,
    val creditCardNumber: String,
    val creditCardValidity: String,
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

data class Item(
    val itemId: Int,
    val name: String,
    val quantity: Int,
    val price: Double
)

data class Order(
    val orderId: Int,
    val userId: String,
    val orderDate: String,
    val items: List<Item>
)
