package com.pocket_plan.j7_003.data.notelist

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.data.settings.SettingsManager
import kotlinx.android.synthetic.main.fragment_note.view.*
import kotlinx.android.synthetic.main.row_note.view.*

/**
 * A simple [Fragment] subclass.
 */

class NoteFr : Fragment() {

    companion object {
        var deletedNote: Note? = null
        lateinit var noteAdapter: NoteAdapter
        var noteLines = 0
        val noteListInstance: NoteList = NoteList()
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

    private fun initializeComponents(myView: View) {
        //ADDING NOTE VIA FLOATING ACTION BUTTON
        myView.btnAddNote.setOnClickListener {
            MainActivity.editNoteHolder = null
            MainActivity.act.changeToFragment(FT.NOTE_EDITOR)
        }

        //TODO READ THIS FROM SETTINGS MANAGER
        val noteColumns = SettingsManager.getSetting("noteColumns") as String

        val optionArray = resources.getStringArray(R.array.noteLines)
        noteLines = when (SettingsManager.getSetting("noteLines")) {
            optionArray[1] -> 0
            optionArray[2] -> 1
            optionArray[3] -> 3
            optionArray[4] -> 5
            optionArray[5] -> 10
            optionArray[6] -> 20
            else -> -1
        }

        //initialize Recyclerview and Adapter
        val myRecycler = myView.recycler_view_note
        noteAdapter = NoteAdapter()
        myRecycler.adapter = noteAdapter
        val lm = StaggeredGridLayoutManager(noteColumns.toInt(), 1)
        myRecycler.layoutManager = lm
        myRecycler.setHasFixedSize(true)
    }

}


class NoteAdapter :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    val noteList = NoteFr.noteListInstance

    fun deleteItem(position: Int) {
        NoteFr.deletedNote = noteList.getNote(position)
        MainActivity.act.updateUndoNoteIcon()
        noteList.deleteNote(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_note, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {

        val currentNote = noteList.getNote(position)

        //EDITING TASK VIA ONCLICK LISTENER ON RECYCLER ITEMS

        holder.itemView.setOnClickListener {
            MainActivity.editNoteHolder = currentNote
            MainActivity.noteColor = noteList.getNote(holder.adapterPosition).color
            MainActivity.act.changeToFragment(FT.NOTE_EDITOR)
//                myEtTitle.requestFocus()
//                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, InputMethodManager.SHOW_FORCED)
        }

        //specifying design of note rows here
        holder.tvNoteTitle.text = currentNote.title

        //replace following true condition with setting to display full note content
        holder.tvNoteContent.text = currentNote.content

        //TODO replace the following two values with custom settings
        if (NoteFr.noteLines == -1) {
            holder.tvNoteContent.maxLines = Int.MAX_VALUE
        } else {
            holder.tvNoteContent.maxLines = NoteFr.noteLines
            holder.tvNoteContent.ellipsize = TextUtils.TruncateAt.END
        }

        val cardColor = when (currentNote.color) {
            NoteColors.RED -> R.color.colorNoteRed
            NoteColors.YELLOW -> R.color.colorNoteYellow
            NoteColors.GREEN -> R.color.colorNoteGreen
            NoteColors.BLUE -> R.color.colorNoteBlue
            NoteColors.PURPLE -> R.color.colorNotePurple
        }

        holder.cvNoteCard.setCardBackgroundColor(
            ContextCompat.getColor(
                MainActivity.act,
                cardColor
            )
        )
    }

    override fun getItemCount() = noteList.size

    //one instance of this class will contain one instance of row_task and meta data like position
    //also holds references to views inside the layout
    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNoteTitle: TextView = itemView.tvNoteTitle
        val tvNoteContent: TextView = itemView.tvNoteContent
        var cvNoteCard: CardView = itemView.cvNoteCard
    }
}

