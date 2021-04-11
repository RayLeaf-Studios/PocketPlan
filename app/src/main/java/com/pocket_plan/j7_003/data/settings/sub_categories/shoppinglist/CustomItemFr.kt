package com.pocket_plan.j7_003.data.settings.sub_categories.shoppinglist

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.data.shoppinglist.AutoCompleteAdapter
import com.pocket_plan.j7_003.data.shoppinglist.ItemTemplate
import com.pocket_plan.j7_003.data.shoppinglist.ShoppingFr
import kotlinx.android.synthetic.main.fragment_custom_items.view.*
import kotlinx.android.synthetic.main.row_custom_item.view.*
import kotlinx.android.synthetic.main.row_task.view.tvName

class CustomItemFr : Fragment() {
    private lateinit var myShoppingFr: ShoppingFr

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
        myShoppingFr = myActivity.getFragment(FT.SHOPPING) as ShoppingFr

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
                   MainActivity.userItemTemplateList.forEach{ item ->
                       MainActivity.itemNameList.remove(item.n)
                   }
                   val newActAdapter = AutoCompleteAdapter(
                       context = myActivity,
                       resource = android.R.layout.simple_spinner_dropdown_item,
                       items = MainActivity.itemNameList.toMutableList()
                   )
                   myShoppingFr.autoCompleteTv.setAdapter(newActAdapter)

                   MainActivity.userItemTemplateList.clear()
                   MainActivity.userItemTemplateList.save()
                   myAdapter.notifyDataSetChanged()
                   updateClearCustomListIcon()
               }
               val titleId = R.string.custom_item_delete_title
               myActivity.dialogConfirmDelete(titleId, action)
           }
            R.id.item_custom_undo -> {
                MainActivity.userItemTemplateList.add(deletedItem!!)
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
       myMenu.findItem(R.id.item_custom_clear).isVisible = MainActivity.userItemTemplateList.size > 0
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
class SwipeToDeleteCustomItem(direction: Int, shoppingFr: ShoppingFr, val mainActivity: MainActivity): ItemTouchHelper
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

        //remove item from item name list, if it doesn't exist as a regular item
        if(mainActivity.itemTemplateList.getTemplateByName(itemName)==null){
            MainActivity.itemNameList.remove(itemName)
        }

        //set new adapter for autocomplete text in add item dialog
        val newActAdapter = AutoCompleteAdapter(
            mainActivity,
            android.R.layout.simple_spinner_dropdown_item,
            items = MainActivity.itemNameList.toMutableList()
        )
        myShoppingFr.autoCompleteTv.setAdapter(newActAdapter)

        //save deleted item for undo purposes
        CustomItemFr.deletedItem = item

        //delete item from userItemTemplateList
        MainActivity.userItemTemplateList.remove(item)

        //Save changes in userItemTemplateList
        MainActivity.userItemTemplateList.save()

        //animate remove in recycler view adapter
        CustomItemFr.myAdapter.notifyItemRemoved(viewHolder.adapterPosition)

        //update options menu
        CustomItemFr.myFragment.updateUndoCustomIcon()
        CustomItemFr.myFragment.updateClearCustomListIcon()
    }
}

class CustomItemAdapter(val mainActivity: MainActivity) :
    RecyclerView.Adapter<CustomItemAdapter.CustomItemViewHolder>(){

    private val round = SettingsManager.getSetting(SettingId.SHAPES_ROUND) as Boolean
    private val cr = mainActivity.resources.getDimension(R.dimen.cornerRadius)

    override fun getItemCount() = MainActivity.userItemTemplateList.size

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

        val currentItem = MainActivity.userItemTemplateList[holder.adapterPosition]
        holder.myItem = currentItem

        //changes design of task based on priority and being checked
        var itemText = currentItem.n
        if(currentItem.n.length > 12){
            itemText = itemText.substring(0,11)+".."
        }
        holder.itemView.tvName.text = itemText + " : "+currentItem.s
        val id = mainActivity.resources.getStringArray(R.array.categoryCodes).indexOf(currentItem.c)
        holder.itemView.tvCategory.text = mainActivity.resources.getStringArray(R.array.categoryNames)[id]
    }

    class CustomItemViewHolder( itemView: View) : RecyclerView.ViewHolder(itemView){
        lateinit var myItem: ItemTemplate
    }
}
