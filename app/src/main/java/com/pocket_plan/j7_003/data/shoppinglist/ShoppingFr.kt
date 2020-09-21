package com.pocket_plan.j7_003.data.shoppinglist

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.settings.SettingsManager
import kotlinx.android.synthetic.main.fragment_shopping.view.*
import kotlinx.android.synthetic.main.row_category.view.*
import kotlinx.android.synthetic.main.row_item.view.*


/**
 * A simple [Fragment] subclass.
 */
class ShoppingFr : Fragment() {
    companion object {
        lateinit var shoppingListInstance: ShoppingList
        lateinit var shoppingListAdapter: ShoppingListAdapter
        lateinit var layoutManager: LinearLayoutManager
        lateinit var myFragment: ShoppingFr

        var offsetTop: Int = 0
        var firstPos: Int = 0
        var expandOne: Boolean = false
        var collapseCheckedSublists: Boolean = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myFragment = this
        shoppingListInstance = ShoppingList()
        expandOne = SettingsManager.getSetting("expandOneCategory") as Boolean
        collapseCheckedSublists = SettingsManager.getSetting("collapseCheckedSublists") as Boolean
        //expand first category, contract all others, if setting says so
        if (expandOne) {
            shoppingListInstance.forEach {
                if (shoppingListInstance.getTagIndex(it.first) == 0) {
                    if (!shoppingListInstance.isTagExpanded(it.first)) {
                        shoppingListInstance.flipExpansionState(it.first)
                    }
                } else {
                    if (shoppingListInstance.isTagExpanded(it.first)) {
                        shoppingListInstance.flipExpansionState(it.first)
                    }
                }
            }
        }

        // Inflate the layout for this fragment
        val myView = inflater.inflate(R.layout.fragment_shopping, container, false)

        //Initialize references to recycler and its adapter
        val myRecycler = myView.recycler_view_shopping
        shoppingListAdapter = ShoppingListAdapter()

        //attach adapter to recycler and initialize parameters of recycler
        myRecycler.adapter = shoppingListAdapter
        layoutManager = LinearLayoutManager(activity)
        myRecycler.layoutManager = layoutManager
        myRecycler.setHasFixedSize(true)

        return myView
    }

    fun prepareForMove() {
        firstPos = layoutManager.findFirstCompletelyVisibleItemPosition()
        offsetTop = 0
        if (firstPos >= 0) {
            val firstView = layoutManager.findViewByPosition(firstPos)
            offsetTop =
                layoutManager.getDecoratedTop(firstView!!) - layoutManager.getTopDecorationHeight(
                    firstView
                )
        }
    }

    fun reactToMove() {
        layoutManager.scrollToPositionWithOffset(firstPos, offsetTop)
    }

}

class ShoppingListAdapter :
    RecyclerView.Adapter<ShoppingListAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_category, parent, false)
        return CategoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {

        if(position == ShoppingFr.shoppingListInstance.size){
            holder.itemView.visibility = View.INVISIBLE
            holder.itemView.layoutParams.height = 250
            holder.itemView.setOnClickListener{}
            return
        }
        holder.itemView.visibility = View.VISIBLE
        holder.itemView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

        //Get reference to currently used shopping list instance
        val shoppingListInstance = ShoppingFr.shoppingListInstance

        //get Tag for current category element
        val tag = shoppingListInstance[position].first

        //set tag for view holder
        holder.tag = tag

        //get Number of unchecked items
        val numberOfItems = shoppingListInstance.getUncheckedSize(tag)


        //Expand or contract recyclerview depending on its expansion state
        holder.subRecyclerView.visibility = when (shoppingListInstance.isTagExpanded(tag)) {
            true -> View.VISIBLE
            false -> View.GONE
        }

        //Sets Text to Number of items in sublist + name of category of sublist
        holder.tvCategoryName.text = tag.name

        //Sets background color of sublist according to the tag
        manageCheckedCategory(
            holder,
            ShoppingFr.shoppingListInstance.areAllChecked(tag),
            numberOfItems,
            tag
        )

        //Setting adapter for this sublist
        val subAdapter = SublistAdapter(tag, holder)
        holder.subRecyclerView.adapter = subAdapter
        holder.subRecyclerView.layoutManager = LinearLayoutManager(MainActivity.act)
        holder.subRecyclerView.setHasFixedSize(true)

        //Initialize and attach swipe helpers
        val swipeHelperLeft = ItemTouchHelper(SwipeItemToDelete(ItemTouchHelper.LEFT))
        swipeHelperLeft.attachToRecyclerView(holder.subRecyclerView)

        val swipeHelperRight = ItemTouchHelper(SwipeItemToDelete(ItemTouchHelper.RIGHT))
        swipeHelperRight.attachToRecyclerView(holder.subRecyclerView)

        //Onclick reaction to expand / contract this sublist
        holder.cvCategory.setOnClickListener {
            val newState = shoppingListInstance.flipExpansionState(holder.tag)
            //if the item gets expanded and the setting says to only expand one
            if (newState == true && ShoppingFr.expandOne) {
                //iterate through all categories and contract one if you find one that's expanded and not the current sublist
                shoppingListInstance.forEach {
                    if (shoppingListInstance.isTagExpanded(it.first) && it.first != holder.tag) {
                        shoppingListInstance.flipExpansionState(it.first)
                        ShoppingFr.shoppingListAdapter.notifyItemChanged(
                            shoppingListInstance.getTagIndex(
                                it.first
                            )
                        )
                    }
                }
            }
            notifyItemChanged(holder.adapterPosition)
        }
    }

    fun manageCheckedCategory(
        holder: CategoryViewHolder, allChecked: Boolean,
        numberOfItems: Int, tag: Tag
    ) {
        if (!allChecked) {
            val colorBackground = ContextCompat
                .getColor(MainActivity.act, R.color.colorOnBackGround)
            holder.cvCategory.setCardBackgroundColor(Color.parseColor(tag.color))
            holder.tvCategoryName.setTextColor(colorBackground)
            holder.tvNumberOfItems.setTextColor(colorBackground)
            holder.tvNumberOfItems.text = numberOfItems.toString()
        } else {
            val colorHint = ContextCompat.getColor(MainActivity.act, R.color.colorHint)
            holder.cvCategory.setCardBackgroundColor(
                ContextCompat.getColor(MainActivity.act, R.color.colorBackgroundElevated)
            )
            holder.tvCategoryName.setTextColor(colorHint)
            holder.tvNumberOfItems.setTextColor(colorHint)
            holder.tvNumberOfItems.text = "✔"
        }
    }

    override fun getItemCount() = ShoppingFr.shoppingListInstance.size + 1

    /**
     * one instance of this class will contain one instance of row_category and meta data like
     * position also holds references to views inside the layout
     */
    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var tag: Tag
        val tvCategoryName: TextView = itemView.tvCategoryName
        var subRecyclerView: RecyclerView = itemView.subRecyclerView
        val cvCategory: CardView = itemView.cvCategory
        val tvNumberOfItems: TextView = itemView.tvNumberOfItems
    }
}

