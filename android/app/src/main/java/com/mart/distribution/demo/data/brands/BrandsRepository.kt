package com.mart.distribution.demo.data.brands

import com.mart.distribution.demo.data.api.MartApi
import com.mart.distribution.demo.data.api.dto.Brand
import com.mart.distribution.demo.data.api.dto.CreateBrandRequest
import com.mart.distribution.demo.data.demo.LocalDemoMartStore
import com.mart.distribution.demo.data.session.SessionRepository

class BrandsRepository(
    private val martApi: MartApi,
    private val sessionRepository: SessionRepository,
    private val localDemoMartStore: LocalDemoMartStore,
) {
    suspend fun list(): List<Brand> {
        return if (sessionRepository.isLocalDemoMode()) {
            localDemoMartStore.brands()
        } else {
            martApi.brands()
        }
    }

    suspend fun create(
        name: String,
        logoUrl: String?,
    ): Brand {
        val payload = CreateBrandRequest(name = name.trim(), logoUrl = logoUrl?.trim()?.takeIf { it.isNotEmpty() })
        return if (sessionRepository.isLocalDemoMode()) {
            localDemoMartStore.createBrand(payload)
        } else {
            martApi.createBrand(payload)
        }
    }

    suspend fun delete(id: String): Brand {
        return if (sessionRepository.isLocalDemoMode()) {
            localDemoMartStore.deleteBrand(id)
        } else {
            martApi.deleteBrand(id)
        }
    }
}
