package com.pocket_plan.j7_003.data.notelist

import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.SearchView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.birthdaylist.BirthdayFr
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.data.notelist.NoteEditorFr.Companion.noteColor
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import kotlinx.android.synthetic.main.fragment_note.view.*
import kotlinx.android.synthetic.main.row_note.view.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 */

class NoteFr : Fragment() {

    lateinit var myMenu: Menu
    lateinit var searchView: SearchView
    companion object {
        lateinit var myAdapter: NoteAdapter
        var noteLines = 0
        val noteListInstance: NoteList = NoteList()
        lateinit var myFragment: NoteFr

        var searching = false
        lateinit var adjustedList: ArrayList<Note>
        lateinit var lastQuery: String
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_notes, menu)
        myMenu = menu
        adjustedList = arrayListOf()

        searchView = menu.findItem(R.id.item_notes_search).actionView as SearchView
        val textListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                //todo fix this
                //close keyboard?
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (searching) {
                    myFragment.search(newText.toString())
                }
                return true
            }
        }

        searchView.setOnQueryTextListener(textListener)

        val onCloseListener = SearchView.OnCloseListener {
            MainActivity.toolBar.title = getString(R.string.menuTitleNotes)
            searchView.onActionViewCollapsed()
            searching = false
            myAdapter.notifyDataSetChanged()
            true
        }

        searchView.setOnCloseListener(onCloseListener)

        searchView.setOnSearchClickListener {
            MainActivity.toolBar.title = ""
            searching = true
            adjustedList.clear()
            myAdapter.notifyDataSetChanged()
        }

        updateNoteSearchIcon()
        super.onCreateOptionsMenu(menu, inflater)
    }

    fun updateNoteSearchIcon(){
        myMenu.findItem(R.id.item_notes_search).isVisible = NoteFr.noteListInstance.size > 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    fun search(query: String) {
        if (query == "") {
            adjustedList.clear()
        } else {
            lastQuery = query
            adjustedList.clear()
            noteListInstance.forEach {note ->
                if (note.content.toLowerCase(Locale.ROOT)
                        .contains(query.toLowerCase(Locale.ROOT)) ||
                    note.title.toLowerCase(Locale.ROOT)
                        .contains(query.toLowerCase(Locale.ROOT))
                ) {
                    adjustedList.add(note)
                }
            }
        }
        myAdapter.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.item_notes_search -> {
                /* no-op, listeners for this view are implemented in onCreateOptionsMenu */
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        myFragment = this
        //inflating layout for NoteFragment
        val myView = inflater.inflate(R.layout.fragment_note, container, false)
        initializeComponents(myView)
        return myView
    }

    private fun initializeComponents(myView: View) {
        //TODO READ THIS FROM SETTINGS MANAGER
        val noteColumns = SettingsManager.getSetting(SettingId.NOTE_COLUMNS) as String

        val setting = SettingsManager.getSetting(SettingId.NOTE_LINES) as Double
        noteLines = setting.toInt()

        //initialize Recyclerview and Adapter
        val myRecycler = myView.recycler_view_note
        myAdapter = NoteAdapter()
        myRecycler.adapter = myAdapter
        val lm = StaggeredGridLayoutManager(noteColumns.toInt(), 1)
        myRecycler.layoutManager = lm
        myRecycler.setHasFixedSize(true)
    }

}


class NoteAdapter :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_note, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {


        val currentNote = when (NoteFr.searching) {
            /**
             * NoteFr is currently in search mode, current note gets grabbed from
             * NoteFr. adjusted list
             */
            true -> NoteFr.adjustedList[position]
            /**
             * NoteFr is currently in normal mode, current note gets grabbed from noteList
             */
            false -> NoteFr.noteListInstance.getNote(position)
        }


        //EDITING TASK VIA ONCLICK LISTENER ON RECYCLER ITEMS
        holder.itemView.setOnClickListener {
            MainActivity.editNoteHolder = currentNote
            noteColor = NoteFr.noteListInstance.getNote(holder.adapterPosition).color
            MainActivity.act.changeToFragment(FT.NOTE_EDITOR)
        }

        //specifying design of note rows here
        holder.tvNoteTitle.text = currentNote.title

        //replace following true condition with setting to display full note content
        holder.tvNoteContent.text = currentNote.content

        //decide how many lines per note are shown, depending on teh setting noteLines
        if (NoteFr.noteLines == -1) {
            holder.tvNoteContent.maxLines = Int.MAX_VALUE
        } else {
            holder.tvNoteContent.maxLines = NoteFr.noteLines
            holder.tvNoteContent.ellipsize = TextUtils.TruncateAt.END
        }

        //set background color depending on currentNote.color
        holder.cvNoteCard.setCardBackgroundColor(currentNote.color.resolved)
    }

    override fun getItemCount(): Int {
        return when (NoteFr.searching) {
            true -> NoteFr.adjustedList.size
            false -> NoteFr.noteListInstance.size
        }
    }

    //one instance of this class will contain one instance of row_task and meta data like position
    //also holds references to views inside the layout
    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNoteTitle: TextView = itemView.tvNoteTitle
        val tvNoteContent: TextView = itemView.tvNoteContent
        var cvNoteCard: CardView = itemView.cvNoteCard
    }
}

