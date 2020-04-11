package com.example.j7_003.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j7_003.MainActivity

import com.example.j7_003.R
import kotlinx.android.synthetic.main.addtask_dialog.view.*
import kotlinx.android.synthetic.main.addtask_dialog_title.view.*
import kotlinx.android.synthetic.main.fragment_note_framgent.view.*
import kotlinx.android.synthetic.main.row_task.view.*

/**
 * A simple [Fragment] subclass.
 */
class NoteFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val database = MainActivity.database

        val myView = inflater.inflate(R.layout.fragment_note_framgent, container, false)

        val myRecycler = myView.recycler_view_note

        //ADDING NOTE VIA FLOATING ACTION BUTTON
        myView.btnAddNote.setOnClickListener() {
            //TODO SWITCH INTO ADD NOTE WINDOW HERE

        }

        val myAdapter = NoteAdapter()

        myRecycler.adapter = myAdapter

        myRecycler.layoutManager = LinearLayoutManager(activity)

        myRecycler.setHasFixedSize(true)

        var swipeHelperLeft = ItemTouchHelper(SwipeLeftToDeleteN(myAdapter))
        swipeHelperLeft.attachToRecyclerView(myRecycler)

        var swipeHelperRight = ItemTouchHelper(SwipeRightToDeleteN(myAdapter))
        swipeHelperRight.attachToRecyclerView(myRecycler)

        return myView
    }

}

class SwipeRightToDeleteN(var adapter: NoteAdapter):
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        var position = viewHolder.adapterPosition
        adapter.deleteItem(position)
    }
}

class SwipeLeftToDeleteN(private var adapter: NoteAdapter):
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        var position = viewHolder.adapterPosition
        adapter.deleteItem(position)
    }
}

class NoteAdapter() :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>(){
    private val database = MainActivity.database
    private val noteList = database.noteList

    fun deleteItem(position: Int){
        database.deleteNote(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteAdapter.NoteViewHolder {
        //parent is Recyclerview the view holder will be placed in
        //context is activity that the recyclerview is placed in
        //parent in inflate tells the inflater where the layout will be placed
        //so it can be inflated to the right size
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_note, parent, false)
        return NoteViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {

        val currentTask = database.getNote(position)
        val activity = MainActivity.myActivity


        //EDITING TASK VIA ONCLICK LISTENER ON RECYCLER ITEMS
        holder.itemView.setOnClickListener(){
            //TODO OPEN EDIT TASK VIEW HERE
        }

        //todo specify design of note rows here
        //holder.name_textview.text = currentTask.title



    }

    override fun getItemCount() = noteList.size

    //one instance of this class will contain one instance of row_task and meta data like position
    //also holds references to views inside the layout

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //todo specify needed attributes for row elements (dependent on itemView)
//        val name_textview: TextView = itemView.name_textview
//        var myView = itemView
    }

}
