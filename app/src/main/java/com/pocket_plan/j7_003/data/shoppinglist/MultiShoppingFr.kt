package com.pocket_plan.j7_003.data.shoppinglist

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.databinding.DialogAddItemBinding
import com.pocket_plan.j7_003.databinding.DialogAddShoppingListBinding
import com.pocket_plan.j7_003.databinding.FragmentMultiShoppingBinding
import com.pocket_plan.j7_003.databinding.TitleDialogBinding
import java.util.Collections
import kotlin.math.abs
import kotlin.math.min

class MultiShoppingFr : Fragment() {

    private lateinit var myMenu: Menu
    private lateinit var myActivity: MainActivity


    private var unitChanged: Boolean = false

    private lateinit var addItemDialog: AlertDialog
    private lateinit var dialogAddItemBinding: DialogAddItemBinding
    lateinit var autoCompleteTv: AutoCompleteTextView

    var editing: Boolean = false
    var editTag: String = ""
    var editPos: Int = 0

    var deletedItems = ArrayList<ArrayDeque<ShoppingItem?>>()
    var activeDeletedItems = ArrayDeque<ShoppingItem?>()

    lateinit var shoppingFragments: ArrayList<ShoppingFr>
    private var currentpos = 0
    private lateinit var activeShoppingFr: ShoppingFr

    private lateinit var pagerAdapter: ScreenSlidePagerAdapter
    private lateinit var tabLayout: TabLayout

    //boolean to signal if a search is currently being performed
    var searching: Boolean = false

    //reference to searchView in toolbar
    private lateinit var searchView: SearchView
    private var searchList = ArrayList<Pair<String, ArrayList<ShoppingItem>>>()

    lateinit var shoppingPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myActivity = activity as MainActivity

        //reset parameters when fragment is opened again
        shoppingFragments = ArrayList()
        currentpos = 0
        editTag = ""
        editPos = 0

        initializeShoppingFragments()

        // Inflate the layout for this fragment
        val fragmentMultiShoppingBinding = FragmentMultiShoppingBinding.inflate(layoutInflater)

        //setup pager and adapter
        shoppingPager = fragmentMultiShoppingBinding.shoppingPager
        pagerAdapter = ScreenSlidePagerAdapter(myActivity)
        shoppingPager.adapter = pagerAdapter
        shoppingPager.isSaveEnabled = false

        val startPage = 0
        shoppingPager.setCurrentItem(startPage, false)

        //create and register onPageChangeCallback on shoppingPager

