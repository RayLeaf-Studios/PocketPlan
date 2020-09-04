package com.example.j7_003.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.NoteColors
import com.example.j7_003.data.database.Database
import com.example.j7_003.data.database.database_objects.Note
import kotlinx.android.synthetic.main.fragment_note.view.*
import kotlinx.android.synthetic.main.row_note.view.*

/**
 * A simple [Fragment] subclass.
 */

class NoteFragment : Fragment() {

    companion object{
        var deletedNote: Note? = null
        lateinit var noteAdapter: NoteAdapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //inflating layout for NoteFragment
        val myView = inflater.inflate(R.layout.fragment_note, container, false)
        initializeComponents(myView)
        return myView
    }

    private fun initializeComponents(myView: View){

        //ADDING NOTE VIA FLOATING ACTION BUTTON
        myView.btnAddNote.setOnClickListener {
            MainActivity.act.changeToCreateNoteFragment()
            MainActivity.editNoteHolder = null
        }

        //TODO READ THIS FROM SETTINGS MANAGER
        val noteColumns = 3

        //initialize Recyclerview and Adapter
        val myRecycler = myView.recycler_view_note
        noteAdapter = NoteAdapter()
        myRecycler.adapter = noteAdapter
        val lm = StaggeredGridLayoutManager(noteColumns, 1)
        myRecycler.layoutManager = lm
        myRecycler.setHasFixedSize(true)

        //initialize item touch helper to support swipe to delete
        val swipeHelperLeft = ItemTouchHelper(SwipeLeftToDeleteN(noteAdapter))
        swipeHelperLeft.attachToRecyclerView(myRecycler)

        val swipeHelperRight = ItemTouchHelper(SwipeRightToDeleteN(noteAdapter))
        swipeHelperRight.attachToRecyclerView(myRecycler)
    }

}


class NoteAdapter :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>(){

    fun deleteItem(position: Int){
        NoteFragment.deletedNote = Database.getNote(position)
        MainActivity.act.updateUndoNoteIcon()
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

        holder.itemView.setOnClickListener {
            MainActivity.editNoteHolder = holder
            MainActivity.noteColor = Database.getNote(holder.adapterPosition).color
            MainActivity.act.changeToCreateNoteFragment()
        }

        //specifying design of note rows here
        holder.tvNoteTitle.text = currentNote.title

        //replace following true condition with setting to display full note content
        holder.tvNoteContent.text = currentNote.content

        //TODO replace the following two values with custom settings
        val displayedLines = 5
        val limitDisplay = true

        if(limitDisplay){
            holder.tvNoteContent.maxLines = displayedLines
            holder.tvNoteContent.ellipsize = TextUtils.TruncateAt.END
        }else{
            holder.tvNoteContent.maxLines = Int.MAX_VALUE
        }

        val cardColor =  when(currentNote.color){
            NoteColors.RED -> R.color.colorNoteRed
            NoteColors.YELLOW -> R.color.colorNoteYellow
            NoteColors.GREEN -> R.color.colorNoteGreen
            NoteColors.BLUE -> R.color.colorNoteBlue
            NoteColors.PURPLE -> R.color.colorNotePurple
        }

        holder.cvNoteCard.setCardBackgroundColor(ContextCompat.getColor(MainActivity.act, cardColor))
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
