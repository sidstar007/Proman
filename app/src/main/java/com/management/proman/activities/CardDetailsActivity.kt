package com.management.proman.activities

import android.app.Activity
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.management.proman.R
import com.management.proman.dialogs.LabelColorListDialog
import com.management.proman.firebase.FirestoreClass
import com.management.proman.models.Board
import com.management.proman.models.Card
import com.management.proman.models.Task
import com.management.proman.models.User
import com.management.proman.utils.Constants

class CardDetailsActivity : BaseActivity() {

    private lateinit var mBoardDetails: Board
    private lateinit var mMembersDetailList: ArrayList<User>
    private var mTaskListPosition = -1
    private var mCardPosition = -1
    private var mSelectedColor = ""

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
            mSelectedColor)

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
}