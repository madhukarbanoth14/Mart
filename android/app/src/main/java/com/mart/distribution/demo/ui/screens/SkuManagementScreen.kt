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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mart.distribution.demo.data.api.dto.CreateProductRequest
import com.mart.distribution.demo.data.api.dto.ProductDto
import com.mart.distribution.demo.feature.home.LoadState
import com.mart.distribution.demo.feature.home.MainViewModel
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmButtonVariant
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.flashmart.FmDataRow
import com.mart.distribution.demo.ui.flashmart.FmDialog
import com.mart.distribution.demo.ui.flashmart.FmEmptyState
import com.mart.distribution.demo.ui.flashmart.FmErrorBanner
import com.mart.distribution.demo.ui.flashmart.FmInfoBanner
import com.mart.distribution.demo.ui.flashmart.FmLoadingState
import com.mart.distribution.demo.ui.flashmart.FmScreen
import com.mart.distribution.demo.ui.flashmart.FmChipRow
import com.mart.distribution.demo.ui.flashmart.FmSectionLabel
import com.mart.distribution.demo.ui.flashmart.FmTextField
import com.mart.distribution.demo.ui.shopkeeper.FmcgShelfCatalog
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.ui.theme.FmSpacing
import com.mart.distribution.demo.ui.theme.MartFieldDefaults
import com.mart.distribution.demo.ui.theme.WholesaleText
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

