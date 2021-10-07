package com.github.kittinunf.fuse.model

import kotlinx.serialization.Serializable

@Serializable
data class SampleProduct(
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
    val description: String = "", // Product identifier
    val required: Boolean = false, // true
    val type: String = "" // number
)

@Serializable
data class Name(
    val description: String = "", // Name of the product
    val required: Boolean = false, // true
    val type: String = "" // string
)

@Serializable
data class Price(
    val minimum: Int = 0, // 0
    val required: Boolean = false, // true
    val type: String = "" // number
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