        tabLayout = fragmentMultiShoppingBinding.tabLayout
        val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    shoppingPager.currentItem = tab.position
                    currentpos = tab.position
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        }
        tabLayout.addOnTabSelectedListener(onTabSelectedListener)
        updateTabs()
        return fragmentMultiShoppingBinding.root
    }

    private fun updateTabs() {
        if (MainActivity.shoppingListWrapper.size == 1) {
            tabLayout.visibility = View.GONE
        } else {
            tabLayout.visibility = View.VISIBLE
        }
        tabLayout.removeAllTabs()
        MainActivity.shoppingListWrapper.forEach {
            tabLayout.addTab(tabLayout.newTab().setText(it.first))
        }
    }

    //initialize all necessary fragments
    private fun initializeShoppingFragments() {
        val isEmpty = deletedItems.isEmpty()
        MainActivity.shoppingListWrapper.forEach {
            val newFr = createShoppingFrInstance(listName = it.first, shoppingList = it.second)
            shoppingFragments.add(newFr)

            if (isEmpty)
                deletedItems.add(ArrayDeque())
        }
        activeShoppingFr = shoppingFragments[0]
    }

    private fun createShoppingFrInstance(listName: String, shoppingList: ShoppingList): ShoppingFr {
        val newFr = ShoppingFr.newInstance()
        newFr.shoppingListInstance = shoppingList
        newFr.shoppingListName = listName
        newFr.myMultiShoppingFr = this
        return newFr
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        myMenu = menu
        inflater.inflate(R.menu.menu_shopping, menu)
        myMenu.findItem(R.id.item_shopping_undo)?.icon?.setTint(myActivity.colorForAttr(R.attr.colorOnBackGround))
        updateShoppingMenu()

        //set reference to searchView from menu
        searchView = menu.findItem(R.id.item_shopping_search).actionView as SearchView

        //create textListener, to listen to keyboard input when a birthday search is performed
        val textListener = object : SearchView.OnQueryTextListener {

            //hide keyboard when search is submitted
            override fun onQueryTextSubmit(query: String?): Boolean {
                myActivity.hideKeyboard()
                return true
            }

            //start a new search whenever input text has changed
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText == null)
                    return true
                if (searching) {
                    activeShoppingFr.search(newText.toString())
                }
                return true
            }
        }

        //apply textListener to SearchView
        searchView.setOnQueryTextListener(textListener)


        searchView.setOnCloseListener {
            myActivity.myBtnAdd.visibility = View.VISIBLE
            //reset title
            myActivity.toolbar.title = getString(R.string.menuTitleShopping)
            //collapse searchView
            searchView.onActionViewCollapsed()
            //signal that no search is being performed
            searching = false
            updateShoppingMenu()
            //reload menu icons
            //reload list elements by notifying data set change to adapter
            activeShoppingFr.query = null
            activeShoppingFr.myAdapter.notifyDataSetChanged()
            true
        }

        searchView.setOnSearchClickListener {
            myActivity.myBtnAdd.visibility = View.GONE
            //removes title from toolbar
            myActivity.toolbar.title = ""
            //sets searching to true, which results in the recyclerViewAdapter reading its elements from
            searching = true
            updateShoppingMenuForSearch()

            //clear adjusted list
            searchList.clear()
            //reload adapter dataSet
            activeShoppingFr.myAdapter.notifyDataSetChanged()
        }

        val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                deletedItems[currentpos] = activeDeletedItems
                currentpos = position
                activeShoppingFr = shoppingFragments[position]
                activeShoppingFr.query = null
                activeDeletedItems = deletedItems[position]
                myActivity.myBtnAdd.visibility = View.VISIBLE
                updateShoppingMenu()
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }
        }
        shoppingPager.registerOnPageChangeCallback(pageChangeCallback)

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun dialogRenameCurrentList() {
        //inflate the dialog with custom view
        val dialogAddShoppingListBinding = DialogAddShoppingListBinding.inflate(layoutInflater)

        //AlertDialogBuilder
        val myBuilder =
            myActivity.let { it1 ->
                AlertDialog.Builder(it1).setView(dialogAddShoppingListBinding.root)
            }

        val titleDialogBinding = TitleDialogBinding.inflate(layoutInflater)
        titleDialogBinding.tvDialogTitle.text = getString(R.string.shoppingDialogRenameList)
        myBuilder.setCustomTitle(titleDialogBinding.root)

        //show dialog
        val myAlertDialog = myBuilder?.create()
        myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        myAlertDialog?.show()

        //show current name
        val oldName = MainActivity.shoppingListWrapper[currentpos].first
        dialogAddShoppingListBinding.etAddShoppingList.setText(oldName)

        dialogAddShoppingListBinding.btnAddShoppingList.setOnClickListener {

            val newName = dialogAddShoppingListBinding.etAddShoppingList.text.toString()
            val taken = MainActivity.shoppingListWrapper.contains(newName)
            if (newName.trim() == "" || taken) {
                val animationShake =
                    AnimationUtils.loadAnimation(myActivity, R.anim.shake)
                dialogAddShoppingListBinding.etAddShoppingList.startAnimation(animationShake)
                return@setOnClickListener
            }
            MainActivity.shoppingListWrapper.rename(oldName, newName)
            activeShoppingFr.shoppingListName = newName
            tabLayout.getTabAt(currentpos)?.text = newName
            myAlertDialog?.dismiss()
        }

        val cancelBtn = dialogAddShoppingListBinding.btnCancelShoppingList
        cancelBtn.setOnClickListener { myAlertDialog?.dismiss() }

        dialogAddShoppingListBinding.etAddShoppingList.requestFocus()
    }

    private fun dialogAddShoppingList() {
        //inflate the dialog with custom view
        val dialogAddShoppingListBinding = DialogAddShoppingListBinding.inflate(layoutInflater)

        //AlertDialogBuilder
        val myBuilder =
            myActivity.let { it1 ->
                AlertDialog.Builder(it1).setView(dialogAddShoppingListBinding.root)
            }
        val titleDialogBinding = TitleDialogBinding.inflate(layoutInflater)
        titleDialogBinding.tvDialogTitle.text = myActivity.getString(R.string.shoppingOptionAddList)
        myBuilder?.setCustomTitle(titleDialogBinding.root)

        //show dialog
        val myAlertDialog = myBuilder?.create()
        myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        myAlertDialog?.show()

        dialogAddShoppingListBinding.btnAddShoppingList.setOnClickListener {
            val newName = dialogAddShoppingListBinding.etAddShoppingList.text.toString()
            val addResult = MainActivity.shoppingListWrapper.add(newName)
            if (newName.trim() == "" || !addResult) {
                val animationShake =
                    AnimationUtils.loadAnimation(myActivity, R.anim.shake)
                dialogAddShoppingListBinding.etAddShoppingList.startAnimation(animationShake)
                return@setOnClickListener
            }

            val newFr = createShoppingFrInstance(
                newName,
                MainActivity.shoppingListWrapper.getListByName(newName)!!
            )
            shoppingFragments.add(newFr)

            deletedItems.add(ArrayDeque())

            tabLayout.addTab(tabLayout.newTab().setText(newName))
            tabLayout.visibility = View.VISIBLE

            shoppingPager.adapter = ScreenSlidePagerAdapter(myActivity)
            shoppingPager.currentItem = MainActivity.shoppingListWrapper.size - 1
            myAlertDialog?.dismiss()
        }

        val cancelBtn = dialogAddShoppingListBinding.btnCancelShoppingList
        cancelBtn.setOnClickListener { myAlertDialog?.dismiss() }

        dialogAddShoppingListBinding.etAddShoppingList.requestFocus()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //decides if options menu will be refreshed immediately after option is selected
        var menuRefresh = true

        when (item.itemId) {
            R.id.item_shopping_delete_list -> {
                val titleId = R.string.shoppingDialogDeleteTitle
                val action: () -> Unit = {
                    MainActivity.shoppingListWrapper.remove(activeShoppingFr.shoppingListName)
                    shoppingFragments.remove(activeShoppingFr)
                    shoppingPager.adapter = ScreenSlidePagerAdapter(myActivity)
                    //This automatically selects the tab left of the deleted tab
                    tabLayout.removeTabAt(currentpos)
                    if (MainActivity.shoppingListWrapper.size == 1) {
                        tabLayout.visibility = View.GONE
                    }
                }
                myActivity.dialogConfirm(titleId, action)
            }

            R.id.item_shopping_add_list -> {
                dialogAddShoppingList()
            }

            R.id.item_shopping_rename_list -> {
                dialogRenameCurrentList()
            }

            R.id.item_shopping_clear_list -> {
                //menu refresh is handled in dialog action
                menuRefresh = false
                dialogShoppingClear()
            }

            R.id.item_shopping_remove_checked -> {
                dialogRemoveCheckedItems()
            }

            R.id.item_shopping_remove_duplicates -> {
                dialogRemoveDuplicateItems()
            }

            R.id.item_shopping_uncheck_all -> {
                //uncheck all shopping items
                activeShoppingFr.shoppingListInstance.uncheckAll()
                activeShoppingFr.myAdapter.notifyDataSetChanged()
            }

            R.id.item_shopping_undo -> {
                //undo the last deletion of a shopping item
                activeShoppingFr.shoppingListInstance.add(activeDeletedItems.last()!!)
                activeDeletedItems.removeLast()
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

    fun refreshItemNamesAndAutoCompleteAdapter() {
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

        //initialize custom arrayAdapter for autocompletion
        val itemNameClone = MainActivity.itemNameList.toMutableList()
        val customAdapter = AutoCompleteAdapter(
            context = myActivity,
            resource = android.R.layout.simple_spinner_dropdown_item,
            items = itemNameClone
        )
        autoCompleteTv.setAdapter(customAdapter)
    }


    /**
     * Prepare layout and adapters for addItemDialog to decrease loading time
     */
    @SuppressLint("InflateParams")
    fun preloadAddItemDialog(passedActivity: MainActivity, layoutInflater: LayoutInflater) {
        myActivity = passedActivity

        //initialize shopping list data
        myActivity.itemTemplateList = ItemTemplateList()
        myActivity.userItemTemplateList = UserItemTemplateList()


        //inflate view for this dialog
        dialogAddItemBinding = DialogAddItemBinding.inflate(layoutInflater)

        //Initialize dialogBuilder and set its title
        val myBuilder =
            myActivity.let { AlertDialog.Builder(it).setView(dialogAddItemBinding.root) }

        val titleDialogBinding = TitleDialogBinding.inflate(layoutInflater)
        titleDialogBinding.tvDialogTitle.text = myActivity.getString(R.string.shoppingAddItemTitle)
        myBuilder?.setCustomTitle(titleDialogBinding.root)

        myBuilder.setCancelable(true)
        addItemDialog = myBuilder?.create()!!
        addItemDialog.setCancelable(true)

        myActivity.titleDialogBinding = titleDialogBinding

        //initialize spinner for categories + adapter
        val spCategory = dialogAddItemBinding.spCategory
        val categoryAdapter = ArrayAdapter(
            myActivity,
            android.R.layout.simple_list_item_1,
            myActivity.resources.getStringArray(R.array.categoryNames)
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategory.adapter = categoryAdapter

        var lastSelectedCategoryIndex = 0

        spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != spCategory.tag) {
                    lastSelectedCategoryIndex = position
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        //initialize spinner for units + its adapter and listener
        val spItemUnit = dialogAddItemBinding.spItemUnit
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
                    unitChanged = true
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                /* no-op */
            }

        }


        //initialize autocompleteTextView
        autoCompleteTv = dialogAddItemBinding.actvItem
        refreshItemNamesAndAutoCompleteAdapter()


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
                //Additionally, ignore optional details when recommending a category
                input = input.trim().split("(")[0].trim()

                //check for existing user template
                var template =
                    myActivity.userItemTemplateList.getTemplateByName(input)

                //if there is none, check for existing regular template
                if (template == null) {
                    template = myActivity.itemTemplateList.getTemplateByName(input)
                }

                //Select correct unit and category, default (unknown input) unit x and last used category
                var unitToSet = 0
                var categoryToSet = lastSelectedCategoryIndex

                //Unit and category from template if there is any
                if (template != null) {
                    unitToSet =
                        myActivity.resources.getStringArray(R.array.units).indexOf(template.s)
                    categoryToSet = myActivity.resources.getStringArray(R.array.categoryCodes)
                        .indexOf(template.c)
                }

                //Apply selections
                spCategory.tag = categoryToSet
                spCategory.setSelection(categoryToSet)

                //select unit depending on text change (user or regular template)
                if (!unitChanged) {
                    spItemUnit.tag = unitToSet
                    spItemUnit.setSelection(unitToSet)
                }

            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        autoCompleteTv.addTextChangedListener(textWatcher)

        //initialize edit text for item amount string
        val etItemAmount = dialogAddItemBinding.etItemAmount
        etItemAmount.setText("1")

        etItemAmount.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etItemAmount.setText("")
            }
        }

        //initialize onclick listener for "cancel" button, which closes the add item dialog
        dialogAddItemBinding.btnCancelItem.setOnClickListener {
            addItemDialog.dismiss()
        }

        //initialize key listener to add item via enter-press by triggering a click on the add button
        autoCompleteTv.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                dialogAddItemBinding.btnAddItemToList.performClick()
                true
            } else false
        }

        //initialize checkMark sprite, which plays an animation when an item is added
        val checkMark = dialogAddItemBinding.ivCheckItemAdded
        checkMark.visibility = View.GONE

        //listener for button to confirm adding item to list
        dialogAddItemBinding.btnAddItemToList.setOnClickListener {
            unitChanged = false

            val nameInput = autoCompleteTv.text.toString()

            //No item string entered => play shake animation
            if (nameInput.trim() == "") {
                val animationShake =
                    AnimationUtils.loadAnimation(myActivity, R.anim.shake)
                dialogAddItemBinding.actvItem.startAnimation(animationShake)
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

            val unitString = spItemUnit.selectedItem.toString()

            if (template == null) {
                //no user item with this name => check for regular template
                template = myActivity.itemTemplateList.getTemplateByName(nameInput)
                if (template == null || categoryCode != template!!.c || spItemUnit.selectedItemPosition != 0) {
                    //item unknown, or item known under different category or with different unit, use selected category and unit,
                    // add item new ItemTemplate to userItemTemplate list, using entered values
                    if (!nameInput.contains("(")) {
                        //Only add a new user item, if it was not an optional item
                        myActivity.userItemTemplateList.add(
                            ItemTemplate(nameInput, categoryCode, unitString)
                        )
                        myActivity.userItemTemplateList.save()
                        refreshItemNamesAndAutoCompleteAdapter()
                    }
                }
            } else if (categoryCode != template!!.c || unitString != template!!.s) {
                // USER ITEM KNOWN BY NAME, BUT UNIT / CATEGORY DIFFER
                //check if there is a regularItem with this name
                val regularTemplate =
                    myActivity.itemTemplateList.getTemplateByName(nameInput)

                //only add a new user item if there is no regular item with this name, this category and this unit
                if (!(regularTemplate != null && regularTemplate.c == categoryCode && regularTemplate.s == unitString)) {
                    template!!.c = categoryCode
                    template!!.s = unitString
                    myActivity.userItemTemplateList.save()
                } else { //known as user item but with different tag or different suggested unit
                    myActivity.userItemTemplateList.removeItem(nameInput)
                }
            }

            //create known item from template
            val item = ShoppingItem(
                nameInput,
                categoryCode,
                unitString,
                etItemAmount.text.toString(),
                unitString,
                false
            )

            //remove item that was tapped to edit, if editing
            // Check if category was change
            if (editing) {
                val deleteSublist = if (template != null) (editTag != categoryCode)
                else activeShoppingFr.shoppingListInstance.getSublistLength(editTag) < 2

                activeShoppingFr.shoppingListInstance.removeItem(editTag, editPos, deleteSublist)
                editing = false
                addItemDialog.dismiss()
            }
            //add new item to list
            if (MainActivity.previousFragmentStack.peek() == FT.SHOPPING) {
                //handling adding in shopping
                activeShoppingFr.shoppingListInstance.add(item)
                activeShoppingFr.myAdapter.notifyDataSetChanged()
                updateShoppingMenu()
            } else {
                //handling adding in home
                MainActivity.shoppingListWrapper[0].second.add(item)
                Toast.makeText(
                    myActivity,
                    myActivity.getString(R.string.shoppingNotificationItemAdded),
                    Toast.LENGTH_SHORT
                ).show()
            }
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
                addItemDialog.dismiss()
            }
        }

        val imm =
            myActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(autoCompleteTv, InputMethodManager.SHOW_IMPLICIT)
    }

    /**
     * Reset and open addItemDialog
     */
    fun openAddItemDialog() {
        //set dialog title to "add item"
        myActivity.titleDialogBinding.tvDialogTitle.text =
            myActivity.getString(R.string.shoppingAddItemTitle)

        //Clear item autoCompleteTextView
        dialogAddItemBinding.actvItem.setText("")

        //Request focus in item autoCompleteTextView
        dialogAddItemBinding.actvItem.requestFocus()

        //set confirm button text to "add"
        dialogAddItemBinding.btnAddItemToList.text =
            myActivity.getString(R.string.birthdayDialogAdd)

        dialogAddItemBinding.spItemUnit.tag = 0
        dialogAddItemBinding.spItemUnit.setSelection(0)
        dialogAddItemBinding.spCategory.setSelection(0)

        //set default amount text to 1
        dialogAddItemBinding.etItemAmount.setText("1")

        //open keyboard
        addItemDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        addItemDialog.show()
    }

    fun openEditItemDialog(item: ShoppingItem) {
        //set dialog title to "editing"
        myActivity.titleDialogBinding.tvDialogTitle.text =
            myActivity.getString(R.string.shoppingEditItemTitle)

        //set confirm Button text to "save"
        dialogAddItemBinding.btnAddItemToList.text =
            resources.getString(R.string.noteDiscardDialogSave)

        //show item name
        dialogAddItemBinding.actvItem.setText(item.name)

        //request focus in item autoCompleteTextView
        dialogAddItemBinding.actvItem.requestFocus()

        //set cursor to end of item name
        dialogAddItemBinding.actvItem.setSelection(item.name!!.length)

        //select correct unit
        val unitIndex = myActivity.resources
            .getStringArray(R.array.units)
            .indexOf(item.suggestedUnit)

        dialogAddItemBinding.spItemUnit.tag = unitIndex
        //Select correct unit when opening dialog for edit
        dialogAddItemBinding.spItemUnit.setSelection(unitIndex)

        unitChanged = false

        //show correct item amount
        dialogAddItemBinding.etItemAmount.setText(item.amount.toString())

        //open keyboard
        addItemDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialogAddItemBinding.actvItem.dismissDropDown()

        //show dialog
        addItemDialog.show()

        editing = true
    }

    private fun dialogShoppingClear() {
        val titleId = R.string.shoppingDialogClearList
        val action: () -> Unit = {
            activeShoppingFr.shoppingListInstance.clear()
            activeShoppingFr.shoppingListInstance.save()
            activeDeletedItems.clear()
            activeShoppingFr.myAdapter.notifyDataSetChanged()
            updateShoppingMenu()
        }
        myActivity.dialogConfirm(titleId, action)
    }

    private fun dialogRemoveCheckedItems() {
        val titleId = R.string.shoppingDialogRemoveChecked
        val action: () -> Unit = {
            activeShoppingFr.shoppingListInstance.removeCheckedItems()
            activeShoppingFr.myAdapter.notifyDataSetChanged()
            updateShoppingMenu()
        }
        myActivity.dialogConfirm(titleId, action)
    }

    private fun dialogRemoveDuplicateItems() {
        val titleId = R.string.shoppingDialogRemoveCheckedDuplicates
        val action: () -> Unit = {
            activeShoppingFr.shoppingListInstance.removeCheckedDuplicateItems()
            activeShoppingFr.myAdapter.notifyDataSetChanged()
            updateShoppingMenu()
        }
        myActivity.dialogConfirm(titleId, action)
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

    //Hides all menu icons besides the search element
    private fun updateShoppingMenuForSearch() {
        myMenu.forEach {
            if (it.itemId == R.id.item_shopping_search) {
                return@forEach
            }
            it.isVisible = false
        }
    }

    fun updateShoppingMenu() {
        //only show undo if item was previously deleted
        updateUndoItemIcon()
        //only show clear option if current list is not empty
        updateClearShoppingListIcon()
        //only show uncheck all and remove checked, if any items are checked
        updateUncheckShoppingListIcon()
        updateRemoveChecked()
        //only show expand all, if any category is collapsed
        updateExpandAllIcon()
        //only show collapse all, if any category is expanded
        updateCollapseAllIcon()
        //only show delete option if there is more than one list
        updateDeleteListIcon()
        //always show option to add / rename lists
        myMenu.findItem(R.id.item_shopping_add_list)?.isVisible = true
        myMenu.findItem(R.id.item_shopping_rename_list)?.isVisible = true
    }

    private fun updateDeleteListIcon() {
        myMenu.findItem(R.id.item_shopping_delete_list)?.isVisible =
            MainActivity.shoppingListWrapper.size > 1
    }

    private fun updateRemoveChecked() {
        myMenu.findItem(R.id.item_shopping_remove_checked)?.isVisible =
            activeShoppingFr.shoppingListInstance.somethingIsChecked()
    }

    private fun updateUndoItemIcon() {
        myMenu.findItem(R.id.item_shopping_undo)?.isVisible = activeDeletedItems.isNotEmpty()
    }

    private fun updateClearShoppingListIcon() {
        myMenu.findItem(R.id.item_shopping_clear_list)?.isVisible =
            activeShoppingFr.shoppingListInstance.size > 0
    }

    private fun updateUncheckShoppingListIcon() {
        myMenu.findItem(R.id.item_shopping_uncheck_all)?.isVisible =
            activeShoppingFr.shoppingListInstance.somethingIsChecked()

    }


    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = MainActivity.shoppingListWrapper.size

        override fun createFragment(position: Int): Fragment {
            return this@MultiShoppingFr.shoppingFragments[position]
        }

    }
}

