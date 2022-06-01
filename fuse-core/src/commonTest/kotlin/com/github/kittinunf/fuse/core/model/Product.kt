package com.github.kittinunf.fuse.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val name: String = "", // Product
    val properties: Properties = Properties()
)

@Serializable
data class Properties(
    val id: Id = Id(),
    val name: Name = Name(),
    val price: Price = Price(),
    val tags: Tags = Tags()
)

@Serializable
data class Id(
    val type: String = "", // number
    val description: String = "", // Product identifier
    val required: Boolean = false // true
)

@Serializable
data class Name(
    val type: String = "", // string
    val description: String = "", // Name of the product
    val required: Boolean = false // true
)

@Serializable
data class Price(
    val type: String = "", // number
    val minimum: Int = 0, // 0
    val required: Boolean = false // true
)

@Serializable
data class Tags(
    val items: Items = Items(),
    val type: String = "" // array
)

@Serializable
data class Items(
    val type: String = "" // string
)
