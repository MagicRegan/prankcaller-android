package com.magicregan.prankcaller.data.model

data class Prank(
    val id: Int,
    val name: String,
    val slug: String,
    val shortDescription: String,
    val longDescription: String,
    val previewUrl: String,
    val smallImage: String,
    val largeImage: String,
    val thumbsUp: Int,
    val thumbsDown: Int,
    val callsSent: Int,
    val hasPrankAI: Boolean,
    val isDynamic: Boolean,
    val isNsfw: Boolean,
    val category: PrankCategory = PrankCategory.POPULAR
)

enum class PrankCategory {
    POPULAR,
    RECENT,
    TRENDING
}
