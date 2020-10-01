package com.pocket_plan.j7_003.data.shoppinglist

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import kotlinx.android.synthetic.main.dialog_add_item.view.*
import kotlinx.android.synthetic.main.fragment_shopping.view.*
import kotlinx.android.synthetic.main.row_category.view.*
import kotlinx.android.synthetic.main.row_item.view.*
import kotlinx.android.synthetic.main.title_dialog.view.*


/**
 * A simple [Fragment] subclass.
 */
class ShoppingFr : Fragment() {
    lateinit var myMenu: Menu

    companion object {

        var deletedItem: ShoppingItem? = null

        lateinit var shoppingListInstance: ShoppingList
        lateinit var shoppingListAdapter: ShoppingListAdapter
        lateinit var layoutManager: LinearLayoutManager
        lateinit var myFragment: ShoppingFr

        var offsetTop: Int = 0
        var firstPos: Int = 0
        var expandOne: Boolean = false
        var collapseCheckedSublists: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_shopping, menu)
        myMenu = menu
        updateShoppingMenu()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_shopping_clear_list -> {
                dialogShoppingClear()
            }

            R.id.item_shopping_uncheck_all -> {
                //uncheck all shopping items
                shoppingListInstance.uncheckAll()
                shoppingListAdapter.notifyDataSetChanged()
            }

