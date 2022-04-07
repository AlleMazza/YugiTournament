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

class PlayerRegistrationFragment(private var tournament: Tournament? = null) : Fragment() {
    private val registeredPlayers = mutableListOf<ViewGroup>()
    private lateinit var mainLayout: LinearLayout

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
            tournament.players.forEach { addPlayer(it.value) }
        }
    }

    private fun addPlayer(player: Player) {
        val layout = LinearLayout(context)
        layout.addView(TextView(context).apply {
            text = player.name
        })
        layout.addView(Button(context).apply {
            text = "Remove Player"
            setOnClickListener { // TODO add "are you sure", also lock or change to drop after tournament started
                tournament?.let {
                    it.removePlayer(player)
                    registeredPlayers.remove(layout)
                    mainLayout.removeView(layout)
                }
            }
        })
        layout.addView(Button(context).apply {
            text = "Drop Player"
            setOnClickListener { // TODO add "are you sure", also lock or change to drop after tournament started
                tournament?.let {
                    it.dropPlayer(player)
                }
            }
        })
        registeredPlayers.add(layout)
        mainLayout.addView(layout)
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
