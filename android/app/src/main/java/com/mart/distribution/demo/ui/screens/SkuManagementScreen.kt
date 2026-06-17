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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.mart.distribution.demo.data.api.dto.CreateProductRequest
import com.mart.distribution.demo.data.api.dto.ProductDto
import com.mart.distribution.demo.feature.home.LoadState
import com.mart.distribution.demo.feature.home.MainViewModel
import com.mart.distribution.demo.ui.components.GradientGoldButton
import com.mart.distribution.demo.ui.components.MartElevatedCard
import com.mart.distribution.demo.ui.shopkeeper.FmcgShelfCatalog
import com.mart.distribution.demo.ui.theme.MartFieldDefaults
import com.mart.distribution.demo.util.FieldFilters
import com.mart.distribution.demo.util.FieldValidators

private data class ProductFormState(
    val name: String = "",
    val brandType: String = "OWN",
    val shelf: String = "STAPLES",
    val basePrice: String = "0",
    val gstPct: String = "18",
    val dealerDisc: String = "10",
    val shopkeeperDisc: String = "5",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkuManagementScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel,
) {
    val ui by mainViewModel.uiState.collectAsState()
    var createBusy by remember { mutableStateOf(false) }
    var editBusyId by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var form by remember { mutableStateOf(ProductFormState()) }
    var editing by remember { mutableStateOf<ProductDto?>(null) }
    var csvDialogOpen by remember { mutableStateOf(false) }
    var csvText by remember { mutableStateOf("") }
    var csvBusy by remember { mutableStateOf(false) }

    val products =
        when (val p = ui.products) {
            is LoadState.Ok -> p.data
            else -> emptyList()
        }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SKU management") },
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
                    "Create, edit, or delete SKUs. Pricing, GST, and discount settings are applied server-side in order calculations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(12.dp))
            ProductForm(
                title = "Add product",
                form = form,
                onFormChange = { form = it; error = null },
                onSubmit = {
                    validateForm(form)?.let { error = it; return@ProductForm }
                    val parsed = parseForm(form)!!
                    createBusy = true
                    mainViewModel.createProduct(
                        name = form.name,
                        brandType = form.brandType,
                        shelf = form.shelf,
                        basePrice = parsed.basePrice,
                        gstPct = parsed.gstPct,
                        dealerDisc = parsed.dealerDisc,
                        shopkeeperDisc = parsed.shopkeeperDisc,
                    ) { err ->
                        createBusy = false
                        if (err != null) {
                            error = err
                        } else {
                            form = ProductFormState()
                        }
                    }
                },
                busy = createBusy,
                submitText = "Add SKU",
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { csvDialogOpen = true; error = null },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Upload SKU sheet (CSV demo)")
            }
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(12.dp))
            Text("Products", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            when (val p = ui.products) {
                is LoadState.Loading -> Text("Loading products…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                is LoadState.Err -> Text(p.message, color = MaterialTheme.colorScheme.error)
                is LoadState.Ok ->
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        items(products, key = { it.id }) { row ->
                            MartElevatedCard(modifier = Modifier.fillMaxWidth()) {
                                Text(row.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${row.brandType} · ${FmcgShelfCatalog.label(row.shelf)} · Price ${row.basePrice} · GST ${row.gstPercentage}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    "Dealer ${row.dealerDiscount}% · Shopkeeper ${row.shopkeeperDiscount}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { editing = row },
                                        modifier = Modifier.weight(1f),
                                    ) { Text("Edit") }
                                    OutlinedButton(
                                        onClick = {
                                            editBusyId = row.id
                                            mainViewModel.deleteProduct(row.id) { err ->
                                                editBusyId = null
                                                if (err != null) error = err
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        enabled = editBusyId != row.id,
                                    ) { Text(if (editBusyId == row.id) "Deleting…" else "Delete") }
                                }
                            }
                        }
                    }
                else -> {}
            }
        }
    }

    editing?.let { row ->
        var editForm by remember(row.id) {
            mutableStateOf(
                ProductFormState(
                    name = row.name,
                    brandType = row.brandType,
                    shelf = row.shelf ?: "STAPLES",
                    basePrice = row.basePrice.toString(),
                    gstPct = row.gstPercentage.toString(),
                    dealerDisc = row.dealerDiscount.toString(),
                    shopkeeperDisc = row.shopkeeperDiscount.toString(),
                ),
            )
        }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("Edit SKU") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                ) {
                    ProductFields(form = editForm, onFormChange = { editForm = it })
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        validateForm(editForm)?.let { error = it; return@TextButton }
                        val parsed = parseForm(editForm)!!
                        editBusyId = row.id
                        mainViewModel.updateProduct(
                            productId = row.id,
                            name = editForm.name,
                            brandType = editForm.brandType,
                            shelf = editForm.shelf,
                            basePrice = parsed.basePrice,
                            gstPct = parsed.gstPct,
                            dealerDisc = parsed.dealerDisc,
                            shopkeeperDisc = parsed.shopkeeperDisc,
                        ) { err ->
                            editBusyId = null
                            if (err != null) {
                                error = err
                            } else {
                                editing = null
                            }
                        }
                    },
                ) { Text(if (editBusyId == row.id) "Saving…" else "Save") }
            },
            dismissButton = {
                TextButton(onClick = { editing = null }) { Text("Cancel") }
            },
        )
    }

    if (csvDialogOpen) {
        AlertDialog(
            onDismissRequest = { if (!csvBusy) csvDialogOpen = false },
            title = { Text("CSV import (demo)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Paste CSV rows (one per line):\n" +
                            "• With shelf: name,brandType,shelf,basePrice,gstPercentage,dealerDiscount,shopkeeperDiscount\n" +
                            "• Legacy 6 columns default shelf to STAPLES",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = csvText,
                        onValueChange = { csvText = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 6,
                        maxLines = 10,
                        placeholder = {
                            Text("KNSR Premium Rice,OWN,STAPLES,1549,18,10,5")
                        },
                        colors = MartFieldDefaults.outlinedColors(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = parseCsvRows(csvText)
                        if (parsed.isEmpty()) {
                            error = "No valid CSV rows found"
                            return@TextButton
                        }
                        csvBusy = true
                        mainViewModel.bulkCreateProducts(parsed) { err ->
                            csvBusy = false
                            if (err != null) {
                                error = err
                            } else {
                                csvText = ""
                                csvDialogOpen = false
                            }
                        }
                    },
                ) { Text(if (csvBusy) "Importing…" else "Import") }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!csvBusy) csvDialogOpen = false },
                ) { Text("Close") }
            },
        )
    }
}

