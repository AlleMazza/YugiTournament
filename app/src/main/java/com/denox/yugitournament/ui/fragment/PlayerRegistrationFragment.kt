package com.denox.yugitournament.ui.fragment

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.denox.yugitournament.R
import com.denox.yugitournament.algorithm.Player
import com.denox.yugitournament.algorithm.Tournament
import com.google.android.material.snackbar.Snackbar

class PlayerRegistrationFragment(private var tournament: Tournament? = null) : Fragment() {
    private val registeredPlayers = mutableListOf<ViewGroup>()
    private lateinit var mainLayout: LinearLayout
    private val removeButtons = mutableListOf<Button>()
    private val dropButtons = mutableListOf<Button>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.player_registration, container, false)
        mainLayout = root.findViewById(R.id.registrationMainLayout)
        loadTournament()
        root.findViewById<Button>(R.id.addPlayerButton).setOnClickListener {
            tournament?.let {
                val nameField = root.findViewById<EditText>(R.id.playerNameText)
                if (nameField.text.isNotEmpty()) {
                    if ((tournament?.currentRound ?: 0) > 0) {
                        var addNextRound = true
                        val builder = AlertDialog.Builder(root.context)
                        builder.apply {
                            setSingleChoiceItems(arrayOf(
                                if (it.pairingsHistory.last().any { ( p1 , p2 ) -> p1 == -1 || p2 == -1 })
                                    getString(R.string.match_against_player_with_bye)
                                else getString(R.string.add_player_with_bye),
                                getString(R.string.add_player_next_round)), 1) { _, i ->
                                addNextRound = if (i == 0) false else true
                            }
                            setPositiveButton(R.string.add_player) { _, _ ->
                                addPlayer(it.addPlayer(nameField.text.toString(),
                                    addToNextRound = addNextRound))
                            }
                            setNegativeButton(R.string.cancel) { dialog, _ ->
                                dialog.cancel()
                            }
                        }
                        builder.create().show()
                    }
                    else {
                        addPlayer(it.addPlayer(nameField.text.toString()))
                        nameField.setText("")
                    }
                }
            }
        }
        return root
    }

    fun loadTournament(newTournament: Tournament? = tournament) {
        tournament = newTournament
        tournament?.let { tournament ->
            registeredPlayers.forEach { mainLayout.removeView(it) }
            registeredPlayers.clear()
            tournament.players.forEach { addPlayer(it.value) }
            checkEnabledButtons()
            tournament.callRegistrationFragment = this
        }
    }

    private fun addPlayer(player: Player) {
        val layout = LinearLayout(context).apply {
            isBaselineAligned = false
        }
        val textView = TextView(context).apply {
            text = player.name
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.0F)
            setEms(6)
        }
        layout.addView(textView)
        val removeButton = Button(context).apply {
            text = getString(R.string.remove_player)
            setOnClickListener {
                if ((tournament?.currentRound ?: 1) < 1) {
                    Snackbar.make(
                        mainLayout,
                        getString(R.string.sure_remove_player),
                        2000
                    ).apply {
                        setAction(getString(R.string.confirm)) {
                            tournament?.let {
                                it.removePlayer(player)
                                registeredPlayers.remove(layout)
                                mainLayout.removeView(layout)
                            }
                        }
                        show()
                    }
                }
            }
        }
        layout.addView(removeButton)
        removeButtons.add(removeButton)
        val dropButton = Button(context).apply {
            text = if (player.isDropped) getString(R.string.undrop_player) else getString(R.string.drop_player)
            setOnClickListener {
                if ((tournament?.currentRound ?: 0) > 0) {
                    if (player.isDropped) { tournament?.undropPlayer(player) }
                    else { tournament?.dropPlayer(player) }
                    text = if (player.isDropped) getString(R.string.undrop_player) else getString(R.string.drop_player)
                }
            }
        }
        layout.addView(dropButton)
        dropButtons.add(dropButton)
        registeredPlayers.add(layout)
        mainLayout.addView(layout)
        checkEnabledButtons()
    }

    fun checkEnabledButtons() {
        removeButtons.forEach {
            it.isEnabled = (tournament?.currentRound ?: 1) < 1
        }
        dropButtons.forEach {
            it.isEnabled = (tournament?.currentRound ?: 0) > 0
        }
    }

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"

        @JvmStatic
        fun newInstance(sectionNumber: Int, tournament: Tournament? = null)
        : PlayerRegistrationFragment {
            return PlayerRegistrationFragment(tournament).apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

}
