package com.denox.yugitournament.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.denox.yugitournament.R
import com.denox.yugitournament.algorithm.DataHolder
import com.denox.yugitournament.algorithm.Tournament
import com.denox.yugitournament.database.AppDatabase
import com.denox.yugitournament.ui.fragment.PlayerPairingsFragment
import com.denox.yugitournament.ui.fragment.PlayerRegistrationFragment
import com.denox.yugitournament.ui.fragment.PlayerStandingsFragment
import com.denox.yugitournament.ui.fragment.SavedTournamentsFragment

private val TAB_TITLES = arrayOf(
    R.string.tab_text_1,
    R.string.tab_text_2,
    R.string.tab_text_3,
    R.string.tab_text_4
)

class SectionsPagerAdapter(private val context: Context, var dataHolder: DataHolder,
                           fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return when (position) {
            0 -> PlayerRegistrationFragment.newInstance(position, dataHolder.tournament)
            1 -> PlayerPairingsFragment.newInstance(position, dataHolder.tournament)
            2 -> PlayerStandingsFragment.newInstance(position, dataHolder.tournament)
            3 -> SavedTournamentsFragment.newInstance(position, dataHolder)
            else -> Fragment()
        }
    }

    override fun getPageTitle(position: Int) = context.resources.getString(TAB_TITLES[position])

    override fun getCount() = 4

}
