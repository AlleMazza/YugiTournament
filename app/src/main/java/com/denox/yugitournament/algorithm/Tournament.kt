package com.denox.yugitournament.algorithm

import androidx.lifecycle.MutableLiveData
import com.denox.yugitournament.database.*
import java.util.*
import kotlin.random.Random

class Tournament(val id: Int = Random.nextInt(), val date: Date) {
    var players = mutableMapOf<Int, Player>()
    var standingsHistory = mutableListOf<List<PlayerRanking>>()
    var pairingsHistory = mutableListOf<List<Pair<Int, Int>>>()
    var currentRound = 0
    val callStandingsUpdate = MutableLiveData(false)

    fun addPlayer(name: String = "placeholder"): Player {
        val newKey = players.keys.maxOrNull()?.plus(1) ?: 1
        val newPlayer = Player(newKey, name)
        players[newKey] = newPlayer
        //byePresent = (players.size % 2 == 1)
        return newPlayer
    }

    fun removePlayer(seed: Int) = players.remove(seed)
    fun removePlayer(player: Player) = removePlayer(player.seed)

    fun dropPlayer(player: Player): Boolean {
        player.isDropped = true
        return true
    }
    fun dropPlayer(seed: Int): Boolean {
        players[seed]?.let { return dropPlayer(it) }
        return false
    }

    fun undropPlayer(seed: Int) = players[seed]?.let {
        if ((it.getMatchHistorySize() == currentRound) ||
                (it.getMatchHistorySize() == currentRound &&
                        let lambda@ { _ ->
                            val lastPairing = pairingsHistory.last().find { pair ->
                                pair.first == it.seed || pair.second == it.seed
                            }
                            return@lambda when {
                                lastPairing?.first == it.seed -> lastPairing.second
                                lastPairing?.second == it.seed -> lastPairing.first
                                else -> null
                            }
                        }?.let { opponent -> it.getResultAgainst(opponent) == null } == true
                        ))
                            it.isDropped = false
        else
            return false
    } != null

    fun playerStandings(): List<PlayerRanking> {
        val playersRanked = players.values.filter { !it.isDropped }
            .sortedBy { it.randomSeed }.map { player ->
                val points = player.points()
                val opponentWR = getOpponentWR(player.seed)
                val opponent2WR = getOpponent2WR(player.seed)
                PlayerRanking(player.seed, player.name, points,
                    buildTiebreaker(points, opponentWR, opponent2WR))
            }
        return playersRanked.sortedByDescending { it.tiebreaker }
    }

    private fun startTournament(keepSeeding: Boolean = false) {
        currentRound = 0
        val randomizedSeeds = MutableList(players.size) { it+1 }
        players.forEach { ( _, player ) ->
            player.dropAllResults()
            if (keepSeeding) { player.randomSeed = player.seed }
            else { player.randomSeed = randomizedSeeds.random() }
            randomizedSeeds.remove(player.randomSeed)
        }
        nextRound()
    }

    private fun nextRound() {
        if (currentRound != 0) { standingsHistory.add(playerStandings()) }
        ++currentRound
        val newPairings =
            if (standingsHistory.isNotEmpty())
                generatePairingsByRanking(standingsHistory.last().filter {
                    players[it.seed]?.isDropped == false
                })
            else
                generatePairings(players.filter { !it.value.isDropped }.map { it.value }
                    .sortedBy { it.randomSeed })
        pairingsHistory.add(newPairings)
        newPairings.forEach {
            if (it.first < 0) { setResult(it.first, it.second, 0) }
            if (it.second < 0) { setResult(it.first, it.second, 3) }
        }
    }

    fun nextRoundOrStart(keepSeeding: Boolean = false) = when (currentRound) {
        0 -> startTournament(keepSeeding)
        else -> nextRound()
    }

    fun cancelLastRound() { // TODO button to call this (with "are you sure?")
        if (currentRound < 1) { return }
        --currentRound
        players.forEach { ( _ , player ) ->
            if (player.getMatchHistorySize() > currentRound) {
                player.dropLastResult()
            }
        }
        if (standingsHistory.isNotEmpty()) { standingsHistory.removeLast() }
        pairingsHistory.removeLast()
    }

    private fun generatePairingsByRanking(standings: List<PlayerRanking>) =
        generatePairings(standings.mapNotNull { players[it.seed] })
    private fun generatePairings(players: List<Player>): List<Pair<Int, Int>> {
        fun recursivePairings(players: List<Player>): List<Pair<Int, Int>>? {
            if (players.isEmpty()) { return emptyList() }
            if (players.size < 2) {
                if (players[0].receivedBye() > 0) { return null }
                return listOf(Pair(players[0].seed, -1))
            }
            for (i in 1 until players.size) {
                if (players[0].getResultAgainst(players[i].seed) == null) {
                    val newPair = Pair(players[0].seed, players[i].seed)
                    if (players.size > 2) {
                        val remainingPlayers = players.filterIndexed { ix, _ -> ix != 0 && ix != i }
                        val recursiveResult = recursivePairings(remainingPlayers)
                        if (recursiveResult != null) {
                            return (recursiveResult + newPair)
                        }
                    }
                    else {
                        return listOf(newPair)
                    }
                }
            }
            return null
        }
        val tryPairings = recursivePairings(players)
        if (tryPairings != null) { return tryPairings }
        // TODO print warning (toast): no possible combinations
        val pairings = mutableListOf<Pair<Int, Int>>()
        for (i in players.indices step 2) {
            pairings.add(Pair(players[i].seed, players[i+1].seed))
        }
        if (pairings.size * 2 != players.size) {
            pairings.add(Pair(players.last().seed, -1))
        }
        return pairings
    }

