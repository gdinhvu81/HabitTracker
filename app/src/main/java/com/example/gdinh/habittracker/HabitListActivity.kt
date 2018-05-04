package com.example.gdinh.habittracker

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu

class HabitListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habit_list)
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_habit_list, menu)

        return true
    }


}
