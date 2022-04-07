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
import com.google.android.material.snackbar.Snackbar

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
        root.findViewById<Button>(R.id.nextRoundButton).setOnClickListener {
            if (tournament?.allResultsGiven() == true) {
                tournament?.nextRoundOrStart()
                loadTournament()
            }
            else {
                Snackbar.make(
                    mainLayout,
                    getString(R.string.matches_unfinished),
                    2000
                ).show()
            }
        }
        root.findViewById<Button>(R.id.cancelRoundButton).setOnClickListener {
            if (tournament?.currentRound ?: 0 > 0) {
                Snackbar.make(
                    mainLayout,
                    getString(R.string.sure_cancel_round),
                    2000
                ).apply {
                    setAction(getString(R.string.confirm)) {
                        tournament?.cancelLastRound()
                        loadTournament()
                    }
                    show()
                }
            }
        }
        return root
    }

    fun loadTournament(newTournament: Tournament? = tournament) {
        tournament = newTournament
        tournament?.let { tournament ->
            pairingsRows.forEach { mainLayout.removeView(it) }
            pairingsRows.clear()
            tournament.getLastPairings()?.forEach {
                val player1 =
                        if (it.first < 0)
                            Player(-1, getString(R.string.bye))
                        else
                            tournament.players[it.first] ?: return@forEach
                val player2 =
                        if (it.second < 0)
                            Player(-1, getString(R.string.bye))
                        else
                            tournament.players[it.second] ?: return@forEach
                addPairing(player1, player2)
            }
            roundLabel.text = getString(R.string.round_x, tournament.currentRound.toString())
            tournament.callPairingsFragment = this
        }
    }

    private fun addPairing(player1: Player, player2: Player) {
        val layout = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        val layoutText = LinearLayout(context)
        layout.addView(layoutText)
        val resultTextView = TextView(context).apply {
            text = when (tournament?.getResult(player1.seed, player2.seed)) {
                0 -> getString(R.string.player_wins, player2.name)
                1 -> getString(R.string.draw)
                3 -> getString(R.string.player_wins, player1.name)
                else -> getString(R.string.no_result)
            }
        }
        layoutText.addView(TextView(context).apply {
            text = getString(R.string.player_vs_player, player1.name, player2.name)
        })
        layoutText.addView(resultTextView)
        if (player1.seed > 0 && player2.seed > 0) {
            val layoutButtons = LinearLayout(context)
            layout.addView(layoutButtons)
            layoutButtons.addView(Button(context).apply {
                text = getString(R.string.player_wins, player1.name)
                setOnClickListener {
                    tournament?.setResult(player1.seed, player2.seed, 3)
                    resultTextView.text = getString(R.string.player_wins, player1.name)
                }
            })
            layoutButtons.addView(Button(context).apply {
                text = getString(R.string.player_wins, player2.name)
                setOnClickListener {
                    tournament?.setResult(player1.seed, player2.seed, 0)
                    resultTextView.text = getString(R.string.player_wins, player2.name)
                }
            })
            layoutButtons.addView(Button(context).apply {
                text = getString(R.string.draw)
                setOnClickListener {
                    tournament?.setResult(player1.seed, player2.seed, 1)
                    resultTextView.text = getString(R.string.draw)
                }
            })
        }
        pairingsRows.add(layout)
        mainLayout.addView(layout)
    }

    fun maximumPairingsExceeded() {
        Snackbar.make(
            mainLayout,
            getString(R.string.max_combinations_exceeded),
            2000
        ).show()
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
