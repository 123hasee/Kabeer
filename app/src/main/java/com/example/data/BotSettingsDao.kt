package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BotSettingsDao {
  @Query("SELECT * FROM bot_settings WHERE id = 1 LIMIT 1")
  fun getSettings(): Flow<BotSettingsEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(settings: BotSettingsEntity)

  @Update
  suspend fun updateSettings(settings: BotSettingsEntity)
}
