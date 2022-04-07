package com.denox.yugitournament.database

import androidx.room.*

@Dao
interface TournamentDao {

    @Query("SELECT * FROM tournaments WHERE id=:tournamentId")
    fun getTournament(tournamentId: Int): List<TournamentEntry>

    @Query("SELECT * FROM tournaments")
    fun getAllTournaments(): List<TournamentEntry>

    @Delete
    fun deleteTournaments(vararg tournaments: TournamentEntry): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTournament(vararg tournaments: TournamentEntry)

}

@Dao
interface PlayersDao {

    @Query("SELECT * FROM players WHERE tournamentId=:tournamentId")
    fun getPlayers(tournamentId: Int): List<PlayerEntry>

    @Query("DELETE FROM players WHERE tournamentId=:tournamentId")
    fun deletePlayers(tournamentId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlayers(vararg players: PlayerEntry)

}

@Dao
interface StandingsDao {

    @Query("SELECT * FROM standings WHERE tournamentId=:tournamentId")
    fun getStandings(tournamentId: Int): List<StandingsEntry>

    @Query("DELETE FROM standings WHERE tournamentId=:tournamentId")
    fun deleteStandings(tournamentId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStandings(vararg standings: StandingsEntry)

}

@Dao
interface PairingsDao {

    @Query("SELECT * FROM pairings WHERE tournamentId=:tournamentId")
    fun getPairings(tournamentId: Int): List<PairingsEntry>

    @Query("DELETE FROM pairings WHERE tournamentId=:tournamentId")
    fun deletePairings(tournamentId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPairings(vararg pairings: PairingsEntry)

}
