package com.neonsaya.setucompose.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: Favorite)

    @Delete
    suspend fun delete(favorite: Favorite)

    @Query("SELECT * FROM favorites ORDER BY pid DESC")
    fun getAllFavorites(): Flow<List<Favorite>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE pid = :pid)")
    fun isFavorite(pid: Long): Flow<Boolean>

    @Query("SELECT * FROM favorites WHERE pid IN (:pids)")
    suspend fun getFavoritesByPids(pids: List<Long>): List<Favorite>

    @Query("SELECT * FROM favorites WHERE pid = :pid")
    suspend fun getFavoriteByPid(pid: Long): Favorite?

    @Query("DELETE FROM favorites WHERE pid = :pid")
    suspend fun deleteByPid(pid: Long)

    @Query("DELETE FROM favorites WHERE pid IN (:pids)")
    suspend fun deleteByPids(pids: List<Long>)
}
