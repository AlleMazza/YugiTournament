package com.denox.yugitournament.algorithm

import com.denox.yugitournament.database.*
import com.denox.yugitournament.ui.fragment.PlayerPairingsFragment
import com.denox.yugitournament.ui.fragment.PlayerRegistrationFragment
import com.denox.yugitournament.ui.fragment.PlayerStandingsFragment
import java.util.*
import kotlin.random.Random

class Tournament(val id: Int = Random.nextInt(), val date: Date) {
    var players = mutableMapOf<Int, Player>()
    var standingsHistory = mutableListOf<List<PlayerRanking>>()
    var pairingsHistory = mutableListOf<List<Pair<Int, Int>>>()
    var currentRound = 0
    var callRegistrationFragment: PlayerRegistrationFragment? = null
    var callPairingsFragment: PlayerPairingsFragment? = null
    var callStandingsFragment: PlayerStandingsFragment? = null

    fun addPlayer(name: String = "placeholder", addToNextRound: Boolean = true): Player {
        val newKey = players.keys.maxOrNull()?.plus(1) ?: 1
        val newPlayer = Player(newKey, name)
        players[newKey] = newPlayer

        if (currentRound > 0) {
            val availableRandomSeeds = MutableList(players.size) { (it*3)+1 }
            var maxRandomSeed = 0
            players.forEach { ( _ , player ) ->
                availableRandomSeeds.remove(player.randomSeed)
                if (player.randomSeed > maxRandomSeed) {
                    maxRandomSeed = player.randomSeed
                }
            }
            newPlayer.randomSeed =
                if (availableRandomSeeds.isNotEmpty()) availableRandomSeeds.random()
                else maxRandomSeed + Random.nextInt(1, 10)
            for (i in 0 until (currentRound - (if (addToNextRound) 0 else 1))) {
                newPlayer.skipRound()
            }
            if (!addToNextRound) {
                val lastPairings = pairingsHistory.last().toMutableList()
                val byeIndex = lastPairings.indexOfFirst { it.first == -1 || it.second == -1 }
                if (byeIndex < 0) {
                    lastPairings.add(Pair(newKey, -1))
                    setResult(newKey, -1, 3)
                }
                else {
                    if (lastPairings[byeIndex].first == -1) {
                        lastPairings[byeIndex] = Pair(newKey, lastPairings[byeIndex].second)
                        players[lastPairings[byeIndex].second]?.dropLastResult()
                    }
                    else {
                        lastPairings[byeIndex] = Pair(lastPairings[byeIndex].first, newKey)
                        players[lastPairings[byeIndex].first]?.dropLastResult()
                    }
                }
                pairingsHistory[pairingsHistory.size - 1] = lastPairings
                callPairingsFragment?.loadTournament()
            }
            callStandingsFragment?.loadTournament()
        }

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

    fun undropPlayer(player: Player): Boolean {
        player.isDropped = false
        return true
    }
    fun undropPlayer(seed: Int) = players[seed]?.let { undropPlayer(it) } ?: false

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
        val randomizedSeeds = MutableList(players.size) { (it*3)+1 }
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
        players.forEach { ( _ , player ) ->
            if (player.isDropped) {
                player.skipRound()
            }
        }
        callRegistrationFragment?.checkEnabledButtons()
    }

    fun nextRoundOrStart(keepSeeding: Boolean = false) = when (currentRound) {
        0 -> startTournament(keepSeeding)
        else -> nextRound()
    }

    fun cancelLastRound() {
        if (currentRound < 1) { return }
        --currentRound
        players.forEach { ( _ , player ) ->
            if (player.getMatchHistorySize() > currentRound) {
                player.dropLastResult()
            }
        }
        if (standingsHistory.isNotEmpty()) { standingsHistory.removeLast() }
        pairingsHistory.removeLast()
        callRegistrationFragment?.checkEnabledButtons()
    }

