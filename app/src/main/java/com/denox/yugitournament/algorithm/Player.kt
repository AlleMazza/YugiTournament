package com.denox.yugitournament.algorithm

import com.denox.yugitournament.database.PlayerEntry

class Player(var seed: Int, var name: String = "placeholder") {
    private var matchHistory = mutableListOf<Pair<Int, Int>>()
    // 0 loss 1 draw 3 win
    // seed -1 = bye
    var isDropped = false
    var randomSeed = 0

    fun points() = matchHistory.map { it.second }.sum()

    // dropped players count as having lost all following rounds, don't know if it is the right way
    fun winRate(numberOfRounds: Int = matchHistory.size): Double {
        val hadBye = receivedBye()
        return (matchHistory.map { it.second }.sum() - hadBye*3) / 3.0 / (numberOfRounds-hadBye)
    }

    fun getResultAgainst(seed: Int) = matchHistory.firstOrNull { it.first == seed }?.second

    fun removeResultAgainst(seed: Int) =
        matchHistory.removeAt(matchHistory.indexOfFirst { it.first == seed })

    fun dropAllResults() = matchHistory.clear()

    fun dropLastResult() = matchHistory.removeLast()

    fun getResultsList() = matchHistory.toList()

    fun getMatchHistorySize() = matchHistory.size

    fun changeResult(seed: Int, result: Int) {
        val ix = matchHistory.indexOfFirst { it.first == seed }
        if (ix < 0) { nextMatchResult(seed, result) }
        else { matchHistory[matchHistory.indexOfFirst { it.first == seed }] = Pair(seed, result) }
    }

    fun nextMatchResult(seed: Int, result: Int) {
        matchHistory.add(Pair(seed, result))
    }

    fun receivedBye() = matchHistory.count { it.first < 0 }

    fun matchHistoryToPlainList() = List(matchHistory.size*2) {
        if (it%2 == 0) { matchHistory[it/2].first }
        else { matchHistory[it/2].second }
    }
    fun matchHistoryFromPlainList(list: List<Int>) {
        matchHistory = MutableList(list.size/2) { Pair(list[it*2], list[it*2+1]) }
    }

    fun toPlayerEntry(tournamentId: Int) = PlayerEntry(
            tournamentId,
            seed,
            name,
            matchHistoryToPlainList(),
            isDropped,
            randomSeed,
    )

    companion object {

        fun fromPlayerEntry(pe: PlayerEntry) = Player(
            pe.seed,
            pe.name
        ).apply {
            matchHistoryFromPlainList(pe.matchHistory)
            isDropped = pe.isDropped
            randomSeed = pe.randomSeed
        }

    }

}
