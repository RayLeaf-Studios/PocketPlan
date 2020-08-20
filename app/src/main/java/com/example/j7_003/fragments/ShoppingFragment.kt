package com.example.j7_003.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j7_003.MainActivity

import com.example.j7_003.R
import kotlinx.android.synthetic.main.fragment_shopping.view.*
import kotlinx.android.synthetic.main.row_category.view.*
import kotlinx.android.synthetic.main.row_item.view.*


/**
 * A simple [Fragment] subclass.
 */
class ShoppingFragment : Fragment() {
    companion object{
        var shoppingList = arrayListOf(
            arrayListOf("Vegetables", "Tomato", "Cucumber"),
            arrayListOf("Fruits", "Apple", "Lemon", "Orange"),
            arrayListOf("Rice and Pasta", "Spaghetti")
        )
        var expansions = arrayListOf(true, true, true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val myView = inflater.inflate(R.layout.fragment_shopping, container, false)

        val btnAddItem = myView.btnAddItem
        btnAddItem.setOnClickListener {
            MainActivity.myActivity.changeToAddItem()
        }

        //Initialize references to recycler and its adapter
        val myRecycler = myView.recycler_view_shopping
        val myAdapter = CategoryAdapter()


        //attach adapter to recycler and initialize parameters of recycler
        myRecycler.adapter = myAdapter
        myRecycler.layoutManager = LinearLayoutManager(activity)
        myRecycler.setHasFixedSize(true)


        expansions = arrayListOf(true, true, true)
        return myView
    }

}

class CategoryAdapter() :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>(){
    fun deleteItem(position: Int){
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



    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        if(ShoppingFragment.expansions[position]){
            holder.subRecyclerView.visibility = View.VISIBLE
        }else{
            holder.subRecyclerView.visibility = View.GONE
        }
        holder.tvCategoryName.text = ShoppingFragment.shoppingList[position][0]
        val subAdapter = ItemAdapter(position)
        holder.subRecyclerView.adapter = subAdapter
        holder.subRecyclerView.layoutManager = LinearLayoutManager(MainActivity.myActivity)
        holder.subRecyclerView.setHasFixedSize(true)
        holder.clCategory.setOnClickListener {
            if(ShoppingFragment.expansions[position]){
                ShoppingFragment.expansions[position] = false
            }else{
                ShoppingFragment.expansions[position] = true
            }
            notifyItemChanged(position)
        }

    }

    override fun getItemCount() = ShoppingFragment.shoppingList.size

    //one instance of this class will contain one instance of row_shopping and meta data like position
    //also holds references to views inside the layout

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategoryName: TextView = itemView.tvCategoryName
        val clCategory: ConstraintLayout = itemView.clCategory
//        val tvDebug: TextView = itemView.debugText
        var subRecyclerView: RecyclerView = itemView.subRecyclerView
    }

}
class ItemAdapter(categoryPosition: Int) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>(){
    private val myCategory = categoryPosition

    fun deleteItem(position: Int){
//        NoteFragment.deletedNote = Database.getNote(position)
//        MainActivity.myActivity.updateUndoNoteIcon()
//        Database.deleteNote(position)
//        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_item, parent, false)
        return ItemViewHolder(itemView)
    }



    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.tvItemTitle.text = ShoppingFragment.shoppingList[myCategory][position+1]
    }

    override fun getItemCount() = ShoppingFragment.shoppingList[myCategory].size-1

    //one instance of this class will contain one instance of row_shopping and meta data like position
    //also holds references to views inside the layout

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItemTitle: TextView = itemView.tvItemTitle
        val cbItem: CheckBox = itemView.cbItem
        var clItemTapfield: ConstraintLayout = itemView.clItemTapfield
    }

}
