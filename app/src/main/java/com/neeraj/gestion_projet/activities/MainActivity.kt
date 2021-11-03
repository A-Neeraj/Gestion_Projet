package com.neeraj.gestion_projet.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.JsonToken
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.neeraj.gestion_projet.R
import com.neeraj.gestion_projet.adapters.BoardItemsAdapter
import com.neeraj.gestion_projet.firebase.FirestoreClass
import com.neeraj.gestion_projet.models.Board
import com.neeraj.gestion_projet.models.User
import com.neeraj.gestion_projet.utils.Constants
import com.neeraj.gestion_projet.activities.TaskListActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.main_content.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var mGoogleSignInClient: GoogleSignInClient

    lateinit var mUserName:String

    private lateinit var mSharedPreferences: SharedPreferences

    companion object{
        const val MY_PROFILE_REQ_CODE=11
        const val CREATE_BOARD_REQ_CODE=12
    }

    private val auth by lazy {
        FirebaseAuth.getInstance()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        setUpActionBar()

        nav_view.setNavigationItemSelectedListener(this)

        mSharedPreferences=this.getSharedPreferences(Constants.GESTIONPROJET_PREFS, Context.MODE_PRIVATE)
        val tokenUpdated=mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED,false)

        if(tokenUpdated){
            showProgressDialog("Please Wait")
            FirestoreClass().loadUserData(this,true)
        }else{
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(this@MainActivity){
                instanceIdResult->
                updateFCMToken(instanceIdResult.token)
            }
        }

        val user=User(FirebaseAuth.getInstance().uid.toString(),FirebaseAuth.getInstance().currentUser?.displayName.toString(),FirebaseAuth.getInstance().currentUser?.email.toString())
        FirestoreClass().registerUser(this,user)

        fab_create_board.setOnClickListener{
            val intent=Intent(this,CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME,mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQ_CODE)
        }


    }

    fun populateBoards(boardsList:ArrayList<Board>){
        hideProgressDialog()
        if(boardsList.size>0){
            rv_boards_list.visibility= View.VISIBLE
            tv_no_boards.visibility=View.GONE

            rv_boards_list.layoutManager=LinearLayoutManager(this)
            rv_boards_list.setHasFixedSize(true)

            val adapter=BoardItemsAdapter(this,boardsList)
            rv_boards_list.adapter=adapter

            adapter.setOnClickListener(object: BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent=Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID,model.docId)
                    startActivity(intent)
                }
            })
        }
        else
        {
            rv_boards_list.visibility=View.GONE
            tv_no_boards.visibility=View.VISIBLE
        }
    }

    private fun setUpActionBar()
    {
        setSupportActionBar(toolbar_main_activity)
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbar_main_activity.setNavigationOnClickListener {
            toggleDrawer()
        }
    }
    private fun toggleDrawer(){
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
        else
            drawerLayout.openDrawer(GravityCompat.START)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
        else
            doubleBackToExit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK && requestCode== MY_PROFILE_REQ_CODE)
        {
            FirestoreClass().loadUserData(this,true)
//            Log.e("Data",)
        }else if(resultCode==Activity.RESULT_OK && requestCode== CREATE_BOARD_REQ_CODE)
        {
            FirestoreClass().getBoardsList(this)
        }
        else
        {
            Log.e("Cancelled","Cancelled")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_myprofile->{
                startActivityForResult(Intent(this,MyProfileActivity::class.java),MY_PROFILE_REQ_CODE)
            }
            R.id.sign_out-> {
                mGoogleSignInClient.signOut().addOnCompleteListener {
                    val intent = Intent(this, IntroActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//                    Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                    finish()
                }
                mSharedPreferences.edit().clear().apply()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)

        return true
    }
    fun updateNavigationUserDetails(user: User,readBoardsList:Boolean){
        hideProgressDialog()
        Glide
            .with(this)
            .load(user.image)
            .fitCenter()
            .placeholder(R.drawable.ic_user)
            .into(user_image);
        mUserName=user.name
        tv_username.text=user.name

        if(readBoardsList==true)
        {
            showProgressDialog("Please Wait")
            FirestoreClass().getBoardsList(this)
        }
    }

    fun tokenUpdateSuccess(){
        hideProgressDialog()
        val editor: SharedPreferences.Editor=mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED,true)
        editor.apply()
        showProgressDialog("Please Wait!")
        FirestoreClass().loadUserData(this,true)
    }

    fun updateFCMToken(token: String){
        val userHashMap=HashMap<String,Any>()
        userHashMap[Constants.FCM_TOKEN] = token
        showProgressDialog("Please Wait!")
        FirestoreClass().updateUserData(this,userHashMap)
    }
}
