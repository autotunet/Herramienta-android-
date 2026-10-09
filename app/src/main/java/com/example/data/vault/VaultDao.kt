package com.example.data.vault

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Query("SELECT * FROM encrypted_vault ORDER BY updatedAt DESC")
    fun getAllVaultItems(): Flow<List<VaultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: VaultEntity): Long

    @Update
    suspend fun updateItem(item: VaultEntity)

    @Delete
    suspend fun deleteItem(item: VaultEntity)

    @Query("DELETE FROM encrypted_vault WHERE id = :id")
    suspend fun deleteById(id: Long)
}
