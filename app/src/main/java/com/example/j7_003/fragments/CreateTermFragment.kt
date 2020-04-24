package com.example.j7_003.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.database.Database
import kotlinx.android.synthetic.main.dialog_add_task.view.*
import kotlinx.android.synthetic.main.fragment_create_term.view.*
import kotlinx.android.synthetic.main.fragment_write_note.*
import kotlinx.android.synthetic.main.title_dialog_add_task.view.*
import org.w3c.dom.Text

/**
 * A simple [Fragment] subclass.
 */
class CreateTermFragment : Fragment() {

    lateinit var etTermTitle: EditText
    lateinit var etTermInfo: EditText
    lateinit var tvTermDate: TextView
    lateinit var tvTermTime: TextView

    lateinit var panelTermDate: ConstraintLayout
    lateinit var panelTermTime: ConstraintLayout
    lateinit var panelTermEndTime: ConstraintLayout

    lateinit var btnDuration30m: Button
    lateinit var btnDuration90m: Button
    lateinit var btnDuration120m: Button
    lateinit var btnDuration180m: Button

    lateinit var btnSaveTerm: Button
    lateinit var myView: View


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myView = inflater.inflate(R.layout.fragment_create_term, container, false)

        //input for title and info
        etTermTitle = myView.etTermTitle
        etTermInfo = myView.etTermInfo

        //display of date and time
         tvTermDate = myView.tvTermDate
         tvTermTime = myView.tvTermTime

        //panels to tap to pick date and time
         panelTermDate = myView.panelTermDate
         panelTermTime = myView.panelTermTime
         panelTermEndTime = myView.panelTermEndTime

        //buttons for quick duration
         btnDuration30m = myView.btnDuration30m
         btnDuration90m = myView.btnDuration90m
         btnDuration120m = myView.btnDuration120m
         btnDuration180m = myView.btnDuration180m

        //button to save term
         btnSaveTerm = myView.btnSaveTerm

        //duration button onclick listeners
        btnDuration30m.setOnClickListener{
            addDuration(30)
        }

        btnDuration90m.setOnClickListener{
            addDuration(90)
        }

        btnDuration120m.setOnClickListener{
            addDuration(120)
        }

        btnDuration180m.setOnClickListener{
            addDuration(180)
        }

        //time and date panels
        panelTermDate.setOnClickListener{
            openDatePicker()
        }

        panelTermTime.setOnClickListener{
            openTimePicker()
        }
        panelTermEndTime.setOnClickListener{
            openTimePicker() //todo handle this differently than start time
        }

        btnSaveTerm.setOnClickListener(){
            saveTerm()
        }
        return myView
    }

    fun addDuration(minutes: Int){

    }

    fun saveTerm(){
        val termTitle = etTermTitle.text
        val termInfo = etTermInfo.text
        MainActivity.myActivity.changeToCalendar()
    }

    fun openDatePicker(){
        val dateSetListener = DatePickerDialog.OnDateSetListener{datePicker, year, month, day ->
            tvTermDate.text = day.toString().padStart(2, '0')+"."+month.toString()
                .padStart(2, '0')+"."+ year.toString()
        }
        val dpd = DatePickerDialog(MainActivity.myActivity, dateSetListener, 2000, 3, 3)
        dpd.show()
    }





    fun openTimePicker(){
        val timeSetListener = TimePickerDialog.OnTimeSetListener{v: TimePicker?, h: Int, m: Int ->
           tvTermTime.text = h.toString()
               .padStart(2, '0') +":"+ m.toString().padStart(2, '0')
        }

        //todo initialize with current time
        val tpd = TimePickerDialog(MainActivity.myActivity, timeSetListener, 5, 5, true)
        tpd.show()
    }

}
