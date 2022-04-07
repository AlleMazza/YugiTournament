package com.denox.yugitournament.algorithm

data class PlayerRanking(
        val seed: Int,
        val name: String,
        val points: Int,
        val tiebreaker: Long
)
