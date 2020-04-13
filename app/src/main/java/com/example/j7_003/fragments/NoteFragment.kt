package com.example.j7_003.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j7_003.MainActivity

import com.example.j7_003.R
import com.example.j7_003.data.NoteColors
import kotlinx.android.synthetic.main.fragment_note.view.*
import kotlinx.android.synthetic.main.row_note.view.*


/**
 * A simple [Fragment] subclass.
 */
class NoteFragment : Fragment() {

    companion object{

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val database = MainActivity.database

        val myView = inflater.inflate(R.layout.fragment_note, container, false)

        val myRecycler = myView.recycler_view_note

        //ADDING NOTE VIA FLOATING ACTION BUTTON
        myView.btnAddNote.setOnClickListener() {
            MainActivity.myActivity.changeToWriteNoteFragment()

        }

        val myAdapter = NoteAdapter()

        myRecycler.adapter = myAdapter

        myRecycler.layoutManager = LinearLayoutManager(activity)

        myRecycler.setHasFixedSize(true)

        val swipeHelperLeft = ItemTouchHelper(SwipeLeftToDeleteN(myAdapter))
        swipeHelperLeft.attachToRecyclerView(myRecycler)

        val swipeHelperRight = ItemTouchHelper(SwipeRightToDeleteN(myAdapter))
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
        val position = viewHolder.adapterPosition
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        //parent is Recyclerview the view holder will be placed in
        //context is activity that the recyclerview is placed in
        //parent in inflate tells the inflater where the layout will be placed
        //so it can be inflated to the right size
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_note, parent, false)
        return NoteViewHolder(itemView)
    }


    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {

        val currentNote = database.getNote(position)

        //EDITING TASK VIA ONCLICK LISTENER ON RECYCLER ITEMS

        holder.itemView.setOnClickListener(){
            MainActivity.myActivity.changeToWriteNoteFragment()
        }

        //specifying design of note rows here
        holder.tvNoteTitle.text = currentNote.title
        holder.tvNoteContent.text = currentNote.note
        when(currentNote.color){
            NoteColors.RED -> holder.notePane.background = ColorDrawable(R.color.colorNoteRed)
            NoteColors.BLUE -> holder.notePane.background = ColorDrawable(R.color.colorNoteBlue)
            NoteColors.YELLOW -> holder.notePane.background = ColorDrawable(R.color.colorNoteYellow)
            NoteColors.GREEN -> holder.notePane.background = ColorDrawable(R.color.colorNoteGreen)
            NoteColors.PURPLE -> holder.notePane.background = ColorDrawable(R.color.colorNotePurple)
        }


    }

    override fun getItemCount() = noteList.size

    //one instance of this class will contain one instance of row_task and meta data like position
    //also holds references to views inside the layout

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNoteTitle = itemView.tvNoteTitle
        val tvNoteContent = itemView.tvNoteContent
        val notePane = itemView.notePane

    }

}
