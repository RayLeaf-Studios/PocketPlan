package com.pocket_plan.j7_003.data.notelist

import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.SearchView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
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

class NoteFr(mainActivity: MainActivity) : Fragment() {

    private val myActivity = mainActivity
    private lateinit var myMenu: Menu
    lateinit var myRecycler: RecyclerView
    lateinit var searchView: SearchView
    var noteListInstance: NoteList = NoteList()

    companion object {
        lateinit var myAdapter: NoteAdapter
        var noteLines = 0

        var deletedNote: Note? = null

        var searching = false

        lateinit var searchResults: ArrayList<Note>
        lateinit var lastQuery: String
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //inflating layout for NoteFragment
        val myView = inflater.inflate(R.layout.fragment_note, container, false)
        myRecycler = myView.recycler_view_note
        deletedNote = null
        initializeComponents()
        myAdapter.notifyDataSetChanged()
        myRecycler.scrollToPosition(0)
        return myView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_notes, menu)
        myMenu = menu
        searchResults = arrayListOf()

        //color tint for undo icon
        myMenu.findItem(R.id.item_notes_undo)?.icon?.setTint(myActivity.colorForAttr(R.attr.colorOnBackGround))

        searchView = menu.findItem(R.id.item_notes_search).actionView as SearchView
        val textListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                myActivity.hideKeyboard()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (searching) {
                    search(newText.toString())
                }
                return true
            }
        }

        searchView.setOnQueryTextListener(textListener)

        //close listener to restore fragment to normal after search is finished
        val onCloseListener = SearchView.OnCloseListener {
            myActivity.toolBar.title = getString(R.string.menuTitleNotes)
            myActivity.myBtnAdd.visibility = View.VISIBLE
            searchView.onActionViewCollapsed()
            searching = false
            myAdapter.notifyDataSetChanged()
            true
        }
        searchView.setOnCloseListener(onCloseListener)

        //onSearchCloseListener to refresh fragment once search is ended
        searchView.setOnSearchClickListener {
            myActivity.myBtnAdd.visibility = View.GONE
            myActivity.toolBar.title = ""
            searching = true
            searchResults.clear()
            myAdapter.notifyDataSetChanged()
        }

        updateNoteSearchIcon()
        updateNoteUndoIcon()
        myRecycler.scrollToPosition(0)
        super.onCreateOptionsMenu(menu, inflater)

    }

    private fun updateNoteSearchIcon() {
        myMenu.findItem(R.id.item_notes_search).isVisible = noteListInstance.size > 0
    }

    private fun updateNoteUndoIcon() {
        myMenu.findItem(R.id.item_notes_undo).isVisible = deletedNote != null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    fun search(query: String) {
        if (query == "") {
            searchResults.clear()
        } else {
            lastQuery = query
            searchResults.clear()

            //search all notes for occurrences of query text, add them to search results
            noteListInstance.forEach { note ->
                if (note.content.toLowerCase(Locale.ROOT)
                        .contains(query.toLowerCase(Locale.ROOT)) ||
                    note.title.toLowerCase(Locale.ROOT)
                        .contains(query.toLowerCase(Locale.ROOT))
                ) {
                    searchResults.add(note)
                }
            }
        }
        myAdapter.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_notes_search -> {
                /* no-op, listeners for this view are implemented in onCreateOptionsMenu */
            }

            R.id.item_notes_undo -> {
                noteListInstance.addFullNote(deletedNote!!)
                if (searching) {
                    search(lastQuery)
                } else {
                    myAdapter.notifyDataSetChanged()
                }

                deletedNote = null
                updateNoteUndoIcon()
                updateNoteSearchIcon()
            }
        }

        return super.onOptionsItemSelected(item)
    }


    private fun initializeComponents() {
        val noteColumns = SettingsManager.getSetting(SettingId.NOTE_COLUMNS) as String

        val setting = SettingsManager.getSetting(SettingId.NOTE_LINES) as Double
        noteLines = setting.toInt()

        //initialize Recyclerview and Adapter
        myAdapter = NoteAdapter(myActivity, this)
        myRecycler.adapter = myAdapter

        //initialize and set layoutManager
        val lm = StaggeredGridLayoutManager(noteColumns.toInt(), 1)
        myRecycler.layoutManager = lm
        myRecycler.setHasFixedSize(true)

        val swipeDirections =
            when (SettingsManager.getSetting(SettingId.NOTES_SWIPE_DELETE) as Boolean) {
                true -> ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                else -> 0
            }

        //itemTouchHelper to drag and reorder notes
        val itemTouchHelper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
               0,
                swipeDirections
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: ViewHolder,
                    target: ViewHolder
                ): Boolean {
                    return true
                }

                override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                    val parsed = viewHolder as NoteAdapter.NoteViewHolder
                    deletedNote = parsed.note

                    //delete note from noteList and save
                    noteListInstance.remove(parsed.note)
                    noteListInstance.save()

                    //refresh search if searching currently
                    if (searching) {
                        search(lastQuery)
                    } else {
                        myAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                    }

                    updateNoteSearchIcon()
                    updateNoteUndoIcon()
                }
            })

        itemTouchHelper.attachToRecyclerView(myRecycler)
    }

}


