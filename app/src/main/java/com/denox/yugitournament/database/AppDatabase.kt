package com.denox.yugitournament.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TournamentEntry::class, PlayerEntry::class, StandingsEntry::class,
    PairingsEntry::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun tournamentDao(): TournamentDao
    abstract fun playersDao(): PlayersDao
    abstract fun standingsDao(): StandingsDao
    abstract fun pairingsDao(): PairingsDao
}
