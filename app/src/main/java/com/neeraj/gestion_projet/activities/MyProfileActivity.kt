package com.neeraj.gestion_projet.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.*
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.neeraj.gestion_projet.R
import com.neeraj.gestion_projet.firebase.FirestoreClass
import com.neeraj.gestion_projet.models.User
import com.neeraj.gestion_projet.utils.Constants
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.nav_header_main.*
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    private var mSelectedImageUri: Uri?=null
    private lateinit var mUserDetails: User
    private var mProfileImageURL:String =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setUpActionBar()

        FirestoreClass().loadUserData(this);

        iv_profile_user_image.setOnClickListener {

            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)
            {
                Constants.showImageChooser(this)
            }
            else
            {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),Constants.READ_STORAGE_PERMISSION)
            }
        }
        btn_update.setOnClickListener {
            if(mSelectedImageUri!=null)
            {
                uploadUserImage()
            }
            else
            {
                showProgressDialog("Please Wait!")
                updateUserProfile()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==Constants.READ_STORAGE_PERMISSION)
        {
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                Constants.showImageChooser(this)
            }
            else
            {
                Toast.makeText(this,"Please grant the storage permission in order to proceed!",Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK && requestCode==Constants.PICK_IMAGE_PERMISSION && data!!.data!=null)
            mSelectedImageUri=data.data

        try {
            Glide
                .with(this@MyProfileActivity)
                .load(mSelectedImageUri)
                .fitCenter()
                .placeholder(R.drawable.ic_user)
                .into(iv_profile_user_image)
        }catch (e:IOException){
            e.printStackTrace()
        }
    }

    private fun setUpActionBar()
    {
        setSupportActionBar(toolbar_my_profile)
        val actionBar=supportActionBar
        if(actionBar!=null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_colour_back)
            actionBar.title="My Profile"
        }
        toolbar_my_profile.setNavigationOnClickListener{
            onBackPressed()
        }
    }

    private fun uploadUserImage()
    {
        showProgressDialog("Please wait while we upload the image")
        if(mSelectedImageUri!=null)
        {
            val sref: StorageReference =FirebaseStorage.getInstance().reference.child("userImage"+System.currentTimeMillis()+"."+Constants.getFileExtension(this,mSelectedImageUri))
            sref.putFile(mSelectedImageUri!!).addOnSuccessListener {
                taskSnapshot->
                Log.i("Firebase Image URL",taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri->
                    mProfileImageURL=uri.toString()
                    updateUserProfile()
                }
            }.addOnFailureListener {
                exception->
                Toast.makeText(this,exception.message,Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }
    }

    fun setUserData(user:User)
    {
        mUserDetails=user

        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .fitCenter()
            .placeholder(R.drawable.ic_user)
            .into(iv_profile_user_image)

        et_name.setText(user.name)
        et_email.setText(user.email)
        if(user.phone!=0L)
        {
            et_mobile.setText(user.phone.toString())
        }
    }

    fun updateUserProfile(){
        val userHashMap=HashMap<String,Any>()

        var anyChangesMade=false

        if(mProfileImageURL.isNotEmpty() && mProfileImageURL!=mUserDetails.image)
        {
            userHashMap[Constants.image]=mProfileImageURL
            anyChangesMade=true
        }
        if(et_name.text.toString()!=mUserDetails.name)
        {
            userHashMap[Constants.NAME]=et_name.text.toString()
            anyChangesMade=true
        }
        if(et_mobile.text.toString()!=mUserDetails.phone.toString())
        {
            userHashMap[Constants.Mobile]=et_mobile.text.toString().toLong()
            anyChangesMade=true
        }
        if(anyChangesMade==true)
            FirestoreClass().updateUserData(this,userHashMap)
    }

    fun profileUpdateSuccess()
    {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}