class SublistAdapter(
    private val tag: Tag, private val parentHolder: ShoppingListAdapter.CategoryViewHolder
) : RecyclerView.Adapter<SublistAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_item, parent, false)
        return ItemViewHolder(itemView, this)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val itemCheckedState = ShoppingFr.shoppingListInstance.isItemChecked(tag, position)

        if (itemCheckedState == null) {
            MainActivity.act.sadToast("invalid checked state")
            return
        }

        holder.itemView.cbItem.isChecked = itemCheckedState
        if (itemCheckedState) {
            holder.itemView.tvItemTitle.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            holder.itemView.tvItemTitle
                .setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorHint))
        } else {
            holder.itemView.tvItemTitle.paintFlags = 0
            holder.itemView.tvItemTitle
                .setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorOnBackGround))
        }

        val item = ShoppingFr.shoppingListInstance.getItem(tag, position)!!
        holder.itemView.tvItemTitle.text = item.amount + item.unit + " " + item.name

        holder.itemView.clItemTapfield.setOnClickListener {
            val newPosition = ShoppingFr.shoppingListInstance.flipItemCheckedState(
                tag,
                holder.adapterPosition
            )

            val numberOfItems = ShoppingFr.shoppingListInstance.getUncheckedSize(holder.tag)

            //If all are checked after the current item got flipped, the list has to go from color to gray
            ShoppingFr.shoppingListAdapter.manageCheckedCategory(
                parentHolder,
                ShoppingFr.shoppingListInstance.areAllChecked(holder.tag),
                numberOfItems,
                holder.tag
            )

            if (ShoppingFr.collapseCheckedSublists && ShoppingFr.shoppingListInstance.areAllChecked(
                    holder.tag
                )
            ) {
                ShoppingFr.shoppingListInstance.flipExpansionState(holder.tag)
                ShoppingFr.shoppingListAdapter.notifyItemChanged(parentHolder.adapterPosition)
            }

            notifyItemChanged(holder.adapterPosition)
            if (newPosition != -1) {
                ShoppingFr.myFragment.prepareForMove()
                notifyItemMoved(holder.adapterPosition, newPosition)
                ShoppingFr.myFragment.reactToMove()
            } else {
                MainActivity.act.sadToast("invalid item checked state")
            }

            val sublistMoveInfo = ShoppingFr.shoppingListInstance.sortTag(tag)
            if (sublistMoveInfo != null) {
                ShoppingFr.myFragment.prepareForMove()
                ShoppingFr.shoppingListAdapter
                    .notifyItemMoved(sublistMoveInfo.first, sublistMoveInfo.second)

                ShoppingFr.myFragment.reactToMove()
            }
        }
        holder.tag = tag
    }

    override fun getItemCount() = ShoppingFr.shoppingListInstance.getSublistLength(tag)

    /**
    one instance of this class will contain one instance of row_item and meta data like position
    also holds references to views inside the layout
     */
    class ItemViewHolder(itemView: View, val adapter: SublistAdapter) :
        RecyclerView.ViewHolder(itemView) {

        lateinit var tag: Tag
    }
}

class SwipeItemToDelete(direction: Int) : ItemTouchHelper.SimpleCallback(0, direction) {
    override fun onMove(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target:
        RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        val parsed = viewHolder as SublistAdapter.ItemViewHolder
        val tagPosition = ShoppingFr.shoppingListInstance.getTagIndex(parsed.tag)
        if (ShoppingFr.shoppingListInstance.removeItem(parsed.tag, position).second) {
            //entire sublist is empty => remove sublist
            ShoppingFr.shoppingListAdapter
                .notifyItemRemoved(tagPosition)
        } else {
            //sublist changed length =>
            ShoppingFr.shoppingListAdapter.notifyItemChanged(tagPosition)
            //check if sublist moved
            val positions = ShoppingFr.shoppingListInstance.sortTag(parsed.tag)
            if (positions != null) {
                //sublist did move => animate movement
                ShoppingFr.myFragment.prepareForMove()
                ShoppingFr.shoppingListAdapter.notifyItemMoved(
                    positions.first, positions.second
                )
                ShoppingFr.myFragment.reactToMove()
            }
        }
        //Todo update undo delete icon?
    }
}
