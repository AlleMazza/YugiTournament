package com.denox.yugitournament.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.denox.yugitournament.R
import com.denox.yugitournament.algorithm.Tournament

class PlayerStandingsFragment(private var tournament: Tournament? = null) : Fragment() {
    private val standingRows = mutableListOf<TableRow>()
    private lateinit var mainLayout: TableLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.player_standings, container, false)
        mainLayout = root.findViewById(R.id.standingsTable)
        loadTournament()
        return root
    }

    fun loadTournament(newTournament: Tournament? = tournament) {
        fun setBGAndMargin(view: View) {
            view.setBackgroundColor(resources.getColor(R.color.white))
            view.layoutParams =
                    if (view.layoutParams != null) (view.layoutParams as TableRow.LayoutParams).apply { setMargins(2, 2, 2, 2) }
                    else TableRow.LayoutParams().apply { setMargins(2, 2, 2, 2) }
        }

        tournament = newTournament
        tournament?.callStandingsFragment = this
        tournament?.let { tournament ->
            standingRows.forEach { mainLayout.removeView(it) }
            standingRows.clear()
            var lastGivenRank = 0
            var lastPoints = Int.MAX_VALUE
            tournament.playerStandings().forEachIndexed { index, player ->
                val newRow = TableRow(context).apply {
                    if (player.points < lastPoints) {
                        lastGivenRank = index+1
                        lastPoints = player.points
                    }
                    addView(TextView(context).apply {
                        text = (lastGivenRank).toString()
                        setBGAndMargin(this)
                    })
                    addView(TextView(context).apply {
                        text = player.name
                        setBGAndMargin(this)
                    })
                    addView(TextView(context).apply {
                        text = player.points.toString()
                        setBGAndMargin(this)
                    })
                    addView(TextView(context).apply {
                        text = player.tiebreaker.toString()
                        setBGAndMargin(this)
                    })
                }
                standingRows.add(newRow)
                mainLayout.addView(newRow)
            }
        }
    }

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"

        @JvmStatic
        fun newInstance(sectionNumber: Int, tournament: Tournament? = null)
        : PlayerStandingsFragment {
            return PlayerStandingsFragment(tournament).apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}
