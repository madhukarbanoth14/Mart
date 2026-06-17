package com.mart.distribution.demo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mart.distribution.demo.feature.brands.BrandsViewModel
import com.mart.distribution.demo.feature.home.LoadState
import com.mart.distribution.demo.ui.theme.MartFieldDefaults
import com.mart.distribution.demo.ui.components.GradientGoldButton
import com.mart.distribution.demo.ui.components.MartElevatedCard
import com.mart.distribution.demo.util.FieldFilters
import com.mart.distribution.demo.util.FieldValidators

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandsManagementScreen(
    navController: NavHostController,
    brandsViewModel: BrandsViewModel,
) {
    val ui by brandsViewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var logoUrl by remember { mutableStateOf("") }
    var createBusy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var deletingId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        brandsViewModel.loadBrands()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Brands management") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
        ) {
            MartElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Create and maintain FMCG brands for catalog filtering and SKU mapping.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = FieldFilters.businessName(it); error = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Brand name") },
                singleLine = true,
                colors = MartFieldDefaults.outlinedColors(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = logoUrl,
                onValueChange = { logoUrl = FieldFilters.url(it); error = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Logo URL (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                singleLine = true,
                colors = MartFieldDefaults.outlinedColors(),
            )
            Spacer(Modifier.height(10.dp))
            GradientGoldButton(
                text = if (createBusy) "Adding…" else "Add brand",
                onClick = {
                    FieldValidators.businessName(name)?.let { error = it; return@GradientGoldButton }
                    FieldValidators.urlOptional(logoUrl)?.let { error = it; return@GradientGoldButton }
                    createBusy = true
                    brandsViewModel.createBrand(name, logoUrl.ifBlank { null }) { err ->
                        createBusy = false
                        if (err != null) {
                            error = err
                        } else {
                            name = ""
                            logoUrl = ""
                        }
                    }
                },
                enabled = !createBusy,
            )
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(12.dp))
            Text("Brands", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            when (val brands = ui.brands) {
                is LoadState.Loading -> Text("Loading brands…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                is LoadState.Err -> Text(brands.message, color = MaterialTheme.colorScheme.error)
                is LoadState.Ok ->
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(brands.data, key = { it.id }) { brand ->
                            MartElevatedCard(modifier = Modifier.fillMaxWidth()) {
                                Text(brand.name, style = MaterialTheme.typography.titleMedium)
                                brand.logoUrl?.let {
                                    Text(
                                        it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            deletingId = brand.id
                                            brandsViewModel.deleteBrand(brand.id) { err ->
                                                deletingId = null
                                                if (err != null) error = err
                                            }
                                        },
                                        enabled = deletingId != brand.id,
                                    ) {
                                        Text(if (deletingId == brand.id) "Deleting…" else "Delete")
                                    }
                                }
                            }
                        }
                    }
                else -> {}
            }
        }
    }
}
