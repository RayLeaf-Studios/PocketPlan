package com.example.j7_003.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.database.ItemTemplateList
import com.example.j7_003.data.database.ShoppingList
import com.example.j7_003.data.database.TagList
import com.example.j7_003.data.database.database_objects.ShoppingItem
import kotlinx.android.synthetic.main.fragment_add_item.*
import kotlinx.android.synthetic.main.fragment_add_item.view.*

class AddItemFragment : Fragment() {

    private lateinit var tagNames: Array<String?>
    private lateinit var itemTemplateList: ItemTemplateList
    private lateinit var actvItem: AutoCompleteTextView
    private lateinit var spItemUnit: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val myView = inflater.inflate(R.layout.fragment_add_item, container, false)

        actvItem = myView.actvItem
        spItemUnit = myView.spItemUnit

        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, InputMethodManager.SHOW_FORCED)

        //todo get taglist here from shoppinglistinstance or something
        val tagList = TagList()
        tagNames = tagList.getTagNames()
        itemTemplateList = ItemTemplateList()

        initializeComponents(myView)

        return myView
    }

    private fun initializeComponents(myView: View){

        //initialize spinner for categorys
        val spCategory = myView.spCategory
        val categoryAdapter = ArrayAdapter<String>(MainActivity.act, android.R.layout.simple_list_item_1, tagNames)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategory.adapter = categoryAdapter

        //Initialize spinner and its adapter to choose its Unit
        val mySpinner = myView.spItemUnit
        val myAdapter = ArrayAdapter<String>(MainActivity.act, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.units))
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mySpinner.adapter = myAdapter

        val itemNameList: ArrayList<String> = ArrayList()
        val itemTemplateList = ItemTemplateList()
        itemTemplateList.forEach{
            itemNameList.add(it.n)
        }

        //TODO remove this and replace it with proper connection to database
        //Initialize autocomplete text view for item name and its adapter
        val autoCompleteTv = myView.actvItem
        val autoCompleteTvAdapter = ArrayAdapter<String>(MainActivity.act, android.R.layout.simple_spinner_dropdown_item, itemNameList)
        autoCompleteTv.setAdapter(autoCompleteTvAdapter)
        autoCompleteTv.requestFocus()

        autoCompleteTv.setOnItemClickListener { parent, view, position, id ->
            handleItemEntered()
        }

        autoCompleteTv.setOnKeyListener { v, keyCode, event ->
            handleItemEntered()
            true
        }

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
        val shoppingInstance = ShoppingList()
        val template = itemTemplateList.getTemplateByName(actvItem.text.toString())
        val tagList = TagList()
        val tag = tagList.getTagByName(spCategory.selectedItem as String)

        if(template!=null && tag!=null){
            val item = ShoppingItem(
                template.n, tag,
                template.s, etItemAmount.text.toString(), spItemUnit.selectedItem.toString(), false)
            shoppingInstance.add(item)
        }else{
//            val item = ShoppingItem(
//                template.n, tag,
//                template.s, etItemAmount.text.toString(), spItemUnit.selectedItem.toString(), false)
//            shoppingInstance.add(item)
        }
        MainActivity.act.changeToShopping()
    }

    private fun handleItemEntered(){
        val template = itemTemplateList.getTemplateByName(actvItem.text.toString())
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