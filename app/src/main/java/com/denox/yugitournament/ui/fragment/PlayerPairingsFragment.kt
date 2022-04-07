package com.denox.yugitournament.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.denox.yugitournament.R
import com.denox.yugitournament.algorithm.Player
import com.denox.yugitournament.algorithm.Tournament

class PlayerPairingsFragment(private var tournament: Tournament? = null) : Fragment() {
    private val pairingsRows = mutableListOf<ViewGroup>()
    private lateinit var mainLayout: LinearLayout
    private lateinit var roundLabel: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.player_pairings, container, false)
        mainLayout = root.findViewById(R.id.pairingsMainLayout)
        roundLabel = root.findViewById(R.id.roundLabel)
        loadTournament()
        root.findViewById<Button>(R.id.nextRoundButton).setOnClickListener { // TODO add "are you sure", also lock if not all results are given
            tournament?.nextRoundOrStart()
            loadTournament()
        }
        return root
    }

    fun loadTournament(newTournament: Tournament? = tournament) {
        tournament = newTournament
        tournament?.let { tournament ->
            pairingsRows.forEach { mainLayout.removeView(it) }
            tournament.getLastPairings()?.forEach {
                val player1 =
                        if (it.first < 0)
                            Player(-1, "BYE")
                        else
                            tournament.players[it.first] ?: return@forEach
                val player2 =
                        if (it.second < 0)
                            Player(-1, "BYE")
                        else
                            tournament.players[it.second] ?: return@forEach
                addPairing(player1, player2)
            }
            roundLabel.text = "Round ${tournament.currentRound}"
        }
    }

    private fun addPairing(player1: Player, player2: Player) {
        val layout = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        val layoutText = LinearLayout(context)
        layout.addView(layoutText)
        val resultTextView = TextView(context).apply {
            text = when (tournament?.getResult(player1.seed, player2.seed)) {
                0 -> "${player2.name} wins"
                1 -> "Draw"
                3 -> "${player1.name} wins"
                else -> "No result"
            }
        }
        layoutText.addView(TextView(context).apply {
            text = "${player1.name} vs ${player2.name}:   "
        })
        layoutText.addView(resultTextView)
        if (player1.seed > 0 && player2.seed > 0) {
            val layoutButtons = LinearLayout(context)
            layout.addView(layoutButtons)
            layoutButtons.addView(Button(context).apply {
                text = "${player1.name} wins"
                setOnClickListener {
                    tournament?.setResult(player1.seed, player2.seed, 3)
                    resultTextView.text = "${player1.name} wins"
                }
            })
            layoutButtons.addView(Button(context).apply {
                text = "${player2.name} wins"
                setOnClickListener {
                    tournament?.setResult(player1.seed, player2.seed, 0)
                    resultTextView.text = "${player2.name} wins"
                }
            })
            layoutButtons.addView(Button(context).apply {
                text = "Draw"
                setOnClickListener {
                    tournament?.setResult(player1.seed, player2.seed, 1)
                    resultTextView.text = "Draw"
                }
            })
        }
        pairingsRows.add(layout)
        mainLayout.addView(layout)
    }

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"

        @JvmStatic
        fun newInstance(sectionNumber: Int, tournament: Tournament? = null)
        : PlayerPairingsFragment {
            return PlayerPairingsFragment(tournament).apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}
