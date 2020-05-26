package com.example.j7_003.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j7_003.MainActivity

import com.example.j7_003.R
import com.example.j7_003.data.database.CalendarManager
import com.example.j7_003.data.database.database_objects.CalendarAppointment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_day.view.*
import kotlinx.android.synthetic.main.row_term.view.*
import org.threeten.bp.LocalDate

/**
 * A simple [Fragment] subclass.
 */
class DayFragment : Fragment() {

    companion object{
        lateinit var dayList: ArrayList<CalendarAppointment>
    }

    lateinit var myView: View
    lateinit var btnAddTermDay: FloatingActionButton
    lateinit var btnPreviousDay: Button
    lateinit var btnNextDay: Button
    lateinit var tvDayViewTitle: TextView
    lateinit var date: LocalDate
    lateinit var myAdapter: TermAdapterDay

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        CalendarManager.init()
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_day, container, false)

        btnAddTermDay = myView.btnAddTermDay
        btnPreviousDay = myView.btnPreviousDay
        btnNextDay = myView.btnNextDay
        tvDayViewTitle = myView.tvDayViewTitle

        date = LocalDate.now()

        updateDayViewTitle()
        updateContentList()


        btnAddTermDay.setOnClickListener {
            changeToCreateTermFragment()
        }

        btnPreviousDay.setOnClickListener{
            date = date.minusDays(1)
            updateContentList()
            updateDayViewTitle()
            myAdapter.notifyDataSetChanged()
            //todo change recycler content and day title to previous day
        }
        btnNextDay.setOnClickListener{
            date = date.plusDays(1)
            updateContentList()
            updateDayViewTitle()
            myAdapter.notifyDataSetChanged()
            //todo change recycler content and day title to next day
        }

        //initialize recycler + adapter
        val myRecycler = myView.recylcer_view_day
        myAdapter = TermAdapterDay()

        myRecycler.adapter = myAdapter
        myRecycler.layoutManager = LinearLayoutManager(MainActivity.myActivity)
        myRecycler.setHasFixedSize(true)
        return myView
    }

    fun updateContentList(){
        dayList = CalendarManager.getDayView(date)
    }



    fun updateDayViewTitle(){
        tvDayViewTitle.text = date.dayOfWeek.toString().substring(0,1)+
                date.dayOfWeek.toString().substring(1,2).decapitalize()+
                " "+date.dayOfMonth+"."+date.monthValue
    }

    fun changeToCreateTermFragment() {
        MainActivity.myActivity.changeToCreateTerm()
    }

}

class TermAdapterDay() :
    RecyclerView.Adapter<TermAdapterDay.TermViewHolderDay>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TermViewHolderDay {
        val itemView = LayoutInflater.from(parent.context)
                //TODO CHANGE row_term_day to fit display of day term
            .inflate(R.layout.row_term_day, parent, false)
        return TermViewHolderDay(itemView)
    }


    override fun onBindViewHolder(holder: TermViewHolderDay, position: Int) {

        //TODO get position-th term of current inspected day here
        val currentTerm = DayFragment.dayList[position]

        holder.itemView.setOnClickListener() {
            //todo start CreateTermFragment in EDIT mode
            MainActivity.myActivity.changeToCreateTerm()
        }

        //todo make these attributes specific for dayView
        holder.tvTitle.text = currentTerm.title
        holder.tvInfo.text = currentTerm.addInfo
        holder.tvTime.text = currentTerm.startTime.toString()
        val date: LocalDate = currentTerm.date
        val dayOfWeekString = date.dayOfWeek.toString().substring(0, 1) + date.dayOfWeek.toString()
            .substring(1, 2).decapitalize()
        val month = date.monthValue.toString()
        val day = date.dayOfMonth.toString()
        holder.tvDate.text = MainActivity.myActivity.getString(
            R.string.termItemDate,
            dayOfWeekString, day, month
        )
    }

    //TODO return number of terms of current inspected day here
    override fun getItemCount() = DayFragment.dayList.size


    class TermViewHolderDay(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /**
         * One instance of this class will contain one "instance" of row_term and meta data
         * like position, it also holds references to views inside of the layout
         */
        //todo only use necessary dayview attributes
        val tvTitle = itemView.tvTermItemTitle
        val tvInfo = itemView.tvTermItemInfo
        val tvDate = itemView.tvTermItemDate
        val tvTime = itemView.tvTermItemTime
        //var myView = itemView
    }

}
