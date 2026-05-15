package com.example.halli_sante_digital.model

data class Product(
    val id: String = "",
    val name: String = "",
    val price: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val sellerName: String = "",
    val sellerPhone: String = "",
    val timestamp: Long = System.currentTimeMillis()
)