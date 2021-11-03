package com.neeraj.gestion_projet.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.database.collection.LLRBNode
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.neeraj.gestion_projet.R
import com.neeraj.gestion_projet.firebase.FirestoreClass
import com.neeraj.gestion_projet.models.Board
import com.neeraj.gestion_projet.utils.Constants
import kotlinx.android.synthetic.main.activity_create_board.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    private var mSelectedImageUri:Uri?=null

    lateinit var mUsername:String

    private var mBoardImageUrl:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)
        setUpActionBar()

        if(intent.hasExtra(Constants.NAME)) {
            mUsername = intent.getStringExtra(Constants.NAME)!!
        }

        iv_board_image.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
            {
                Constants.showImageChooser(this)
            }
            else
            {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),Constants.READ_STORAGE_PERMISSION)
            }
        }
        btn_create.setOnClickListener {
            if(mSelectedImageUri!=null){
                uploadBoardImage()
            }
            else
            {
                showProgressDialog("Please Wait!")
                createBoard()
            }
        }
    }

    private fun createBoard(){
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUID())

        var board=Board(et_board_name.text.toString(),mBoardImageUrl,mUsername,assignedUsersArrayList)

        FirestoreClass().createBoard(this,board)
    }

    private fun uploadBoardImage(){
        showProgressDialog("Please Wait!")

        val sref: StorageReference = FirebaseStorage.getInstance().reference.child("board_image"+System.currentTimeMillis()+"."+Constants.getFileExtension(this,mSelectedImageUri))
            sref.putFile(mSelectedImageUri!!).addOnSuccessListener {
                    taskSnapshot->
                Log.i("Firebase Image URL",taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri->
                    mBoardImageUrl=uri.toString()
                    createBoard()
                }
            }.addOnFailureListener {
                    exception->
                Toast.makeText(this,exception.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
    }

    fun boardCreated()
    {
        setResult(Activity.RESULT_OK)
        hideProgressDialog()
        finish()
    }

    private fun setUpActionBar()
    {
        setSupportActionBar(toolbar_create_board_activity)
        val actionBar=supportActionBar
        if(actionBar!=null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_colour_back)
            actionBar.title="Create Board"
//            actionBar.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.black)))
        }
        toolbar_create_board_activity.setNavigationOnClickListener{
            onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK && requestCode== Constants.PICK_IMAGE_PERMISSION && data!!.data!=null)
            mSelectedImageUri=data.data

        try {
            Glide
                .with(this)
                .load(mSelectedImageUri)
                .fitCenter()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(iv_board_image)
        }catch (e: IOException){
            e.printStackTrace()
        }
    }
}