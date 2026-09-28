package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class RoutineEntity(
  @PrimaryKey val dateString: String, // "yyyy-MM-dd"
  val hydrateDone: Boolean = false,
  val exerciseDone: Boolean = false,
  val meditateDone: Boolean = false,
  val chartPrepDone: Boolean = false,
  val alertsSet: Boolean = false,
  val tradeWindowObserved: Boolean = false,
  val midDayBreakDone: Boolean = false,
  val eodJournalDone: Boolean = false,
  val eodMarkupDone: Boolean = false,
  val mindsetRating: Int = 5, // 1 to 5
  val reflectionNotes: String = ""
) {
  val completionPercent: Int
    get() {
      val checks = listOf(
        hydrateDone, exerciseDone, meditateDone,
        chartPrepDone, alertsSet, tradeWindowObserved,
        midDayBreakDone, eodJournalDone, eodMarkupDone
      )
      val done = checks.count { it }
      return (done * 100) / checks.size
    }
}
