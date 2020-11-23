package com.pocket_plan.j7_003.data.shoppinglist

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
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
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 */
class ShoppingFr : Fragment() {
    private lateinit var myMenu: Menu

    companion object {

        var deletedItem: ShoppingItem? = null

        lateinit var shoppingListInstance: ShoppingList
        lateinit var myAdapter: ShoppingListAdapter
        lateinit var layoutManager: LinearLayoutManager
        lateinit var myFragment: ShoppingFr

        var editTag: String = ""
        var editPos: Int = 0
        var editing = false

        lateinit var autoCompleteTv: AutoCompleteTextView

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
        myMenu.findItem(R.id.item_shopping_undo)?.icon?.setTint(MainActivity.act.colorForAttr(R.attr.colorOnBackGround))
        updateShoppingMenu()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //decides if options menu will be refreshed immediately after option is selected
        var menuRefresh = true

        when (item.itemId) {
            R.id.item_shopping_clear_list -> {
                //menu refresh is handled in dialog action
                menuRefresh = false
                dialogShoppingClear()
            }

            R.id.item_shopping_uncheck_all -> {
                //uncheck all shopping items
                shoppingListInstance.uncheckAll()
                myAdapter.notifyDataSetChanged()
            }

            R.id.item_shopping_undo -> {
                //undo the last deletion of a shopping item
                shoppingListInstance.add(deletedItem!!)
                deletedItem = null
                myAdapter.notifyDataSetChanged()

            }
            R.id.item_shopping_collapse_all -> {
                //collapse all categories
                shoppingListInstance.collapseAllTags()
                myAdapter.notifyItemRangeChanged(0, shoppingListInstance.size)
            }
            R.id.item_shopping_expand_all -> {
                //expand all categories
                shoppingListInstance.expandAllTags()
                myAdapter.notifyItemRangeChanged(0, shoppingListInstance.size)
            }
        }

        if(menuRefresh) updateShoppingMenu()

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
        //if expandOne Setting = true, expand one category, contract all others
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
        myAdapter = ShoppingListAdapter()

        //attach adapter to recycler and initialize parameters of recycler
        myRecycler.adapter = myAdapter
        layoutManager = LinearLayoutManager(activity)
        myRecycler.layoutManager = layoutManager
        myRecycler.setHasFixedSize(true)

        //ItemTouchHelper to support drag and drop reordering
        val itemTouchHelper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.END or ItemTouchHelper.START,
                0
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
                ): Boolean {
                    val fromPos = viewHolder.adapterPosition

                    if (fromPos == shoppingListInstance.size) {
                        return true
                    }


                    var toPos = target.adapterPosition

                    if (toPos == shoppingListInstance.size) toPos--

                    //swap items in list
                    Collections.swap(
                        shoppingListInstance, fromPos, toPos
                    )

                    shoppingListInstance.save()

                    // move item in `fromPos` to `toPos` in adapter.
                    myAdapter.notifyItemMoved(fromPos, toPos)

                    return true // true if moved, false otherwise
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    /* no-op, swiping categories is not supported */
                }
            })

        itemTouchHelper.attachToRecyclerView(myRecycler)

        return myView
    }

    /**
     * Refreshes option menu, removes options that can't be executed
     */
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
            myAdapter.notifyDataSetChanged()
            deletedItem = null
            myFragment.updateShoppingMenu()
        }
        MainActivity.act.dialogConfirmDelete(titleId, action)
    }

    /**
     * Helper function to prevent scrolling due to notifyMove
     */
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

    /**
     * Helper function to prevent scrolling due to notifyMove
     */
    fun reactToMove() {
        layoutManager.scrollToPositionWithOffset(firstPos, offsetTop)
    }

    /**
     * Prepare layout and adapters for addItemDialog to decrease loading time
     */
    @SuppressLint("InflateParams")
    fun preloadAddItemDialog(mylayoutInflater: LayoutInflater) {

        //initialize shopping list data
        MainActivity.tagList = TagList()
        MainActivity.itemTemplateList = ItemTemplateList()
        MainActivity.userItemTemplateList = UserItemTemplateList()
        shoppingListInstance = ShoppingList()

        //initialize itemNameList
        MainActivity.itemNameList = ArrayList()

        MainActivity.userItemTemplateList.forEach {
            MainActivity.itemNameList.add(it.n)
        }

        MainActivity.itemTemplateList.forEach {
            //Todo check why this if is necessary
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
        MainActivity.shoppingTitle = customTitle
        myBuilder?.setCustomTitle(customTitle)
        MainActivity.addItemDialog = myBuilder?.create()

        //initialize autocompleteTextView and spinner for item unit
        val actvItem = MainActivity.addItemDialogView!!.actvItem
        val spItemUnit = MainActivity.addItemDialogView!!.spItemUnit


        //initialize spinner for categories
        val spCategory = MainActivity.addItemDialogView!!.spCategory
        val categoryAdapter = ArrayAdapter<String>(
            MainActivity.act,
            android.R.layout.simple_list_item_1,
            MainActivity.act.resources.getStringArray(R.array.categoryNames)
        )

        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategory.adapter = categoryAdapter


        val unitAdapter = ArrayAdapter(
            MainActivity.act, android.R.layout.simple_list_item_1,
            MainActivity.act.resources.getStringArray(R.array.units)
        )

        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spItemUnit.adapter = unitAdapter


        spItemUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0) {
                    MainActivity.unitChanged = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                /* no-op */
            }

        }


        //initialize autocompleteTextView and its adapter
        autoCompleteTv = MainActivity.addItemDialogView!!.actvItem
        val autoCompleteTvAdapter = ArrayAdapter(
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
                    spCategory.setSelection(
                        MainActivity.act.resources.getStringArray(R.array.categoryCodes)
                            .indexOf(template.c)
                    )

                    //display correct unit
                    val unitPointPos =
                        MainActivity.act.resources.getStringArray(R.array.units).indexOf(template.s)
                    if (!MainActivity.unitChanged) {
                        spItemUnit.setSelection(unitPointPos)
                        MainActivity.unitChanged = false
                    }
                    return
                }

                //check for existing item template
                template = MainActivity.itemTemplateList.getTemplateByName(actvItem.text.toString())
                if (template != null) {
                    //display correct category
                    spCategory.setSelection(
                        MainActivity.act.resources.getStringArray(R.array.categoryCodes)
                            .indexOf(template.c)
                    )

                    //display correct unit
                    val unitPointPos =
                        MainActivity.act.resources.getStringArray(R.array.units).indexOf(template.s)
                    if (!MainActivity.unitChanged) {
                        spItemUnit.setSelection(unitPointPos)
                        MainActivity.unitChanged = false
                    }
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

        etItemAmount.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etItemAmount.setText("")
            }
        }

        MainActivity.addItemDialogView!!.btnCancelItem.setOnClickListener {
            MainActivity.addItemDialog?.dismiss()
        }

        autoCompleteTv.setOnKeyListener { v, keyCode, event ->
            if(keyCode==KeyEvent.KEYCODE_ENTER && event.action==KeyEvent.ACTION_DOWN){
                MainActivity.addItemDialogView!!.btnAddItemToList.performClick()
            }
            true
        }

        val checkMark = MainActivity.addItemDialogView!!.ivCheckItemAdded
        checkMark.visibility = View.GONE
        //Button to Confirm adding Item to list
        MainActivity.addItemDialogView!!.btnAddItemToList.setOnClickListener {


            if (actvItem.text.toString() == "") {
                //No item string entered => play shake animation
                val animationShake =
                    AnimationUtils.loadAnimation(MainActivity.act, R.anim.shake)
                MainActivity.addItemDialogView!!.actvItem.startAnimation(animationShake)
                return@setOnClickListener
            }

            //check mark animation to confirm adding of item
            checkMark.visibility = View.VISIBLE
            checkMark.animate().translationYBy(-80f).setDuration(600L).withEndAction { checkMark.animate().translationY(0f).setDuration(0).start() }.start()
            checkMark.animate().alpha(0f).setDuration(600L).withEndAction {
                checkMark.animate().alpha(1f).setDuration(0).start()
                checkMark.visibility = View.GONE
            }.start()

            //selected categoryCode
            val categoryCode =
                MainActivity.act.resources.getStringArray(R.array.categoryCodes)[MainActivity.act.resources.getStringArray(
                    R.array.categoryNames
                ).indexOf(spCategory.selectedItem as String)]

            //check if user template exists for this string
            var template =
                MainActivity.userItemTemplateList.getTemplateByName(actvItem.text.toString())

            if (template == null) {
                //no user item with this name => check for regular template

                template = MainActivity.itemTemplateList.getTemplateByName(actvItem.text.toString())
                if (template == null || categoryCode != template!!.c || spItemUnit.selectedItemPosition != 0) {
                    //item unknown, or item known under different category or with different unit, use selected category and unit,
                    // add item, and save it to userTemplate list

                    MainActivity.userItemTemplateList.add(
                        ItemTemplate(
                            actvItem.text.toString(), categoryCode,
                            spItemUnit.selectedItem.toString()
                        )
                    )

                    val item = ShoppingItem(
                        actvItem.text.toString(), categoryCode,
                        spItemUnit.selectedItem.toString(),
                        etItemAmount.text.toString(),
                        spItemUnit.selectedItem.toString(),
                        false
                    )
                    if(editing){
                        shoppingListInstance.removeItem(editTag, editPos)
                        editing = false
                        MainActivity.addItemDialog?.dismiss()
                    }
                    shoppingListInstance.add(item)

                    if (MainActivity.previousFragmentStack.peek() == FT.SHOPPING) {
                        myAdapter.notifyDataSetChanged()
                        myFragment.updateShoppingMenu()
                    } else {
                        Toast.makeText(
                            MainActivity.act,
                            MainActivity.act.getString(R.string.shopping_item_added),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    MainActivity.itemNameList.add(actvItem.text.toString())
                    val autoCompleteTvAdapter2 = ArrayAdapter(
                        MainActivity.act,
                        android.R.layout.simple_spinner_dropdown_item,
                        MainActivity.itemNameList
                    )
                    autoCompleteTv.setAdapter(autoCompleteTvAdapter2)
                    actvItem.setText("")
                    etItemAmount.setText("1")
                    spItemUnit.setSelection(0)
                    MainActivity.unitChanged = false
                    actvItem.requestFocus()
                    if (MainActivity.previousFragmentStack.peek() == FT.HOME || SettingsManager.getSetting(
                            SettingId.CLOSE_ITEM_DIALOG
                        ) as Boolean
                    ) {
                        MainActivity.addItemDialog?.dismiss()
                    }
                    return@setOnClickListener
                }
            }

            if (categoryCode != template!!.c || spItemUnit.selectedItem.toString() != template!!.s) {
                //known as user item but with different tag or different suggested unit
                MainActivity.userItemTemplateList.removeItem(actvItem.text.toString())
                MainActivity.userItemTemplateList.add(
                    ItemTemplate(
                        actvItem.text.toString(), categoryCode,
                        spItemUnit.selectedItem.toString()
                    )
                )
            }
            //add already known item to list
            val item = ShoppingItem(
                template!!.n,
                categoryCode,
                template!!.s,
                etItemAmount!!.text.toString(),
                spItemUnit.selectedItem.toString(),
                false
            )
            shoppingListInstance.add(item)
            if(editing){
                shoppingListInstance.removeItem(editTag, editPos)
                editing = false
                MainActivity.addItemDialog?.dismiss()
            }
            if (MainActivity.previousFragmentStack.peek() == FT.SHOPPING) {
                myAdapter.notifyDataSetChanged()
                myFragment.updateShoppingMenu()
            } else {
                Toast.makeText(
                    MainActivity.act,
                    MainActivity.act.getString(R.string.shopping_item_added),
                    Toast.LENGTH_SHORT
                ).show()
            }
            actvItem.setText("")
            etItemAmount.setText("1")
            spItemUnit.setSelection(0)
            MainActivity.unitChanged = false
            actvItem.requestFocus()
            if (MainActivity.previousFragmentStack.peek() == FT.HOME) {
                MainActivity.addItemDialog?.dismiss()
            }
        }

        val imm =
            MainActivity.act.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, InputMethodManager.SHOW_FORCED)
    }

    /**
     * Reset and open addItemDialog
     */
    fun openAddItemDialog() {
        MainActivity.shoppingTitle!!.tvDialogTitle.text = MainActivity.act.getString(R.string.shoppingAddItemTitle)
        MainActivity.addItemDialogView!!.actvItem.setText("")
        MainActivity.addItemDialogView!!.btnAddItemToList.text = MainActivity.act.getString(R.string.birthdayDialogAdd)
        MainActivity.addItemDialogView!!.spItemUnit.setSelection(0)
        MainActivity.unitChanged = false
        MainActivity.addItemDialogView!!.etItemAmount.setText("1")
        MainActivity.addItemDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        MainActivity.addItemDialog?.show()
    }

    fun openEditItemDialog(item: ShoppingItem) {
        MainActivity.shoppingTitle!!.tvDialogTitle.text = MainActivity.act.getString(R.string.shoppingEditItemTitle)
        MainActivity.addItemDialogView!!.btnAddItemToList.text = resources.getString(R.string.noteDiscardDialogSave)
        //show item name
        MainActivity.addItemDialogView!!.actvItem.setText(item.name)
        //set cursor to end of item name
        MainActivity.addItemDialogView!!.actvItem.setSelection(item.name!!.length)
        //select correct unit
        MainActivity.addItemDialogView!!.spItemUnit.setSelection(MainActivity.act.resources.getStringArray(R.array.units).indexOf(item.suggestedUnit))
        //mark that unit did not get changed
        MainActivity.unitChanged = false
        //show correct item amount
        MainActivity.addItemDialogView!!.etItemAmount.setText(item.amount.toString())
        //open keyboard
        MainActivity.addItemDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        MainActivity.addItemDialogView!!.actvItem.dismissDropDown()
        //show dialog
        MainActivity.addItemDialog?.show()
        editing = true
    }

}

