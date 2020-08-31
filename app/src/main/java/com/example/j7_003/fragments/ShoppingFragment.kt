package com.example.j7_003.fragments

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
        //TODO REMOVE THESE 2
        var shoppingList = arrayListOf(
            arrayListOf("Vegetables", "1kg Tomaten ", "2 Gurken"),
            arrayListOf("Fruits", "3 Äpfel", "1 Zitrone", "1 Orange"),
            arrayListOf("Rice and Pasta", "1 Pck Spaghetti")
        )
        var expansions = arrayListOf(false, false, false)
        //TODO uncomment this
//        var deletedItem: Item? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //TODO add button to empty the entire list

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

class CategoryAdapter :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>(){
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



    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        //TODO REPLACE THIS CONDITION WITH DATABASE ACCESS (EXPANSION OF position-th sublist)
        if(ShoppingFragment.expansions[position]){
            holder.subRecyclerView.visibility = View.VISIBLE
        }else{
            holder.subRecyclerView.visibility = View.GONE
        }

        //TODO REPLACE THIS WITH DATABASE ACCESS (TITLE OF position-th sublist)
        holder.tvCategoryName.text = ShoppingFragment.shoppingList[position][0]

        //TODO REPLACE THIS WITH SOMEHOW SHOWING COLOR OF CATEGORY
        holder.cvCategory.setCardBackgroundColor(ContextCompat.getColor(MainActivity.myActivity, R.color.colorBirthdayLabel))

        //Setting adapter for this sublist
        val subAdapter = ItemAdapter(position)
        holder.subRecyclerView.adapter = subAdapter
        holder.subRecyclerView.layoutManager = LinearLayoutManager(MainActivity.myActivity)
        holder.subRecyclerView.setHasFixedSize(true)

        //Onclick reaction to expand / contract this sublist
        holder.cvCategory.setOnClickListener {
            //TODO REPLACE THIS WITH DATABASE ACCESS
            if(ShoppingFragment.expansions[position]){
                ShoppingFragment.expansions[position] = false
            }else{
                for(i in 0..2){
                    ShoppingFragment.expansions[i] = false
                    notifyItemChanged(i)
                }
                ShoppingFragment.expansions[position] = true
            }
            notifyItemChanged(position)
        }

    }

    //TODO replace this with database access, get amount of sublists
    override fun getItemCount() = ShoppingFragment.shoppingList.size

    //one instance of this class will contain one instance of row_category and meta data like position
    //also holds references to views inside the layout

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategoryName: TextView = itemView.tvCategoryName
        var subRecyclerView: RecyclerView = itemView.subRecyclerView
        val cvCategory: CardView = itemView.cvCategory
    }

}
class ItemAdapter(categoryPosition: Int) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>(){
    private val myCategory = categoryPosition

    fun deleteItem(position: Int){
        //TODO replace following line with saving the deleted item
        //NoteFragment.deletedNote = Database.getNote(position)

        //TODO replace following line with deleting item in database, and saving feedback for the cases in following todo
        //Database.deleteNote(position)

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



    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.tvItemTitle.text = ShoppingFragment.shoppingList[myCategory][position+1]
        holder.clItemTapfield.setOnClickListener {
            //Todo manage checking item
        }
    }

    override fun getItemCount() = ShoppingFragment.shoppingList[myCategory].size-1

    //one instance of this class will contain one instance of row_item and meta data like position
    //also holds references to views inside the layout

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItemTitle: TextView = itemView.tvItemTitle
        val cbItem: CheckBox = itemView.cbItem
        var clItemTapfield: ConstraintLayout = itemView.clItemTapfield
    }

}
