package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
  @Query("SELECT * FROM routines ORDER BY dateString DESC")
  fun getAllRoutines(): Flow<List<RoutineEntity>>

  @Query("SELECT * FROM routines WHERE dateString = :dateString LIMIT 1")
  fun getRoutineForDate(dateString: String): Flow<RoutineEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateRoutine(routine: RoutineEntity)

  @Update
  suspend fun updateRoutine(routine: RoutineEntity)
}
