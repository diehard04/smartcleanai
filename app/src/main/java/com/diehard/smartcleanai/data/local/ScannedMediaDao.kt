package com.diehard.smartcleanai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class MediaCacheCheck(
    val mediaId: Long,
    val dateModified: Long
)

@Dao
interface ScannedMediaDao {

    @Query("SELECT * FROM scanned_media WHERE isIgnored = 0")
    fun getAllActiveMedia(): Flow<List<ScannedMediaEntity>>

    @Query("SELECT * FROM scanned_media")
    suspend fun getAllEntitiesDirect(): List<ScannedMediaEntity>

    @Query("SELECT mediaId, dateModified FROM scanned_media")
    suspend fun getAllCacheChecks(): List<MediaCacheCheck>

    @Query("SELECT * FROM scanned_media WHERE category = :category AND isIgnored = 0")
    fun getMediaByCategory(category: String): Flow<List<ScannedMediaEntity>>

    @Query("SELECT * FROM scanned_media WHERE category = :category AND isIgnored = 0")
    suspend fun getMediaByCategoryDirect(category: String): List<ScannedMediaEntity>

    @Query("SELECT * FROM scanned_media WHERE isIgnored = 1")
    fun getIgnoredMedia(): Flow<List<ScannedMediaEntity>>

    @Query("SELECT * FROM scanned_media WHERE mediaId = :mediaId LIMIT 1")
    suspend fun getEntityById(mediaId: Long): ScannedMediaEntity?

    @Query("SELECT * FROM scanned_media WHERE mediaId IN (:ids)")
    suspend fun getEntitiesByIds(ids: List<Long>): List<ScannedMediaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ScannedMediaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ScannedMediaEntity)

    @Update
    suspend fun update(entity: ScannedMediaEntity)

    @Query("UPDATE scanned_media SET isIgnored = :isIgnored WHERE mediaId = :mediaId")
    suspend fun updateIgnored(mediaId: Long, isIgnored: Boolean)

    @Query("DELETE FROM scanned_media WHERE mediaId IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM scanned_media")
    suspend fun clearAll()
}
