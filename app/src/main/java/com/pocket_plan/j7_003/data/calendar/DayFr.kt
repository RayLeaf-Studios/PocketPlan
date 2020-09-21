package com.pocket_plan.j7_003.data.calendar

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_day.view.*
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit
import java.util.*

class DayFr : Fragment() {

    /*
        companion object holds reference to itself, its viewpager and current date
    */
    companion object {
        lateinit var dayFragment: DayFr
        lateinit var dayPager: ViewPager2
        lateinit var date: LocalDate
    }

    private lateinit var myView: View
    private lateinit var btnAddTermDay: FloatingActionButton
    private lateinit var tvDayViewTitle: TextView
    private lateinit var tvYear: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //initialize CalendarManager
        CalendarManager.init()

        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_day, container, false)
        dayFragment = this

        //initialize references to gui elements
        btnAddTermDay = myView.btnAddTermDay
        tvDayViewTitle = myView.tvDayViewTitle
        tvYear = myView.tvYear

        date = LocalDate.now()

        //initializes viewpager
        dayPager = myView.dayPager
        val pagerAdapter = ScreenSlidePagerAdapter(MainActivity.act)
        dayPager.adapter = pagerAdapter

        //pageChangeCallback to react to scrolling
        val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                changeToPosition(position)
            }
        }
        dayPager.registerOnPageChangeCallback(pageChangeCallback)


        //starts in the middle for "infinite" scrolling left / right
        dayPager.setCurrentItem(Int.MAX_VALUE / 2, false)

        //onclick listener to change current date by clicking on the date up top
        tvDayViewTitle.setOnClickListener {
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                date = date.withYear(year).withMonth(month + 1).withDayOfMonth(day)
                val delta = ChronoUnit.DAYS.between(LocalDate.now(), date).toInt()
                val newPosition = (Int.MAX_VALUE / 2) + delta
                dayPager.currentItem = newPosition
                updateDayViewTitle()
            }
            val dpd = DatePickerDialog(
                MainActivity.act,
                dateSetListener,
                date.year,
                date.monthValue - 1,
                date.dayOfMonth
            )
            dpd.show()
        }

        //button to create a new term
        btnAddTermDay.setOnClickListener {
            changeToCreateTermFragment()
        }

        //initialize fragment with current date
        updateDayViewTitle()
        return myView
    }


    //adapter for viewpager2
    private class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = Int.MAX_VALUE

        override fun createFragment(position: Int): Fragment {
            return TestFragment.newInstance(position)
        }

    }

    //switches the current dayList to the dayList of a date according to a position
    fun changeToPosition(position: Int) {
        val delta = -(Int.MAX_VALUE / 2 - position)
        val newDate: LocalDate
        newDate = LocalDate.now().plusDays(delta.toLong())
        changeDateTo(newDate)
    }

    //switches the current dayList to the dayList of the passed date
    @SuppressLint("SetTextI18n")
    fun changeDateTo(newDate: LocalDate) {
        val monthString = newDate.month.toString()

        tvDayViewTitle.text = newDate.dayOfWeek.toString().substring(0, 1) +
                newDate.dayOfWeek.toString().substring(1, 2).toLowerCase(Locale.ROOT) +
                " " + newDate.dayOfMonth + ". " + monthString.substring(0, 1) +
                monthString.substring(1, 3).toLowerCase(Locale.ROOT)
        tvYear.text = newDate.year.toString()
        date = newDate
    }

    @SuppressLint("SetTextI18n")
    fun updateDayViewTitle() {
        val monthString = date.month.toString()

        tvDayViewTitle.text = date.dayOfWeek.toString().substring(0, 1) +
                date.dayOfWeek.toString().substring(1, 2).toLowerCase(Locale.ROOT) +
                " " + date.dayOfMonth + ". " + monthString.substring(0, 1) +
                monthString.substring(1, 3).toLowerCase(Locale.ROOT)
        tvYear.text = date.year.toString()
    }

    private fun changeToCreateTermFragment() {
        MainActivity.act.changeToFragment(FT.CREATE_TERM)
    }
}



