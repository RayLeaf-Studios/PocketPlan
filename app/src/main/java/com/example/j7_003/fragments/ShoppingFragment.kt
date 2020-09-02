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
import kotlin.random.Random


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
        MainActivity.myActivity.titleDebug("called"+ Random.nextInt())
        return CategoryViewHolder(itemView)
    }



    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {

        //Get reference to currently used shopping list instance
        val shoppingListInstance = ShoppingFragment.shoppingListInstance

        //get Tag for current category element
        val tag = shoppingListInstance[position].first

        //set tag for view holder
        holder.tag = tag

       //get Number of unchecked items
        val numberOfItems = shoppingListInstance.getUncheckedSize(tag)

        //Expand or contract recyclerview depending on its expansion state
        holder.subRecyclerView.visibility = when(shoppingListInstance.isTagExpanded(tag)){
            true -> View.VISIBLE
            false -> View.GONE
        }

        //Sets Text to Number of items in sublist + name of category of sublist
        holder.tvCategoryName.text = numberOfItems.toString() + " " + tag.n

        //Sets background color of sublist according to the tag
        holder.cvCategory.setCardBackgroundColor(Color.parseColor(tag.c))

        //Setting adapter for this sublist
        val subAdapter = SublistAdapter(tag)
        holder.subRecyclerView.adapter = subAdapter
        holder.subRecyclerView.layoutManager = LinearLayoutManager(MainActivity.myActivity)
        holder.subRecyclerView.setHasFixedSize(true)

        //Initialize and attach swipe helpers
        val swipeHelperLeft = ItemTouchHelper(SwipeLeftToDeleteI())
        swipeHelperLeft.attachToRecyclerView(holder.subRecyclerView)

        val swipeHelperRight = ItemTouchHelper(SwipeRightToDeleteI())
        swipeHelperRight.attachToRecyclerView(holder.subRecyclerView)

        //Onclick reaction to expand / contract this sublist
        holder.cvCategory.setOnClickListener {
            shoppingListInstance.flipExpansionState(holder.tag)
            notifyItemChanged(holder.adapterPosition)
        }

    }

    override fun getItemCount() = ShoppingFragment.shoppingListInstance.size

    //one instance of this class will contain one instance of row_category and meta data like position
    //also holds references to views inside the layout

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var tag: Tag
        val tvCategoryName: TextView = itemView.tvCategoryName
        var subRecyclerView: RecyclerView = itemView.subRecyclerView
        val cvCategory: CardView = itemView.cvCategory
    }

}
class SublistAdapter(private val tag: Tag) :
    RecyclerView.Adapter<SublistAdapter.ItemViewHolder>(){

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
