package com.mart.distribution.demo.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleInk4
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import java.util.Calendar

@Composable
fun MartAppFooter(
    onPrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier,
    darkBackground: Boolean = false,
) {
    val year = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val muted = if (darkBackground) Color.White.copy(alpha = 0.62f) else WholesaleMuted
    val subtle = if (darkBackground) Color.White.copy(alpha = 0.45f) else WholesaleInk4
    val linkColor = if (darkBackground) Color.White.copy(alpha = 0.92f) else WholesaleBlue

    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            "© $year KNSR Mart · FlashMart",
            fontSize = 11.sp,
            color = muted,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Privacy Policy",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = linkColor,
                modifier = Modifier.clickable(onClick = onPrivacyPolicy),
            )
            Text("·", fontSize = 12.sp, color = subtle)
            Text(
                "All rights reserved",
                fontSize = 11.sp,
                color = subtle,
            )
        }
    }
}
