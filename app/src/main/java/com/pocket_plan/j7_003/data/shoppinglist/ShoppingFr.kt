package com.pocket_plan.j7_003.data.shoppinglist

import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.databinding.FragmentShoppingBinding
import com.pocket_plan.j7_003.databinding.RowCategoryBinding
import com.pocket_plan.j7_003.databinding.RowItemBinding
import kotlinx.coroutines.runBlocking
import java.util.Locale


class ShoppingFr : Fragment() {
    private var _fragmentBinding: FragmentShoppingBinding? = null
    private val fragmentBinding get() = _fragmentBinding!!
    private lateinit var myActivity: MainActivity
    lateinit var myMultiShoppingFr: MultiShoppingFr
    lateinit var shoppingListInstance: ShoppingList
    lateinit var shoppingListName: String
    var query: String? = null

    lateinit var myAdapter: ShoppingListAdapter

    companion object {

        var suggestSimilar: Boolean =
            SettingsManager.getSetting(SettingId.SUGGEST_SIMILAR_ITEMS) as Boolean

        val moveCheckedSublistsDown =
            SettingsManager.getSetting(SettingId.MOVE_CHECKED_DOWN) as Boolean

        lateinit var layoutManager: LinearLayoutManager

        var offsetTop: Int = 0
        var firstPos: Int = 0
        var expandOne: Boolean = false
        var collapseCheckedSublists: Boolean = false

        @JvmStatic
        fun newInstance() =
            ShoppingFr().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    fun getCategoryVisibility(category: Pair<String, ArrayList<ShoppingItem>>): Boolean {

        val categoryName =
            myActivity.resources.getStringArray(R.array.categoryNames)[myActivity.resources.getStringArray(
                R.array.categoryCodes
            ).indexOf(category.first)]
        if (categoryName.lowercase(Locale.ROOT).contains(query!!.lowercase(Locale.ROOT))) {
            return true
        }
        category.second.forEachIndexed { index, item ->
            if (index == 0) {
                return@forEachIndexed
            }
            if (item.name!!.lowercase().contains(query!!.lowercase())) {
                return true
            }

        }
        return false
    }

    fun getItemVisibility(item: ShoppingItem): Boolean {
        val categoryName =
            myActivity.resources.getStringArray(R.array.categoryNames)[myActivity.resources.getStringArray(
                R.array.categoryCodes
            ).indexOf(item.tag)]
        if (categoryName.lowercase().contains(query!!.lowercase())) {
            return true
        }
        if (item.name!!.lowercase().contains(query!!.lowercase())) {
            return true
        }
        return false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentBinding = FragmentShoppingBinding.inflate(inflater, container, false)

        myActivity = activity as MainActivity
        query = null
        myAdapter = ShoppingListAdapter(myActivity, this)

        //load settings
        expandOne = SettingsManager.getSetting(SettingId.EXPAND_ONE_CATEGORY) as Boolean
        collapseCheckedSublists =
            SettingsManager.getSetting(SettingId.COLLAPSE_CHECKED_SUBLISTS) as Boolean

        //if expandOne Setting = true, collapse all categories
        if (expandOne) {
            shoppingListInstance.forEach {
                if (shoppingListInstance.isTagExpanded(it.first)) {
                    shoppingListInstance.flipExpansionState(it.first)
                }
            }
        }


        //Initialize references to recycler and its adapter
        val myRecycler = fragmentBinding.recyclerViewShopping
        val swipeRefresher = fragmentBinding.swipeRefreshLayoutShopping

        swipeRefresher.isEnabled = shoppingListInstance.isSyncModeEnabled()

        swipeRefresher.setOnRefreshListener {
            if (!shoppingListInstance.isSyncModeEnabled()) {
                swipeRefresher.isRefreshing = false
                return@setOnRefreshListener
            }

            runBlocking {
                myMultiShoppingFr.fetchList(shoppingListInstance.getSyncId()!!)
            }
            swipeRefresher.isRefreshing = false
        }

        //attach adapter to recycler and initialize parameters of recycler
        myRecycler.adapter = myAdapter
        layoutManager = LinearLayoutManager(activity)
        myRecycler.layoutManager = layoutManager
        myRecycler.setHasFixedSize(true)


        //ItemTouchHelper to support drag and drop reordering
        val itemTouchHelper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                0
            ) {
                var previousPosition: Int = -1
                var moving = false

                override fun getDragDirs(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    return when (myMultiShoppingFr.searching) {
                        true -> 0
                        else -> ItemTouchHelper.UP or ItemTouchHelper.DOWN
                    }
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    val currentPosition = viewHolder.bindingAdapterPosition
                    //mark that moving has ended (to allow a new previousPosition when move is detected)
                    moving = false

                    // don't refresh item if
                    // currentPosition == -1   =>  clearView got called due to a swipe to delete
                    // currentPosition == previousPosition   =>  item was moved, but placed back to original position
                    // previousPosition == -1   =>  item was selected but not moved
                    if (currentPosition == -1 || currentPosition == previousPosition || previousPosition == -1) {
                        previousPosition = -1
                        super.clearView(recyclerView, viewHolder)
                        return
                    }

                    //save category that was moved
                    val movedCategory = shoppingListInstance[previousPosition]
                    //remove it from its previous position
                    shoppingListInstance.removeAt(previousPosition)
                    //re-add it at the current adapter position
                    shoppingListInstance.add(currentPosition, movedCategory)
                    shoppingListInstance.updateOrder()
                    shoppingListInstance.save()

                    if (moveCheckedSublistsDown) {
                        //get tag of this category
                        val tag = (viewHolder as ShoppingListAdapter.CategoryViewHolder).tag
                        //get position
                        val position = viewHolder.bindingAdapterPosition
                        //get boolean if all items are checked
                        val oldAllChecked = shoppingListInstance.areAllChecked(tag)

                        //get new checked state
                        val newAllChecked = if (currentPosition > previousPosition) {
                            //if moved down, take status from above
                            shoppingListInstance.areAllChecked(shoppingListInstance[position - 1].first)
                        } else {
                            //if moved up, take status from below
                            shoppingListInstance.areAllChecked(shoppingListInstance[position + 1].first)
                        }

                        if (oldAllChecked != newAllChecked) {
                            //auto expand / collapse when checkedState changed

                            //if setting says to collapse all sub lists, the new checked state is all checked,
                            //and its currently expanded, collapse it
                            if (collapseCheckedSublists && newAllChecked && shoppingListInstance.isTagExpanded(
                                    tag
                                )
                            ) {
                                shoppingListInstance.flipExpansionState(tag)
                            }

                            //if new state is all unchecked, and its currently not expanded, expand it
                            if (!newAllChecked && !shoppingListInstance.isTagExpanded(tag)) {
                                shoppingListInstance.flipExpansionState(tag)
                                //adjust other categories if setting says to only expand one
                                if (expandOne) {
                                    //iterate through all categories and contract one if you find one that's expanded and not the current sublist
                                    shoppingListInstance.forEach {
                                        if (shoppingListInstance.isTagExpanded(it.first) && it.first != tag) {
                                            shoppingListInstance.flipExpansionState(it.first)
                                            myAdapter.notifyItemChanged(
                                                shoppingListInstance.getTagIndex(
                                                    it.first
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            //flip checked state of this category
                            shoppingListInstance.equalizeCheckedStates(tag)
                            myAdapter.notifyItemChanged(position)
                            shoppingListInstance.save()
                        }
                    }
                    super.clearView(recyclerView, viewHolder)
                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
                ): Boolean {

                    if (!moving) {
                        //if not moving, save new previous position
                        previousPosition = viewHolder.bindingAdapterPosition

                        //and prevent new previous positions from being set until this move is over
                        moving = true
                    }

                    //get start and target position of item that gets dragged
                    val fromPos = viewHolder.bindingAdapterPosition
                    val toPos = target.bindingAdapterPosition

                    // animate move of category from `fromPos` to `toPos` in adapter.
                    myAdapter.notifyItemMoved(fromPos, toPos)

                    return true // true if moved, false otherwise
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    /* no-op, swiping categories is not supported */
                }
            })

        itemTouchHelper.attachToRecyclerView(myRecycler)

        return fragmentBinding.root
    }

    /**
     * Helper function to prevent scrolling due to notifyMove
     */
    fun prepareForMove() {
        firstPos = layoutManager.findFirstVisibleItemPosition()
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

    fun search(query: String) {
        this.query = query
        myAdapter.notifyDataSetChanged()
    }
}

/**
 * Adapter for categories
 */
class ShoppingListAdapter(mainActivity: MainActivity, shoppingFr: ShoppingFr) :
    RecyclerView.Adapter<ShoppingListAdapter.CategoryViewHolder>() {
    private val myFragment = shoppingFr
    private val myActivity = mainActivity
    private val round = SettingsManager.getSetting(SettingId.SHAPES_ROUND) as Boolean
    private val collapseCheckedSublists =
        SettingsManager.getSetting(SettingId.COLLAPSE_CHECKED_SUBLISTS) as Boolean
    private val moveCheckedSublistsDown =
        SettingsManager.getSetting(SettingId.MOVE_CHECKED_DOWN) as Boolean
    private val cr = myActivity.resources.getDimension(R.dimen.cornerRadius)
    private val density = myActivity.resources.displayMetrics.density

    private lateinit var sublistAdapter: SublistAdapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val rowCategoryBinding =
            RowCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(rowCategoryBinding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        if (myFragment.shoppingListInstance[position].first == "meta") {
            // don't ask why, but setting only the root to GONE doesn't work
            holder.binding.root.visibility = View.GONE
            holder.binding.subRecyclerView.visibility = View.GONE
            holder.binding.tvNumberOfItems.visibility = View.GONE
            holder.binding.ivCheckMark.visibility = View.GONE
            holder.binding.tvCategoryName.visibility = View.GONE
            holder.binding.ivExpand.visibility = View.GONE
            holder.binding.clTapExpand.visibility = View.GONE
            holder.binding.cvCategory.visibility = View.GONE
            holder.binding.divider3.visibility = View.GONE
            return
        }

        //long click listener playing shake animation to indicate moving is possible
        holder.binding.root.setOnLongClickListener {
            if (myFragment.myMultiShoppingFr.searching) {
                return@setOnLongClickListener true
            }
            val animationShake =
                AnimationUtils.loadAnimation(myActivity, R.anim.shake_small)
            holder.binding.root.startAnimation(animationShake)
            return@setOnLongClickListener true
        }

        //Get reference to currently used shopping list instance
        val shoppingListInstance = myFragment.shoppingListInstance

        //get Tag for current category element
        val tag = shoppingListInstance[position].first

        //set tag for view holder
        holder.tag = tag

        val categoryIndex = shoppingListInstance.getTagIndex(tag)
        val category = shoppingListInstance[categoryIndex]
        if (myFragment.query != null && !myFragment.getCategoryVisibility(category)) {
            holder.itemView.layoutParams.height = 0
            val params = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(0, 0, 0, 0)
            return
        }
        holder.binding.root.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        val params = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        val margin = (density * 10).toInt()
        params.setMargins(margin, margin, margin, margin)

        val numberOfItems = shoppingListInstance.getUncheckedSize(tag)

        val expanded = shoppingListInstance.isTagExpanded(tag) || myFragment.query != null

        //Expand or contract recyclerview depending on its expansion state
        holder.binding.subRecyclerView.visibility = when (expanded) {
            true -> View.VISIBLE
            false -> View.GONE
        }

        //Flip expansion arrow to show expansion state of category
        holder.binding.ivExpand.rotation = when (expanded) {
            true -> 180f
            else -> 0f
        }

        //Sets Text name of category of sublist
        holder.binding.tvCategoryName.text =
            myActivity.resources.getStringArray(R.array.categoryNames)[myActivity.resources.getStringArray(
                R.array.categoryCodes
            ).indexOf(tag)]

        //Sets background color of sublist according to the tag
        manageCheckedCategory(
            holder,
            myFragment.shoppingListInstance.areAllChecked(tag),
            numberOfItems,
            tag
        )

        //Setting adapter for this sublist
        val subAdapter = SublistAdapter(tag, holder, myActivity, myFragment)
        sublistAdapter = subAdapter
        holder.binding.subRecyclerView.adapter = subAdapter
        holder.binding.subRecyclerView.layoutManager = LinearLayoutManager(myActivity)
        holder.binding.subRecyclerView.setHasFixedSize(true)

        holder.binding.subRecyclerView.setOnLongClickListener {
            if (myFragment.myMultiShoppingFr.searching) return@setOnLongClickListener true
            val animationShake =
                AnimationUtils.loadAnimation(myActivity, R.anim.shake_small)
            holder.itemView.startAnimation(animationShake)
            true
        }

        //Initialize and attach swipe helpers to recyclerview of sublist
        val swipeHelperLeft = ItemTouchHelper(SwipeItemToDelete(ItemTouchHelper.LEFT, myFragment))
        swipeHelperLeft.attachToRecyclerView(holder.binding.subRecyclerView)

        val swipeHelperRight = ItemTouchHelper(SwipeItemToDelete(ItemTouchHelper.RIGHT, myFragment))
        swipeHelperRight.attachToRecyclerView(holder.binding.subRecyclerView)

        //Onclick reaction to expand / contract this sublist
        holder.binding.clTapExpand.setOnClickListener {
            val newState: Boolean = shoppingListInstance.flipExpansionState(holder.tag)!!
            //if the item gets expanded and the setting says to only expand one
            if (newState && ShoppingFr.expandOne) {
                //iterate through all categories and contract one if you find one that's expanded and not the current sublist
                shoppingListInstance.forEach {
                    if (shoppingListInstance.isTagExpanded(it.first) && it.first != holder.tag) {
                        shoppingListInstance.flipExpansionState(it.first)
                        myFragment.myAdapter.notifyItemChanged(
                            shoppingListInstance.getTagIndex(
                                it.first
                            )
                        )
                    }
                }
            }
            notifyItemChanged(holder.bindingAdapterPosition)
            myFragment.myMultiShoppingFr.updateExpandAllIcon()
            myFragment.myMultiShoppingFr.updateCollapseAllIcon()
        }

        //long click listener on clTapExpand to ensure shake animation for long click on whole category holder
        holder.binding.clTapExpand.setOnLongClickListener {
            if (myFragment.myMultiShoppingFr.searching) return@setOnLongClickListener true
            val animationShake =
                AnimationUtils.loadAnimation(myActivity, R.anim.shake_small)
            holder.itemView.startAnimation(animationShake)
            true
        }


        holder.binding.tvNumberOfItems.setOnClickListener {
            if (myFragment.shoppingListInstance.isLocked()) return@setOnClickListener

            //get new checked state of all items (result)
            val newAllChecked = shoppingListInstance.equalizeCheckedStates(tag)
            if (collapseCheckedSublists) {
                if (newAllChecked && shoppingListInstance.isTagExpanded(tag)) {
                    shoppingListInstance.flipExpansionState(tag)
                } else if (!newAllChecked && !shoppingListInstance.isTagExpanded(tag)) {
                    shoppingListInstance.flipExpansionState(tag)
                    if (ShoppingFr.expandOne) {
                        //iterate through all categories and contract one if you find one that's expanded and not the current sublist
                        myFragment.shoppingListInstance.forEach {
                            if (myFragment.shoppingListInstance.isTagExpanded(it.first) && it.first != tag) {
                                myFragment.shoppingListInstance.flipExpansionState(it.first)
                                myFragment.myAdapter.notifyItemChanged(
                                    myFragment.shoppingListInstance.getTagIndex(
                                        it.first
                                    )
                                )
                            }
                        }
                    }
                }
            }

            notifyItemChanged(holder.bindingAdapterPosition)

            if (moveCheckedSublistsDown) {
                val sublistMoveInfo = myFragment.shoppingListInstance.sortCategoriesByChecked(tag)
                if (sublistMoveInfo != null) {
                    myFragment.prepareForMove()
                    myFragment.myAdapter
                        .notifyItemMoved(sublistMoveInfo.first, sublistMoveInfo.second)

                    myFragment.reactToMove()
                }

            }
            myFragment.myMultiShoppingFr.updateShoppingMenu()
        }
    }

    fun updateCheckedState() {
        sublistAdapter.updateCheckedState()
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
                myActivity.colorForAttr(R.attr.colorOnBackGround)


            val colorCategory =
                myActivity.colorForAttr(R.attr.colorCategory)


            //get pair of color ids for right categories
            val gradientPair: Pair<Int, Int> = when (tag) {
                "So" -> Pair(R.attr.colorSonstiges, R.attr.colorSonstigesL)
                "Ob" -> Pair(R.attr.colorObstundGemüse, R.attr.colorObstundGemüseL)
                "Gt" -> Pair(R.attr.colorGetränke, R.attr.colorGetränkeL)
                "Nu" -> Pair(R.attr.colorNudelnundGetreide, R.attr.colorNudelnundGetreideL)
                "Bw" -> Pair(R.attr.colorBackwaren, R.attr.colorBackwarenL)
                "Km" -> Pair(R.attr.colorKühlregalMilch, R.attr.colorKühlregalMilchL)
                "Kf" -> Pair(R.attr.colorKühlregalFleisch, R.attr.colorKühlregalFleischL)
                "Ve" -> Pair(R.attr.colorVegan, R.attr.colorVeganL)
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
                    myActivity.colorForAttr(gradientPair.second),
                    myActivity.colorForAttr(gradientPair.first)
                )
            )

            //round corners if setting says so
            if (round) myGradientDrawable.cornerRadii = floatArrayOf(cr, cr, cr, cr, cr, cr, cr, cr)

            //set category background
            holder.binding.cvCategory.background = myGradientDrawable

            //set text colors to white
            holder.binding.tvCategoryName.setTextColor(colorCategory)
            holder.binding.tvNumberOfItems.setTextColor(colorOnBackground)

            //display number of unchecked items
            holder.binding.tvNumberOfItems.text = numberOfItems.toString()

            //hide checkMark
            holder.binding.ivCheckMark.visibility = View.GONE
        } else {
            //get title color
            val colorTitle =
                myActivity.colorForAttr(R.attr.colorCheckedCategoryTitle)


            //create gradient drawable for checked category background
            val myGradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    myActivity.colorForAttr(R.attr.colorGrayL),
                    myActivity.colorForAttr(R.attr.colorGray)
                )
            )

            //round corners if setting says so
            if (round) myGradientDrawable.cornerRadii = floatArrayOf(cr, cr, cr, cr, cr, cr, cr, cr)

            //set background for checked category
            holder.binding.cvCategory.background = myGradientDrawable

            //hint text color for checked category
            holder.binding.tvCategoryName.setTextColor(colorTitle)

            //clear text displaying number of items
            holder.binding.tvNumberOfItems.text = ""

            //show checkMark
            holder.binding.ivCheckMark.visibility = View.VISIBLE
        }
    }

    /**
     * Returns amount of categories
     */
    override fun getItemCount(): Int {
        return myFragment.shoppingListInstance.size
    }

    /**
     * one instance of this class will contain one instance of row_category and meta data like
     * position also holds references to views inside the layout
     */
    class CategoryViewHolder(rowCategoryBinding: RowCategoryBinding) :
        RecyclerView.ViewHolder(rowCategoryBinding.root) {
        lateinit var tag: String
        val binding = rowCategoryBinding
    }
}

/**
 * Adapter for items in the sublists
 */
class SublistAdapter(
    private val tag: String,
    private val parentHolder: ShoppingListAdapter.CategoryViewHolder,
    mainActivity: MainActivity,
    shoppingFr: ShoppingFr
) : RecyclerView.Adapter<SublistAdapter.ItemViewHolder>() {
    private val myActivity = mainActivity
    private val myFragment = shoppingFr
    private val density = myActivity.resources.displayMetrics.density

    //boolean stating if design is round or not
    private val round = SettingsManager.getSetting(SettingId.SHAPES_ROUND) as Boolean

    //corner radius of items
    private val cr = myActivity.resources.getDimension(R.dimen.cornerRadius)

    //setting if checked sublists should be moved below unchecked sublists
    private val moveCheckedSublistsDown =
        SettingsManager.getSetting(SettingId.MOVE_CHECKED_DOWN) as Boolean

    private lateinit var holder : ItemViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val rowItemBinding =
            RowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        holder = ItemViewHolder(rowItemBinding)
        return holder
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        //longClickListener on item to ensure shake animation for category
        holder.binding.root.setOnLongClickListener {
            if (myFragment.myMultiShoppingFr.searching) return@setOnLongClickListener true
            val animationShake =
                AnimationUtils.loadAnimation(myActivity, R.anim.shake_small)
            parentHolder.binding.root.startAnimation(animationShake)
            true
        }


        //get shopping item
        val item = myFragment.shoppingListInstance.getItem(tag, position)!!

        if (myFragment.query != null && !myFragment.getItemVisibility(item)) {
            holder.binding.root.layoutParams.height = 0
            val params = holder.binding.root.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(0, 0, 0, 0)
            return
        }
        holder.binding.root.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        val params = holder.binding.root.layoutParams as ViewGroup.MarginLayoutParams
        val margin = (density * 4).toInt()
        params.setMargins(margin, margin, margin, margin)
        //manage onClickListener to edit item
        holder.binding.root.setOnClickListener {
            if(myFragment.shoppingListInstance.isLocked()) return@setOnClickListener

            myFragment.myMultiShoppingFr.editTag = tag
            myFragment.myMultiShoppingFr.editPos = position
            myFragment.myMultiShoppingFr.openEditItemDialog(item)
        }

        //set tag of surrounding category for holder
        holder.tag = tag

        //initialize checkbox
        holder.binding.cbItem.isChecked = item.checked

        //initialize text
        holder.binding.tvItemTitle.text = when (item.amount == "") {
            true -> item.name
            else ->
                myActivity.getString(
                    R.string.shoppingItemTitle, item.amount, item.unit, item.name
                )
        }

        //background drawable for item
        val myGradientDrawable: GradientDrawable

        //initialize text / background color and strike through flag
        if (item.checked) {
            holder.binding.tvItemTitle.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            holder.binding.tvItemTitle
                .setTextColor(
                    myActivity.colorForAttr(R.attr.colorHint)
                )

            myGradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    myActivity.colorForAttr(R.attr.colorGrayD),
                    myActivity.colorForAttr(R.attr.colorGrayD)
                )
            )

        } else {
            //white and no strike through otherwise
            holder.binding.tvItemTitle.paintFlags = 0
            holder.binding.tvItemTitle
                .setTextColor(
                    myActivity.colorForAttr(R.attr.colorOnBackGround)
                )
            myGradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    myActivity.colorForAttr(R.attr.colorBackground),
                    myActivity.colorForAttr(R.attr.colorBackground)
                )
            )

        }

        //round corners if setting says so
        if (round) myGradientDrawable.cornerRadii = floatArrayOf(cr, cr, cr, cr, cr, cr, cr, cr)

        //set background of item
        holder.itemView.background = myGradientDrawable


        //Onclick Listener for checkBox
        holder.binding.clItemTapfield.setOnClickListener {
            val oldItem = myFragment.shoppingListInstance.getItem(tag, holder.bindingAdapterPosition)
            val newItem = oldItem?.copy().let { it?.checked = !it.checked; it }

            if (myFragment.shoppingListInstance.isLocked()) return@setOnClickListener
            if (myFragment.shoppingListInstance.isSyncModeEnabled()) {
                myFragment.myMultiShoppingFr.updateSyncedItem(oldItem!!, newItem!!)
                return@setOnClickListener
            }
            updateCheckedState()
        }

        holder.binding.clItemTapfield.setOnLongClickListener {
            if (myFragment.myMultiShoppingFr.searching) return@setOnLongClickListener true
            val animationShake =
                AnimationUtils.loadAnimation(myActivity, R.anim.shake_small)
            parentHolder.itemView.startAnimation(animationShake)
            true
        }
    }

    fun updateCheckedState() {
        //flip checkedState of item and save new position (flipItemCheckedState sorts list and returns new position)
        val newPosition = myFragment.shoppingListInstance.flipItemCheckedState(
            tag,
            holder.bindingAdapterPosition
        )

        //get number of uncheckedItems in current sublist
        val numberOfItems = myFragment.shoppingListInstance.getUncheckedSize(holder.tag)

        //If all are checked after the current item got flipped, the list has to go from color to gray
        myFragment.myAdapter.manageCheckedCategory(
            parentHolder,
            myFragment.shoppingListInstance.areAllChecked(holder.tag),
            numberOfItems,
            holder.tag
        )

        //If setting says to collapse checked sublists, and current sublist is fully checked,
        //collapse it and notify item change
        if (ShoppingFr.collapseCheckedSublists && myFragment.shoppingListInstance.areAllChecked(
                holder.tag
            )
        ) {
            myFragment.shoppingListInstance.flipExpansionState(holder.tag)
            myFragment.myAdapter.notifyItemChanged(parentHolder.bindingAdapterPosition)
        }

        notifyItemChanged(holder.bindingAdapterPosition)


        if (newPosition > -1) {
            notifyItemMoved(holder.bindingAdapterPosition, newPosition)
        }

        //if the setting moveCheckedSublistsDown is true, sort categories by their checked state
        //and animate the move from old to new position
        if (moveCheckedSublistsDown) {
            val sublistMoveInfo = myFragment.shoppingListInstance.sortCategoriesByChecked(tag)
            if (sublistMoveInfo != null) {
                myFragment.prepareForMove()
                myFragment.myAdapter
                    .notifyItemMoved(sublistMoveInfo.first, sublistMoveInfo.second)

                myFragment.reactToMove()
            }

        }
        myFragment.myMultiShoppingFr.updateShoppingMenu()
    }

    override fun getItemCount(): Int {
        return myFragment.shoppingListInstance.getSublistLength(tag)
    }

    /**
    one instance of this class will contain one instance of row_item and meta data like position
    also holds references to views inside the layout
     */
    class ItemViewHolder(rowItemBinding: RowItemBinding) :
        RecyclerView.ViewHolder(rowItemBinding.root) {

        lateinit var tag: String
        var binding = rowItemBinding
    }
}