    private fun getOpponentWR(seed: Int): Double {
        var opponents = 0
        var winRatesTotal = 0.0
        players[seed]?.getResultsList()?.forEach { ( seed, _ ) ->
            if (seed == -1) { return@forEach }
            winRatesTotal +=
                    if (players[seed]?.isDropped != false)
                        players[seed]?.winRate() ?: 0.0
                    else
                        players[seed]?.winRate(currentRound) ?: 0.0
            ++opponents
        } ?: return 0.0
        return if (opponents == 0) 0.0 else (winRatesTotal/opponents)
    }

    private fun getOpponent2WR(seed: Int): Double {
        var opponents = 0
        var winRatesTotal = 0.0
        players[seed]?.getResultsList()?.forEach { ( seed, _ ) ->
            if (seed == -1) { return@forEach }
            winRatesTotal += getOpponentWR(seed)
            ++opponents
        } ?: return 0.0
        return if (opponents == 0) 0.0 else (winRatesTotal/opponents)
    }

    private fun buildTiebreaker(points: Int, opponentWR: Double, opponent2WR: Double): Long =
        points * 1000000 + (opponentWR*1000).toLong() * 1000 + (opponent2WR*1000).toLong()

    fun setResult(seed1: Int, seed2: Int, result1: Int): Boolean {
        if (result1 != 0 && result1 != 1 && result1 != 3) { return false }
        players[seed1]?.changeResult(seed2, result1)
        players[seed2]?.changeResult(seed1, when (result1) { 0 -> 3; 1 -> 1; 3 -> 0; else -> -1 })
        callStandingsUpdate.postValue(true)
        return true
    }

    fun getResult(seed1: Int, seed2: Int) = players[seed1]?.getResultAgainst(seed2)

    fun getLastPairings() =
            if (pairingsHistory.isEmpty()) null
            else pairingsHistory.last()

    fun isClear() = players.isEmpty() && pairingsHistory.isEmpty() &&
            standingsHistory.isEmpty() && currentRound == 0

    fun toTournamentEntry(database: AppDatabase?)
    : TournamentEntry {
        if (database != null) {
            database.playersDao().insertPlayers(
                *players.map { it.value.toPlayerEntry(id) }.toTypedArray())
            database.standingsDao().insertStandings(
                *standingsHistory.mapIndexed { index, list ->
                    StandingsEntry(
                        id,
                        index,
                        list.map { it.seed },
                        list.map { it.name },
                        list.map { it.points },
                        list.map { it.tiebreaker }
                    )
                }.toTypedArray()
            )
            database.pairingsDao().insertPairings(
                *pairingsHistory.mapIndexed { index, list ->
                    PairingsEntry(
                        id,
                        index,
                        list.map { it.first },
                        list.map { it.second }
                    )
                }.toTypedArray()
            )
        }
        return TournamentEntry(
            id,
            date,
            currentRound,
        )
    }

    fun saveTournament(database: AppDatabase) {
        database.tournamentDao().insertTournament(toTournamentEntry(database))
    }

    fun insertPlayers(list: List<PlayerEntry>) {
        players.clear()
        list.forEach { it1 -> Player.fromPlayerEntry(it1).let { it2 -> players[it2.seed] } }
    }

    fun insertStandings(list: List<StandingsEntry>) {
        standingsHistory.clear()
        list.sortedBy { it.index }.forEach {
            standingsHistory.add(it.seedsList.indices.map { i ->
                PlayerRanking(
                    it.seedsList[i],
                    it.namesList[i],
                    it.pointsList[i],
                    it.tiebreakerList[i],
                )
            })
        }
    }

    fun insertPairings(list: List<PairingsEntry>) {
        pairingsHistory.clear()
        list.sortedBy { it.index }.forEach {
            pairingsHistory.add(it.player1List.indices.map { i ->
                Pair(it.player1List[i], it.player2List[i])
            })
        }
    }

    companion object {

        fun fromTournamentEntry(te: TournamentEntry, database: AppDatabase?) = Tournament(
            te.id,
            te.date
        ).apply {
            currentRound = te.currentRound
            if (database != null) {
                insertPlayers(database.playersDao().getPlayers(id))
                insertStandings(database.standingsDao().getStandings(id))
                insertPairings(database.pairingsDao().getPairings(id))
            }
        }

        fun newTournament() = Tournament(date = Calendar.getInstance().time)

    }

}
