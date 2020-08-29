package com.example.j7_003.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import kotlinx.android.synthetic.main.fragment_add_item.view.*

class AddItemFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
            "Wirsing ",
            "Zitrone ",
            "Zucchini",
            "Zwetschge ",
            "Zwiebel ")
        // Inflate the layout for this fragment
        val myView = inflater.inflate(R.layout.fragment_add_item, container, false)
        val myActv = myView.actvItem
        val actvAdapter = ArrayAdapter<String>(MainActivity.myActivity, android.R.layout.simple_spinner_dropdown_item, items)
        myActv.setAdapter(actvAdapter)

        myView.btnAddItemToList.setOnClickListener { MainActivity.myActivity.changeToShopping() }



        val mySpinner = myView.spItemAmount
        val myAdapter = ArrayAdapter<String>(MainActivity.myActivity, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.units))
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mySpinner.adapter = myAdapter
        return myView


    }

}