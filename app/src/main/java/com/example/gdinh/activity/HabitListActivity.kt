package com.example.gdinh.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.example.gdinh.adapter.HabitsAdapter
import com.example.gdinh.analytics.HabitAnalytics
import com.example.gdinh.model.Habit
import com.example.gdinh.model.HabitList
import com.example.gdinh.model.HabitRecord
import com.example.gdinh.myapplication.BuildConfig
import com.example.gdinh.myapplication.R
import com.example.gdinh.sync.FirebaseSyncUtils
import com.example.gdinh.util.HabitScoreUtils
import com.example.gdinh.view.GridSpacingItemDecoration
import com.firebase.ui.auth.AuthUI
import kotlinx.android.synthetic.main.activity_habit_list.*
import java.util.*


class HabitListActivity : AppCompatActivity(), HabitsAdapter.OnClickListener {

    private lateinit var habitsAdapter: HabitsAdapter

    // Firebase instance variables.
    private var userHabitsQuery: Query? = null
    private var valueEventListener: ValueEventListener? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configure()
    }

    private fun configure() {
        setContentView(R.layout.activity_habit_list)
        setSupportActionBar(findViewById(R.id.toolbar))

        initFirebase()

        val habitList = HabitList(ArrayList())
        habitsAdapter = HabitsAdapter(habitList, this)

        // Puts habits into a grid
        rv_habits.layoutManager = GridLayoutManager(this, NUM_OF_COLUMNS)
        rv_habits.addItemDecoration(GridSpacingItemDecoration(NUM_OF_COLUMNS, SPACE_BETWEEN_ITEMS, true))
        rv_habits.adapter = habitsAdapter
        rv_habits.setHasFixedSize(true)
        rv_habits.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (dy > 0 && fab.isShown) {
                    fab.hide()
                } else if (dy < 0 && !fab.isShown) {
                    fab.show()
                }
            }
        })

        fab.setOnClickListener { createHabit() }
    }

    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onPause() {
        super.onPause()

        firebaseAuth.removeAuthStateListener(authStateListener)
        detachDatabaseReadListener()
    }

    override fun onClick(habit: Habit, position: Int) {
        showDetail(habit)
    }

    // Creates a database to store habits to
    private fun initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                onSignedInInitialize()
            } else {
                onSignedOutCleanup()
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(!BuildConfig.DEBUG, true)
                                .setAvailableProviders(Arrays.asList<AuthUI.IdpConfig>(
                                        AuthUI.IdpConfig.EmailBuilder().build()
                                ))
                                .build(),
                        RC_SIGN_IN)
            }
        }
    }

    private fun onSignedInInitialize() {
        detachDatabaseReadListener()

        HabitAnalytics.logLogin(FirebaseAuth.getInstance().currentUser!!)
        userHabitsQuery = FirebaseSyncUtils.currentUserHabitsQuery
        assert(userHabitsQuery != null)
        userHabitsQuery!!.keepSynced(true)

        attachDatabaseReadListener()
    }

    private fun onSignedOutCleanup() {
        habitsAdapter.clear()
        detachDatabaseReadListener()
    }

    private fun attachDatabaseReadListener() {
        if (valueEventListener != null) {
            return
        }

        showProgressIndicator()

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                hideProgressIndicator()
                processOnDataChange(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                hideProgressIndicator()
                Log.e(TAG, "Cancelled to query habits: " + databaseError.toString())
            }
        }

        userHabitsQuery!!.addValueEventListener(valueEventListener)
    }

    private fun detachDatabaseReadListener() {
        if (valueEventListener != null) {
            userHabitsQuery!!.removeEventListener(valueEventListener!!)
            valueEventListener = null
        }
        userHabitsQuery = null
    }

    private fun processOnDataChange(dataSnapshot: DataSnapshot) {
        val habits = ArrayList<Habit>(dataSnapshot.childrenCount.toInt())
        for (data in dataSnapshot.children) {
            val parsedRecord = data.getValue(HabitRecord::class.java)
            habits.add(Habit(data.key, parsedRecord!!))
        }

        habitsAdapter.habits = habits
        empty_view.visibility = if (habitsAdapter.habits!!.isEmpty()) View.VISIBLE else View.INVISIBLE

        HabitScoreUtils.processAll(habits)
    }

    /**
     * Method to show details of a habit
     * Creates an intent that goes to DetailHabitActivity page
     */
    private fun showDetail(habit: Habit) {
        HabitAnalytics.logViewHabitListItem(habit)
        val intent = Intent(this, DetailHabitActivity::class.java)
        intent.putExtra(DetailHabitActivity.HABIT_EXTRA_KEY, habit)
        startActivity(intent)
    }

    /**
     * Method to create a new habit
     * Creates an intent that goes to EditHabitActivity page
     */
    private fun createHabit() {
        if (firebaseAuth.currentUser != null) {
            startActivity(Intent(this, EditHabitActivity::class.java))
        }
    }

    private fun showProgressIndicator() {
        progress_bar.visibility = View.VISIBLE
    }

    private fun hideProgressIndicator() {
        progress_bar.visibility = View.INVISIBLE
    }

    companion object {
        private const val TAG = "HabitListActivity"

        private const val NUM_OF_COLUMNS = 2
        private const val SPACE_BETWEEN_ITEMS = 32
        private const val RC_SIGN_IN = 10
    }
}
