package com.mart.distribution.demo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmButtonVariant
import com.mart.distribution.demo.ui.flashmart.FmLogoMark
import com.mart.distribution.demo.ui.theme.WholesaleBlueTint
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleText

@Composable
fun WelcomeScreen(
    onCreateAccount: () -> Unit,
    onLogin: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(280.dp)
                        .clip(RoundedCornerShape(140.dp))
                        .background(WholesaleBlueTint),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FmLogoMark(size = 84.dp)
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Text(
                        "FMCG essentials, delivered fast",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WholesaleMuted,
                    )
                }
            }
        }
        Column(modifier = Modifier.padding(horizontal = 28.dp, vertical = 6.dp)) {
            Text(
                "Your shop's",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WholesaleText,
                lineHeight = 35.sp,
                letterSpacing = (-0.7).sp,
            )
            Text(
                "supply, simplified.",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WholesaleText,
                lineHeight = 35.sp,
                letterSpacing = (-0.7).sp,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Order FMCG stock at dealer prices, track every delivery, and get GST invoices — all in one app.",
                fontSize = 15.sp,
                color = WholesaleMuted,
                lineHeight = 21.sp,
            )
            Spacer(Modifier.height(22.dp))
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                FmButton("Create account", onClick = onCreateAccount, variant = FmButtonVariant.Primary)
                FmButton("I already have an account", onClick = onLogin, variant = FmButtonVariant.Soft)
            }
            Spacer(Modifier.height(18.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "Privacy Policy  ·  Terms & Conditions",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WholesaleMuted,
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
