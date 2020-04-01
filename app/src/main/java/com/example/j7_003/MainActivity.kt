package com.example.j7_003

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.row_simple.view.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView.adapter = MyAdapter(this)

        btnaddTodoTask.setOnClickListener(){
            Toast.makeText(this, "this worked!", Toast.LENGTH_SHORT).show()
        }
    }

    private class MyAdapter(context: Context) : BaseAdapter(){
        //responsible for how many rows in my list
        private val mContext: Context = context
        private val tasks = arrayListOf<Task>(
            Task("App programmie3ren", 3), Task("Beispie2l", 2), Task("Logo designen1", 1), Task("test2", 2)
        )

        override fun getCount(): Int {
            return tasks.size
        }

        fun sortTasks(){
            tasks.sortBy{
                t -> t.priority
            }
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            sortTasks()
            val layoutInflater = LayoutInflater.from(mContext)
            val row_simple = layoutInflater.inflate(R.layout.row_simple, parent, false)

            row_simple.name_textview.text = tasks[position].title
            row_simple.setOnClickListener(){
                Toast.makeText(mContext, "${tasks[position].title} got duplicated!", Toast.LENGTH_SHORT).show()
                tasks.add(tasks[position])
                notifyDataSetChanged()
                sortTasks()
            }

            row_simple.btnDelete.setOnClickListener(){
                Toast.makeText(mContext, "${tasks[position].title} got deleted!", Toast.LENGTH_SHORT).show()
                tasks.remove(tasks[position])
                notifyDataSetChanged()
                sortTasks()
            }

            return row_simple

        }

        //this can be ignored for now
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        //this can be ignored for now
        override fun getItem(position: Int): Any {
            return "TEST STRING"
        }
    }
}


