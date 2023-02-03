package com.denox.yugitournament.database

import androidx.room.Entity

@Entity(tableName = "Players", primaryKeys = ["tournamentId", "seed"])
data class PlayerEntry(
    val tournamentId: Int,
    val seed: Int,
    val name: String,
    val matchHistory: List<Int>,
    val isDropped: Boolean,
    val randomSeed: Int,
    val skippedRounds: List<Int>,
)
