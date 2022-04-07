package com.denox.yugitournament.database

import androidx.room.Entity

@Entity(tableName = "Pairings", primaryKeys = ["tournamentId", "index"])
data class PairingsEntry(
    val tournamentId: Int,
    val index: Int,
    val player1List: List<Int>,
    val player2List: List<Int>,
)
