package com.mart.distribution.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.mart.distribution.demo.ui.LocalAppContainer
import com.mart.distribution.demo.ui.MartNavHost
import com.mart.distribution.demo.ui.theme.WholesaleTheme
import com.mart.distribution.demo.data.payment.RazorpayBridge
import com.mart.distribution.demo.data.payment.RazorpayResultEvent
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as MartApplication
        setContent {
            WholesaleTheme {
                CompositionLocalProvider(LocalAppContainer provides app.container) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        MartNavHost()
                    }
                }
            }
        }
    }

    override fun onPaymentSuccess(
        razorpayPaymentId: String?,
        paymentData: PaymentData?,
    ) {
        RazorpayBridge.emit(
            RazorpayResultEvent(
                success = true,
                paymentId = razorpayPaymentId,
                orderId = paymentData?.orderId,
                signature = paymentData?.signature,
            ),
        )
    }

    override fun onPaymentError(
        code: Int,
        response: String?,
        paymentData: PaymentData?,
    ) {
        RazorpayBridge.emit(
            RazorpayResultEvent(
                success = false,
                paymentId = paymentData?.paymentId,
                orderId = paymentData?.orderId,
                signature = paymentData?.signature,
                error = "Razorpay error $code: ${response ?: "Unknown error"}",
            ),
        )
    }
}
