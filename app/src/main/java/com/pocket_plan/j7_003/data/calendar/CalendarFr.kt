package com.pocket_plan.j7_003.data.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.databinding.FragmentCalendarBinding

class CalendarFr: Fragment() {
    private var _frBinding: FragmentCalendarBinding? = null
    private val frBinding get() = _frBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _frBinding = FragmentCalendarBinding.inflate(inflater, container, false)
        return frBinding.root
    }
}