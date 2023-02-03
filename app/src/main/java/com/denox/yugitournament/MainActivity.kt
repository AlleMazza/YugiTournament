package com.denox.yugitournament

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import androidx.viewpager.widget.ViewPager
import com.denox.yugitournament.algorithm.DataHolder
import com.denox.yugitournament.database.AppDatabase
import com.denox.yugitournament.ui.main.SectionsPagerAdapter
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {
    private lateinit var sectionsPagerAdapter: SectionsPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val dataHolder = DataHolder(
            database = Room.databaseBuilder(applicationContext, AppDatabase::class.java,
                "tournamentsDB").addMigrations(AppDatabase.MIGRATION_1_2)
                .allowMainThreadQueries().build()
        )
        sectionsPagerAdapter =
            SectionsPagerAdapter(this, dataHolder, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
    }

    override fun onStop() {
        super.onStop()
        sectionsPagerAdapter.dataHolder.tournament.saveTournament(
            sectionsPagerAdapter.dataHolder.database)
    }

}