            R.id.item_shopping_undo -> {
                //undo the last deletion of a shopping item
                shoppingListInstance.add(deletedItem!!)
                deletedItem = null
                shoppingListAdapter.notifyDataSetChanged()

            }
            R.id.item_shopping_collapse_all -> {
                //collapse all categories
                shoppingListInstance.collapseAllTags()
                shoppingListAdapter.notifyItemRangeChanged(0, shoppingListInstance.size)

            }
            R.id.item_shopping_expand_all -> {
                //expand all categories
                shoppingListInstance.expandAllTags()
                shoppingListAdapter.notifyItemRangeChanged(0, shoppingListInstance.size)

            }
        }
        updateShoppingMenu()

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myFragment = this
        deletedItem = null
        shoppingListInstance = ShoppingList()
        expandOne = SettingsManager.getSetting(SettingId.EXPAND_ONE_CATEGORY) as Boolean
        collapseCheckedSublists =
            SettingsManager.getSetting(SettingId.COLLAPSE_CHECKED_SUBLISTS) as Boolean
        //if expandOne Setting = true, expand one category, contract all others, if setting says so
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

    fun updateShoppingMenu() {
        updateUndoItemIcon()
        updateDeleteShoppingListIcon()
        updateUncheckShoppingListIcon()
        updateExpandAllIcon()
        updateCollapseAllIcon()
    }

    private fun updateUndoItemIcon() {
        myMenu.findItem(R.id.item_shopping_undo)?.isVisible = deletedItem != null
    }

    fun updateExpandAllIcon() {
        myMenu.findItem(R.id.item_shopping_expand_all)?.isVisible =
            shoppingListInstance.somethingsCollapsed() && !(SettingsManager.getSetting(SettingId.EXPAND_ONE_CATEGORY) as Boolean)
    }

    fun updateCollapseAllIcon() {
        myMenu.findItem(R.id.item_shopping_collapse_all)?.isVisible =
            shoppingListInstance.somethingIsExpanded()
    }

    private fun updateDeleteShoppingListIcon() {
        myMenu.findItem(R.id.item_shopping_clear_list)?.isVisible = shoppingListInstance.size > 0
    }

    private fun updateUncheckShoppingListIcon() {
        myMenu.findItem(R.id.item_shopping_uncheck_all)?.isVisible =
            shoppingListInstance.somethingIsChecked()

    }


    @SuppressLint("InflateParams")
    private fun dialogShoppingClear() {
        val titleId = R.string.shopping_dialog_clear_title
        val action: () -> Unit = {
            shoppingListInstance.clear()
            shoppingListInstance.save()
            shoppingListAdapter.notifyDataSetChanged()
            myFragment.updateShoppingMenu()
        }
        MainActivity.act.dialogConfirmDelete(titleId, action)
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

    @SuppressLint("InflateParams")
    fun preloadAddItemDialog(mylayoutInflater: LayoutInflater) {

        //initialize shopping list data
        MainActivity.tagList = TagList()
        MainActivity.tagNames = MainActivity.tagList.getTagNames()
        MainActivity.itemTemplateList = ItemTemplateList()
        MainActivity.userItemTemplateList = UserItemTemplateList()
        shoppingListInstance = ShoppingList()

        //initialize itemNameList
        MainActivity.itemNameList = ArrayList()

        MainActivity.userItemTemplateList.forEach {
            MainActivity.itemNameList.add(it.n)
        }

        MainActivity.itemTemplateList.forEach {
            if (!MainActivity.itemNameList.contains(it.n)) {
                MainActivity.itemNameList.add(it.n)
            }
        }

        //inflate view for this dialog
        MainActivity.addItemDialogView =
            mylayoutInflater.inflate(R.layout.dialog_add_item, null)

        //Initialize dialogBuilder and set its title
        val myBuilder = MainActivity.act.let { it1 ->
            AlertDialog.Builder(it1).setView(MainActivity.addItemDialogView)
        }
        val customTitle = mylayoutInflater.inflate(R.layout.title_dialog, null)
        customTitle.tvDialogTitle.text = MainActivity.act.getString(R.string.shoppingAddItemTitle)
        myBuilder?.setCustomTitle(customTitle)
        MainActivity.addItemDialog = myBuilder?.create()

        //initialize autocompleteTextView and spinner for item unit
        val actvItem = MainActivity.addItemDialogView!!.actvItem
        val spItemUnit = MainActivity.addItemDialogView!!.spItemUnit


        //initialize spinner for categories
        val spCategory = MainActivity.addItemDialogView!!.spCategory
        val categoryAdapter = ArrayAdapter<String>(
            MainActivity.act, android.R.layout.simple_list_item_1, MainActivity.tagNames
        )

        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategory.adapter = categoryAdapter

        //Initialize spinner and its adapter to choose its Unit
        val mySpinner = MainActivity.addItemDialogView!!.spItemUnit
        val myAdapter = ArrayAdapter<String>(
            MainActivity.act, android.R.layout.simple_list_item_1,
            MainActivity.act.resources.getStringArray(R.array.units)
        )

        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mySpinner.adapter = myAdapter


        //initialize autocompleteTextView and its adapter
        val autoCompleteTv = MainActivity.addItemDialogView!!.actvItem
        val autoCompleteTvAdapter = ArrayAdapter<String>(
            MainActivity.act,
            android.R.layout.simple_spinner_dropdown_item,
            MainActivity.itemNameList
        )

        autoCompleteTv.setAdapter(autoCompleteTvAdapter)
        autoCompleteTv.requestFocus()

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //check for existing user template
                var template =
                    MainActivity.userItemTemplateList.getTemplateByName(actvItem.text.toString())
                if (template != null) {
                    //display correct category
                    spCategory.setSelection(MainActivity.tagNames.indexOf(template.c.name))

                    //display correct unit
                    val unitPointPos = MainActivity.act.resources.getStringArray(R.array.units).indexOf(template.s)
                    spItemUnit.setSelection(unitPointPos)
                    return
                }

                //check for existing item template
                template = MainActivity.itemTemplateList.getTemplateByName(actvItem.text.toString())
                if (template != null) {
                    //display correct category
                    spCategory.setSelection(MainActivity.tagNames.indexOf(template.c.name))

                    //display correct unit
                    val unitPointPos = MainActivity.act.resources.getStringArray(R.array.units).indexOf(template.s)
                    spItemUnit.setSelection(unitPointPos)
                } else {
                    spCategory.setSelection(0)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }

        autoCompleteTv.addTextChangedListener(textWatcher)

        //initialize edit text for item amount string
        val etItemAmount = MainActivity.addItemDialogView!!.etItemAmount
        etItemAmount.setText("1")

        var firstTap = true
        etItemAmount.setOnFocusChangeListener { _, _ ->
            if (firstTap) {
                etItemAmount.setText("")
                firstTap = false
            }
        }

        MainActivity.addItemDialogView!!.btnCancelItem.setOnClickListener {
            MainActivity.addItemDialog?.dismiss()
        }
        //Button to Confirm adding Item to list
        MainActivity.addItemDialogView!!.btnAddItemToList.setOnClickListener {
            if (actvItem.text.toString() == "") {
                //animation
                val animationShake =
                    AnimationUtils.loadAnimation(MainActivity.act, R.anim.shake)
                MainActivity.addItemDialogView!!.actvItem.startAnimation(animationShake)
                return@setOnClickListener
            }
            val tagList = TagList()
            val tag = tagList.getTagByName(spCategory.selectedItem as String)
            //check if user template exists
            var template =
                MainActivity.userItemTemplateList.getTemplateByName(actvItem.text.toString())

            if (template == null) {
                //no user item with this name => check for regular template
                template = MainActivity.itemTemplateList.getTemplateByName(actvItem.text.toString())
                if (template == null || tag != template!!.c) {
                    //item unknown, use selected category, add item, and save it to userTemplate list
                    MainActivity.userItemTemplateList.add(
                        ItemTemplate(
                            actvItem.text.toString(), tag,
                            spItemUnit.selectedItem.toString()
                        )
                    )
                    val item = ShoppingItem(
                        actvItem.text.toString(), tag,
                        spItemUnit.selectedItem.toString(),
                        etItemAmount.text.toString(),
                        spItemUnit.selectedItem.toString(),
                        false
                    )
                    shoppingListInstance.add(item)

                    if (MainActivity.activeFragmentTag == FT.SHOPPING) {
                        shoppingListAdapter.notifyDataSetChanged()
                        myFragment.updateShoppingMenu()
                    } else {
                        Toast.makeText(MainActivity.act, MainActivity.act.getString(R.string.shopping_item_added), Toast.LENGTH_SHORT)
                            .show()
                    }
                    MainActivity.itemNameList.add(actvItem.text.toString())
                    val autoCompleteTvAdapter2 = ArrayAdapter<String>(
                        MainActivity.act,
                        android.R.layout.simple_spinner_dropdown_item,
                        MainActivity.itemNameList
                    )
                    autoCompleteTv.setAdapter(autoCompleteTvAdapter2)
                    actvItem.setText("")
                    if (MainActivity.activeFragmentTag == FT.HOME || SettingsManager.getSetting(
                            SettingId.CLOSE_ITEM_DIALOG
                        ) as Boolean
                    ) {
                        MainActivity.addItemDialog?.dismiss()
                    }
                    return@setOnClickListener
                }
            }

            if (tag != template!!.c) {
                //known as user item but with different tag
                MainActivity.userItemTemplateList.removeItem(actvItem.text.toString())
                MainActivity.userItemTemplateList.add(
                    ItemTemplate(
                        actvItem.text.toString(), tag,
                        spItemUnit.selectedItem.toString()
                    )
                )
            }
            //add already known item to list
            val item = ShoppingItem(
                template!!.n,
                tag,
                template!!.s,
                etItemAmount!!.text.toString(),
                spItemUnit.selectedItem.toString(),
                false
            )
            ShoppingFr.shoppingListInstance.add(item)
            if (MainActivity.activeFragmentTag == FT.SHOPPING) {
                shoppingListAdapter.notifyDataSetChanged()
                myFragment.updateShoppingMenu()
            } else {
                Toast.makeText(MainActivity.act, MainActivity.act.getString(R.string.shopping_item_added), Toast.LENGTH_SHORT).show()
            }
            actvItem.setText("")
            if (MainActivity.activeFragmentTag == FT.HOME) {
                MainActivity.addItemDialog?.dismiss()
            }
        }

        val imm =
            MainActivity.act.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, InputMethodManager.SHOW_FORCED)
    }

    fun openAddItemDialog() {
        MainActivity.addItemDialogView!!.actvItem.setText("")
        MainActivity.addItemDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        MainActivity.addItemDialog?.show()
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

        if (position == ShoppingFr.shoppingListInstance.size) {
            holder.itemView.visibility = View.INVISIBLE
            holder.itemView.layoutParams.height = 250
            holder.itemView.subRecyclerView.visibility = View.GONE
            holder.itemView.setOnClickListener {}
            return
        }
        holder.itemView.subRecyclerView.visibility = View.VISIBLE
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
            ShoppingFr.myFragment.updateExpandAllIcon()
            ShoppingFr.myFragment.updateCollapseAllIcon()
        }
    }

    fun manageCheckedCategory(
        holder: CategoryViewHolder, allChecked: Boolean,
        numberOfItems: Int, tag: Tag
    ) {
        if (!allChecked) {
            val colorBackground = ContextCompat
                .getColor(MainActivity.act, R.color.colorOnBackGround)
            val background = when(tag.name){
                "Sonstiges" -> R.drawable.s_sonstiges
                "Obst & Gemüse" -> R.drawable.s_obst
                "Getränke" -> R.drawable.s_getraenke
                "Nudeln & Getreide" -> R.drawable.s_nudeln
                "Backwaren" -> R.drawable.s_backwaren
                "Kühlregal Milch" -> R.drawable.s_milch
                "Kühlregal Fleisch" -> R.drawable.s_fleisch
                "Tiefkühl" -> R.drawable.s_tiefkuehl
                "Konserven & Fertiges" -> R.drawable.s_konserven
                "Frühstück & Co." -> R.drawable.s_fruehstuck
                "Gewürze & Dressings" -> R.drawable.s_gewuerze
                "Haushalt" -> R.drawable.s_haushalt
                "Snacks" -> R.drawable.s_snacks
                "Backzutaten" -> R.drawable.s_backzutaten
                "Drogerie & Kosmetik" -> R.drawable.s_drogerie
                 else -> R.drawable.s_alkohol
            }
            holder.cvCategory.setBackgroundResource(background)
            holder.tvCategoryName.setTextColor(colorBackground)
            holder.tvNumberOfItems.setTextColor(colorBackground)
            holder.tvNumberOfItems.text = numberOfItems.toString()
        } else {
            val colorHint = ContextCompat.getColor(MainActivity.act, R.color.colorHint)
            holder.cvCategory.setBackgroundResource(R.drawable.s_checked)
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
            MainActivity.act.toast("invalid checked state")
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
        holder.itemView.tvItemTitle.text = MainActivity.act.getString(
            R.string.shoppingItemTitle, item.amount, item.unit, item.name
        )

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
            if (newPosition > -1) {
                notifyItemMoved(holder.adapterPosition, newPosition)
            } else {
                MainActivity.act.toast("invalid item checked state")
            }

            val sublistMoveInfo = ShoppingFr.shoppingListInstance.sortTag(tag)
            if (sublistMoveInfo != null) {
                ShoppingFr.myFragment.prepareForMove()
                ShoppingFr.shoppingListAdapter
                    .notifyItemMoved(sublistMoveInfo.first, sublistMoveInfo.second)

                ShoppingFr.myFragment.reactToMove()
            }
            ShoppingFr.myFragment.updateShoppingMenu()
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
        val removeInfo = ShoppingFr.shoppingListInstance.removeItem(parsed.tag, position)
        if (removeInfo.second) {
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
        ShoppingFr.deletedItem = removeInfo.first
        ShoppingFr.myFragment.updateShoppingMenu()

    }
}