/**
 * Adapter for categories
 */
class ShoppingListAdapter :
    RecyclerView.Adapter<ShoppingListAdapter.CategoryViewHolder>() {
    private val round = SettingsManager.getSetting(SettingId.SHAPES_ROUND) as Boolean
    private val cr = MainActivity.act.resources.getDimension(R.dimen.cornerRadius)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_category, parent, false)
        return CategoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {

        if (position == ShoppingFr.shoppingListInstance.size) {
            val density = MainActivity.act.resources.displayMetrics.density
            holder.itemView.layoutParams.height = (100 * density).toInt()
            holder.itemView.visibility = View.INVISIBLE
            holder.itemView.subRecyclerView.visibility = View.GONE
            holder.itemView.setOnClickListener {}
            holder.itemView.setOnLongClickListener { true }
            return
        }
        holder.itemView.visibility = View.VISIBLE
        holder.itemView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

        //long click listener playing shake animation to indicate moving is possible
        holder.itemView.setOnLongClickListener {
            val animationShake =
                AnimationUtils.loadAnimation(MainActivity.act, R.anim.shake_small)
            holder.itemView.startAnimation(animationShake)
            true
        }

        //Get reference to currently used shopping list instance
        val shoppingListInstance = ShoppingFr.shoppingListInstance

        //get Tag for current category element
        val tag = shoppingListInstance[position].first

        //set tag for view holder
        holder.tag = tag

        //get Number of unchecked items
        val numberOfItems = shoppingListInstance.getUncheckedSize(tag)

        val expanded = shoppingListInstance.isTagExpanded(tag)

        //Expand or contract recyclerview depending on its expansion state
        holder.subRecyclerView.visibility = when (expanded) {
            true -> View.VISIBLE
            false -> View.GONE
        }

        holder.itemView.ivExpand.rotation = when (expanded) {
            true -> 180f
            else -> 0f
        }

        //Sets Text name of category of sublist
        holder.tvCategoryName.text =
            MainActivity.act.resources.getStringArray(R.array.categoryNames)[MainActivity.act.resources.getStringArray(
                R.array.categoryCodes
            ).indexOf(tag)]

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

        holder.subRecyclerView.setOnLongClickListener {
            val animationShake =
                AnimationUtils.loadAnimation(MainActivity.act, R.anim.shake_small)
            holder.itemView.startAnimation(animationShake)
            true
        }

        //Initialize and attach swipe helpers
        val swipeHelperLeft = ItemTouchHelper(SwipeItemToDelete(ItemTouchHelper.LEFT))
        swipeHelperLeft.attachToRecyclerView(holder.subRecyclerView)

        val swipeHelperRight = ItemTouchHelper(SwipeItemToDelete(ItemTouchHelper.RIGHT))
        swipeHelperRight.attachToRecyclerView(holder.subRecyclerView)

        //Onclick reaction to expand / contract this sublist
        holder.itemView.clTapExpand.setOnClickListener {
            val newState = shoppingListInstance.flipExpansionState(holder.tag)
            //if the item gets expanded and the setting says to only expand one
            if (newState == true && ShoppingFr.expandOne) {
                //iterate through all categories and contract one if you find one that's expanded and not the current sublist
                shoppingListInstance.forEach {
                    if (shoppingListInstance.isTagExpanded(it.first) && it.first != holder.tag) {
                        shoppingListInstance.flipExpansionState(it.first)
                        ShoppingFr.myAdapter.notifyItemChanged(
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

        //long click listener on clTapExpand to ensure shake animation for long click on whole category holder
        holder.itemView.clTapExpand.setOnLongClickListener {
            val animationShake =
                AnimationUtils.loadAnimation(MainActivity.act, R.anim.shake_small)
            holder.itemView.startAnimation(animationShake)
            true
        }
    }

    /**
     * manages background for category depending on its checkedState and category
     */
    fun manageCheckedCategory(
        holder: CategoryViewHolder, allChecked: Boolean,
        numberOfItems: Int, tag: String
    ) {
        if (!allChecked) {
            //get onBackGroundColor resolved
            val colorOnBackground =
                MainActivity.act.colorForAttr(R.attr.colorOnBackGround)


            val colorCategory =
                MainActivity.act.colorForAttr(R.attr.colorCategory)


            //get pair of color ids for right categories
            val gradientPair: Pair<Int, Int> = when (tag) {
                "So" -> Pair(R.attr.colorSonstiges, R.attr.colorSonstigesL)
                "Ob" -> Pair(R.attr.colorObstundGemüse, R.attr.colorObstundGemüseL)
                "Gt" -> Pair(R.attr.colorGetränke, R.attr.colorGetränkeL)
                "Nu" -> Pair(R.attr.colorNudelnundGetreide, R.attr.colorNudelnundGetreideL)
                "Bw" -> Pair(R.attr.colorBackwaren, R.attr.colorBackwarenL)
                "Km" -> Pair(R.attr.colorKühlregalMilch, R.attr.colorKühlregalMilchL)
                "Kf" -> Pair(R.attr.colorKühlregalFleisch, R.attr.colorKühlregalFleischL)
                "Tk" -> Pair(R.attr.colorTiefkühl, R.attr.colorTiefkühlL)
                "Ko" -> Pair(R.attr.colorKonservenFertiges, R.attr.colorKonservenFertigesL)
                "Fr" -> Pair(R.attr.colorFrühstück, R.attr.colorFrühstückL)
                "Gw" -> Pair(R.attr.colorGewürze, R.attr.colorGewürzeL)
                "Ha" -> Pair(R.attr.colorHaushalt, R.attr.colorHaushaltL)
                "Sn" -> Pair(R.attr.colorSnacks, R.attr.colorSnacksL)
                "Bz" -> Pair(R.attr.colorBackzutaten, R.attr.colorBackzutatenL)
                "Dr" -> Pair(R.attr.colorDrogerieKosmetik, R.attr.colorDrogerieKosmetikL)
                "Al" -> Pair(R.attr.colorAlkoholTabak, R.attr.colorAlkoholTabakL)
                else -> Pair(R.attr.colorBackgroundElevated, R.attr.colorBackgroundElevated)
            }

            //create gradient drawable as category background from color pair
            val myGradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    MainActivity.act.colorForAttr(gradientPair.second),
                    MainActivity.act.colorForAttr(gradientPair.first)
                )
            )

            //round corners if setting says so
            if (round) myGradientDrawable.cornerRadii = floatArrayOf(cr, cr, cr, cr, cr, cr, cr, cr)

            //set category background
            holder.cvCategory.background = myGradientDrawable

            //set text colors to white
            holder.tvCategoryName.setTextColor(colorCategory)
            holder.tvNumberOfItems.setTextColor(colorOnBackground)

            //display number of unchecked items
            holder.tvNumberOfItems.text = numberOfItems.toString()

            //hide checkMark
            holder.itemView.ivCheckMark.visibility = View.GONE
        } else {
            //get title color
            val colorTitle =
                MainActivity.act.colorForAttr(R.attr.colorCheckedCategoryTitle)


            //create gradient drawable for checked category background
            val myGradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    MainActivity.act.colorForAttr(R.attr.colorGrayL),
                    MainActivity.act.colorForAttr(R.attr.colorGray)
                )
            )

            //round corners if setting says so
            if (round) myGradientDrawable.cornerRadii = floatArrayOf(cr, cr, cr, cr, cr, cr, cr, cr)

            //set background for checked category
            holder.cvCategory.background = myGradientDrawable

            //hint text color for checked category
            holder.tvCategoryName.setTextColor(colorTitle)

            //clear text displaying number of items
            holder.tvNumberOfItems.text = ""

            //show checkMark
            holder.itemView.ivCheckMark.visibility = View.VISIBLE
        }
    }

    /**
     * Returns amount of categories + 1 (List buffer item)
     */
    override fun getItemCount(): Int {
        return ShoppingFr.shoppingListInstance.size + 1
    }

    /**
     * one instance of this class will contain one instance of row_category and meta data like
     * position also holds references to views inside the layout
     */
    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var tag: String
        val tvCategoryName: TextView = itemView.tvCategoryName
        var subRecyclerView: RecyclerView = itemView.subRecyclerView
        val cvCategory: CardView = itemView.cvCategory
        val tvNumberOfItems: TextView = itemView.tvNumberOfItems
    }
}

