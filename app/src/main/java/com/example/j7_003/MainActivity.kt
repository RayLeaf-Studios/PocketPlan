package com.example.j7_003

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import com.example.j7_003.logic.Database
import com.example.j7_003.logic.Task
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.row_simple.view.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val database: Database
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        database = Database()

        val listAdapter = MyAdapter(this, database)
        listView.adapter = listAdapter


        btnAddTodoTask.setOnClickListener{
            Toast.makeText(this, "this worked beginning!", Toast.LENGTH_SHORT).show()
            Toast.makeText(this, "this worked! ${database.taskList.size}", Toast.LENGTH_SHORT).show()
            database.taskList.add(Task("button added this", 1))
            listAdapter.notifyDataSetChanged()
        }
    }

    private class MyAdapter(context: Context, database: Database) : BaseAdapter() {
        //responsible for how many rows in my list,
        private var tasks = database.taskList
        private val mContext: Context = context

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
            val rowSimple = layoutInflater.inflate(R.layout.row_simple, parent, false)

            rowSimple.name_textview.text = tasks[position].title
            rowSimple.setOnClickListener{
                Toast.makeText(mContext, "${tasks[position].title} got duplicated!", Toast.LENGTH_SHORT).show()
                tasks.add(tasks[position])
                notifyDataSetChanged()
                sortTasks()
            }

            rowSimple.btnDelete.setOnClickListener{
                Toast.makeText(mContext, "${tasks[position].title} got deleted!", Toast.LENGTH_SHORT).show()
                tasks.remove(tasks[position])
                notifyDataSetChanged()
                sortTasks()
            }

            return rowSimple

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


