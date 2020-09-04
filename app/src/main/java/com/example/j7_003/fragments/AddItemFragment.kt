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

    lateinit var tags: Array<String>
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

        //todo get taglist here from shoppinglistinstance or something
        tags = arrayOf("Sonstige","Obst und Gemüse", "Käse", "Molkereiprodukte")
        initializeComponents(myView)

        return myView
    }

    private fun initializeComponents(myView: View){



        val spCategory = myView.spCategory
        val categoryAdapter = ArrayAdapter<String>(MainActivity.act, android.R.layout.simple_list_item_1, tags)
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

        val listInstance1 = ItemTemplateList()
        autoCompleteTv.setOnItemClickListener { parent, view, position, id ->
            val template = listInstance1.getTemplateByName(autoCompleteTv.text.toString())
            if(template!=null){
                //template.c.n "=" template.category.name
//                tvCategoryField.text = template.c.n
                //todo set correct selection in tvcategory
                spCategory.setSelection(tags.indexOf(template.c.n))
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
                //todo set correct selection in tvcategory
//                tvCategoryField.text = ""

                spCategory.setSelection(0)
            }
        }

        autoCompleteTv.setOnKeyListener { v, keyCode, event ->
            val template = listInstance1.getTemplateByName(autoCompleteTv.text.toString())
            if(template!=null){
                //todo set select correct selection
//                tvCategoryField.text = template.c.n
            }
            else{
                //todo set select correct selection
//                tvCategoryField.text = ""
            }
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
            //TODO read values from name(autoText), amount textvield and unit spinner, then add item
            val shoppingInstance = ShoppingList()
            val listInstance = ItemTemplateList()

            val template = listInstance.getTemplateByName(autoCompleteTv.text.toString())
            if(template!=null){
                val item = ShoppingItem(
                    template.n, template.c,
                    template.s, etItemAmount.text.toString(), mySpinner.selectedItem.toString(), false)
                shoppingInstance.add(item)
            }else{
                //TODO Handle unknown item
            }
            MainActivity.act.changeToShopping()
        }
    }

}