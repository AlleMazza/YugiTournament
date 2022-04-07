package com.denox.yugitournament.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
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
                    addPlayer(it.addPlayer(nameField.text.toString()))
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
        val layout = LinearLayout(context)
        layout.addView(TextView(context).apply {
            text = player.name
        })
        val removeButton = Button(context).apply {
            text = getString(R.string.remove_player)
            setOnClickListener {
                if (tournament?.currentRound ?: 1 <= 0) {
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
                if (tournament?.currentRound ?: 0 >= 1) {
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
    }

    fun checkEnabledButtons() {
        removeButtons.forEach {
            if (tournament?.currentRound ?: 1 >= 1) {
                it.isEnabled = false
            }
        }
        dropButtons.forEach {
            if (tournament?.currentRound ?: 0 <= 0) {
                it.isEnabled = false
            }
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
