package com.mart.distribution.demo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mart.distribution.demo.ui.components.RoleCard
import com.mart.distribution.demo.ui.components.RoleCardSpec
import com.mart.distribution.demo.ui.flashmart.FmAppHeader
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.theme.FmSpacing
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBlueTint
import com.mart.distribution.demo.ui.theme.WholesaleDealerBlue
import com.mart.distribution.demo.ui.theme.WholesaleDealerBlueTint
import com.mart.distribution.demo.ui.theme.WholesaleGoldInk
import com.mart.distribution.demo.ui.theme.WholesaleGoldTint

private val USER_TYPE_ROLES =
    listOf(
        RoleCardSpec(
            id = "SHOPKEEPER",
            icon = Icons.Filled.Storefront,
            title = "Shopkeeper",
            body = "Purchase products for your shop at dealer prices.",
            bg = WholesaleBlueTint,
            fg = WholesaleBlue,
        ),
        RoleCardSpec(
            id = "DEALER",
            icon = Icons.Filled.Inventory2,
            title = "Dealer",
            body = "Manage inventory and serve retailers in your area.",
            bg = WholesaleDealerBlueTint,
            fg = WholesaleDealerBlue,
        ),
        RoleCardSpec(
            id = "EMPLOYEE",
            icon = Icons.Filled.Groups,
            title = "Employee",
            body = "Onboard partners and manage relationships.",
            bg = WholesaleGoldTint,
            fg = WholesaleGoldInk,
        ),
    )

@Composable
fun UserTypeScreen(
    onBack: () -> Unit,
    onContinue: (role: String) -> Unit,
) {
    var selected by remember { mutableStateOf("SHOPKEEPER") }

    Column(modifier = Modifier.fillMaxSize()) {
        FmAppHeader(
            title = "Choose your account",
            subtitle = "You can't change this later",
            onBack = onBack,
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = FmSpacing.screenH),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            USER_TYPE_ROLES.forEach { role ->
                RoleCard(
                    spec = role,
                    selected = selected == role.id,
                    onClick = { selected = role.id },
                )
            }
        }
        Column(modifier = Modifier.padding(horizontal = FmSpacing.screenH, vertical = 16.dp)) {
            FmButton("Continue", onClick = { onContinue(selected) })
        }
    }
}
