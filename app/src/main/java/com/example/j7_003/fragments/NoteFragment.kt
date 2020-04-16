package com.example.j7_003.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.j7_003.MainActivity

import com.example.j7_003.R
import com.example.j7_003.data.Database
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

        val myView = inflater.inflate(R.layout.fragment_note, container, false)

        val myRecycler = myView.recycler_view_note

        //ADDING NOTE VIA FLOATING ACTION BUTTON
        myView.btnAddNote.setOnClickListener() {
            MainActivity.myActivity.changeToWriteNoteFragment()
            MainActivity.editNoteHolder = null
        }

        val myAdapter = NoteAdapter()

        myRecycler.adapter = myAdapter

        val lm = StaggeredGridLayoutManager(2, 1)
        myRecycler.layoutManager = lm

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
        val position = viewHolder.adapterPosition
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

    fun deleteItem(position: Int){
        Database.deleteNote(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_note, parent, false)
        return NoteViewHolder(itemView)
    }



    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {

        val currentNote = Database.getNote(position)

        //EDITING TASK VIA ONCLICK LISTENER ON RECYCLER ITEMS

        holder.itemView.setOnClickListener(){
            MainActivity.editNoteHolder = holder
            MainActivity.noteColor = Database.getNote(holder.adapterPosition).color
            MainActivity.myActivity.changeToWriteNoteFragment()
        }

        //specifying design of note rows here
        holder.tvNoteTitle.text = currentNote.title
        holder.tvNoteContent.text = currentNote.note

        val cardColor =  when(currentNote.color){
            NoteColors.RED -> R.color.colorNoteRed
            NoteColors.YELLOW -> R.color.colorNoteYellow
            NoteColors.GREEN -> R.color.colorNoteGreen
            NoteColors.BLUE -> R.color.colorNoteBlue
            NoteColors.PURPLE -> R.color.colorNotePurple
        }

        holder.cvNoteCard.setCardBackgroundColor(ContextCompat.getColor(MainActivity.myActivity, cardColor))


    }

    override fun getItemCount() = Database.noteList.size

    //one instance of this class will contain one instance of row_task and meta data like position
    //also holds references to views inside the layout

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNoteTitle: TextView = itemView.tvNoteTitle
        val tvNoteContent: TextView = itemView.tvNoteContent
        var cvNoteCard: CardView = itemView.cvNoteCard
    }

}
