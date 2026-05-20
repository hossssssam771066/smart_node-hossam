package com.smartnode.app.di

import com.smartnode.app.data.repository.AssetRepositoryImpl
import com.smartnode.app.data.repository.IdentityRepositoryImpl
import com.smartnode.app.data.repository.TransactionLogRepositoryImpl
import com.smartnode.app.domain.repository.AssetRepository
import com.smartnode.app.domain.repository.IdentityRepository
import com.smartnode.app.domain.repository.TransactionLogRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds domain-layer repository interfaces to their data-layer implementations.
 * Use @Binds (not @Provides) so the call sites can depend on the abstract
 * contract and Hilt picks the impl at injection time.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindIdentityRepository(impl: IdentityRepositoryImpl): IdentityRepository

    @Binds
    @Singleton
    abstract fun bindAssetRepository(impl: AssetRepositoryImpl): AssetRepository

    @Binds
    @Singleton
    abstract fun bindTransactionLogRepository(impl: TransactionLogRepositoryImpl): TransactionLogRepository
}