/**
 * Adapter for items in the sublists
 */
class SublistAdapter(
    private val tag: String, private val parentHolder: ShoppingListAdapter.CategoryViewHolder
) : RecyclerView.Adapter<SublistAdapter.ItemViewHolder>() {

    //boolean stating if design is round or not
    private val round = SettingsManager.getSetting(SettingId.SHAPES_ROUND) as Boolean

    //corner radius of items
    private val cr = MainActivity.act.resources.getDimension(R.dimen.cornerRadius)

    //setting if checked sublists should be moved below unchecked sublists
    private val moveCheckedSublistsDown =
        SettingsManager.getSetting(SettingId.MOVE_CHECKED_DOWN) as Boolean

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_item, parent, false)
        return ItemViewHolder(itemView, this)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        //longClickListener on item to ensure shake animation for category
        holder.itemView.setOnLongClickListener {
            val animationShake =
                AnimationUtils.loadAnimation(MainActivity.act, R.anim.shake_small)
            parentHolder.itemView.startAnimation(animationShake)
            true
        }


        //get shopping item
        val item = ShoppingFr.shoppingListInstance.getItem(tag, position)!!

        holder.itemView.setOnClickListener {
            ShoppingFr.editTag = tag
            ShoppingFr.editPos = position
            ShoppingFr.myFragment.openEditItemDialog(item)
        }

        //set tag of surrounding category for holder
        holder.tag = tag

        //initialize checkbox
        holder.itemView.cbItem.isChecked = item.checked

        //initialize text
        holder.itemView.tvItemTitle.text = MainActivity.act.getString(
            R.string.shoppingItemTitle, item.amount, item.unit, item.name
        )

        //background drawable for item
        val myGradientDrawable: GradientDrawable

        //initialize text / background color and strike through flag
        if (item.checked) {
            holder.itemView.tvItemTitle.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            holder.itemView.tvItemTitle
                .setTextColor(
                    MainActivity.act.colorForAttr(R.attr.colorHint)
                )

            myGradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    MainActivity.act.colorForAttr(R.attr.colorGrayD),
                    MainActivity.act.colorForAttr(R.attr.colorGrayD)
                )
            )

        } else {
            //white and no strike through otherwise
            holder.itemView.tvItemTitle.paintFlags = 0
            holder.itemView.tvItemTitle
                .setTextColor(
                    MainActivity.act.colorForAttr(R.attr.colorOnBackGround)
                )
            myGradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    MainActivity.act.colorForAttr(R.attr.colorBackground),
                    MainActivity.act.colorForAttr(R.attr.colorBackground)
                )
            )

        }

        //round corners if setting says so
        if (round) myGradientDrawable.cornerRadii = floatArrayOf(cr, cr, cr, cr, cr, cr, cr, cr)

        //set background of item
        holder.itemView.background = myGradientDrawable


        //Onclick Listener for checkBox
        holder.itemView.clItemTapfield.setOnClickListener {

            //flip checkedState of item and save new position (flipItemCheckedState sorts list and returns new position)
            val newPosition = ShoppingFr.shoppingListInstance.flipItemCheckedState(
                tag,
                holder.adapterPosition
            )

            //get number of uncheckedItems in current sublist
            val numberOfItems = ShoppingFr.shoppingListInstance.getUncheckedSize(holder.tag)

            //If all are checked after the current item got flipped, the list has to go from color to gray
            ShoppingFr.myAdapter.manageCheckedCategory(
                parentHolder,
                ShoppingFr.shoppingListInstance.areAllChecked(holder.tag),
                numberOfItems,
                holder.tag
            )

            //If setting says to collapse checked sublists, and current sublist is fully checked,
            //collapse it and notify item change
            if (ShoppingFr.collapseCheckedSublists && ShoppingFr.shoppingListInstance.areAllChecked(
                    holder.tag
                )
            ) {
                ShoppingFr.shoppingListInstance.flipExpansionState(holder.tag)
                ShoppingFr.myAdapter.notifyItemChanged(parentHolder.adapterPosition)
            }

            notifyItemChanged(holder.adapterPosition)


            if (newPosition > -1) {
                notifyItemMoved(holder.adapterPosition, newPosition)
            } else {
                MainActivity.act.toast("invalid item checked state")
            }

            //if the setting moveCheckedSublistsDown is true, sort categories by their checked state
            //and animate the move from old to new position
            if (moveCheckedSublistsDown) {
                val sublistMoveInfo = ShoppingFr.shoppingListInstance.sortCategoriesByChecked(tag)
                if (sublistMoveInfo != null) {
                    ShoppingFr.myFragment.prepareForMove()
                    ShoppingFr.myAdapter
                        .notifyItemMoved(sublistMoveInfo.first, sublistMoveInfo.second)

                    ShoppingFr.myFragment.reactToMove()
                }

            }
            ShoppingFr.myFragment.updateShoppingMenu()
        }

        holder.itemView.clItemTapfield.setOnLongClickListener {
            val animationShake =
                AnimationUtils.loadAnimation(MainActivity.act, R.anim.shake_small)
            parentHolder.itemView.startAnimation(animationShake)
            true
        }
    }

    override fun getItemCount() = ShoppingFr.shoppingListInstance.getSublistLength(tag)

    /**
    one instance of this class will contain one instance of row_item and meta data like position
    also holds references to views inside the layout
     */
    class ItemViewHolder(itemView: View, val adapter: SublistAdapter) :
        RecyclerView.ViewHolder(itemView) {

        lateinit var tag: String
    }
}

