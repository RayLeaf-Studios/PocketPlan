package com.example.j7_003.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import androidx.core.widget.doOnTextChanged
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.database.*
import com.example.j7_003.data.database.database_objects.ShoppingItem
import kotlinx.android.synthetic.main.fragment_add_item.*
import kotlinx.android.synthetic.main.fragment_add_item.view.*
import kotlin.random.Random

class AddItemFragment : Fragment() {

    private lateinit var tagNames: Array<String?>
    private lateinit var itemTemplateList: ItemTemplateList
    private lateinit var userItemTemplateList: UserItemTemplateList
    private lateinit var shoppingListInstance: ShoppingList
    private lateinit var actvItem: AutoCompleteTextView
    private lateinit var spItemUnit: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val myView = inflater.inflate(R.layout.fragment_add_item, container, false)

        //initialize autocompleteTextView and spinner for item unit
        actvItem = myView.actvItem
        spItemUnit = myView.spItemUnit


        //initialize tagNames and itemTemplateList
        val tagList = TagList()
        tagNames = tagList.getTagNames()
        itemTemplateList = ItemTemplateList()
        userItemTemplateList = UserItemTemplateList()
        shoppingListInstance = ShoppingList()

        initializeComponents(myView)

        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, InputMethodManager.SHOW_FORCED)

        return myView
    }

    private fun initializeComponents(myView: View){

        //initialize spinner for categories
        val spCategory = myView.spCategory
        val categoryAdapter = ArrayAdapter<String>(MainActivity.act, android.R.layout.simple_list_item_1, tagNames)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategory.adapter = categoryAdapter

        //Initialize spinner and its adapter to choose its Unit
        val mySpinner = myView.spItemUnit
        val myAdapter = ArrayAdapter<String>(MainActivity.act, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.units))
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mySpinner.adapter = myAdapter

        //initialize itemNameList
        val itemNameList: ArrayList<String> = ArrayList()

        itemTemplateList.forEach{
            itemNameList.add(it.n)
        }
        userItemTemplateList.forEach{
            itemNameList.add(it.n)
        }

        //initialize autocompleteTextView and its adapter
        val autoCompleteTv = myView.actvItem
        val autoCompleteTvAdapter = ArrayAdapter<String>(MainActivity.act, android.R.layout.simple_spinner_dropdown_item, itemNameList)
        autoCompleteTv.setAdapter(autoCompleteTvAdapter)
        autoCompleteTv.requestFocus()

        val textWatcher = object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                handleItemEntered()
            }

            override fun afterTextChanged(s: Editable?) {
            }

        }

        autoCompleteTv.addTextChangedListener(textWatcher)


        //initialize edit text for item amount string
        val etItemAmount = myView.etItemAmount
        etItemAmount.setText("1")
        var firstTap = true
        etItemAmount.setOnFocusChangeListener { v, hasFocus ->
            if(firstTap){
                etItemAmount.setText("")
                firstTap = false
            }
        }

        //Button to Confirm adding Item to list
        myView.btnAddItemToList.setOnClickListener {
            handleAddingItem()
        }
    }

    private fun handleAddingItem(){
        val tagList = TagList()
        val tag = tagList.getTagByName(spCategory.selectedItem as String)
        //check if user template exists
        var template = userItemTemplateList.getTemplateByName(actvItem.text.toString())
        if(template==null){
            template = itemTemplateList.getTemplateByName(actvItem.text.toString())
            if(template==null){
                //item unknown, use selected category, add item, and save it to userTemplate list
                userItemTemplateList.add(ItemTemplate(actvItem.text.toString(), tag, spItemUnit.selectedItem.toString()))
                val item = ShoppingItem(
                    actvItem.text.toString(), tag,
                    spItemUnit.selectedItem.toString(), etItemAmount.text.toString(), spItemUnit.selectedItem.toString(), false)
                shoppingListInstance.add(item)
                MainActivity.act.changeToShopping()
                return
            }
        }
        //add already known item to list
        val item = ShoppingItem(
            template.n, tag,
            template.s, etItemAmount.text.toString(), spItemUnit.selectedItem.toString(), false)
        shoppingListInstance.add(item)
        MainActivity.act.changeToShopping()
    }

    private fun handleItemEntered(){
        //check for existing user template
        var template = userItemTemplateList.getTemplateByName(actvItem.text.toString())
        if(template!=null){
            //display correct category
            spCategory.setSelection(tagNames.indexOf(template.c.n))

            //display correct unit
            val unitPointPos = resources.getStringArray(R.array.units).indexOf(template.s)
            spItemUnit.setSelection(unitPointPos)
            return
        }

        //check for existing item template
        template = itemTemplateList.getTemplateByName(actvItem.text.toString())
        if(template!=null){
            //display correct category
            spCategory.setSelection(tagNames.indexOf(template.c.n))

            //display correct unit
            val unitPointPos = resources.getStringArray(R.array.units).indexOf(template.s)
            spItemUnit.setSelection(unitPointPos)
        }
        else{
            spCategory.setSelection(0)
        }
    }

}