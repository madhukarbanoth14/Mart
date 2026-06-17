package com.mart.distribution.demo.ui.navigation

/**
 * Root navigation bucket aligned with backend [UserRole] (ADMIN, EMPLOYEE, DEALER, SHOPKEEPER).
 * Admin and employee share the same graph (oversight / team views).
 */
enum class MartNavRole {
    SHOPKEEPER,
    DEALER,
    ADMIN_OR_EMPLOYEE,
    ;

    companion object {
        fun fromApiRole(role: String): MartNavRole {
            return when (role.uppercase()) {
                "SHOPKEEPER" -> SHOPKEEPER
                "DEALER" -> DEALER
                "ADMIN", "EMPLOYEE" -> ADMIN_OR_EMPLOYEE
                "SUPER_ADMIN" -> ADMIN_OR_EMPLOYEE
                else -> ADMIN_OR_EMPLOYEE
            }
        }
    }
}
