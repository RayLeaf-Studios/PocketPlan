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
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.database.CalendarManager
import com.example.j7_003.data.database.database_objects.CalendarAppointment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_day.view.*
import kotlinx.android.synthetic.main.row_term.view.tvTermItemInfo
import kotlinx.android.synthetic.main.row_term.view.tvTermItemTitle
import kotlinx.android.synthetic.main.row_term_day.view.*
import org.threeten.bp.LocalDate


private const val MIN_SCALE = 0.85f
private const val MIN_ALPHA = 0.5f

/**
 * A simple [Fragment] subclass.
 */
class DayFragment : Fragment() {

    companion object{
        lateinit var dayList: ArrayList<CalendarAppointment>
        lateinit var dayFragment: DayFragment
        lateinit var dayPager: ViewPager2
        lateinit var date: LocalDate
    }

    lateinit var myView: View
    lateinit var btnAddTermDay: FloatingActionButton
    lateinit var tvDayViewTitle: TextView
    lateinit var tvYear: TextView

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        CalendarManager.init()
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_day, container, false)

        dayFragment = this

        btnAddTermDay = myView.btnAddTermDay
        tvDayViewTitle = myView.tvDayViewTitle
        tvYear = myView.tvYear

        date = LocalDate.now()

        dayPager = myView.dayPager
        val pagerAdapter = ScreenSlidePagerAdapter(MainActivity.myActivity)
        dayPager.adapter = pagerAdapter

        val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                changeToPosition(position)
            }
        }
        dayPager.registerOnPageChangeCallback(pageChangeCallback)


        dayPager.setCurrentItem(Int.MAX_VALUE/2, false)

        tvDayViewTitle.setOnClickListener {
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                date = date.withYear(year).withMonth(month+1).withDayOfMonth(day)
                val newPosition = (Int.MAX_VALUE / 2) + LocalDate.now().until(date).days
                dayPager.setCurrentItem(newPosition)
                update()
            }
            val dpd = DatePickerDialog(MainActivity.myActivity, dateSetListener, date.year, date.monthValue-1, date.dayOfMonth)
            dpd.show()
        }

        btnAddTermDay.setOnClickListener {
            changeToCreateTermFragment()
        }

        //initialize recycler + adapter
        //val myRecycler = myView.recylcer_view_day

       // myRecycler.adapter = myAdapter
       // myRecycler.layoutManager = LinearLayoutManager(MainActivity.myActivity)
       // myRecycler.setHasFixedSize(true)

        update()

        return myView
    }



    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = Int.MAX_VALUE

            override fun createFragment(position: Int): Fragment{
            return TestFragment.newInstance(position)
        }

    }

    fun update(){
        updateDayViewTitle()
    }

    fun changeToPosition(position: Int){
        val delta = -(Int.MAX_VALUE/2 - position)
        val newDate: LocalDate
        newDate = LocalDate.now().plusDays(delta.toLong())
        changeDateTo(newDate)
    }

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



