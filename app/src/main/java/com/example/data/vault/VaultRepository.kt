package com.example.data.vault

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class DecryptedVaultItem(
    val id: Long = 0,
    val title: String,
    val usernameOrEmail: String,
    val secret: String,
    val category: String,
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

class VaultRepository(private val vaultDao: VaultDao) {

    val allItems: Flow<List<DecryptedVaultItem>> = vaultDao.getAllVaultItems().map { list ->
        list.map { entity ->
            val decryptedSecret = VaultCryptoHelper.decrypt(entity.encryptedSecret, entity.iv)
            DecryptedVaultItem(
                id = entity.id,
                title = entity.title,
                usernameOrEmail = entity.usernameOrEmail,
                secret = decryptedSecret,
                category = entity.category,
                notes = entity.notes,
                updatedAt = entity.updatedAt
            )
        }
    }

    suspend fun saveItem(
        id: Long = 0,
        title: String,
        usernameOrEmail: String,
        secret: String,
        category: String,
        notes: String = ""
    ): Long {
        val encrypted = VaultCryptoHelper.encrypt(secret)
        val entity = VaultEntity(
            id = id,
            title = title,
            usernameOrEmail = usernameOrEmail,
            encryptedSecret = encrypted.ciphertextBase64,
            iv = encrypted.ivBase64,
            category = category,
            notes = notes,
            updatedAt = System.currentTimeMillis()
        )
        return if (id == 0L) {
            vaultDao.insertItem(entity)
        } else {
            vaultDao.updateItem(entity)
            id
        }
    }

    suspend fun deleteById(id: Long) {
        vaultDao.deleteById(id)
    }

    companion object {
        fun create(context: Context): VaultRepository {
            val db = VaultDatabase.getInstance(context)
            return VaultRepository(db.vaultDao())
        }
    }
}
