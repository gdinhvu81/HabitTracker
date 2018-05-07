package com.example.gdinh.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import com.example.gdinh.model.Habit
import com.example.gdinh.myapplication.R
import com.example.gdinh.sync.FirebaseSyncUtils
import com.example.gdinh.view.model.HabitDetailViewModel

import kotlinx.android.synthetic.main.activity_detail_habit.*

/**
 * This class will be able to show the details of a habit.
 * It allows users to increment and decrement to the habit score.
 * Will bring user to edit page so they can edit the habit as well as delete the habit if needed
 */
class DetailHabitActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener{


    private lateinit var habit: Habit
    private val viewModel = HabitDetailViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configure()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detail_habit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Menu button to press back to habit screen
            android.R.id.home -> {
                onBackPressed()
                true
            }
            // Goes to edit page to edit habit
            R.id.action_edit -> {
                editHabit()
                true
            }
            // Deletes the current habit
            R.id.action_delete -> {
                delete()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Gets data from edit page and updates the habit
        if (requestCode == RC_EDIT_HABIT && resultCode == Activity.RESULT_OK) {
            data?.let {
                habit = data.getParcelableExtra(EditHabitActivity.EDIT_HABIT_RESULT)
                updateUI()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun configure() {
        setContentView(R.layout.activity_detail_habit)

        // Call read extra method
        readExtras()
        // Call update UI method
        updateUI()

        // Button to increase habit by one
        bt_increase.setOnClickListener { onIncreaseScoreClick() }
        // Button to decrease habit by one
        bt_decrease.setOnClickListener { onDecreaseScoreClick() }
    }

    /**
     * Method that updates the name, score and date of habit
     */
    private fun updateUI() {
        // Shows habit name in menu bar
        supportActionBar?.title = habit.record.name

        val goalAcheived: TextView = findViewById(R.id.tv_date_range)
        tv_score.text = viewModel.getScoreString(habit.record.score)
        goalAcheived.setText("Goals Acheived")
    }

    /**
     * Reads extra details of habit when pressed
     */
    private fun readExtras() {
        if (intent.hasExtra(HABIT_EXTRA_KEY)) {
            habit = intent.getParcelableExtra(HABIT_EXTRA_KEY)
        } else {
            throw IllegalArgumentException("Put habit in the intent extras to be able to see details")
        }
    }

    /**
     * Intent created to go to edit page when edit menu button is pressed
     */
    private fun editHabit() {
        val intent = Intent(this, EditHabitActivity::class.java)
        intent.putExtra(EditHabitActivity.EDIT_HABIT_EXTRA_KEY, habit)
        startActivityForResult(intent, RC_EDIT_HABIT)
    }

    /**
     * Method to increase habit score
     */
    private fun onIncreaseScoreClick() {
        val oldScore = habit.record.score
        habit.increaseScore()
        updateScoreIfNeeded(oldScore)
    }

    /**
     * Method to decrease habit score
     */
    private fun onDecreaseScoreClick() {
        val oldScore = habit.record.score
        habit.decreaseScore()
        updateScoreIfNeeded(oldScore)
    }

    /**
     * Method to update score and save to database
     */
    private fun updateScoreIfNeeded(oldValue: Int) {
        if (oldValue != habit.record.score) {
            updateUI()
            FirebaseSyncUtils.applyChangesForHabit(habit)
        }
    }

    /**
     * Method to delete habit from database
     */
    private fun delete() {
        AlertDialog.Builder(this)
                .setTitle(R.string.action_delete)
                .setMessage(R.string.delete_habit_message)
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    FirebaseSyncUtils.deleteHabit(habit)
                    finish()
                }
                .setNegativeButton(android.R.string.no, null)
                .show()
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            updateUI()
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    companion object {

        val HABIT_EXTRA_KEY = "com.example.gdinh.activities.habit"
        private val RC_EDIT_HABIT = 1234
    }
}
