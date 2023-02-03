package com.denox.yugitournament.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TournamentEntry::class, PlayerEntry::class, StandingsEntry::class,
    PairingsEntry::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun tournamentDao(): TournamentDao
    abstract fun playersDao(): PlayersDao
    abstract fun standingsDao(): StandingsDao
    abstract fun pairingsDao(): PairingsDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE players ADD skippedRounds TEXT NOT NULL DEFAULT '[]'")
            }
        }
    }

}
