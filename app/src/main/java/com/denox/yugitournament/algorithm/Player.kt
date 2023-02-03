package com.denox.yugitournament.algorithm

import com.denox.yugitournament.database.PlayerEntry

class Player(var seed: Int, var name: String = "placeholder") {
    private var matchHistory = mutableListOf<Pair<Int, Int>?>()
    // 0 loss 1 draw 3 win
    // seed -1 = bye
    var isDropped = false
    var randomSeed = 0

    fun points() = matchHistory.sumOf { it?.second ?: 0 }

    // dropped players count as having lost all following rounds, don't know if it is the right way
    fun winRate(numberOfRounds: Int = matchHistory.size): Double {
        val hadBye = receivedBye()
        return (matchHistory.sumOf { it?.second ?: 0 } - hadBye*3) / 3.0 / (numberOfRounds-hadBye)
    }

    fun getResultAgainst(seed: Int) =
        matchHistory.firstOrNull { (it?.first ?: -999) == seed }?.second

    fun removeResultAgainst(seed: Int) =
        matchHistory.removeAt(matchHistory.indexOfFirst { it?.first == seed })

    fun dropAllResults() = matchHistory.clear()

    fun dropLastResult() = matchHistory.removeLast()

    fun getResultsList() = matchHistory.toList()

    fun getMatchHistorySize() = matchHistory.size

    fun changeResult(seed: Int, result: Int) {
        val ix = matchHistory.indexOfFirst { it?.first == seed }
        if (ix < 0) { nextMatchResult(seed, result) }
        else { matchHistory[matchHistory.indexOfFirst { it?.first == seed }] = Pair(seed, result) }
    }

    fun nextMatchResult(seed: Int, result: Int) {
        matchHistory.add(Pair(seed, result))
    }

    fun skipRound() { matchHistory.add(null) }

    fun receivedBye() = matchHistory.count { (it?.first ?: 0) < 0 }

    private fun skippedRounds(): List<Int> {
        val list = mutableListOf<Int>()
        for (i in 0 until matchHistory.size) {
            if (matchHistory[i] == null) { list.add(i) }
        }
        return list
    }
    private fun matchHistoryToPlainList(): List<Int> {
        val list = mutableListOf<Int>()
        matchHistory.forEach { result ->
            if (result != null) {
                list.add(result.first)
                list.add(result.second)
            }
        }
        return list
    }
    private fun matchHistoryFromPlainListAndSkippedRounds(list: List<Int>,
                                                          skippedRounds: List<Int>) {
        matchHistory = mutableListOf()
        var skipped = 0
        for (i in 0 until (list.size/2 + skippedRounds.size)) {
            if (skipped < skippedRounds.size && skippedRounds.contains(i)) {
                matchHistory.add(null)
                ++skipped
            }
            else {
                matchHistory.add(Pair(list[i*2], list[i*2+1]))
            }
        }
    }

    fun toPlayerEntry(tournamentId: Int) = PlayerEntry(
        tournamentId,
        seed,
        name,
        matchHistoryToPlainList(),
        isDropped,
        randomSeed,
        skippedRounds(),
    )

    companion object {

        fun fromPlayerEntry(pe: PlayerEntry) = Player(
            pe.seed,
            pe.name
        ).apply {
            matchHistoryFromPlainListAndSkippedRounds(pe.matchHistory, pe.skippedRounds)
            isDropped = pe.isDropped
            randomSeed = pe.randomSeed
        }

    }

}