@Composable
fun SkuManagementScreen(
    navController: NavHostController? = null,
    mainViewModel: MainViewModel,
    embeddedInTab: Boolean = false,
) {
    val ui by mainViewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { mainViewModel.loadProducts() }
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

    val infoMessage =
        if (embeddedInTab) {
            "Add your products and set pricing. Shopkeepers in your area order from this catalog."
        } else {
            "Create, edit, or delete SKUs. Pricing, GST, and discounts are applied automatically in order calculations."
        }

    @Composable
    fun SkuManagementBody(modifier: Modifier = Modifier) {
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .then(
                        if (embeddedInTab) {
                            Modifier.padding(horizontal = FmSpacing.listH)
                        } else {
                            Modifier
                        },
                    ),
        ) {
            FmInfoBanner(message = infoMessage)
            Spacer(Modifier.height(FmSpacing.sectionGap))
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
            Spacer(Modifier.height(FmSpacing.itemGap))
            FmButton(
                text = "Import from CSV",
                onClick = { csvDialogOpen = true; error = null },
                variant = FmButtonVariant.Outline,
            )
            error?.let {
                Spacer(Modifier.height(FmSpacing.itemGap))
                FmErrorBanner(message = it)
            }
            Spacer(Modifier.height(FmSpacing.sectionGap))
            FmSectionLabel(title = "Catalog")
            when (val p = ui.products) {
                is LoadState.Loading -> FmLoadingState(message = "Loading catalog…")
                is LoadState.Err -> FmErrorBanner(message = p.message)
                is LoadState.Ok ->
                    if (products.isEmpty()) {
                        FmEmptyState(
                            icon = Icons.Outlined.Inventory2,
                            title = "No products yet",
                            message = "Add your first SKU above. Set base price, GST, and discount tiers before shopkeepers can order.",
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(FmSpacing.itemGap),
                            modifier = Modifier.weight(1f),
                        ) {
                            items(products, key = { it.id }) { row ->
                                SkuProductRow(
                                    row = row,
                                    editBusy = editBusyId == row.id,
                                    onEdit = { editing = row },
                                    onDelete = {
                                        editBusyId = row.id
                                        mainViewModel.deleteProduct(row.id) { err ->
                                            editBusyId = null
                                            if (err != null) error = err
                                        }
                                    },
                                )
                            }
                            item { Spacer(Modifier.height(24.dp)) }
                        }
                    }
                else -> {}
            }
        }
    }

    if (embeddedInTab) {
        SkuManagementBody()
    } else {
        FmScreen(
            title = "SKU management",
            subtitle = "Products, pricing & discounts",
            onBack = { navController?.popBackStack() },
        ) { modifier ->
            SkuManagementBody(modifier.verticalScroll(rememberScrollState()))
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
        FmDialog(
            title = "Edit SKU",
            onDismiss = { editing = null },
            confirmLabel = "Save changes",
            confirmBusy = editBusyId == row.id,
            onConfirm = {
                validateForm(editForm)?.let { error = it; return@FmDialog }
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
        ) {
            ProductFields(form = editForm, onFormChange = { editForm = it })
        }
    }

    if (csvDialogOpen) {
        FmDialog(
            title = "CSV import",
            onDismiss = { if (!csvBusy) csvDialogOpen = false },
            confirmLabel = "Import rows",
            confirmBusy = csvBusy,
            dismissLabel = "Close",
            onConfirm = {
                val parsed = parseCsvRows(csvText)
                if (parsed.isEmpty()) {
                    error = "No valid CSV rows found"
                    return@FmDialog
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
        ) {
            FmInfoBanner(
                message =
                    "One row per line: name, brandType, shelf, basePrice, gstPercentage, dealerDiscount, shopkeeperDiscount. " +
                        "Legacy 6-column rows default shelf to STAPLES.",
                tint = com.mart.distribution.demo.ui.theme.WholesaleSurface2,
                accent = com.mart.distribution.demo.ui.theme.WholesaleInk2,
            )
            FmTextField(
                value = csvText,
                onValueChange = { csvText = it },
                label = "CSV data",
                placeholder = "KNSR Premium Rice,OWN,STAPLES,1549,18,10,5",
                singleLine = false,
                minLines = 6,
                maxLines = 10,
                keyboardOptions = MartFieldDefaults.englishMultilineKeyboard,
            )
        }
    }
}

@Composable
private fun SkuProductRow(
    row: ProductDto,
    editBusy: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    FmCard(modifier = Modifier.fillMaxWidth()) {
        FmDataRow(
            title = row.name,
            subtitle =
                "${row.brandType} · ${FmcgShelfCatalog.label(row.shelf)} · ₹${row.basePrice} · GST ${row.gstPercentage}% · " +
                    "Dealer ${row.dealerDiscount}% · Shop ${row.shopkeeperDiscount}%",
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FmButton(
                text = "Edit",
                onClick = onEdit,
                variant = FmButtonVariant.Outline,
                modifier = Modifier.weight(1f),
            )
            FmButton(
                text = if (editBusy) "Deleting…" else "Delete",
                onClick = onDelete,
                variant = FmButtonVariant.Ghost,
                enabled = !editBusy,
                modifier = Modifier.weight(1f),
            )
        }
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
    FmCard(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = WholesaleText)
        Spacer(Modifier.height(10.dp))
        ProductFields(form = form, onFormChange = onFormChange)
        Spacer(Modifier.height(12.dp))
        FmButton(
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
    Column(verticalArrangement = Arrangement.spacedBy(FmSpacing.fieldGap)) {
        FmTextField(
            value = form.name,
            onValueChange = { onFormChange(form.copy(name = FieldFilters.businessName(it, maxLen = 120))) },
            label = "Product name",
        )
        FmTextField(
            value = form.brandType,
            onValueChange = { onFormChange(form.copy(brandType = FieldFilters.brandType(it))) },
            label = "Brand type",
            placeholder = "OWN or OTHER",
        )
        FmChipRow(
            options = FmcgShelfCatalog.ids.map { it to FmcgShelfCatalog.label(it) },
            selectedId = form.shelf,
            onSelect = { onFormChange(form.copy(shelf = it)) },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FmTextField(
                value = form.basePrice,
                onValueChange = { onFormChange(form.copy(basePrice = FieldFilters.decimal(it))) },
                label = "Base price",
                modifier = Modifier.weight(1f),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            FmTextField(
                value = form.gstPct,
                onValueChange = { onFormChange(form.copy(gstPct = FieldFilters.percentage(it))) },
                label = "GST %",
                modifier = Modifier.weight(1f),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FmTextField(
                value = form.dealerDisc,
                onValueChange = { onFormChange(form.copy(dealerDisc = FieldFilters.percentage(it))) },
                label = "Dealer disc %",
                modifier = Modifier.weight(1f),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            FmTextField(
                value = form.shopkeeperDisc,
                onValueChange = { onFormChange(form.copy(shopkeeperDisc = FieldFilters.percentage(it))) },
                label = "Shopkeeper disc %",
                modifier = Modifier.weight(1f),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
        }
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
