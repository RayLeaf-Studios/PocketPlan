package com.pocket_plan.j7_003.data.shoppinglist

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import kotlinx.android.synthetic.main.dialog_add_item.view.*
import kotlinx.android.synthetic.main.dialog_add_shopping_list.*
import kotlinx.android.synthetic.main.dialog_add_shopping_list.view.*
import kotlinx.android.synthetic.main.dialog_add_task.view.*
import kotlinx.android.synthetic.main.fragment_multi_shopping.*
import kotlinx.android.synthetic.main.fragment_multi_shopping.view.*
import kotlinx.android.synthetic.main.title_dialog.view.*
import kotlinx.android.synthetic.main.toolbar.*

class MultiShoppingFr : Fragment() {

    private lateinit var myMenu: Menu
    private lateinit var myActivity: MainActivity

    private lateinit var addItemDialog: AlertDialog
    var addItemDialogView: View? = null
    lateinit var autoCompleteTv: AutoCompleteTextView

    var deletedItem: ShoppingItem? = null

    var editing: Boolean = false
    var editTag: String = ""
    var editPos: Int = 0

    lateinit var shoppingListWrapper: ShoppingListWrapper
    lateinit var shoppingFragments: ArrayList<ShoppingFr>
    private var currentpos = 0
    private lateinit var activeShoppingFr: ShoppingFr

    private lateinit var pagerAdapter: ScreenSlidePagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myActivity = activity as MainActivity

        //reset parameters when fragment is opened again
        shoppingFragments = ArrayList()
        currentpos = 0
        editTag = ""
        editPos = 0

        initializeShoppingFragments()

        // Inflate the layout for this fragment
        val myView = inflater.inflate(R.layout.fragment_multi_shopping, container, false)

        //setup pager and adapter
        val shoppingPager = myView.shoppingPager
        pagerAdapter = ScreenSlidePagerAdapter(myActivity)
        shoppingPager.adapter = pagerAdapter
        shoppingPager.isSaveEnabled = false

        //todo maybe choose which fragment should be displayed first
        val startPage = 0
        shoppingPager.setCurrentItem(startPage, false)
        myActivity.changeTitle(shoppingListWrapper[startPage].first)

