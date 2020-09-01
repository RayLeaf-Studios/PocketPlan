package com.example.j7_003.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.database.ItemTemplateList
import com.example.j7_003.data.database.ShoppingList
import com.example.j7_003.data.database.database_objects.ShoppingItem
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

        initializeComponents(myView)

        return myView
    }

    private fun initializeComponents(myView: View){

        //TODO remove this and replace it with proper connection to database
        val items = arrayOf(
            "Ananas",
            "Apfel",
            "Aprikose",
            "Artischocke",
            "Aubergine",
            "Avocado",
            "Bohnen",
            "Blaukraut",
            "Banane ",
            "Birne ",
            "Blumenkohl",
            "Brokkoli",
            "Brombeere",
            "Chili",
            "Chinakohl",
            "Champignon ",
            "Cranberries",
            "Drillinge ",
            "Datteln",
            "Eissalat",
            "Erbsen",
            "Erdbeeren",
            "Feige ",
            "Feldsalat ",
            "Grünkohl",
            "Grapefruit ",
            "Granatapfel",
            "Guave",
            "Gurke",
            "Hagebutte ",
            "Honigmelone ",
            "Heidelbeere ",
            "Himbeere",
            "Holunderbeere",
            "Ingwer ",
            "Johannisbeere",
            "Kokosnuss ",
            "Kaki",
            "Kartoffeln",
            "Kürbis ",
            "Kohlrabi",
            "Kirsche ",
            "Knoblauch",
            "Kiwi ",
            "Lauch",
            "Limette ",
            "Litschi ",
            "Mais",
            "Maronen ",
            "Maracuja ",
            "Mandarine ",
            "Mango ",
            "Melone ",
            "Mirabelle ",
            "Nektarine ",
            "Orange",
            "Olive ",
            "Papaya",
            "Paprika",
            "Pastinaken",
            "Peperoni",
            "Passionsfrucht",
            "Pfirsich ",
            "Pflaume ",
            "Physalis ",
            "Pilze",
            "Preiselbeeren ",
            "Pomelo ",
            "Quitte ",
            "Radieschen",
            "Rucola ",
            "Rhabarber ",
            "Rosenkohl",
            "Rote Beete ",
            "Rotkohl",
            "Salat",
            "Schnittlauch ",
            "Sauerkraut ",
            "Sprossen ",
            "Sanddorn ",
            "Schwarzwurzel",
            "Sellerie",
            "Spargel",
            "Spinat",
            "Stachelbeere",
            "Steckrübe",
            "Süßkartoffel ",
            "Tomate",
            "Weintraube",
            "Weißkohl",
            "Wirsing",
            "Zitrone",
            "Zucchini",
            "Zwetschge",
            "Zwiebel")

        //Initialize autocomplete text view for item name and its adapter
        val autoCompleteTv = myView.actvItem
        val autoCompleteTvAdapter = ArrayAdapter<String>(MainActivity.myActivity, android.R.layout.simple_spinner_dropdown_item, items)
        autoCompleteTv.setAdapter(autoCompleteTvAdapter)


        //Initialize spinner and its adapter to choose its Unit
        val mySpinner = myView.spItemUnit
        val myAdapter = ArrayAdapter<String>(MainActivity.myActivity, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.units))
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mySpinner.adapter = myAdapter

        //initialize edit text for item amount string
        val etItemAmount = myView.etItemAmount


        //Button to Confirm adding Item to list
        myView.btnAddItemToList.setOnClickListener {
            //TODO read values from name(autoText), amount textvield and unit spinner, then add item
            val shoppingInstance = ShoppingList()
            val listInstance = ItemTemplateList()

            val template = listInstance.getTemplateByName(autoCompleteTv.text.toString())
            if(template!=null){
                val item = ShoppingItem(
                    template.name, template.category,
                    template.suggestedUnit, etItemAmount.text.toString(), mySpinner.selectedItem.toString(), false)
                shoppingInstance.add(item)
            }else{
                //TODO Handle unknown item
            }
            MainActivity.myActivity.changeToShopping()
        }
    }

}