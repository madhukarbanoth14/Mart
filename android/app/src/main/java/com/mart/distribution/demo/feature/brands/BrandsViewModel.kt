package com.mart.distribution.demo.feature.brands

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mart.distribution.demo.data.api.dto.Brand
import com.mart.distribution.demo.data.brands.BrandsRepository
import com.mart.distribution.demo.feature.home.LoadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BrandsUiState(
    val brands: LoadState<List<Brand>> = LoadState.Idle,
    val selectedBrandId: String? = null,
    val actionError: String? = null,
)

class BrandsViewModel(
    private val repository: BrandsRepository,
) : ViewModel() {
    private val _ui = MutableStateFlow(BrandsUiState())
    val uiState: StateFlow<BrandsUiState> = _ui.asStateFlow()

    fun loadBrands(force: Boolean = false) {
        if (!force && _ui.value.brands is LoadState.Ok) return
        viewModelScope.launch {
            val showLoading = force || _ui.value.brands !is LoadState.Ok
            if (showLoading) {
                _ui.update { it.copy(brands = LoadState.Loading, actionError = null) }
            }
            try {
                val rows = repository.list()
                _ui.update { prev ->
                    val selected = prev.selectedBrandId?.takeIf { id -> rows.any { it.id == id } }
                    prev.copy(brands = LoadState.Ok(rows), selectedBrandId = selected)
                }
            } catch (e: Exception) {
                if (_ui.value.brands !is LoadState.Ok) {
                    _ui.update { it.copy(brands = LoadState.Err(e.message ?: "Failed to load brands")) }
                }
            }
        }
    }

    fun selectBrand(id: String?) {
        _ui.update { it.copy(selectedBrandId = id) }
    }

    fun createBrand(
        name: String,
        logoUrl: String?,
        onDone: (String?) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                repository.create(name, logoUrl)
                loadBrands(force = true)
                onDone(null)
            } catch (e: Exception) {
                val msg = e.message ?: "Could not create brand"
                _ui.update { it.copy(actionError = msg) }
                onDone(msg)
            }
        }
    }

    fun deleteBrand(
        id: String,
        onDone: (String?) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                repository.delete(id)
                loadBrands(force = true)
                onDone(null)
            } catch (e: Exception) {
                val msg = e.message ?: "Could not delete brand"
                _ui.update { it.copy(actionError = msg) }
                onDone(msg)
            }
        }
    }
}