/**
 * ItemTouchHelper to support swipe to delete of shopping items
 */
class SwipeItemToDelete(direction: Int) : ItemTouchHelper.SimpleCallback(0, direction) {
    override fun onMove(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target:
        RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        //position of item in sublist
        val position = viewHolder.adapterPosition

        //ViewHolder as ItemViewHolder
        val parsed = viewHolder as SublistAdapter.ItemViewHolder

        //position of category in shoppingList
        val tagPosition = ShoppingFr.shoppingListInstance.getTagIndex(parsed.tag)

        //Pair of deleted item and boolean stating if sublist is empty now
        val removeInfo = ShoppingFr.shoppingListInstance.removeItem(parsed.tag, position)

        if (removeInfo.second) {
            //entire sublist is empty => remove sublist
            ShoppingFr.myAdapter
                .notifyItemRemoved(tagPosition)
        } else {
            //sublist changed length =>
            ShoppingFr.myAdapter.notifyItemChanged(tagPosition)

            //check if sublist moved
            val positions = ShoppingFr.shoppingListInstance.sortCategoriesByChecked(parsed.tag)

            if (positions != null) {
                //sublist did move => animate movement
                ShoppingFr.myFragment.prepareForMove()
                ShoppingFr.myAdapter.notifyItemMoved(
                    positions.first, positions.second
                )
                ShoppingFr.myFragment.reactToMove()
            }
        }

        //cache deleted item to allow undo
        ShoppingFr.deletedItem = removeInfo.first

        //update options menu
        ShoppingFr.myFragment.updateShoppingMenu()

    }
}
