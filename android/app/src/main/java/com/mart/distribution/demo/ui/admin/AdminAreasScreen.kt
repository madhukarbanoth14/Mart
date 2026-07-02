package com.mart.distribution.demo.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mart.distribution.demo.data.api.dto.AreaDto
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
import com.mart.distribution.demo.ui.flashmart.FmSectionLabel
import com.mart.distribution.demo.ui.flashmart.FmSuccessBanner
import com.mart.distribution.demo.ui.flashmart.FmTextField
import com.mart.distribution.demo.ui.theme.FmSpacing
import com.mart.distribution.demo.ui.theme.MartFieldDefaults

@Composable
fun AdminAreasScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel,
) {
    val ui by mainViewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { mainViewModel.loadAreas() }

    var newArea by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }
    var editingArea by remember { mutableStateOf<AreaDto?>(null) }
    var editName by remember { mutableStateOf("") }
    var editBusy by remember { mutableStateOf(false) }

    val areas =
        when (val a = ui.areas) {
            is LoadState.Ok -> a.data
            else -> emptyList()
        }

    FmScreen(
        title = "Manage areas",
        subtitle = "Territories for dealers & shopkeepers",
        onBack = { navController.popBackStack() },
    ) { modifier ->
        Column(
            modifier =
                modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(FmSpacing.itemGap),
        ) {
            FmInfoBanner(
                message = "Define the territories your team assigns when onboarding dealers and shopkeepers.",
            )
            FmCard(modifier = Modifier.fillMaxWidth()) {
                FmSectionLabel(title = "Add territory")
                FmTextField(
                    value = newArea,
                    onValueChange = { newArea = it; error = null; info = null },
                    label = "Area name",
                    placeholder = "e.g. Warangal",
                    keyboardOptions = MartFieldDefaults.englishTextKeyboard,
                    error = error,
                )
                Spacer(Modifier.height(12.dp))
                FmButton(
                    text = if (busy) "Adding…" else "Add area",
                    onClick = {
                        if (busy) return@FmButton
                        busy = true
                        error = null
                        info = null
                        mainViewModel.createArea(newArea) { err ->
                            busy = false
                            if (err != null) {
                                error = err
                            } else {
                                info = "Area \"${newArea.trim()}\" added."
                                newArea = ""
                            }
                        }
                    },
                    enabled = !busy,
                )
            }
            info?.let { FmSuccessBanner(message = it) }
            FmSectionLabel(title = "Existing areas")
            when (val a = ui.areas) {
                is LoadState.Loading -> FmLoadingState(message = "Loading areas…")
                is LoadState.Err -> FmErrorBanner(message = a.message)
                else ->
                    if (areas.isEmpty()) {
                        FmEmptyState(
                            icon = Icons.Outlined.Map,
                            title = "No areas configured",
                            message = "Add your first territory above. Employees will assign dealers and shopkeepers to these areas.",
                        )
                    } else {
                        areas.forEach { ar ->
                            FmCard(modifier = Modifier.fillMaxWidth()) {
                                FmDataRow(
                                    title = ar.name,
                                    subtitle =
                                        ar.dealer?.name?.let { "Assigned dealer · $it" }
                                            ?: "No dealer assigned",
                                )
                                Spacer(Modifier.height(10.dp))
                                FmButton(
                                    text = "Rename",
                                    onClick = {
                                        editingArea = ar
                                        editName = ar.name
                                        error = null
                                        info = null
                                    },
                                    variant = FmButtonVariant.Outline,
                                )
                            }
                        }
                    }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    editingArea?.let { area ->
        FmDialog(
            title = "Rename area",
            onDismiss = { if (!editBusy) editingArea = null },
            confirmLabel = "Save",
            confirmBusy = editBusy,
            onConfirm = {
                if (editBusy) return@FmDialog
                editBusy = true
                error = null
                mainViewModel.updateArea(area.id, editName) { err ->
                    editBusy = false
                    if (err != null) {
                        error = err
                    } else {
                        info = "Area renamed to \"${editName.trim()}\"."
                        editingArea = null
                    }
                }
            },
        ) {
            FmTextField(
                value = editName,
                onValueChange = { editName = it; error = null },
                label = "Area name",
                placeholder = "e.g. Warangal",
                keyboardOptions = MartFieldDefaults.englishTextKeyboard,
                error = error,
            )
        }
    }
}