@Composable
private fun ProductForm(
    title: String,
    form: ProductFormState,
    onFormChange: (ProductFormState) -> Unit,
    onSubmit: () -> Unit,
    busy: Boolean,
    submitText: String,
) {
    MartElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        ProductFields(form = form, onFormChange = onFormChange)
        Spacer(Modifier.height(10.dp))
        GradientGoldButton(
            text = if (busy) "Saving…" else submitText,
            onClick = onSubmit,
            enabled = !busy,
        )
    }
}

@Composable
private fun ProductFields(
    form: ProductFormState,
    onFormChange: (ProductFormState) -> Unit,
) {
    OutlinedTextField(
        value = form.name,
        onValueChange = { onFormChange(form.copy(name = FieldFilters.businessName(it, maxLen = 120))) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Product name") },
        singleLine = true,
        colors = MartFieldDefaults.outlinedColors(),
    )
    OutlinedTextField(
        value = form.brandType,
        onValueChange = { onFormChange(form.copy(brandType = FieldFilters.brandType(it))) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Brand type (OWN/OTHER)") },
        singleLine = true,
        colors = MartFieldDefaults.outlinedColors(),
    )
    Text("FMCG shelf", style = MaterialTheme.typography.labelLarge)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FmcgShelfCatalog.ids.forEach { sid ->
            FilterChip(
                selected = form.shelf == sid,
                onClick = { onFormChange(form.copy(shelf = sid)) },
                label = { Text(FmcgShelfCatalog.label(sid)) },
            )
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = form.basePrice,
            onValueChange = { onFormChange(form.copy(basePrice = FieldFilters.decimal(it))) },
            modifier = Modifier.weight(1f),
            label = { Text("Price") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            colors = MartFieldDefaults.outlinedColors(),
        )
        OutlinedTextField(
            value = form.gstPct,
            onValueChange = { onFormChange(form.copy(gstPct = FieldFilters.percentage(it))) },
            modifier = Modifier.weight(1f),
            label = { Text("GST %") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            colors = MartFieldDefaults.outlinedColors(),
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = form.dealerDisc,
            onValueChange = { onFormChange(form.copy(dealerDisc = FieldFilters.percentage(it))) },
            modifier = Modifier.weight(1f),
            label = { Text("Dealer disc %") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            colors = MartFieldDefaults.outlinedColors(),
        )
        OutlinedTextField(
            value = form.shopkeeperDisc,
            onValueChange = { onFormChange(form.copy(shopkeeperDisc = FieldFilters.percentage(it))) },
            modifier = Modifier.weight(1f),
            label = { Text("Shopkeeper disc %") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            colors = MartFieldDefaults.outlinedColors(),
        )
    }
}

private data class ParsedForm(
    val basePrice: Double,
    val gstPct: Double,
    val dealerDisc: Double,
    val shopkeeperDisc: Double,
)

private fun validateForm(form: ProductFormState): String? {
    FieldValidators.businessName(form.name)?.let { return "Product name: $it" }
    FieldValidators.brandType(form.brandType)?.let { return it }
    if (form.shelf.uppercase() !in FmcgShelfCatalog.ids.toSet()) return "Select a valid shelf"
    FieldValidators.decimalInRange(form.basePrice, 0.01, 1_000_000.0, "Price")?.let { return it }
    FieldValidators.decimalInRange(form.gstPct, 0.0, 100.0, "GST %")?.let { return it }
    FieldValidators.decimalInRange(form.dealerDisc, 0.0, 100.0, "Dealer discount")?.let { return it }
    FieldValidators.decimalInRange(form.shopkeeperDisc, 0.0, 100.0, "Shopkeeper discount")?.let { return it }
    return null
}

private fun parseForm(form: ProductFormState): ParsedForm? {
    if (validateForm(form) != null) return null
    if (form.name.isBlank()) return null
    if (form.shelf.uppercase() !in FmcgShelfCatalog.ids.toSet()) return null
    val base = form.basePrice.toDoubleOrNull() ?: return null
    val gst = form.gstPct.toDoubleOrNull() ?: return null
    val dealer = form.dealerDisc.toDoubleOrNull() ?: return null
    val shopkeeper = form.shopkeeperDisc.toDoubleOrNull() ?: return null
    if (gst !in 0.0..100.0 || dealer !in 0.0..100.0 || shopkeeper !in 0.0..100.0) return null
    if (form.brandType.uppercase() !in setOf("OWN", "OTHER")) return null
    return ParsedForm(base, gst, dealer, shopkeeper)
}

private fun parseCsvRows(input: String): List<CreateProductRequest> {
    return input
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .mapNotNull { line ->
            val cols = line.split(",").map { it.trim() }
            if (cols.size < 6) return@mapNotNull null
            val name = cols[0]
            val brandType = cols[1].uppercase()
            val shelf: String
            val basePrice: Double
            val gst: Double
            val dealer: Double
            val shopkeeper: Double
            if (cols.size >= 7) {
                shelf = cols[2].uppercase()
                basePrice = cols[3].toDoubleOrNull() ?: return@mapNotNull null
                gst = cols[4].toDoubleOrNull() ?: return@mapNotNull null
                dealer = cols[5].toDoubleOrNull() ?: return@mapNotNull null
                shopkeeper = cols[6].toDoubleOrNull() ?: return@mapNotNull null
            } else {
                shelf = "STAPLES"
                basePrice = cols[2].toDoubleOrNull() ?: return@mapNotNull null
                gst = cols[3].toDoubleOrNull() ?: return@mapNotNull null
                dealer = cols[4].toDoubleOrNull() ?: return@mapNotNull null
                shopkeeper = cols[5].toDoubleOrNull() ?: return@mapNotNull null
            }
            if (name.isBlank() || brandType !in setOf("OWN", "OTHER")) return@mapNotNull null
            if (shelf !in FmcgShelfCatalog.ids.toSet()) return@mapNotNull null
            if (gst !in 0.0..100.0 || dealer !in 0.0..100.0 || shopkeeper !in 0.0..100.0) return@mapNotNull null
            CreateProductRequest(
                name = name,
                brandType = brandType,
                shelf = shelf,
                basePrice = basePrice,
                gstPercentage = gst,
                dealerDiscount = dealer,
                shopkeeperDiscount = shopkeeper,
            )
        }
        .toList()
}
