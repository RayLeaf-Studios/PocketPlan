package com.example.j7_003.fragments

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.database.CalendarManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_day.view.*
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit

class DayFragment : Fragment() {

    /*
        companion object holds reference to itself, its viewpager and current date
    */
    companion object{
        lateinit var dayFragment: DayFragment
        lateinit var dayPager: ViewPager2
        lateinit var date: LocalDate
    }

    lateinit var myView: View
    lateinit var btnAddTermDay: FloatingActionButton
    lateinit var tvDayViewTitle: TextView
    lateinit var tvYear: TextView

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
        val pagerAdapter = ScreenSlidePagerAdapter(MainActivity.myActivity)
        dayPager.adapter = pagerAdapter

        //pageChangeCallback to react to scrolling
        val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                changeToPosition(position)
            }
        }
        dayPager.registerOnPageChangeCallback(pageChangeCallback)


        //starts in the middle for "infinite" scrolling left / right
        dayPager.setCurrentItem(Int.MAX_VALUE/2, false)

        //onclick listener to change current date by clicking on the date up top
        tvDayViewTitle.setOnClickListener {
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                date = date.withYear(year).withMonth(month+1).withDayOfMonth(day)
                val delta = ChronoUnit.DAYS.between(LocalDate.now(), date).toInt()
                val newPosition = (Int.MAX_VALUE / 2) + delta
                dayPager.setCurrentItem(newPosition)
                updateDayViewTitle()
            }
            val dpd = DatePickerDialog(MainActivity.myActivity, dateSetListener, date.year, date.monthValue-1, date.dayOfMonth)
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
    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = Int.MAX_VALUE

            override fun createFragment(position: Int): Fragment{
            return TestFragment.newInstance(position)
        }

    }

    //switches the current daylist to the daylist of a date according to a position
    fun changeToPosition(position: Int){
        val delta = -(Int.MAX_VALUE/2 - position)
        val newDate: LocalDate
        newDate = LocalDate.now().plusDays(delta.toLong())
        changeDateTo(newDate)
    }

    //switches the current daylist to the daylist of the passed date
    fun changeDateTo(newDate: LocalDate){
        val monthString = newDate.month.toString()

        tvDayViewTitle.text = newDate.dayOfWeek.toString().substring(0,1)+
                newDate.dayOfWeek.toString().substring(1,2).decapitalize()+
                " "+newDate.dayOfMonth+". "+ monthString.substring(0,1)+
                monthString.substring(1,3).toLowerCase()
        tvYear.text = newDate.year.toString()
        date = newDate
    }

    fun updateDayViewTitle(){
        val monthString = date.month.toString()

        tvDayViewTitle.text = date.dayOfWeek.toString().substring(0,1)+
                date.dayOfWeek.toString().substring(1,2).decapitalize()+
                " "+date.dayOfMonth+". "+ monthString.substring(0,1)+
                monthString.substring(1,3).toLowerCase()
        tvYear.text = date.year.toString()
    }

    fun changeToCreateTermFragment() {
        MainActivity.myActivity.changeToCreateTerm()
    }

}



