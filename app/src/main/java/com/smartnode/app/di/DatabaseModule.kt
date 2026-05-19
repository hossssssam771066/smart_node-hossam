package com.smartnode.app.di

import android.content.Context
import androidx.room.Room
import com.smartnode.app.data.local.SmartNodeDatabase
import com.smartnode.app.data.security.DatabaseKeyManager
import com.smartnode.app.data.security.KeystoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.sqlcipher.database.SupportFactory

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideKeystoreManager(): KeystoreManager = KeystoreManager()

    @Provides
    @Singleton
    fun provideDatabaseKeyManager(
        @ApplicationContext context: Context,
        keystoreManager: KeystoreManager,
    ): DatabaseKeyManager = DatabaseKeyManager(context, keystoreManager)

    @Provides
    @Singleton
    fun provideSmartNodeDatabase(
        @ApplicationContext context: Context,
        keyManager: DatabaseKeyManager,
    ): SmartNodeDatabase {
        val passphrase = keyManager.getOrCreatePassphrase()
        val factory = SupportFactory(passphrase)
        return Room.databaseBuilder(
            context,
            SmartNodeDatabase::class.java,
            SmartNodeDatabase.NAME,
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }
}
