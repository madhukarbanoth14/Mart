package com.mart.distribution.demo.data.demo

import com.mart.distribution.demo.data.api.dto.UserBriefDto
import com.mart.distribution.demo.data.session.SessionUser

/**
 * Offline login when [com.mart.distribution.demo.BuildConfig.USE_LOCAL_DEMO_AUTH] is true.
 * Matches Prisma seed emails and password (investor APK on any network).
 */
object LocalDemoAuthConfig {
    const val LOCAL_DEMO_BEARER_TOKEN: String = "local-demo-no-server"

    /** Same as backend seed (`Password@123`). */
    const val DEMO_PASSWORD: String = "Password@123"

    private val usersByEmail: Map<String, SessionUser> =
        listOf(
            SessionUser("demo-user-admin", "Super Admin", "admin@martdemo.com", "ADMIN"),
            SessionUser("demo-user-employee", "Field Employee", "employee@martdemo.com", "EMPLOYEE"),
            SessionUser("demo-user-dealer", "City Dealer", "dealer@martdemo.com", "DEALER"),
            SessionUser("demo-user-shop1", "Shopkeeper One", "shop1@martdemo.com", "SHOPKEEPER", areaName = "Central Zone", assignedDealer = UserBriefDto("demo-user-dealer", "City Dealer", "dealer@martdemo.com", "9000000003")),
            SessionUser("demo-user-shop2", "Shopkeeper Two", "shop2@martdemo.com", "SHOPKEEPER", areaName = "North Zone", assignedDealer = UserBriefDto("demo-user-dealer", "City Dealer", "dealer@martdemo.com", "9000000003")),
        ).associateBy { it.email.lowercase() }

    private val usersByPhone: Map<String, SessionUser> =
        mapOf(
            "9000000001" to usersByEmail.getValue("admin@martdemo.com"),
            "9000000002" to usersByEmail.getValue("employee@martdemo.com"),
            "9000000003" to usersByEmail.getValue("dealer@martdemo.com"),
            "9000000004" to usersByEmail.getValue("shop1@martdemo.com"),
            "9000000005" to usersByEmail.getValue("shop2@martdemo.com"),
        )

    fun resolveDemoUser(
        identifier: String,
        password: String,
    ): SessionUser? {
        if (password != DEMO_PASSWORD) return null
        val key = identifier.trim()
        return usersByEmail[key.lowercase()] ?: usersByPhone[key]
    }
}
