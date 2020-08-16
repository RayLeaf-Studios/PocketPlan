package com.example.j7_003.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment

import androidx.constraintlayout.widget.ConstraintLayout
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.database.CalendarManager
import kotlinx.android.synthetic.main.fragment_create_term.*


import kotlinx.android.synthetic.main.fragment_create_term.view.*
import kotlinx.android.synthetic.main.fragment_create_term.view.btnDiscardTermChanges
import kotlinx.android.synthetic.main.fragment_create_term.view.tvTermEndTime
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime


import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class CreateTermFragment : Fragment() {

    lateinit var etTermTitle: EditText
    lateinit var etTermInfo: EditText
    lateinit var tvTermDate: TextView
    lateinit var tvTermTime: TextView
    lateinit var tvTermEndTime: TextView

    lateinit var panelTermDate: ConstraintLayout
    lateinit var panelTermTime: ConstraintLayout
    lateinit var panelTermEndTime: ConstraintLayout

    lateinit var btnDuration30m: Button
    lateinit var btnDuration60m: Button
    lateinit var btnDuration90m: Button
    lateinit var btnDuration120m: Button
    lateinit var btnDuration180m: Button

    lateinit var btnDiscardTermChanges: Button
    lateinit var btnSaveTerm: Button
    lateinit var myView: View

    lateinit var startDateTime: LocalDateTime
    lateinit var endLocalTime: LocalTime


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myView = inflater.inflate(R.layout.fragment_create_term, container, false)
        initComponents()
        showDefaultValues()
        return myView
    }

    @SuppressLint("SetTextI18n")
    fun showDefaultValues() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        startDateTime = LocalDateTime.of(year, month+1, day, hour, minute)
        endLocalTime = LocalTime.of(hour, minute)


        tvTermDate.text = day.toString().padStart(2, '0') + "." + (month+1).toString()
            .padStart(2, '0') + "." + year.toString()
        tvTermTime.text = hour.toString()
            .padStart(2, '0') + ":" + minute.toString().padStart(2, '0')
        tvTermEndTime.text = "(optional)"
    }

    @SuppressLint("SetTextI18n")
    fun setDuration(minutes: Int) {
        val newEndTime = LocalTime.of(startDateTime.hour, startDateTime.minute).plusMinutes(minutes.toLong())
        if(newEndTime.isAfter(LocalTime.of(startDateTime.hour, startDateTime.minute))){
            endLocalTime = LocalTime.of(newEndTime.hour, newEndTime.minute)
            tvTermEndTime.text = endLocalTime.hour.toString()
                .padStart(2, '0') + ":" + endLocalTime.minute.toString().padStart(2, '0')
        }
    }

    fun saveTerm() {
        val termTitle = etTermTitle.text.toString()
        val termInfo = etTermInfo.text.toString()
        CalendarManager.addAppointment(termTitle, termInfo, startDateTime, endLocalTime)
        if(MainActivity.fromHome){
            MainActivity.myActivity.changeToHome()
        }else{
            MainActivity.myActivity.changeToDayView()
        }
        MainActivity.fromHome = false
    }

    @SuppressLint("SetTextI18n")
    fun openDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            startDateTime = startDateTime.withYear(year).withMonth(month+1).withDayOfMonth(day)
            tvTermDate.text = day.toString().padStart(2, '0') + "." + (month+1).toString()
                .padStart(2, '0') + "." + year.toString()
        }
        val dpd = DatePickerDialog(MainActivity.myActivity, dateSetListener, startDateTime.year, startDateTime.monthValue-1, startDateTime.dayOfMonth)
        dpd.show()
    }


    @SuppressLint("SetTextI18n")
    fun openTimePickerStart() {
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _: TimePicker?, h: Int, m: Int ->
            startDateTime = startDateTime.withHour(h).withMinute(m)
            tvTermTime.text = h.toString()
                .padStart(2, '0') + ":" + m.toString().padStart(2, '0')
        }
        val tpd = TimePickerDialog(MainActivity.myActivity, timeSetListener, startDateTime.hour, startDateTime.minute, true)
        tpd.show()
    }
    @SuppressLint("SetTextI18n")
    fun openTimePickerEnd() {
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _: TimePicker?, h: Int, m: Int ->
            endLocalTime = LocalTime.of(h,m)
            if(endLocalTime.isBefore(LocalTime.of(startDateTime.hour, startDateTime.minute))){
               endLocalTime = LocalTime.of(startDateTime.hour, startDateTime.minute)
            }
            tvTermEndTime.text = endLocalTime.hour.toString()
                .padStart(2, '0') + ":" + endLocalTime.minute.toString().padStart(2, '0')
        }
        val tpd = TimePickerDialog(MainActivity.myActivity, timeSetListener, startDateTime.hour, startDateTime.minute, true)
        tpd.show()
    }

    fun initComponents() {
        //input for title and info
        etTermTitle = myView.etTermTitle
        etTermInfo = myView.etTermInfo

        //display of date and time
        tvTermDate = myView.tvTermDate
        tvTermTime = myView.tvTermTime
        tvTermEndTime = myView.tvTermEndTime

        //panels to tap to pick date and time
        panelTermDate = myView.panelTermDate
        panelTermTime = myView.panelTermTime
        panelTermEndTime = myView.panelTermEndTime

        //buttons for quick duration
        btnDuration30m = myView.btnDuration30m
        btnDuration60m = myView.btnDuration60m
        btnDuration90m = myView.btnDuration90m
        btnDuration120m = myView.btnDuration120m
        btnDuration180m = myView.btnDuration180m

        //button to save term
        btnSaveTerm = myView.btnSaveTerm
        btnDiscardTermChanges = myView.btnDiscardTermChanges

        //duration button onclick listeners
        btnDuration30m.setOnClickListener {
            setDuration(30)
        }

        btnDuration60m.setOnClickListener{
            setDuration(60)
        }

        btnDuration90m.setOnClickListener {
            setDuration(90)
        }

        btnDuration120m.setOnClickListener {
            setDuration(120)
        }

        btnDuration180m.setOnClickListener {
            setDuration(180)
        }

        //time and date panels
        panelTermDate.setOnClickListener {
            openDatePicker()
        }

        panelTermTime.setOnClickListener {
            openTimePickerStart()
        }
        panelTermEndTime.setOnClickListener {
            openTimePickerEnd()
        }

        btnSaveTerm.setOnClickListener() {
            saveTerm()
        }

        btnDiscardTermChanges.setOnClickListener(){
            if(MainActivity.fromHome){
                MainActivity.myActivity.changeToHome()
            }else{
                MainActivity.myActivity.changeToDayView()
            }
            MainActivity.fromHome = false
        }

    }

}
