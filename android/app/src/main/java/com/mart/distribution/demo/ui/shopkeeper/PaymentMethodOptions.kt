package com.mart.distribution.demo.ui.shopkeeper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.CreditScore
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.ui.graphics.vector.ImageVector

data class PaymentMethodOption(
    val id: String,
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
    val razorpayGateway: Boolean,
)

fun paymentMethodOptions(useRazorpayCheckout: Boolean): List<PaymentMethodOption> =
    listOf(
        PaymentMethodOption(
            id = "card",
            label = "Credit / Debit Card",
            subtitle =
                if (useRazorpayCheckout) {
                    "Enter card number, expiry & CVV in Razorpay"
                } else {
                    "Demo card form"
                },
            icon = Icons.Outlined.CreditCard,
            razorpayGateway = true,
        ),
        PaymentMethodOption(
            id = "upi",
            label = "UPI",
            subtitle =
                if (useRazorpayCheckout) {
                    "GPay, PhonePe, Paytm & UPI ID in Razorpay"
                } else {
                    "Demo UPI confirm"
                },
            icon = Icons.Outlined.Payments,
            razorpayGateway = true,
        ),
        PaymentMethodOption(
            id = "netbanking",
            label = "Net Banking",
            subtitle = "Select your bank in Razorpay checkout",
            icon = Icons.Outlined.AccountBalance,
            razorpayGateway = true,
        ),
        PaymentMethodOption(
            id = "wallet",
            label = "Wallets",
            subtitle = "Paytm, PhonePe wallet, Mobikwik & more",
            icon = Icons.Outlined.AccountBalanceWallet,
            razorpayGateway = true,
        ),
        PaymentMethodOption(
            id = "emi",
            label = "EMI",
            subtitle = "Card EMI & cardless EMI via Razorpay",
            icon = Icons.Outlined.CreditScore,
            razorpayGateway = true,
        ),
        PaymentMethodOption(
            id = "paylater",
            label = "Pay Later",
            subtitle = "LazyPay, Simpl & other pay-later options",
            icon = Icons.Outlined.Schedule,
            razorpayGateway = true,
        ),
        PaymentMethodOption(
            id = "cod",
            label = "Pay on delivery",
            subtitle = "Cash or UPI to dealer when order arrives",
            icon = Icons.Outlined.LocalShipping,
            razorpayGateway = false,
        ),
    )

fun PaymentMethodOption.isEnabled(useRazorpayCheckout: Boolean): Boolean =
    when {
        id == "cod" -> true
        useRazorpayCheckout && razorpayGateway -> true
        !useRazorpayCheckout && (id == "card" || id == "upi") -> true
        else -> false
    }
