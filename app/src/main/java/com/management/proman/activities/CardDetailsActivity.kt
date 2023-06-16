package com.management.proman.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.management.proman.R
import com.management.proman.adapters.CardMemberListItemsAdapter
import com.management.proman.dialogs.LabelColorListDialog
import com.management.proman.dialogs.MembersListDialog
import com.management.proman.firebase.FirestoreClass
import com.management.proman.models.*
import com.management.proman.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {

    private lateinit var mBoardDetails: Board
    private lateinit var mMembersDetailList: ArrayList<User>
    private var mTaskListPosition = -1
    private var mCardPosition = -1
    private var mSelectedColor = ""
    private var mSelectedDueDateMilliSeconds: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)

        getIntentData()
        setupActionBar()

        val etNameCardDetails = findViewById<EditText>(R.id.et_name_card_details)
        etNameCardDetails.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)

        mSelectedColor = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].labelColor
        if (mSelectedColor.isNotEmpty()) {
            setColor()
        }

        val btnUpdateCardDetails = findViewById<Button>(R.id.btn_update_card_details)
        btnUpdateCardDetails.setOnClickListener {
            if (etNameCardDetails.text.isNotEmpty()) {
                updateCardDetails()
            }
            else {
                Toast.makeText(this, "Please enter a card name!", Toast.LENGTH_LONG).show()
            }
        }

        val tvSelectLabelColor = findViewById<TextView>(R.id.tv_select_label_color)
        tvSelectLabelColor.setOnClickListener {
            labelColorListDialog()
        }

        setUpSelectedMembersList()

        mSelectedDueDateMilliSeconds = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].dueDate

        if (mSelectedDueDateMilliSeconds > 0) {
            val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            val selectedDate = simpleDateFormat.format(Date(mSelectedDueDateMilliSeconds))
            val tvSelectDueDate = findViewById<TextView>(R.id.tv_select_due_date)
            tvSelectDueDate.text = selectedDate
        }

        val tvSelectMembers = findViewById<TextView>(R.id.tv_select_members)
        tvSelectMembers.setOnClickListener {
            membersListDialog()
        }

        val tvSelectDueDate = findViewById<TextView>(R.id.tv_select_due_date)
        tvSelectDueDate.setOnClickListener {
            showDataPicker()
        }
    }

    private fun setupActionBar() {
        val toolbarCardDetailsActivity = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_card_details_activity)
        setSupportActionBar(toolbarCardDetailsActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24)
            actionBar.title = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name
        }

        toolbarCardDetailsActivity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun getIntentData() {
        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)) {
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)) {
            mCardPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)) {
            mMembersDetailList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun updateCardDetails() {

        val etNameCardDetails = findViewById<EditText>(R.id.et_name_card_details)

        val card = Card(etNameCardDetails.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo,
            mSelectedColor, mSelectedDueDateMilliSeconds)

        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)

        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition] = card

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    private fun deleteCard() {
        val cardsList: ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards

        cardsList.removeAt(mCardPosition)

        val taskList: ArrayList<Task> = mBoardDetails.taskList

        taskList.removeAt(taskList.size - 1)

        taskList[mTaskListPosition].cards = cardsList

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete_card -> {
                //TODO add alert dialog before deleting
                deleteCard()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun colorsList(): ArrayList<String> {
        val colorsList: ArrayList<String> = ArrayList()

        colorsList.add("#4169E1")    // Royal Blue
        colorsList.add("#008000")    // Emerald Green
        colorsList.add("#FF7F50")    // Coral Pink
        colorsList.add("#E6E6FA")    // Lavender
        colorsList.add("#DAA520")    // Goldenrod
        colorsList.add("#008080")    // Teal Blue
        colorsList.add("#DA70D6")    // Orchid

        return colorsList
    }

    private fun setColor() {
        val tvSelectLabelColor = findViewById<TextView>(R.id.tv_select_label_color)
        tvSelectLabelColor.text = ""
        tvSelectLabelColor.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    private fun labelColorListDialog() {
        val colorsList: ArrayList<String> = colorsList()

        val listDialog = object : LabelColorListDialog (
            this,
            colorsList,
            resources.getString(R.string.str_select_label_color),
            mSelectedColor) {
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }

        listDialog.show()
    }

    private fun membersListDialog() {
        var cardAssignedMembersList = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        if (cardAssignedMembersList.size > 0) {
            for (i in mMembersDetailList.indices) {
                for (j in cardAssignedMembersList) {
                    if (mMembersDetailList[i].id == j) {
                        mMembersDetailList[i].selected = true
                    }
                }
            }
        }
        else {
            for (i in mMembersDetailList.indices) {
                mMembersDetailList[i].selected = false
            }
        }

        val listDialog = object : MembersListDialog(this, mMembersDetailList, resources.getString(R.string.select_members)) {
            override fun onItemSelected(user: User, action: String) {
                if (action == Constants.SELECT) {
                    if (!mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.contains(user.id)) {
                        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.add(user.id)
                    }
                }
                else {
                    mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.remove(user.id)
                    for (i in mMembersDetailList.indices) {
                        if (mMembersDetailList[i].id == user.id) {
                            mMembersDetailList[i].selected = false
                        }
                    }
                }
                setUpSelectedMembersList()
            }
        }.show()
    }

    private fun setUpSelectedMembersList() {
        val cardAssignedMembersList = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

        for (i in mMembersDetailList.indices) {
            for (j in cardAssignedMembersList) {
                if (mMembersDetailList[i].id == j) {
                    val selectedMember = SelectedMembers(mMembersDetailList[i].id, mMembersDetailList[i].image)
                    selectedMembersList.add(selectedMember)
                }
            }
        }

        if (selectedMembersList.size > 0) {
            selectedMembersList.add(SelectedMembers("",""))

            val tvSelectedMembers = findViewById<TextView>(R.id.tv_select_members)
            tvSelectedMembers.visibility = View.GONE

            val rvSelectedMemberList = findViewById<RecyclerView>(R.id.rv_selected_members_list)
            rvSelectedMemberList.visibility = View.VISIBLE
            rvSelectedMemberList.layoutManager = GridLayoutManager(this, 6)
            val adapter = CardMemberListItemsAdapter(this, selectedMembersList, true)
            rvSelectedMemberList.adapter = adapter

            adapter.setOnClickListener(
                object : CardMemberListItemsAdapter.OnClickListener {
                    override fun onClick() {
                        membersListDialog()
                    }
                }
            )
        }
        else {
            val tvSelectedMembers = findViewById<TextView>(R.id.tv_select_members)
            tvSelectedMembers.visibility = View.VISIBLE

            val rvSelectedMemberList = findViewById<RecyclerView>(R.id.rv_selected_members_list)
            rvSelectedMemberList.visibility = View.GONE
        }
    }
    private fun showDataPicker() {

        val c = Calendar.getInstance()
        val year =
            c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->

                val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"

                val sMonthOfYear =
                    if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"

                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"

                val tvSelectDueDate = findViewById<TextView>(R.id.tv_select_due_date)
                tvSelectDueDate.text = selectedDate


                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

                val theDate = sdf.parse(selectedDate)

                mSelectedDueDateMilliSeconds = theDate!!.time
            },
            year,
            month,
            day
        )
        dpd.show()

    }
}