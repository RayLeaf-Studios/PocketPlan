package com.example.j7_003.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.Database
import kotlinx.android.synthetic.main.fragment_home.view.*


/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {

    lateinit var myView: View
    lateinit var myDatabase: Database

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myView = inflater.inflate(R.layout.fragment_home, container, false)
        myDatabase = MainActivity.database

        displayTasks(myView)

        return myView
    }

    fun displayTasks(myview: View){
        var p1TaskCounter = 0
        var i = 0
        val taskList = myDatabase.taskList
        for(i in 0..taskList.size-1){
            if(taskList[i].priority>1){
                break
            }
            p1TaskCounter++
        }
        val displayTaskCount = minOf(p1TaskCounter, 3)
        var taskPanelText = ""
        for(i in 0..displayTaskCount-1){
            taskPanelText+=(i+1).toString()+". "+taskList[i].title+"\n"
        }
        val additionalTasks = p1TaskCounter-displayTaskCount
        if(additionalTasks!=0){
            var addedLetter = "s"
            if(additionalTasks==1){
                addedLetter = ""
            }
            taskPanelText+="\n+ "+additionalTasks+" more important task"+addedLetter
        }
        myView.task_panel.text = taskPanelText
    }

}
