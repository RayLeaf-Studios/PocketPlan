package com.pocket_plan.j7_003.data.settings.sub_categories.shoppinglist

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.data.shoppinglist.UserItemTemplateList
import kotlinx.android.synthetic.main.fragment_custom_items.view.*
import kotlinx.android.synthetic.main.row_custom_item.view.*
import kotlinx.android.synthetic.main.row_task.view.tvName

class CustomItemFr : Fragment() {

    lateinit var myMenu: Menu
    companion object{
        lateinit var myFragment: CustomItemFr
        lateinit var myAdapter: CustomItemAdapter
        lateinit var myRecycler: RecyclerView

        lateinit var userItemTemplateList: UserItemTemplateList

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val myView = inflater.inflate(R.layout.fragment_custom_items, container, false)
        myRecycler = myView.recycler_view_customItems
        myFragment = this

        userItemTemplateList = UserItemTemplateList()

        /**
         * Connecting Adapter, Layout-Manager and Swipe Detection to UI elements
         */

        myAdapter = CustomItemAdapter()
        myRecycler.adapter = myAdapter
        myRecycler.layoutManager = LinearLayoutManager(activity)
        myRecycler.setHasFixedSize(true)


        val swipeHelperLeft = ItemTouchHelper(SwipeToDeleteCustomItem(ItemTouchHelper.LEFT, myAdapter))
        swipeHelperLeft.attachToRecyclerView(myRecycler)
        val swipeHelperRight = ItemTouchHelper(SwipeToDeleteCustomItem(ItemTouchHelper.RIGHT, myAdapter))
        swipeHelperRight.attachToRecyclerView(myRecycler)


        return myView
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
           R.id.item_custom_clear -> {
               val action : () -> Unit = {
                   userItemTemplateList.clear()
                   userItemTemplateList.save()
                   myAdapter.notifyDataSetChanged()
                   updateClearCustomListIcon()
               }
               val titleId = R.string.custom_item_delete_title
               MainActivity.act.dialogConfirmDelete(titleId, action)
           }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    private fun updateClearCustomListIcon(){
       myMenu.findItem(R.id.item_custom_clear).isVisible = userItemTemplateList.size > 0
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        myMenu = menu
        inflater.inflate(R.menu.menu_custom_items, menu)
        super.onCreateOptionsMenu(menu, inflater)
        updateClearCustomListIcon()
    }

    //Deletes all checked tasks and animates the deletion

}
class SwipeToDeleteCustomItem(direction: Int,  val adapter: CustomItemAdapter): ItemTouchHelper
.SimpleCallback(0, direction){
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val parsed = viewHolder as CustomItemAdapter.CustomItemViewHolder
        CustomItemFr.userItemTemplateList.removeItem(parsed.itemView.tvName.text.toString())
        CustomItemFr.myAdapter.notifyItemRemoved(viewHolder.adapterPosition)
    }
}

class CustomItemAdapter :
    RecyclerView.Adapter<CustomItemAdapter.CustomItemViewHolder>(){

    private val round = SettingsManager.getSetting(SettingId.SHAPES_ROUND) as Boolean
    private val cr = MainActivity.act.resources.getDimension(R.dimen.cornerRadius)

    override fun getItemCount() = CustomItemFr.userItemTemplateList.size

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

        val currentItem = CustomItemFr.userItemTemplateList[holder.adapterPosition]

        //changes design of task based on priority and being checked
        holder.itemView.tvName.text = currentItem.n
        val id = MainActivity.act.resources.getStringArray(R.array.categoryCodes).indexOf(currentItem.c)
        holder.itemView.tvCategory.text = MainActivity.act.resources.getStringArray(R.array.categoryNames)[id]
    }

    class CustomItemViewHolder( itemView: View) : RecyclerView.ViewHolder(itemView)
}