        //create and register onPageChangeCallback on shoppingPager
        val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentpos = position
                activeShoppingFr = shoppingFragments[position]
                myActivity.changeTitle(shoppingListWrapper[position].first)
                deletedItem = null
                updateShoppingMenu()
            }
        }
        shoppingPager.registerOnPageChangeCallback(pageChangeCallback)

        return myView
    }

    //initialize all necessary fragments
    private fun initializeShoppingFragments() {
        shoppingListWrapper.forEach {
            val newFr = ShoppingFr.newInstance()
            newFr.shoppingListInstance = it.second
            newFr.myMultiShoppingFr = this
            shoppingFragments.add(newFr)
        }
        activeShoppingFr = shoppingFragments[0]
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_shopping, menu)
        myMenu = menu
        myMenu.findItem(R.id.item_shopping_undo)?.icon?.setTint(myActivity.colorForAttr(R.attr.colorOnBackGround))
        updateShoppingMenu()
        super.onCreateOptionsMenu(menu, inflater)
    }

    fun dialogRenameCurrentList() {
        //inflate the dialog with custom view
        val myDialogView =
            LayoutInflater.from(myActivity).inflate(R.layout.dialog_add_shopping_list, null)

        //AlertDialogBuilder
        val myBuilder =
            myActivity.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }

        val customTitle = layoutInflater.inflate(R.layout.title_dialog, null)
        customTitle.tvDialogTitle.text = getString(R.string.shopping_dialog_rename_title)
        myBuilder.setCustomTitle(customTitle)

        //show dialog
        val myAlertDialog = myBuilder?.create()
        myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        myAlertDialog?.show()

        //show current name
        val oldName = shoppingListWrapper[currentpos].first
        myDialogView.etAddShoppingList.setText(oldName)

        myDialogView.btnAddShoppingList.setOnClickListener {

            val newName = myDialogView.etAddShoppingList.text.toString()
            if(newName.trim()==""){
                val animationShake =
                    AnimationUtils.loadAnimation(myActivity, R.anim.shake)
                myDialogView.etAddShoppingList.startAnimation(animationShake)
                return@setOnClickListener
            }

            shoppingListWrapper.rename(oldName, newName)
            myActivity.myNewToolbar.title = newName
            myAlertDialog?.dismiss()
        }

        val cancelBtn = myDialogView.btnCancelShoppingList
        cancelBtn.setOnClickListener { myAlertDialog?.dismiss() }

        myDialogView.etAddShoppingList.requestFocus()
    }

    private fun dialogAddShoppingList() {
        //inflate the dialog with custom view
        val myDialogView =
            LayoutInflater.from(myActivity).inflate(R.layout.dialog_add_shopping_list, null)

        //AlertDialogBuilder
        val myBuilder =
            myActivity.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
        val customTitle = myActivity.layoutInflater.inflate(R.layout.title_dialog, null)
        customTitle.tvDialogTitle.text = myActivity.getString(R.string.shopping_option_add_list)
        myBuilder?.setCustomTitle(customTitle)

        //show dialog
        val myAlertDialog = myBuilder?.create()
        myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        myAlertDialog?.show()

        myDialogView.btnAddShoppingList.setOnClickListener {
            val newName = myDialogView.etAddShoppingList.text.toString()

            if (newName.trim() == "") {
                val animationShake =
                    AnimationUtils.loadAnimation(myActivity, R.anim.shake)
                myDialogView!!.etAddShoppingList.startAnimation(animationShake)
                return@setOnClickListener
            }
            shoppingListWrapper.add(newName)

            val newFr = ShoppingFr.newInstance()
            newFr.shoppingListInstance = shoppingListWrapper.getListByName(newName)!!
            newFr.myMultiShoppingFr = this

            shoppingFragments.add(newFr)

            shoppingPager.adapter = ScreenSlidePagerAdapter(myActivity)
            shoppingPager.currentItem = shoppingListWrapper.size - 1
            myAlertDialog?.dismiss()
        }

        val cancelBtn = myDialogView.btnCancelShoppingList
        cancelBtn.setOnClickListener { myAlertDialog?.dismiss() }

        myDialogView.etAddShoppingList.requestFocus()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //decides if options menu will be refreshed immediately after option is selected
        var menuRefresh = true

        when (item.itemId) {
            R.id.item_shopping_delete_list -> {
                val titleId = R.string.shopping_dialog_delete_title
                val action: () -> Unit = {
                    if (shoppingListWrapper.remove(myActivity.myNewToolbar.title.toString())) {
                        //removing was successful, remove fragment from fragment list, update adapter
                        shoppingFragments.remove(activeShoppingFr)
                        shoppingPager.adapter = ScreenSlidePagerAdapter(myActivity)
                    } else {
                        val toastText = getString(R.string.shoppingToastZeroListWarning)
                        Toast.makeText(myActivity, toastText, Toast.LENGTH_LONG).show()
                    }
                }
                myActivity.dialogConfirmDelete(titleId, action)
            }

            R.id.item_shopping_add_list -> {
                dialogAddShoppingList()
            }

            R.id.item_shopping_clear_list -> {
                //menu refresh is handled in dialog action
                menuRefresh = false
                dialogShoppingClear()
            }

            R.id.item_shopping_uncheck_all -> {
                //uncheck all shopping items
                activeShoppingFr.shoppingListInstance.uncheckAll()
                activeShoppingFr.myAdapter.notifyDataSetChanged()
            }

            R.id.item_shopping_undo -> {
                //undo the last deletion of a shopping item
                activeShoppingFr.shoppingListInstance.add(deletedItem!!)
                deletedItem = null
                activeShoppingFr.myAdapter.notifyDataSetChanged()
            }

            R.id.item_shopping_collapse_all -> {
                //collapse all categories
                activeShoppingFr.shoppingListInstance.collapseAllTags()
                activeShoppingFr.myAdapter.notifyItemRangeChanged(
                    0,
                    activeShoppingFr.shoppingListInstance.size
                )
            }

            R.id.item_shopping_expand_all -> {
                //expand all categories
                activeShoppingFr.shoppingListInstance.expandAllTags()
                activeShoppingFr.myAdapter.notifyItemRangeChanged(
                    0,
                    activeShoppingFr.shoppingListInstance.size
                )
            }
        }

        if (menuRefresh) updateShoppingMenu()

        return super.onOptionsItemSelected(item)
    }


    /**
     * Prepare layout and adapters for addItemDialog to decrease loading time
     */
    @SuppressLint("InflateParams")
    fun preloadAddItemDialog(passedActivity: MainActivity, mylayoutInflater: LayoutInflater) {
        myActivity = passedActivity

        //initialize shopping list data
        myActivity.itemTemplateList = ItemTemplateList()
        myActivity.userItemTemplateList = UserItemTemplateList()

        //initialize itemNameList
        MainActivity.itemNameList = ArrayList()

        //add userItemNames to itemNameList
        myActivity.userItemTemplateList.forEach {
            MainActivity.itemNameList.add(it.n)
        }

        //add all regular items to itemNameList
        myActivity.itemTemplateList.forEach {
            if (!MainActivity.itemNameList.contains(it.n)) {
                MainActivity.itemNameList.add(it.n)
            }
        }

        //inflate view for this dialog
        addItemDialogView =
            mylayoutInflater.inflate(R.layout.dialog_add_item, null)

        //Initialize dialogBuilder and set its title
        val myBuilder = myActivity.let { it1 ->
            AlertDialog.Builder(it1).setView(addItemDialogView)
        }

        val customTitle = mylayoutInflater.inflate(R.layout.title_dialog, null)
        customTitle.tvDialogTitle.text = myActivity.getString(R.string.shoppingAddItemTitle)
        myBuilder?.setCustomTitle(customTitle)

        myBuilder.setCancelable(true)
        addItemDialog = myBuilder?.create()!!
        addItemDialog.setCancelable(true)

        myActivity.shoppingTitle = customTitle

        //initialize spinner for categories + adapter
        val spCategory = addItemDialogView!!.spCategory
        val categoryAdapter = ArrayAdapter(
            myActivity,
            android.R.layout.simple_list_item_1,
            myActivity.resources.getStringArray(R.array.categoryNames)
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategory.adapter = categoryAdapter

        //initialize spinner for units + its adapter and listener
        val spItemUnit = addItemDialogView!!.spItemUnit
        val unitAdapter = ArrayAdapter(
            myActivity, android.R.layout.simple_list_item_1,
            myActivity.resources.getStringArray(R.array.units)
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
                if (spItemUnit.tag != position && position != 0) {
                    MainActivity.unitChanged = true
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                /* no-op */
            }

        }


        //initialize autocompleteTextView
        autoCompleteTv = addItemDialogView!!.actvItem

        //initialize custom arrayAdapter for autocompletion
        val itemNameClone = MainActivity.itemNameList.toMutableList()
        val customAdapter = AutoCompleteAdapter(
            context = myActivity,
            resource = android.R.layout.simple_spinner_dropdown_item,
            items = itemNameClone
        )
        autoCompleteTv.setAdapter(customAdapter)

        //request focus in item name text field
        autoCompleteTv.requestFocus()

        //initialize text watcher to trigger updating of category and unit
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //read user input into item text field
                var input = autoCompleteTv.text.toString()

                //remove leading and trailing white spaces of user input, to recognize items even when accidental whitespaces are added
                input = input.trim()

                //check for existing user template
                var template =
                    myActivity.userItemTemplateList.getTemplateByName(input)

                //if there is none, check for existing regular template
                if (template == null) {
                    template = myActivity.itemTemplateList.getTemplateByName(input)
                }

                //if template now is not null, select correct unit and category
                if (template != null) {
                    //display correct category
                    spCategory.setSelection(
                        myActivity.resources.getStringArray(R.array.categoryCodes)
                            .indexOf(template.c)
                    )

                    //display correct unit
                    val unitPointPos =
                        myActivity.resources.getStringArray(R.array.units).indexOf(template.s)
                    if (!MainActivity.unitChanged) {
                        spItemUnit.tag = unitPointPos
                        spItemUnit.setSelection(unitPointPos)
                    }
                } else {
                    //else if entered string is unknown select "other" and "x" as defaults
                    spCategory.setSelection(0)
                    if (!MainActivity.unitChanged) {
                        spItemUnit.tag = 0
                        spItemUnit.setSelection(0)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        autoCompleteTv.addTextChangedListener(textWatcher)

        //initialize edit text for item amount string
        val etItemAmount = addItemDialogView!!.etItemAmount
        etItemAmount.setText("1")

        etItemAmount.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etItemAmount.setText("")
            }
        }

        //initialize onclick listener for "cancel" button, which closes the add item dialog
        addItemDialogView!!.btnCancelItem.setOnClickListener {
            addItemDialog?.dismiss()
        }

        //initialize key listener to add item via enter-press by triggering a click on the add button
        autoCompleteTv.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                addItemDialogView!!.btnAddItemToList.performClick()
                true
            } else false
        }

        //initialize checkMark sprite, which plays an animation when an item is added
        val checkMark = addItemDialogView!!.ivCheckItemAdded
        checkMark.visibility = View.GONE

        //listener for button to confirm adding item to list
        addItemDialogView!!.btnAddItemToList.setOnClickListener {
            MainActivity.unitChanged = false

            val nameInput = autoCompleteTv.text.toString()

            //No item string entered => play shake animation
            if (nameInput == "") {
                val animationShake =
                    AnimationUtils.loadAnimation(myActivity, R.anim.shake)
                addItemDialogView!!.actvItem.startAnimation(animationShake)
                return@setOnClickListener
            }

            //checkMark animation to confirm adding of item
            checkMark.visibility = View.VISIBLE
            checkMark.animate().translationYBy(-80f).alpha(0f).setDuration(600L).withEndAction {
                checkMark.animate().translationY(0f).alpha(1f).setDuration(0).start()
                checkMark.visibility = View.GONE
            }.start()

            //get selected categoryCode
            val categoryCode =
                myActivity.resources.getStringArray(R.array.categoryCodes)[myActivity.resources.getStringArray(
                    R.array.categoryNames
                ).indexOf(spCategory.selectedItem as String)]

            //check if user template exists for this string
            var template =
                myActivity.userItemTemplateList.getTemplateByName(nameInput)

            //TODO clean up logic here
            if (template == null) {
                //no user item with this name => check for regular template
                template = myActivity.itemTemplateList.getTemplateByName(nameInput)
                if (template == null || categoryCode != template!!.c || spItemUnit.selectedItemPosition != 0) {
                    //item unknown, or item known under different category or with different unit, use selected category and unit,
                    // add item new ItemTemplate to userItemTemplate list, using entered values
                    myActivity.userItemTemplateList.add(
                        ItemTemplate(
                            nameInput, categoryCode,
                            spItemUnit.selectedItem.toString()
                        )
                    )

                    //create new Shopping item using entered values
                    val item = ShoppingItem(
                        nameInput, categoryCode,
                        spItemUnit.selectedItem.toString(),
                        etItemAmount.text.toString(),
                        spItemUnit.selectedItem.toString(),
                        false
                    )

                    //if currently editing, remove the item that was tapped to edit
                    if (editing) {
                        activeShoppingFr.shoppingListInstance.removeItem(editTag, editPos)
                        editing = false
                        addItemDialog?.dismiss()
                    }

                    //add new item to list
                    activeShoppingFr.shoppingListInstance.add(item)

                    //trigger adapter and menu refresh if currently in shoppingFr
                    if (MainActivity.previousFragmentStack.peek() == FT.SHOPPING) {
                        activeShoppingFr.myAdapter.notifyDataSetChanged()
                        updateShoppingMenu()
                    } else {
                        //display "Item added" Toast, when adding from home
                        Toast.makeText(
                            myActivity,
                            myActivity.getString(R.string.shopping_item_added),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                    //if itemNameList does not contain this item name, add it to the list and create
                    //and set a new adapter for autocompleteTv
                    if (!MainActivity.itemNameList.contains(nameInput)) {
                        MainActivity.itemNameList.add(nameInput)
                        val itemClone2 = MainActivity.itemNameList.toMutableList()
                        val newCustomAdapter = AutoCompleteAdapter(
                            context = myActivity,
                            resource = android.R.layout.simple_spinner_dropdown_item,
                            items = itemClone2
                        )
                        autoCompleteTv.setAdapter(newCustomAdapter)

                    }

                    //restore dialog to normal after adding
                    autoCompleteTv.setText("")
                    etItemAmount.setText("1")
                    spItemUnit.tag = 0
                    spItemUnit.setSelection(0)
                    autoCompleteTv.requestFocus()

                    //close dialog if setting says so, or dialog was opened from home fragment
                    if (MainActivity.previousFragmentStack.peek() == FT.HOME || SettingsManager.getSetting(
                            SettingId.CLOSE_ITEM_DIALOG
                        ) as Boolean
                    ) {
                        addItemDialog?.dismiss()
                    }
                    return@setOnClickListener
                }
            }

            if (categoryCode != template!!.c || spItemUnit.selectedItem.toString() != template!!.s) {
                //known as user item but with different tag or different suggested unit
                myActivity.userItemTemplateList.removeItem(autoCompleteTv.text.toString())

                //check if there is a regularItem with this name
                val regularTemplate =
                    myActivity.itemTemplateList.getTemplateByName(autoCompleteTv.text.toString())

                //only add a new user item if there is no regular item with this name, this category and this unit
                if (!(regularTemplate != null && regularTemplate.c == categoryCode && regularTemplate.s == spItemUnit.selectedItem.toString())) {
                    myActivity.userItemTemplateList.add(
                        ItemTemplate(
                            autoCompleteTv.text.toString(), categoryCode,
                            spItemUnit.selectedItem.toString()
                        )
                    )
                }
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

            if (editing) {
                //remove item that was tapped to edit, if editing
                activeShoppingFr.shoppingListInstance.removeItem(editTag, editPos)
                editing = false
                addItemDialog?.dismiss()
            }
            //add new item to list
            activeShoppingFr.shoppingListInstance.add(item)
            if (MainActivity.previousFragmentStack.peek() == FT.SHOPPING) {
                activeShoppingFr.myAdapter.notifyDataSetChanged()
                updateShoppingMenu()
            } else {
                Toast.makeText(
                    myActivity,
                    myActivity.getString(R.string.shopping_item_added),
                    Toast.LENGTH_SHORT
                ).show()
            }
            autoCompleteTv.setText("")
            etItemAmount.setText("1")
            spItemUnit.tag = 0
            spItemUnit.setSelection(0)
            autoCompleteTv.requestFocus()
            if (MainActivity.previousFragmentStack.peek() == FT.HOME) {
                addItemDialog?.dismiss()
            }
        }

        val imm =
            myActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, InputMethodManager.SHOW_FORCED)
    }

    /**
     * Reset and open addItemDialog
     */
    fun openAddItemDialog() {
        //set dialog title to "add item"
        myActivity.shoppingTitle!!.tvDialogTitle.text =
            myActivity.getString(R.string.shoppingAddItemTitle)

        //Clear item autoCompleteTextView
        addItemDialogView!!.actvItem.setText("")

        //Request focus in item autoCompleteTextView
        addItemDialogView!!.actvItem.requestFocus()

        //set confirm button text to "add"
        addItemDialogView!!.btnAddItemToList.text =
            myActivity.getString(R.string.birthdayDialogAdd)

        addItemDialogView!!.spItemUnit.tag = 0
        addItemDialogView!!.spItemUnit.setSelection(0)

        //set default amount text to 1
        addItemDialogView!!.etItemAmount.setText("1")

        //open keyboard
        addItemDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        addItemDialog?.show()
    }

    fun openEditItemDialog(item: ShoppingItem) {
        //set dialog title to "editing"
        myActivity.shoppingTitle!!.tvDialogTitle.text =
            myActivity.getString(R.string.shoppingEditItemTitle)

        //set confirm Button text to "save"
        addItemDialogView!!.btnAddItemToList.text =
            resources.getString(R.string.noteDiscardDialogSave)

        //show item name
        addItemDialogView!!.actvItem.setText(item.name)

        //request focus in item autoCompleteTextView
        addItemDialogView!!.actvItem.requestFocus()

        //set cursor to end of item name
        addItemDialogView!!.actvItem.setSelection(item.name!!.length)

        //select correct unit
        val unitIndex = myActivity.resources.getStringArray(
            R.array.units
        ).indexOf(item.suggestedUnit)
        addItemDialogView!!.spItemUnit.tag = unitIndex
        addItemDialogView!!.spItemUnit.setSelection(unitIndex)

        MainActivity.unitChanged = false

        //show correct item amount
        addItemDialogView!!.etItemAmount.setText(item.amount.toString())

        //open keyboard
        addItemDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        addItemDialogView!!.actvItem.dismissDropDown()

        //show dialog
        addItemDialog?.show()

        editing = true
    }

    @SuppressLint("InflateParams")
    private fun dialogShoppingClear() {
        val titleId = R.string.shopping_dialog_clear_title
        val action: () -> Unit = {
            activeShoppingFr.shoppingListInstance.clear()
            activeShoppingFr.shoppingListInstance.save()
            activeShoppingFr.myAdapter.notifyDataSetChanged()
            deletedItem = null
            updateShoppingMenu()
        }
        myActivity.dialogConfirmDelete(titleId, action)
    }

    fun updateExpandAllIcon() {
        myMenu.findItem(R.id.item_shopping_expand_all)?.isVisible =
            activeShoppingFr.shoppingListInstance.somethingsCollapsed() && !(SettingsManager.getSetting(
                SettingId.EXPAND_ONE_CATEGORY
            ) as Boolean)
    }

    fun updateCollapseAllIcon() {
        myMenu.findItem(R.id.item_shopping_collapse_all)?.isVisible =
            activeShoppingFr.shoppingListInstance.somethingIsExpanded()
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


    private fun updateDeleteShoppingListIcon() {
        myMenu.findItem(R.id.item_shopping_clear_list)?.isVisible =
            activeShoppingFr.shoppingListInstance.size > 0
    }

    private fun updateUncheckShoppingListIcon() {
        myMenu.findItem(R.id.item_shopping_uncheck_all)?.isVisible =
            activeShoppingFr.shoppingListInstance.somethingIsChecked()

    }


    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = shoppingListWrapper.size

        override fun createFragment(position: Int): Fragment {
            return this@MultiShoppingFr.shoppingFragments[position]
        }

    }
}