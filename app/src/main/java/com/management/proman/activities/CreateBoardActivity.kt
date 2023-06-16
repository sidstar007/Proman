package com.management.proman.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.management.proman.R
import com.management.proman.firebase.FirestoreClass
import com.management.proman.models.Board
import com.management.proman.utils.Constants
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    private var mSelectedImageFileUri: Uri? = null

    private lateinit var mUserName: String

    private var mBoardImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)

        setupActionBar()

        if (intent.hasExtra(Constants.NAME)) {
            mUserName = intent.getStringExtra(Constants.NAME).toString()
        }
        val userImage = findViewById<ImageView>(R.id.iv_board_image)
        userImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Constants.showImageChooser(this)
            }
            else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        val btnCreate = findViewById<Button>(R.id.btn_create)
        btnCreate.setOnClickListener {
            if (mSelectedImageFileUri != null) {
                uploadBoardImage()
            }
            else {
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }
    }

    private fun setupActionBar() {
        val toolbarCreateBoardActivity = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_create_board_activity)
        setSupportActionBar(toolbarCreateBoardActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24)
            actionBar.title = resources.getString(R.string.create_board_title)
        }

        toolbarCreateBoardActivity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this)
            }
        }
        else {
            Toast.makeText(this, "Storage permission denied by user, please visit settings to give permission.",
                Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null) {
            mSelectedImageFileUri = data.data

            try {
                val userImage = findViewById<ImageView>(R.id.iv_board_image)
                Glide
                    .with(this)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(userImage)
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun boardCreatedSuccessfully() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun uploadBoardImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImageFileUri != null) {
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child("BOARD_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(this,
                mSelectedImageFileUri!!
            ))
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                    taskSnapShot ->
                Log.e("Firebase Image URL", taskSnapShot.metadata!!.reference!!.downloadUrl.toString())

                taskSnapShot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri -> Log.e("Downloadable Image URL", uri.toString())
                    mBoardImageURL = uri.toString()

                    createBoard()
                }
            }.addOnFailureListener {
                    exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createBoard() {
        val assignedUsersArrayList : ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserID())

        val etBoardName = findViewById<EditText>(R.id.et_board_name)
        var board = Board(etBoardName.text.toString(), mBoardImageURL, mUserName, assignedUsersArrayList)

        FirestoreClass().createBoard(this, board)
    }
}