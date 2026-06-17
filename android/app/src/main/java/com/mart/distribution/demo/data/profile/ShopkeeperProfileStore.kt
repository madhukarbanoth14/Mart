package com.mart.distribution.demo.data.profile

/**
 * In-app shopkeeper profile preferences (demo / client preview).
 * Persists for the app session; defaults match seed demo data.
 */
object ShopkeeperProfileStore {
    var storeName: String = "Shopkeeper One Store"
    var storeAddress: String = "12, Main Market Road, Central Zone, Hyderabad — 500001"
    var storePhone: String = "9000000004"

    var gstin: String = "36AABCU9603R1Z5"
    var gstBusinessName: String = "Shopkeeper One Trading Co."

    var orderAlerts: Boolean = true
    var deliveryAlerts: Boolean = true
    var promoAlerts: Boolean = false

    const val DUMMY_CARD_MASKED: String = "Visa ···· 4242"
    const val DUMMY_CARD_NUMBER: String = "4111 1111 1111 1111"
    const val DUMMY_CARD_EXPIRY: String = "12/28"
    const val DUMMY_CARD_CVV: String = "123"
    const val DUMMY_CARD_NAME: String = "Shopkeeper One"
    const val DUMMY_UPI_ID: String = "shop1@upi"
}
