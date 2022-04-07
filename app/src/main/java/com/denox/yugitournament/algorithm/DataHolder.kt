package com.denox.yugitournament.algorithm

import com.denox.yugitournament.database.AppDatabase
import java.util.*

data class DataHolder(
    var tournament: Tournament = Tournament.newTournament(),
    var database: AppDatabase,
)
