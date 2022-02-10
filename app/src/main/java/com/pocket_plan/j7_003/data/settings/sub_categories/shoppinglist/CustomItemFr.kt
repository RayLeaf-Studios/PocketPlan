package com.pocket_plan.j7_003.data.settings.sub_categories.shoppinglist

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.data.shoppinglist.ItemTemplate
import com.pocket_plan.j7_003.data.shoppinglist.MultiShoppingFr
import kotlinx.android.synthetic.main.fragment_custom_items.view.*
import kotlinx.android.synthetic.main.row_custom_item.view.*
import kotlinx.android.synthetic.main.row_task.view.tvName

class CustomItemFr : Fragment() {
    private lateinit var myShoppingFr: MultiShoppingFr

    private lateinit var myActivity: MainActivity
    private lateinit var myMenu: Menu

    companion object{
        lateinit var myFragment: CustomItemFr
        lateinit var myAdapter: CustomItemAdapter
        lateinit var myRecycler: RecyclerView
        var deletedItem: ItemTemplate? = null

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        myActivity = activity as MainActivity
        myShoppingFr = myActivity.getFragment(FT.SHOPPING) as MultiShoppingFr

        val myView = inflater.inflate(R.layout.fragment_custom_items, container, false)
        myRecycler = myView.recycler_view_customItems
        myFragment = this
        deletedItem = null

        /**
         * Connecting Adapter, Layout-Manager and Swipe Detection to UI elements
         */

        myAdapter = CustomItemAdapter(myActivity)
        myRecycler.adapter = myAdapter
        myRecycler.layoutManager = LinearLayoutManager(activity)
        myRecycler.setHasFixedSize(true)

        val swipeHelperLeft = ItemTouchHelper(SwipeToDeleteCustomItem(ItemTouchHelper.LEFT, myShoppingFr, myActivity))
        swipeHelperLeft.attachToRecyclerView(myRecycler)
        val swipeHelperRight = ItemTouchHelper(SwipeToDeleteCustomItem(ItemTouchHelper.RIGHT, myShoppingFr, myActivity))
        swipeHelperRight.attachToRecyclerView(myRecycler)

        return myView
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
           R.id.item_custom_clear -> {
               val action : () -> Unit = {
                   //remove user items from itemNameList and update act adapter so they
                   //don't show up in the add item dialog anymore
                   myActivity.userItemTemplateList.clear()
                   myActivity.userItemTemplateList.save()
                   myActivity.multiShoppingFr.refreshItemNamesAndAutoCompleteAdapter()
                   myAdapter.notifyDataSetChanged()
                   updateClearCustomListIcon()
               }
               val titleId = R.string.custom_item_delete_title
               myActivity.dialogConfirm(titleId, action)
           }
            R.id.item_custom_undo -> {
                //Return if deletedItem = null, this should never happen
                if(deletedItem == null) return true
                //Re-Add item to userItemTemplateList
                myActivity.userItemTemplateList.add(deletedItem!!)
                myActivity.userItemTemplateList.save()
                //Re-Add itemName to itemNameList
                myActivity.multiShoppingFr.refreshItemNamesAndAutoCompleteAdapter()

                deletedItem = null
                myAdapter.notifyDataSetChanged()
                updateUndoCustomIcon()
                updateClearCustomListIcon()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    fun updateClearCustomListIcon(){
       myMenu.findItem(R.id.item_custom_clear).isVisible = myActivity.userItemTemplateList.size > 0
    }

    fun updateUndoCustomIcon(){
        myMenu.findItem(R.id.item_custom_undo).isVisible = deletedItem != null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        myMenu = menu
        inflater.inflate(R.menu.menu_custom_items, menu)
        super.onCreateOptionsMenu(menu, inflater)
        updateClearCustomListIcon()
        updateUndoCustomIcon()
    }
    //Deletes all checked tasks and animates the deletion

}

class SwipeToDeleteCustomItem(direction: Int, shoppingFr: MultiShoppingFr, val myActivity: MainActivity): ItemTouchHelper
.SimpleCallback(0, direction){

    private val myShoppingFr = shoppingFr

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val parsedViewHolder = viewHolder as CustomItemAdapter.CustomItemViewHolder
        val item = parsedViewHolder.myItem
        val itemName = item.n

        //save deleted item for undo purposes
        CustomItemFr.deletedItem = item

        //delete item from userItemTemplateList
        myActivity.userItemTemplateList.remove(item)
        myActivity.userItemTemplateList.save()

        myActivity.multiShoppingFr.refreshItemNamesAndAutoCompleteAdapter()

        //animate remove in recycler view adapter
        CustomItemFr.myAdapter.notifyItemRemoved(viewHolder.bindingAdapterPosition)

        //update options menu
        CustomItemFr.myFragment.updateUndoCustomIcon()
        CustomItemFr.myFragment.updateClearCustomListIcon()
    }
}

class CustomItemAdapter(val myActivity: MainActivity) :
    RecyclerView.Adapter<CustomItemAdapter.CustomItemViewHolder>(){

    private val round = SettingsManager.getSetting(SettingId.SHAPES_ROUND) as Boolean
    private val cr = myActivity.resources.getDimension(R.dimen.cornerRadius)

    override fun getItemCount() = myActivity.userItemTemplateList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_custom_item, parent, false)
        return CustomItemViewHolder(itemView)
    }


    @SuppressLint("SetTextI18n", "InflateParams")
    override fun onBindViewHolder(holder: CustomItemViewHolder, position: Int) {


        if(round){
            holder.itemView.cvCustom.radius = cr
        }

        val currentItem = myActivity.userItemTemplateList[holder.bindingAdapterPosition]
        holder.myItem = currentItem

        //changes design of task based on priority and being checked
        var itemText = currentItem.n
        if(currentItem.n.length > 12){
            itemText = itemText.substring(0,11)+".."
        }
        holder.itemView.tvName.text = itemText + " : "+currentItem.s
        val id = myActivity.resources.getStringArray(R.array.categoryCodes).indexOf(currentItem.c)
        holder.itemView.tvCategory.text = myActivity.resources.getStringArray(R.array.categoryNames)[id]
    }

    class CustomItemViewHolder( itemView: View) : RecyclerView.ViewHolder(itemView){
        lateinit var myItem: ItemTemplate
    }
}
