package com.denox.yugitournament.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.denox.yugitournament.R
import com.denox.yugitournament.algorithm.DataHolder
import com.denox.yugitournament.algorithm.Player
import com.denox.yugitournament.algorithm.Tournament
import com.denox.yugitournament.database.TournamentEntry

class SavedTournamentsFragment(private var dataHolder: DataHolder) : Fragment() {
    private val tournamentsLoaded = mutableListOf<ViewGroup>()
    private lateinit var mainLayout: TableLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.saved_tournaments, container, false)
        mainLayout = root.findViewById(R.id.savedTournamentsMainLayout)
        loadDatabase()
        return root
    }

    fun loadDatabase() {
        tournamentsLoaded.forEach { mainLayout.removeView(it) }
        dataHolder.database.tournamentDao().getAllTournaments()
            .sortedBy { it.date }.forEach { addTournament(it) }
    }

    private fun addTournament(te: TournamentEntry) { // TODO add a fragments refresh when loading a new tournament
        val layout = LinearLayout(context)
        layout.addView(TextView(context).apply {
            text = resources.getString(R.string.saved_tournament_string, te.date.toString(), te.currentRound.toString())
        })
        layout.addView(Button(context).apply {
            text = "Load Tournament"
            setOnClickListener { // TODO add "are you sure"
                dataHolder.tournament.saveTournament(dataHolder.database)
                dataHolder.tournament = Tournament.fromTournamentEntry(te, dataHolder.database)
            }
        })
        layout.addView(Button(context).apply {
            text = "Delete Tournament"
            setOnClickListener { // TODO add "are you sure"
                dataHolder.database.tournamentDao().deleteTournaments(
                    dataHolder.tournament.toTournamentEntry(null))
                dataHolder.tournament = Tournament.newTournament()
            }
        })
        tournamentsLoaded.add(layout)
        mainLayout.addView(layout)
    }

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"

        @JvmStatic
        fun newInstance(sectionNumber: Int, dataHolder: DataHolder): SavedTournamentsFragment {
            return SavedTournamentsFragment(dataHolder).apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }

    }

}
