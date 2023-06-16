package com.management.proman.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
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
import com.management.proman.models.User
import com.management.proman.utils.Constants
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageURL: String = ""
    private lateinit var mUserDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setupActionBar()

        FirestoreClass().loadUserData(this)

        val userImage = findViewById<ImageView>(R.id.iv_profile_user_image)
        userImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this@MyProfileActivity)
            }
            else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        val btnUpdateInfo = findViewById<Button>(R.id.btn_update)
        btnUpdateInfo.setOnClickListener {
            if (mSelectedImageFileUri != null) {
                uploadUserImage()
            }
            else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this)
            }
        }
        else {
            Toast.makeText(this, "Storage permission denied by user, please visit settings to give permission.",Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null) {
            mSelectedImageFileUri = data.data

            try {
                val userImage = findViewById<ImageView>(R.id.iv_profile_user_image)
                Glide
                    .with(this@MyProfileActivity)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(userImage)
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun setupActionBar() {
        val toolbarMyProfileActivity = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_my_profile_activity)
        setSupportActionBar(toolbarMyProfileActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24)
            actionBar.title = resources.getString(R.string.my_profile_title)
        }

        toolbarMyProfileActivity.setNavigationOnClickListener { onBackPressed() }
    }

    fun setUserDataInUI(user: User) {

        mUserDetails = user

        val userImage = findViewById<ImageView>(R.id.iv_profile_user_image)
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(userImage)

        val etName = findViewById<EditText>(R.id.et_name_profile)
        val etEmail = findViewById<EditText>(R.id.et_email_profile)
        val etMobile = findViewById<EditText>(R.id.et_mobile_profile)

        etName.setText(user.name)
        etEmail.setText(user.email)
        if (user.mobile != 0L) {
            etMobile.setText(user.mobile.toString())
        }
    }

    private fun uploadUserImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImageFileUri != null) {
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child("USER_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(this,
                mSelectedImageFileUri!!
            ))
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapShot ->
                    Log.e("Firebase Image URL", taskSnapShot.metadata!!.reference!!.downloadUrl.toString())

                taskSnapShot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri -> Log.e("Downloadable Image URL", uri.toString())
                    mProfileImageURL = uri.toString()

                    updateUserProfileData()
                }
            }.addOnFailureListener {
                exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun profileUpdateSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()

    }

    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()

        var changesMade: Boolean = false

        val etName = findViewById<EditText>(R.id.et_name_profile)
        val etMobile = findViewById<EditText>(R.id.et_mobile_profile)

        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image) {
            userHashMap[Constants.IMAGE] = mProfileImageURL
            changesMade = true
        }
        if (etName.text.toString().isNotEmpty() && etName.text.toString() != mUserDetails.name) {
            userHashMap[Constants.NAME] = etName.text.toString()
            changesMade = true
        }
        if (etMobile.text.toString().isNotEmpty() && etMobile.text.toString() != mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = etMobile.text.toString().toLong()
            changesMade = true
        }

        if (changesMade) {
            FirestoreClass().updateUserProfileData(this, userHashMap)
        }

    }
}