class NoteAdapter(mainActivity: MainActivity, noteFr: NoteFr) :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    private val myActivity = mainActivity
    private val round = SettingsManager.getSetting(SettingId.SHAPES_ROUND) as Boolean
    private val cr = myActivity.resources.getDimension(R.dimen.cornerRadius)
    private val myNoteFr = noteFr

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
            true -> NoteFr.searchResults[position]
            /**
             * NoteFr is currently in normal mode, current note gets grabbed from noteList
             */
            false -> myNoteFr.noteListInstance.getNote(position)
        }

        holder.note = currentNote

        //EDITING TASK VIA ONCLICK LISTENER ON RECYCLER ITEMS
        holder.itemView.setOnClickListener {
            MainActivity.editNoteHolder = currentNote
            noteColor = currentNote.color

            //move current note to top
            val noteToMove = holder.note
            val noteIndex = myNoteFr.noteListInstance.indexOf(currentNote)

            myNoteFr.noteListInstance.removeAt(noteIndex)
            myNoteFr.noteListInstance.add(0, noteToMove)
            myNoteFr.noteListInstance.save()

            myActivity.changeToFragment(FT.NOTE_EDITOR)
            myActivity.hideKeyboard()
        }

        //when title is empty, hide it else show it and set the proper text
        if (currentNote.title == "") {
            holder.tvNoteTitle.visibility = View.GONE
        } else {
            holder.tvNoteTitle.visibility = View.VISIBLE
            holder.tvNoteTitle.text = currentNote.title
        }

        holder.tvNoteContent.text = currentNote.content

        //decide how many lines per note are shown, depending on teh setting noteLines
        if (NoteFr.noteLines == -1) {
            holder.tvNoteContent.maxLines = Int.MAX_VALUE
        } else {
            holder.tvNoteContent.maxLines = NoteFr.noteLines
            holder.tvNoteContent.ellipsize = TextUtils.TruncateAt.END
        }

        holder.cvNoteCard.radius = when (round) {
            true -> cr
            else -> 0f
        }

        holder.itemView.cvNoteCard.setCardBackgroundColor(myActivity.colorForAttr(currentNote.color.colorCode))
    }

    override fun getItemCount(): Int {
        return when (NoteFr.searching) {
            true -> NoteFr.searchResults.size
            false -> myNoteFr.noteListInstance.size
        }
    }

    //one instance of this class will contain one instance of row_task and meta data like position
    //also holds references to views inside the layout
    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNoteTitle: TextView = itemView.tvNoteTitle
        val tvNoteContent: TextView = itemView.tvNoteContent
        var cvNoteCard: CardView = itemView.cvNoteCard
        lateinit var note: Note
    }

}
