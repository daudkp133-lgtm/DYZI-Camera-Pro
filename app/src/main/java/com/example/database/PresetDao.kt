package com.example.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {
    @Query("SELECT * FROM presets ORDER BY isDefault DESC, name ASC")
    fun getAllPresets(): Flow<List<PresetEntity>>

    @Query("SELECT * FROM presets WHERE id = :id LIMIT 1")
    suspend fun getPresetById(id: String): PresetEntity?

    @Query("SELECT COUNT(*) FROM presets")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresets(presets: List<PresetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: PresetEntity)

    @Update
    suspend fun updatePreset(preset: PresetEntity)

    @Delete
    suspend fun deletePreset(preset: PresetEntity)

    @Query("UPDATE presets SET isDefault = 0")
    suspend fun clearDefaultPreset()

    @Query("UPDATE presets SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultPreset(id: String)
}
