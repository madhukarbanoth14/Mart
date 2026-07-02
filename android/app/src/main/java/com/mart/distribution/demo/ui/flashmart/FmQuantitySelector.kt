package com.mart.distribution.demo.ui.flashmart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBorder
import com.mart.distribution.demo.ui.theme.WholesaleInk4
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleRed
import com.mart.distribution.demo.ui.theme.WholesaleSurface2
import com.mart.distribution.demo.ui.theme.WholesaleSurface3
import com.mart.distribution.demo.ui.theme.WholesaleText

object FmQuantityDefaults {
    val quickChips = listOf(10, 25, 50, 100, 250, 500, 1000)
    const val minQuantity = 1
    const val maxQuantity = 10000
}

fun coerceWholeQuantity(
    raw: String,
    min: Int = FmQuantityDefaults.minQuantity,
    max: Int = FmQuantityDefaults.maxQuantity,
): Int? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null
    if (trimmed.contains('.') || trimmed.contains(',')) return null
    val value = trimmed.toIntOrNull() ?: return null
    if (value < min) return null
    return value.coerceAtMost(max)
}

@Composable
fun FmQuantityInput(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    min: Int = FmQuantityDefaults.minQuantity,
    max: Int = FmQuantityDefaults.maxQuantity,
    compact: Boolean = false,
) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    fun commit(raw: String) {
        val parsed = coerceWholeQuantity(raw, min, max)
        if (parsed == null) {
            error = "Enter a whole number between $min and $max"
            text = value.toString()
            return
        }
        error = null
        text = parsed.toString()
        if (parsed != value) onValueChange(parsed)
    }

    LaunchedEffect(value) {
        if (text.toIntOrNull() != value) {
            text = value.toString()
        }
    }

    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FmQuantityStepButton(
                icon = Icons.Filled.Remove,
                enabled = value > min,
                onClick = { onValueChange((value - 1).coerceAtLeast(min)) },
            )
            OutlinedTextField(
                value = text,
                onValueChange = { next ->
                    val filtered = next.filter { it.isDigit() }
                    text = filtered
                    error = null
                },
                modifier = Modifier.widthIn(min = if (compact) 56.dp else 72.dp, max = 96.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle =
                    androidx.compose.ui.text.TextStyle(
                        fontSize = if (compact) 14.sp else 16.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                shape = RoundedCornerShape(10.dp),
            )
            FmQuantityStepButton(
                icon = Icons.Filled.Add,
                enabled = value < max,
                onClick = { onValueChange((value + 1).coerceAtMost(max)) },
            )
        }
        error?.let {
            Text(it, fontSize = 11.sp, color = WholesaleRed, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun FmQuantityStepButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (enabled) WholesaleSurface2 else WholesaleSurface3)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (enabled) WholesaleText else WholesaleInk4,
            modifier = Modifier.padding(2.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FmQuantityPickerDialog(
    productName: String,
    initialQuantity: Int = 1,
    min: Int = FmQuantityDefaults.minQuantity,
    max: Int = FmQuantityDefaults.maxQuantity,
    quickChips: List<Int> = FmQuantityDefaults.quickChips,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var quantityText by remember { mutableStateOf(initialQuantity.coerceIn(min, max).toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    fun parsedQuantity(): Int? = coerceWholeQuantity(quantityText, min, max)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Enter quantity", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(productName, fontSize = 14.sp, color = WholesaleMuted, maxLines = 2)
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { next ->
                        quantityText = next.filter { it.isDigit() }
                        error = null
                    },
                    label = { Text("Quantity") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null,
                )
                Text("Quick select", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = WholesaleInk4)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    quickChips.forEach { chip ->
                        val selected = quantityText == chip.toString()
                        Text(
                            chip.toString(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) Color.White else WholesaleText,
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (selected) WholesaleBlue else WholesaleSurface3)
                                    .clickable {
                                        quantityText = chip.coerceIn(min, max).toString()
                                        error = null
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
                error?.let { Text(it, fontSize = 12.sp, color = WholesaleRed) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val qty = parsedQuantity()
                    if (qty == null) {
                        error = "Enter a whole number between $min and $max"
                    } else {
                        onConfirm(qty)
                    }
                },
            ) {
                Text("Add to cart", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
