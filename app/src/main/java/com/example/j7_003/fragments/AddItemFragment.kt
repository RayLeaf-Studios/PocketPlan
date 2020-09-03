package com.example.j7_003.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.database.ItemTemplateList
import com.example.j7_003.data.database.ShoppingList
import com.example.j7_003.data.database.database_objects.ShoppingItem
import kotlinx.android.synthetic.main.fragment_add_item.*
import kotlinx.android.synthetic.main.fragment_add_item.view.*

class AddItemFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val myView = inflater.inflate(R.layout.fragment_add_item, container, false)

        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, InputMethodManager.SHOW_FORCED)

        initializeComponents(myView)

        return myView
    }

    private fun initializeComponents(myView: View){


        val tvCategoryField = myView.tvCategoryField
        tvCategoryField.text = ""

        val itemNameList: ArrayList<String> = ArrayList()
        val itemTemplateList = ItemTemplateList()
        itemTemplateList.forEach{
            itemNameList.add(it.n)
        }

        //TODO remove this and replace it with proper connection to database
        //Initialize autocomplete text view for item name and its adapter
        val autoCompleteTv = myView.actvItem
        val autoCompleteTvAdapter = ArrayAdapter<String>(MainActivity.myActivity, android.R.layout.simple_spinner_dropdown_item, itemNameList)
        autoCompleteTv.setAdapter(autoCompleteTvAdapter)
        autoCompleteTv.requestFocus()

        val listInstance1 = ItemTemplateList()
        autoCompleteTv.setOnItemClickListener { parent, view, position, id ->
            val template = listInstance1.getTemplateByName(autoCompleteTv.text.toString())
            if(template!=null){
                tvCategoryField.text = template.c.n
                val unitPointPos = when(template.s){
                    "kg" -> 1
                    "g" -> 2
                    "L" -> 3
                    "ml" -> 4
                    else -> 0
                }
                spItemUnit.setSelection(unitPointPos)
            }
            else{
                tvCategoryField.text = ""
            }
        }

        autoCompleteTv.setOnKeyListener { v, keyCode, event ->
            val template = listInstance1.getTemplateByName(autoCompleteTv.text.toString())
            if(template!=null){
                tvCategoryField.text = template.c.n
            }
            else{
                tvCategoryField.text = ""
            }
            true
        }


        //Initialize spinner and its adapter to choose its Unit
        val mySpinner = myView.spItemUnit
        val myAdapter = ArrayAdapter<String>(MainActivity.myActivity, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.units))
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mySpinner.adapter = myAdapter

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
            //TODO read values from name(autoText), amount textvield and unit spinner, then add item
            val shoppingInstance = ShoppingList()
            val listInstance = ItemTemplateList()

            val template = listInstance.getTemplateByName(autoCompleteTv.text.toString())
            if(template!=null){
                val item = ShoppingItem(
                    template.n, template.c,
                    template.s, etItemAmount.text.toString(), mySpinner.selectedItem.toString(), false)
                shoppingInstance.add(item)
                //TODO SORT AFTER ADDING
            }else{
                //TODO Handle unknown item
            }
            MainActivity.myActivity.changeToShopping()
        }
    }

}