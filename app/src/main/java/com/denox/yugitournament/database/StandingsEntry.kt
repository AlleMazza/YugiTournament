package com.denox.yugitournament.database

import androidx.room.Entity

@Entity(tableName = "Standings", primaryKeys = ["tournamentId", "index"])
data class StandingsEntry(
    val tournamentId: Int,
    val index: Int,
    val seedsList: List<Int>,
    val namesList: List<String>,
    val pointsList: List<Int>,
    val tiebreakerList: List<Long>,
)
