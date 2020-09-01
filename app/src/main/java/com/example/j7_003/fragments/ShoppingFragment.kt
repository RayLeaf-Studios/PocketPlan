package com.example.j7_003.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.database.ShoppingList
import com.example.j7_003.data.database.database_objects.Tag
import kotlinx.android.synthetic.main.fragment_shopping.view.*
import kotlinx.android.synthetic.main.row_category.view.*
import kotlinx.android.synthetic.main.row_item.view.*


/**
 * A simple [Fragment] subclass.
 */
class ShoppingFragment : Fragment() {
    companion object{
        //TODO uncomment this
//        var deletedItem: Item? = null
        lateinit var shoppingListInstance: ShoppingList
        lateinit var shoppingListAdapter: ShoppingListAdapater
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        shoppingListInstance = ShoppingList()


        //TODO add button to empty the entire list

        // Inflate the layout for this fragment
        val myView = inflater.inflate(R.layout.fragment_shopping, container, false)

        val btnAddItem = myView.btnAddItem
        btnAddItem.setOnClickListener {
            MainActivity.myActivity.changeToAddItem()
        }

        //Initialize references to recycler and its adapter
        val myRecycler = myView.recycler_view_shopping
        shoppingListAdapter = ShoppingListAdapater()


        //attach adapter to recycler and initialize parameters of recycler
        myRecycler.adapter = shoppingListAdapter
        myRecycler.layoutManager = LinearLayoutManager(activity)
        myRecycler.setHasFixedSize(true)

        return myView
    }

}

class ShoppingListAdapater :
    RecyclerView.Adapter<ShoppingListAdapater.CategoryViewHolder>(){
    fun deleteItem(position: Int){
        //TODO decide if entire categories should be allowed to be deleted
//        NoteFragment.deletedNote = Database.getNote(position)
//        MainActivity.myActivity.updateUndoNoteIcon()
//        Database.deleteNote(position)
//        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_category, parent, false)
        return CategoryViewHolder(itemView)
    }



    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        //TODO REPLACE THIS CONDITION WITH DATABASE ACCESS (EXPANSION OF position-th sublist)
        val numberOfItems = ShoppingFragment.shoppingListInstance.getUncheckedSize(position)

        if(ShoppingFragment.shoppingListInstance.isTagExpanded(position)){
            holder.subRecyclerView.visibility = View.VISIBLE
        }else{
            holder.subRecyclerView.visibility = View.GONE
        }

        //TODO REPLACE THIS WITH DATABASE ACCESS (TITLE OF position-th sublist)
        holder.tvCategoryName.text = numberOfItems.toString() + " " + ShoppingFragment.shoppingListInstance[position].first.n


        //TODO REPLACE THIS WITH SOMEHOW SHOWING COLOR OF CATEGORY
        holder.cvCategory.setCardBackgroundColor(Color.parseColor(ShoppingFragment.shoppingListInstance[position].first.c))

        //Setting adapter for this sublist
        val subAdapter = SublistAdapter(ShoppingFragment.shoppingListInstance[position].first, position)
        holder.subRecyclerView.adapter = subAdapter
        holder.subRecyclerView.layoutManager = LinearLayoutManager(MainActivity.myActivity)
        holder.subRecyclerView.setHasFixedSize(true)

        val swipeHelperLeft = ItemTouchHelper(SwipeLeftToDeleteI(subAdapter))
        swipeHelperLeft.attachToRecyclerView(holder.subRecyclerView)

        val swipeHelperRight = ItemTouchHelper(SwipeRightToDeleteI(subAdapter))
        swipeHelperRight.attachToRecyclerView(holder.subRecyclerView)

        //Onclick reaction to expand / contract this sublist
        holder.cvCategory.setOnClickListener {
            //TODO REPLACE THIS WITH DATABASE ACCESS
            ShoppingFragment.shoppingListInstance.flipExpansionState(position)
            notifyItemChanged(position)
        }

    }

    //TODO replace this with database access, get amount of sublists
    override fun getItemCount() = ShoppingFragment.shoppingListInstance.size

    //one instance of this class will contain one instance of row_category and meta data like position
    //also holds references to views inside the layout

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategoryName: TextView = itemView.tvCategoryName
        var subRecyclerView: RecyclerView = itemView.subRecyclerView
        val cvCategory: CardView = itemView.cvCategory
    }

}
class SublistAdapter(tag: Tag, categoryPosition: Int) :
    RecyclerView.Adapter<SublistAdapter.ItemViewHolder>(){
    private val myTag = tag
    private val superPosition = categoryPosition

    fun deleteItem(position: Int){

        val feedback = ShoppingFragment.shoppingListInstance.removeItem(myTag, position)

        //TODO replace following line with saving the deleted item (feedback.first)
        //NoteFragment.deletedNote = Database.getNote(position)

        if(feedback.second){
            MainActivity.myActivity.titleDebug("second is true")
            ShoppingFragment.shoppingListAdapter.notifyItemRemoved(superPosition)
        }else{
            MainActivity.myActivity.titleDebug("second is false")
            ShoppingFragment.shoppingListAdapter.notifyItemChanged(superPosition)
        }


        //TODO handle deletion of Item
        //If item was last unchecked item and there are more checked items => sublist moves => notify move (sublist contracts?)
        //If item was last item in list => sublist gets removed => notify removed
        //If neither of the upper cases: notify item removed in sublist

        //TODO replace following line with updating undoItemIcon()
        //MainActivity.myActivity.updateUndoNoteIcon()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_item, parent, false)
        return ItemViewHolder(itemView)
    }



    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = ShoppingFragment.shoppingListInstance.getItem(myTag, position)!!
        holder.tvItemTitle.text = item.amount + item.unit + " " + item.name
        holder.clItemTapfield.setOnClickListener {

            //Todo manage checking item
        }

    }

    override fun getItemCount() = ShoppingFragment.shoppingListInstance.getSublistLength(myTag)

    //one instance of this class will contain one instance of row_item and meta data like position
    //also holds references to views inside the layout

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItemTitle: TextView = itemView.tvItemTitle
        val cbItem: CheckBox = itemView.cbItem
        var clItemTapfield: ConstraintLayout = itemView.clItemTapfield
    }

}
class SwipeRightToDeleteI(var adapter: SublistAdapter):
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        adapter.deleteItem(position)
        //Todo update undo delete icon?
    }
}

class SwipeLeftToDeleteI(private var adapter: SublistAdapter):
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        adapter.deleteItem(position)
        //Todo update undo delete icon?
    }
}
