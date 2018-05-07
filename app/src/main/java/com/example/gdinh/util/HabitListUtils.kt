package com.example.gdinh.util

import com.example.gdinh.model.ResetFrequency

class HabitListUtils(private val dates: List<Long> = ArrayList()) {
    fun filteredBy(type: ResetFrequency.Type): List<Long> {
        if (type == ResetFrequency.Type.NEVER) {
            return dates
        }

        return dates.filter {
            HabitDateUtils.isDateInType(it, type)
        }
    }
}
