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
        val subAdapter = SublistAdapter(ShoppingFragment.shoppingListInstance[position].first)

        holder.subRecyclerView.adapter = subAdapter
        holder.subRecyclerView.layoutManager = LinearLayoutManager(MainActivity.myActivity)
        holder.subRecyclerView.setHasFixedSize(true)

        val swipeHelperLeft = ItemTouchHelper(SwipeLeftToDeleteI())
        swipeHelperLeft.attachToRecyclerView(holder.subRecyclerView)

        val swipeHelperRight = ItemTouchHelper(SwipeRightToDeleteI())
        swipeHelperRight.attachToRecyclerView(holder.subRecyclerView)

        //Onclick reaction to expand / contract this sublist
        holder.cvCategory.setOnClickListener {
            //TODO REPLACE THIS WITH DATABASE ACCESS
            ShoppingFragment.shoppingListInstance.flipExpansionState(holder.adapterPosition)
            notifyItemChanged(holder.adapterPosition)
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
class SublistAdapter(private val tag: Tag) :
    RecyclerView.Adapter<SublistAdapter.ItemViewHolder>(){
    fun deleteItem(position: Int){

        val index = ShoppingFragment.shoppingListInstance.getTagIndex(tag)
        val feedback = ShoppingFragment.shoppingListInstance.removeItem(tag, position)

        //TODO replace following line with saving the deleted item (feedback.first)
        //NoteFragment.deletedNote = Database.getNote(position)

        if(feedback.second){
//            ShoppingFragment.shoppingListAdapter.notifyItemRemoved(index)
            ShoppingFragment.shoppingListAdapter.notifyDataSetChanged()
        }else{
//            ShoppingFragment.shoppingListAdapter.notifyItemChanged(index)
            ShoppingFragment.shoppingListAdapter.notifyDataSetChanged()
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
        val item = ShoppingFragment.shoppingListInstance.getItem(tag, position)!!
        holder.tvItemTitle.text = item.amount + item.unit + " " + item.name
        holder.clItemTapfield.setOnClickListener {

            //Todo manage checking item
        }
        holder.tag = tag

    }

    override fun getItemCount() = ShoppingFragment.shoppingListInstance.getSublistLength(tag)

    //one instance of this class will contain one instance of row_item and meta data like position
    //also holds references to views inside the layout

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var tag: Tag
        val tvItemTitle: TextView = itemView.tvItemTitle
        val cbItem: CheckBox = itemView.cbItem
        var clItemTapfield: ConstraintLayout = itemView.clItemTapfield
    }

}
class SwipeRightToDeleteI():
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        val parsed = viewHolder as SublistAdapter.ItemViewHolder
        ShoppingFragment.shoppingListInstance.removeItem(parsed.tag, position)
        ShoppingFragment.shoppingListAdapter.notifyDataSetChanged()
        //Todo update undo delete icon?
    }
}

class SwipeLeftToDeleteI():
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        val parsed = viewHolder as SublistAdapter.ItemViewHolder
        ShoppingFragment.shoppingListInstance.removeItem(parsed.tag, position)
        ShoppingFragment.shoppingListAdapter.notifyDataSetChanged()

        //Todo update undo delete icon?
    }
}
