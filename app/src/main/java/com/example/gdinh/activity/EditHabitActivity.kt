package com.example.gdinh.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.example.gdinh.analytics.HabitAnalytics
import com.example.gdinh.model.Habit
import com.example.gdinh.model.HabitRecord
import com.example.gdinh.model.ReminderTime
import com.example.gdinh.model.ResetFrequency
import com.example.gdinh.myapplication.R
import com.example.gdinh.picker.ColorPicker
import com.example.gdinh.picker.TimePickerFragment
import com.example.gdinh.sync.FirebaseSyncUtils
import com.example.gdinh.util.HabitScoreUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_edit_habit.*
import java.util.*

/**
 * This class will allow user to edit a habit.
 * User will be able to create a habit or
 * change name, color, date and set reminder and target goal
 */
class EditHabitActivity : AppCompatActivity(), TimePickerFragment.OnTimeSetListener {

    /**
     * The original habit.
     * If original habit is not null, then we are in editing mode, otherwise creating new.
     */
    private var originalHabit: Habit? = null
    private var editingHabit = Habit()

    // Gets data from user they wish to have
    private val isInputCorrect: Boolean
        get() {
            // Gets name of habit
            val name = et_habit_name.text.toString()
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, R.string.toast_empty_name, Toast.LENGTH_SHORT).show()
                return false
            }
            // Gets goal from user
            val targetString = et_habit_target.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(targetString)) {
                Toast.makeText(this, R.string.toast_target_empty, Toast.LENGTH_LONG).show()
                return false
            }
            // Make sure user input a correct target
            try {
                editingHabit.record.target = Integer.parseInt(targetString)
            } catch (e: NumberFormatException) {
                Toast.makeText(this, R.string.toast_failed_target_value, Toast.LENGTH_LONG).show()
                et_habit_target.requestFocus()
                return false
            }

            return true
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configure()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit_habit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Button to go back to previous page
            android.R.id.home -> {
                setResult(Activity.RESULT_CANCELED)
                onBackPressed()
                true
            }
            // Button to save edits that have been made
            R.id.action_save -> {
                save()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun configure() {
        setContentView(R.layout.activity_edit_habit)

        readExtras()

        val titleId = if (originalHabit == null)
            R.string.activity_create_label
        else
            R.string.activity_edit_label
        supportActionBar?.setTitle(titleId)

        val record = editingHabit.record
        et_habit_name.setText(record.name)
        if (record.color != HabitRecord.DEFAULT_COLOR) {
            et_habit_name.setTextColor(record.color)
        }

        et_habit_target.setText(record.target.toString())
        updateTimeText()

        val resetFrequencies = Arrays.asList(*ResetFrequency.ALL)
        val resetAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, resetFrequencies)
        spinner_reset.adapter = resetAdapter
        spinner_reset.prompt = resources.getString(R.string.spinner_prompt)

        val selection = if (originalHabit == null)
            ResetFrequency.NEVER
        else
            originalHabit!!.record.resetFreq
        spinner_reset.setSelection(resetFrequencies.indexOf(selection))

        spinner_reset.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position).toString()
                editingHabit.record.resetFreq = selected
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        tv_reminder_time.setOnClickListener { onDateSpinnerClick() }
    }

    /**
     * Reads data from original habit
     */
    private fun readExtras() {
        if (intent.hasExtra(EDIT_HABIT_EXTRA_KEY)) {
            originalHabit = intent.getParcelableExtra(EDIT_HABIT_EXTRA_KEY)
            editingHabit = originalHabit!!.copy()
        }
    }

    /**
     * Method to allow user how long they want to work on their habit
     */
    private fun onDateSpinnerClick() {
        val timePickerFragment: TimePickerFragment
        timePickerFragment = if (editingHabit.isReminderOn) {
            val record = editingHabit.record
            TimePickerFragment.newInstance(record.reminderHour, record.reminderMin)
        } else {
            TimePickerFragment()
        }
        timePickerFragment.setOnTimeSetListener(this)
        timePickerFragment.show(supportFragmentManager, "TimePicker")
    }

    /**
     * Allows user to change the reminder time
     */
    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        editingHabit.record.reminderHour = hourOfDay
        editingHabit.record.reminderMin = minute
        updateTimeText()
    }

    /**
     * Allows user to choose color of grid buttons
     * Color picker is used from class assignment
     */
    fun showColorPicker(view: View){
        val intent = Intent(this, ColorPicker::class.java)
        startActivityForResult(intent, 1)
    }

    /**
     * Uses color picker to save color to grid and text
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var color: Int = 0
        // Sets background and text to color
        if (requestCode == 1 && resultCode == Activity.RESULT_OK){
            color = data!!.getIntExtra("Color", 0)
            et_habit_name.setTextColor(color)
            editingHabit.record.color = color
        }
    }

    override fun onCancel() {
        editingHabit.record.reminderHour = HabitRecord.REMINDER_OFF
        editingHabit.record.reminderMin = HabitRecord.REMINDER_OFF
        updateTimeText()
    }

    /**
     * Updates reminder time
     */
    private fun updateTimeText() {
        if (editingHabit.isReminderOn) {
            val record = editingHabit.record
            tv_reminder_time.text = ReminderTime.getTimeString(record.reminderHour, record.reminderMin)
        } else {
            tv_reminder_time.setText(R.string.off)
        }
    }

    /**
     * Saves all edits to database
     */
    private fun save() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null || !isInputCorrect) return

        putChanges()

        if (originalHabit == null) {
            createNew()
        } else {
            applyChanges()
        }
    }

    /**
     * Adds habit to database if no edits needed
     */
    private fun createNew() {
        HabitAnalytics.logCreateHabitWithName(editingHabit.record.name)
        FirebaseSyncUtils.createNewHabitRecord(editingHabit.record)
        finish()
    }

    /**
     * Saves changes to database
     */
    private fun applyChanges() {
        val data = Intent()
        data.putExtra(EDIT_HABIT_RESULT, editingHabit)
        setResult(Activity.RESULT_OK, data)

        HabitScoreUtils.resetScore(editingHabit)
        FirebaseSyncUtils.applyChangesForHabit(editingHabit)

        finish()
    }

    /**
     * Puts the changes accordingly to user
     */
    private fun putChanges() {
        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val now = System.currentTimeMillis()
        val record = editingHabit.record

        if (originalHabit == null) {
            record.createdAt = now
            record.resetTimestamp = now
        }

        record.userId = currentUser.uid
        record.name = et_habit_name.text.toString().trim { it <= ' ' }
    }

    companion object {
        val EDIT_HABIT_RESULT = "com.example.gdinh.activities.edit_result"
        val EDIT_HABIT_EXTRA_KEY = "com.example.gdinh.activities.edit"
    }
}
