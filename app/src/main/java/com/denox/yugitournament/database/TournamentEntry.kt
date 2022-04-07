package com.denox.yugitournament.database

import androidx.room.Entity
import java.util.*

@Entity(tableName = "Tournaments", primaryKeys = ["id", "date"])
data class TournamentEntry(
    val id: Int,
    val date: Date,
    val currentRound: Int,
)
