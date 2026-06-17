package com.mart.distribution.demo.ui.shopkeeper

/**
 * FMCG shelf taxonomy — values must match backend Prisma enum `ProductShelf`.
 */
object FmcgShelfCatalog {
    val ids: List<String> =
        listOf(
            "STAPLES",
            "OILS_GHEE",
            "SUGAR_SALT_BASICS",
            "BEVERAGES",
            "SNACKS_BISCUITS",
            "HOME_CARE",
            "PERSONAL_CARE",
            "MILK_DRINKS",
            "DETERGENTS",
            "FEMININE_BABY_CARE",
            "SHAMPOOS",
            "GENERAL_CARE",
            "ATTA",
            "BISCUITS_COOKIES",
            "NOODLES",
            "CHOCOLATES",
            "AGARBATTIS",
            "SOAPS",
            "STATIONERY",
            "COFFEE",
            "SAUCE",
            "EDIBLE_OIL",
            "NON_EDIBLE_OIL",
        )

    private val labels: Map<String, String> =
        mapOf(
            "STAPLES" to "Staples",
            "OILS_GHEE" to "Oils & ghee",
            "SUGAR_SALT_BASICS" to "Sugar, salt & basics",
            "BEVERAGES" to "Beverages",
            "SNACKS_BISCUITS" to "Snacks & biscuits",
            "HOME_CARE" to "Home care",
            "PERSONAL_CARE" to "Personal care",
            "MILK_DRINKS" to "Milk drinks",
            "DETERGENTS" to "Detergents",
            "FEMININE_BABY_CARE" to "Feminine & baby care",
            "SHAMPOOS" to "Shampoos",
            "GENERAL_CARE" to "General care",
            "ATTA" to "Atta",
            "BISCUITS_COOKIES" to "Biscuits & cookies",
            "NOODLES" to "Noodles",
            "CHOCOLATES" to "Chocolates",
            "AGARBATTIS" to "Agarbattis",
            "SOAPS" to "Soaps",
            "STATIONERY" to "Stationery",
            "COFFEE" to "Coffee",
            "SAUCE" to "Sauce",
            "EDIBLE_OIL" to "Edible oil",
            "NON_EDIBLE_OIL" to "Non edible oil",
        )

    fun label(shelfId: String?): String =
        labels[shelfId?.uppercase()] ?: (shelfId ?: "Staples")

    private val emojis: Map<String, String> =
        mapOf(
            "STAPLES" to "📦",
            "OILS_GHEE" to "🫙",
            "SUGAR_SALT_BASICS" to "🧂",
            "BEVERAGES" to "🥤",
            "SNACKS_BISCUITS" to "🍿",
            "HOME_CARE" to "🧹",
            "PERSONAL_CARE" to "🧴",
            "MILK_DRINKS" to "🥛",
            "DETERGENTS" to "🧺",
            "FEMININE_BABY_CARE" to "👶",
            "SHAMPOOS" to "💆",
            "GENERAL_CARE" to "✨",
            "ATTA" to "🌾",
            "BISCUITS_COOKIES" to "🍪",
            "NOODLES" to "🍜",
            "CHOCOLATES" to "🍫",
            "AGARBATTIS" to "🪔",
            "SOAPS" to "🧼",
            "STATIONERY" to "✏️",
            "COFFEE" to "☕",
            "SAUCE" to "🍅",
            "EDIBLE_OIL" to "🫒",
            "NON_EDIBLE_OIL" to "🛢️",
        )

    fun emoji(shelfId: String?): String = emojis[shelfId?.uppercase()] ?: "📦"
}