/**
 * ItemTouchHelper to support swipe to delete of shopping items
 */
class SwipeItemToDelete(direction: Int, shoppingFr: ShoppingFr) :
    ItemTouchHelper.SimpleCallback(0, direction) {

    private val myFragment = shoppingFr

    override fun onMove(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target:
        RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun getSwipeDirs(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        if (myFragment.shoppingListInstance.isLocked()) {
            return 0
        }

        return super.getSwipeDirs(recyclerView, viewHolder)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        //position of item in sublist
        val position = viewHolder.bindingAdapterPosition

        //ViewHolder as ItemViewHolder
        val parsed = viewHolder as SublistAdapter.ItemViewHolder

        //position of category in shoppingList
        val tagPosition = myFragment.shoppingListInstance.getTagIndex(parsed.tag)

        //Check if before the swipe, all items were checked (deleting an item from a fully checked category), so we don't collapse it then
        val previouslyAllChecked = myFragment.shoppingListInstance.areAllChecked(parsed.tag)

        //Pair of deleted item and boolean stating if sublist is empty now
        val removeInfo = myFragment.shoppingListInstance.removeItem(parsed.tag, position)

        if (myFragment.shoppingListInstance.isSyncModeEnabled() && removeInfo.first != null) {
            myFragment.myMultiShoppingFr.deleteSyncedItem(removeInfo.first!!)
        }

        if (removeInfo.second) {
            //entire sublist is empty => remove sublist
            myFragment.myAdapter
                .notifyItemRemoved(tagPosition)
        } else {
            //sublist changed length =>

            if (ShoppingFr.collapseCheckedSublists && myFragment.shoppingListInstance.areAllChecked(
                    parsed.tag
                ) && !previouslyAllChecked
            ) {
                myFragment.shoppingListInstance.flipExpansionState(parsed.tag)
            }

            myFragment.myAdapter.notifyItemChanged(tagPosition)

            //check if sublist moved
            val positions = myFragment.shoppingListInstance.sortCategoriesByChecked(parsed.tag)

            if (positions != null) {
                //sublist did move => animate movement
                myFragment.prepareForMove()
                myFragment.myAdapter.notifyItemMoved(
                    positions.first, positions.second
                )
                myFragment.reactToMove()
            }
        }

        //cache deleted item to allow undo
        myFragment.myMultiShoppingFr.activeDeletedItems.add(removeInfo.first)

        //update options menu
        myFragment.myMultiShoppingFr.updateShoppingMenu()

    }
}

