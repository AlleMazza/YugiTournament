package com.denox.yugitournament.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.denox.yugitournament.R
import com.denox.yugitournament.algorithm.DataHolder
import com.denox.yugitournament.algorithm.Tournament
import com.denox.yugitournament.database.TournamentEntry
import com.google.android.material.snackbar.Snackbar

class SavedTournamentsFragment(private var dataHolder: DataHolder) : Fragment() {
    private val tournamentsLoaded = mutableListOf<ViewGroup>()
    private lateinit var mainLayout: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.saved_tournaments, container, false)
        mainLayout = root.findViewById(R.id.savedTournamentsMainLayout)
        root.findViewById<Button>(R.id.newTournamentButton).setOnClickListener {
            Snackbar.make(
                mainLayout,
                getString(R.string.sure_new_tournament),
                2000
            ).apply {
                setAction(getString(R.string.confirm)) {
                    val oldTournament = dataHolder.tournament
                    oldTournament.saveTournament(dataHolder.database)
                    dataHolder.tournament = Tournament.newTournament()
                    oldTournament.callNewTournament(dataHolder.tournament)
                    loadDatabase()
                }
                show()
            }
        }
        loadDatabase()
        return root
    }

    fun loadDatabase() {
        tournamentsLoaded.forEach { mainLayout.removeView(it) }
        tournamentsLoaded.clear()
        dataHolder.database.tournamentDao().getAllTournaments()
            .sortedByDescending { it.date }.forEach { addTournament(it) }
    }

    private fun addTournament(te: TournamentEntry) {
        val layout = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        val layoutButtons = LinearLayout(context)
        layout.addView(TextView(context).apply {
            text = getString(R.string.saved_tournament_string, te.date.toString(), te.currentRound.toString())
        })
        layout.addView(layoutButtons)
        layoutButtons.addView(Button(context).apply {
            text = getString(R.string.load_tournament)
            setOnClickListener {
                Snackbar.make(
                    mainLayout,
                    context.getString(R.string.sure_load_tournament),
                    2000
                ).apply {
                    setAction(context.getString(R.string.confirm)) {
                        val oldTournament = dataHolder.tournament
                        oldTournament.saveTournament(dataHolder.database)
                        dataHolder.tournament = Tournament.fromTournamentEntry(te, dataHolder.database)
                        oldTournament.callNewTournament(dataHolder.tournament)
                    }
                    show()
                }
            }
        })
        layoutButtons.addView(Button(context).apply {
            text = getString(R.string.delete_tournament)
            setOnClickListener {
                Snackbar.make(
                    mainLayout,
                    getString(R.string.sure_delete_tournament),
                    2000
                ).apply {
                    setAction(context.getString(R.string.confirm)) {
                        dataHolder.database.tournamentDao().deleteTournaments(te)
                        dataHolder.tournament = Tournament.newTournament()
                        tournamentsLoaded.remove(layout)
                        mainLayout.removeView(layout)
                    }
                    show()
                }
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
