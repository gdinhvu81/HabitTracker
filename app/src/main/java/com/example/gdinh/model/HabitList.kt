package com.example.gdinh.model

import kotlin.collections.ArrayList

/**
 * Class to be able to add habits to the list
 */
class HabitList(habits: MutableList<Habit>? = ArrayList()) {

    var habits = habits
        set(newValue) = if (newValue == null) {
            clear()
        } else {
            field = newValue
        }

    fun add(habit: Habit) {
        habits?.add(habit)

    }

    fun clear() {
        habits?.clear()
    }
}
