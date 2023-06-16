package com.management.proman.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.management.proman.R
import com.management.proman.adapters.BoardItemAdapter
import com.management.proman.firebase.FirestoreClass
import com.management.proman.models.Board
import com.management.proman.models.User
import com.management.proman.utils.Constants
import java.text.FieldPosition
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        const val MY_PROFILE_REQUEST_CODE: Int = 11
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }

    private lateinit var mUserName: String
    private lateinit var mSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBar()

        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)

        mSharedPreferences = this.getSharedPreferences(Constants.PROMAN_PREFERENCES, Context.MODE_PRIVATE)

        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        if (tokenUpdated) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().loadUserData(this, true)
        }
        else {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener {
                    token ->
                    updateFCMToken(token)
                }
        }

        FirestoreClass().loadUserData(this, true)

        val fabAdd = findViewById<FloatingActionButton>(R.id.fab_create_board)
        fabAdd.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME,mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }
    }

    private fun setupActionBar() {
        val toolbarMainActivity = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_main_activity)
        setSupportActionBar(toolbarMainActivity)
        toolbarMainActivity.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbarMainActivity.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        else {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        else {
            doubleBackToExit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                startActivityForResult(Intent(this, MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE)
            }

            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                mSharedPreferences.edit().clear().apply()

                Toast.makeText(this, "Signed Out", Toast.LENGTH_LONG).show()
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(Intent(this, IntroActivity::class.java))
                finish()
            }
        }

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)

        return true
    }

    fun updateNavigationUserDetails(user: User, readBoardsList: Boolean) {
        hideProgressDialog()
        mUserName = user.name

        val userImage = findViewById<ImageView>(R.id.nav_user_image)

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(userImage)

        val tvUserName = findViewById<TextView>(R.id.tv_username)
        tvUserName.text = user.name

        if (readBoardsList) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE) {
            FirestoreClass().loadUserData(this)
        }
        else if (resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE) {
            FirestoreClass().getBoardsList(this)
        }
        else {
            Log.e("Cancelled", "Cancelled")
        }
    }

    fun populateBoardsListToUI(boardsList: ArrayList<Board>) {
        hideProgressDialog()

        if (boardsList.size > 0) {
            val rvBoardsList = findViewById<RecyclerView>(R.id.rv_boards_list)
            val tvNoBoardsAvailable = findViewById<TextView>(R.id.tv_no_boards_available)

            rvBoardsList.visibility = View.VISIBLE
            tvNoBoardsAvailable.visibility = View.GONE

            rvBoardsList.layoutManager = LinearLayoutManager(this)
            rvBoardsList.setHasFixedSize(true)

            val adapter = BoardItemAdapter(this, boardsList)

            rvBoardsList.adapter = adapter

            adapter.setOnClickListener(object : BoardItemAdapter.OnClickListener {
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })
        }
        else {
            val rvBoardsList = findViewById<RecyclerView>(R.id.rv_boards_list)
            val tvNoBoardsAvailable = findViewById<TextView>(R.id.tv_no_boards_available)

            rvBoardsList.visibility = View.GONE
            tvNoBoardsAvailable.visibility = View.VISIBLE
        }
    }

    fun tokenUpdateSuccess() {
        hideProgressDialog()
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().loadUserData(this, true)
    }

    private fun updateFCMToken(token: String) {
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().updateUserProfileData(this, userHashMap)
    }
}