class AutoCompleteAdapter(
    context: Context,
    resource: Int,
    textViewResourceId: Int = 0,
    items: List<String> = listOf()
) : ArrayAdapter<Any>(context, resource, textViewResourceId, items) {


    internal var itemNames: MutableList<String> = mutableListOf()
    internal var suggestions: MutableList<String> = mutableListOf()
    var imWorking: Boolean = false
    val maxSuggestions = 5

    init {
        itemNames = items.toMutableList()
        suggestions = ArrayList()
    }

    /**
     * Custom Filter implementation for custom suggestions we provide.
     */
    private var filter: Filter = object : Filter() {

        override fun performFiltering(inputCharSequence: CharSequence?): FilterResults {
            //convert inputCharSequence to string, remove leading or trailing white spaces and change it to lower case
            val input = inputCharSequence.toString().trim().lowercase().split("(")[0].trim()

            val result = FilterResults()

            //don't perform search if a search is currently being performed, or input length is < 2
            if (imWorking || input.length < 2 || inputCharSequence == null) {
                return result
            }

            //indicate that a search is being performed
            imWorking = true

            //clear suggestions from previous search
            suggestions.clear()


            //checks for every item if it starts with input (case insensitive search) (stop if maxSuggestions reached)
            itemNames.forEach {
                if (suggestions.size >= maxSuggestions) return@forEach
                if (it.lowercase().startsWith(input)) {
                    suggestions.add(it)
                }
            }

            //sort all results starting with the input by length to suggest the shortest ones first
            suggestions.sortBy { it.length }

            //If less than 5 items that start with "input" have been found, add
            //items that contain "input" to the end of the list
            itemNames.forEach {
                if (suggestions.size >= maxSuggestions) return@forEach
                if (it.lowercase().contains(input)) {
                    if (!suggestions.contains(it)) {
                        suggestions.add(it)
                    }
                }
            }

            //if anything was found that starts with, or contains the "input", or if the setting says
            //to only show perfect matches and don't suggest similar items, return the current suggestions
            if (suggestions.isNotEmpty() || !ShoppingFr.suggestSimilar) {
                result.values = suggestions
                result.count = suggestions.size
                return result
            }

            //create a new mutable list containing all item names
            val possibles: MutableList<String> = mutableListOf()
            possibles.addAll(itemNames)

            //create map that saves itemNames with their "likelihood score"
            val withValues: MutableMap<String, Int> = mutableMapOf()

            //calculates likelihood score for every item
            possibles.forEach { itemName ->
                //index to iterate over string
                var i = 0
                //score that indicates how much this item matches the input
                var likelihoodScore = 0
                //Copy of the itemName in which found letters will be replaced with empty ones
                var lettersLeft = itemName.lowercase()

                val lowerItem = lettersLeft
                //Add 2 points to score, if item starts with the same char as the input
                if (lowerItem[0] == input[0]) {
                    likelihoodScore += 2
                }

                //Add 2 points to score, if item ends with the same char as the input
                if (lowerItem.last() == input.last()) {
                    likelihoodScore += 2
                }

                //Iterate over the overlapping part of the words
                while (i < min(itemName.length, input.length)) {
                    when {
                        lowerItem[i] == input[i] -> {
                            //increase score by 2 if this char occurs at this index
                            likelihoodScore += 2
                        }

                        lettersLeft.contains(input[i]) -> {
                            //increase score by 1 if this char occurs anywhere in the string
                            likelihoodScore++
                            //Remove letter from word copy so it can't be counted twice
                            lettersLeft = lettersLeft.replaceFirst(input[i], '\u0000')
                        }

                        else -> {
                            //decrease score by 1 if Letter does not occur in item name
                            likelihoodScore -= 1
                        }
                    }
                    i++
                }
                //subtract length difference from likelihood score
                likelihoodScore -= abs(itemName.length - input.length)
                //store score for this item name in the withValues map
                withValues[itemName] = likelihoodScore
            }

            //Filter map for all items that pass the likelihood threshold of their length / 1.2.
            //Decrease this value to make the algorithm more stricter
            //Sort it descending by value (highest values first) and add the first <maxSuggestions> items to the suggestions
            withValues.toList().filter { (name, value) -> value > name.length / 1.2 }
                .sortedByDescending { (_, value) -> value }.forEach {
                    if (suggestions.size >= maxSuggestions) return@forEach
                    suggestions.add(it.first)
                }

            result.values = suggestions
            result.count = suggestions.size
            return result
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {

            if (results.values == null) {
                //return nothing was found
                return
            }

            val filterList = Collections.synchronizedList(results.values as List<*>)

            if (results.count > 0) {
                clear()
                addAll(filterList)
                notifyDataSetChanged()
            }
            imWorking = false
        }
    }

    override fun getFilter(): Filter {
        return filter
    }
}