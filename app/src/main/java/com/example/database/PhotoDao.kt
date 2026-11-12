package com.example.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos ORDER BY timestampMillis DESC")
    fun getAllPhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE isFavorite = 1 ORDER BY timestampMillis DESC")
    fun getFavoritePhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE isEdited = 1 ORDER BY timestampMillis DESC")
    fun getEditedPhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE isEdited = 0 ORDER BY timestampMillis DESC")
    fun getCameraPhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE id = :id LIMIT 1")
    suspend fun getPhotoById(id: Long): PhotoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity): Long

    @Update
    suspend fun updatePhoto(photo: PhotoEntity)

    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)

    @Query("UPDATE photos SET isFavorite = :isFav WHERE id = :id")
    suspend fun setFavorite(id: Long, isFav: Boolean)
}
