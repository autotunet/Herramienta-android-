package com.example.data.vault

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "encrypted_vault")
data class VaultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val usernameOrEmail: String,
    val encryptedSecret: String, // Base64 ciphertext
    val iv: String,              // Base64 Initialization Vector
    val category: String,        // "Contraseña", "Nota Secreta", "API Key", "Tarjeta", "Credencial"
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
