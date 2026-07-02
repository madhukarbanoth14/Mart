package com.mart.distribution.demo.ui.onboarding

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.mart.distribution.demo.ui.theme.MartFieldDefaults
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Collects the counterparty's shop/business name, postal address and (optionally)
 * their current GPS location. Hoisted state so the parent onboarding screen owns
 * the values and submits them.
 */
@Composable
fun OnboardingShopLocationSection(
    shopName: String,
    onShopNameChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    latitude: Double?,
    longitude: Double?,
    onLocationCaptured: (Double?, Double?) -> Unit,
    modifier: Modifier = Modifier,
    shopNameLabel: String = "Shop / business name (optional)",
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var capturing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    fun captureLocation() {
        capturing = true
        isError = false
        statusMessage = "Getting current location…"
        scope.launch {
            try {
                val loc = awaitCurrentLocation(context)
                if (loc != null) {
                    onLocationCaptured(loc.latitude, loc.longitude)
                    isError = false
                    statusMessage = null
                } else {
                    isError = true
                    statusMessage = "Couldn't get a location fix. Make sure GPS is on and try again."
                }
            } catch (_: SecurityException) {
                isError = true
                statusMessage = "Location permission is required to capture the location."
            } catch (_: Exception) {
                isError = true
                statusMessage = "Couldn't get the location. Try again."
            } finally {
                capturing = false
            }
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { grants ->
            val granted =
                grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                captureLocation()
            } else {
                isError = true
                statusMessage = "Location permission denied. You can still enter the address manually."
            }
        }

    fun onUseLocationClick() {
        if (capturing) return
        if (hasLocationPermission(context)) {
            captureLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = shopName,
            onValueChange = onShopNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(shopNameLabel) },
            singleLine = true,
            keyboardOptions = MartFieldDefaults.englishTextKeyboard,
            colors = MartFieldDefaults.outlinedColors(),
        )
        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Address (optional)") },
            placeholder = { Text("Shop no, street, area, city, pincode") },
            keyboardOptions = MartFieldDefaults.englishMultilineKeyboard,
            minLines = 2,
            colors = MartFieldDefaults.outlinedColors(),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { onUseLocationClick() }, enabled = !capturing) {
                Icon(Icons.Outlined.MyLocation, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (capturing) "Getting location…" else "Use current location")
            }
        }
        val captured = latitude != null && longitude != null
        when {
            statusMessage != null ->
                Text(
                    statusMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        if (isError) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            captured ->
                Text(
                    "Location captured: ${"%.5f".format(latitude ?: 0.0)}, ${"%.5f".format(longitude ?: 0.0)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            else ->
                Text(
                    "Optional: capture the shop location to share with the delivery partner.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

private suspend fun awaitCurrentLocation(context: Context): Location? =
    suspendCancellableCoroutine { cont ->
        val client = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()
        try {
            client
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        if (cont.isActive) cont.resume(loc)
                    } else {
                        // Fall back to the last known location if no fresh fix.
                        client.lastLocation
                            .addOnSuccessListener { last -> if (cont.isActive) cont.resume(last) }
                            .addOnFailureListener { if (cont.isActive) cont.resume(null) }
                    }
                }
                .addOnFailureListener { e -> if (cont.isActive) cont.resumeWithExceptionSafe(e) }
        } catch (e: SecurityException) {
            if (cont.isActive) cont.resumeWithExceptionSafe(e)
        }
        cont.invokeOnCancellation { cts.cancel() }
    }

private fun <T> kotlinx.coroutines.CancellableContinuation<T>.resumeWithExceptionSafe(
    e: Throwable,
) {
    resumeWith(Result.failure(e))
}