    private fun generatePairingsByRanking(standings: List<PlayerRanking>) =
        generatePairings(standings.mapNotNull { players[it.seed] })
    private fun generatePairings(players: List<Player>): List<Pair<Int, Int>> {
        fun recursivePairings(players: List<Player>): List<Pair<Int, Int>>? {
            if (players.isEmpty()) { return emptyList() }
            if (players.size % 2 != 0) {
                for (i in (players.size - 1) downTo 0) {
                    if (players[i].receivedBye() <= 0) {
                        val newPair = Pair(players[i].seed, -1)
                        val remainingPlayers = players.filterIndexed { ix, _ -> ix != i }
                        val recursiveResult = recursivePairings(remainingPlayers)
                        if (recursiveResult != null) {
                            return (recursiveResult + newPair)
                        }
                    }
                }
                return null
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

        val tryPairings = recursivePairings(players)?.reversed()
        if (tryPairings != null) { return tryPairings }
        callPairingsFragment?.maximumPairingsExceeded()
        val pairings = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until (players.size-1) step 2) {
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
        players[seed]?.getResultsList()?.forEach { pair ->
            val pairSeed = pair?.first ?: return@forEach
            if (pairSeed == -1) { return@forEach }
            winRatesTotal +=
                    if (players[pairSeed]?.isDropped != false)
                        players[pairSeed]?.winRate() ?: 0.0
                    else
                        players[pairSeed]?.winRate(currentRound) ?: 0.0
            ++opponents
        } ?: return 0.0
        return if (opponents == 0) 0.0 else (winRatesTotal/opponents)
    }

    private fun getOpponent2WR(seed: Int): Double {
        var opponents = 0
        var winRatesTotal = 0.0
        players[seed]?.getResultsList()?.forEach { pair ->
            val pairSeed = pair?.first ?: return@forEach
            if (pairSeed == -1) { return@forEach }
            winRatesTotal += getOpponentWR(pairSeed)
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
        callStandingsFragment?.loadTournament()
        return true
    }

    fun getResult(seed1: Int, seed2: Int) = players[seed1]?.getResultAgainst(seed2)

    fun getLastPairings() =
            if (pairingsHistory.isEmpty()) null
            else pairingsHistory.last()

    fun isEmpty() = players.isEmpty() && pairingsHistory.isEmpty() &&
            standingsHistory.isEmpty() && currentRound == 0

    fun allResultsGiven(): Boolean {
        players.forEach { ( _, player ) ->
            if (!(player.isDropped) && player.getMatchHistorySize() != currentRound) {
                return false
            }
        }
        return true
    }

    fun toTournamentEntry(database: AppDatabase?): TournamentEntry {
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
        if (!isEmpty()) {
            database.tournamentDao().insertTournament(toTournamentEntry(database))
        }
    }

    fun loadPlayers(list: List<PlayerEntry>) {
        players.clear()
        list.forEach { it1 -> Player.fromPlayerEntry(it1).let { it2 -> players[it2.seed] = it2 } }
    }

    fun loadStandings(list: List<StandingsEntry>) {
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

    fun loadPairings(list: List<PairingsEntry>) {
        pairingsHistory.clear()
        list.sortedBy { it.index }.forEach {
            pairingsHistory.add(it.player1List.indices.map { i ->
                Pair(it.player1List[i], it.player2List[i])
            })
        }
    }

    fun callNewTournament(newTournament: Tournament?) {
        callRegistrationFragment?.loadTournament(newTournament)
        callPairingsFragment?.loadTournament(newTournament)
        callStandingsFragment?.loadTournament(newTournament)
    }

    companion object {

        fun fromTournamentEntry(te: TournamentEntry, database: AppDatabase?) = Tournament(
            te.id,
            te.date
        ).apply {
            currentRound = te.currentRound
            if (database != null) {
                loadPlayers(database.playersDao().getPlayers(id))
                loadStandings(database.standingsDao().getStandings(id))
                loadPairings(database.pairingsDao().getPairings(id))
            }
        }

        fun newTournament() = Tournament(date = Calendar.getInstance().time)

    }

}
