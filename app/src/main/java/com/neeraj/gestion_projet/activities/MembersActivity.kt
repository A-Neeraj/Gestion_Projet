package com.neeraj.gestion_projet.activities

import android.app.Activity
import android.app.Dialog
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.tools.build.jetifier.core.utils.Log
import com.neeraj.gestion_projet.R
import com.neeraj.gestion_projet.adapters.MembersListItemAdapter
import com.neeraj.gestion_projet.firebase.FirestoreClass
import com.neeraj.gestion_projet.models.Board
import com.neeraj.gestion_projet.models.User
import com.neeraj.gestion_projet.utils.Constants
import kotlinx.android.synthetic.main.activity_members.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.dialog_search_member.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder
import java.net.ContentHandler
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.sql.Connection

class MembersActivity : BaseActivity() {

    lateinit var mBoardDetails: Board
    lateinit var mAssignedMembersList: ArrayList<User>
    private var anyChangesMade:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)

        if(intent.hasExtra(Constants.BOARD_DETAIL))
        {
            mBoardDetails= intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }
        setUpActionBar()

        showProgressDialog("Please Wait!")
        FirestoreClass().getAssignedMembers(this,mBoardDetails.assignedTo)
    }

    fun setUpMembersList(list: ArrayList<User>)
    {
        mAssignedMembersList=list
        hideProgressDialog()

        rv_members_list.layoutManager=LinearLayoutManager(this@MembersActivity)
        rv_members_list.setHasFixedSize(true)

        val adapter=MembersListItemAdapter(this@MembersActivity,list)
        rv_members_list.adapter=adapter
    }

    fun memberDetails(user: User)
    {
        mBoardDetails.assignedTo.add(user.id)
        FirestoreClass().assignMemberToBoard(this,mBoardDetails,user)
    }

    private fun setUpActionBar()
    {
        setSupportActionBar(toolbar_members_activity)
        val actionBar=supportActionBar
        if(actionBar!=null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_colour_back)
            actionBar.title="Members"
        }
        toolbar_members_activity.setNavigationOnClickListener{
            onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_add_member->{
                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearchMember(){
        val dialog=Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.tv_add.setOnClickListener {
            val email=dialog.et_email_search_member.text.toString()
            if(email.isNotEmpty())
            {
                dialog.dismiss()
                showProgressDialog("Please Wait")
                FirestoreClass().getMemberDetails(this,email)
            }
            else{
                Toast.makeText(this@MembersActivity,"Please enter email address",Toast.LENGTH_SHORT).show()

            }
        }

        dialog.tv_cancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onBackPressed() {
        if(anyChangesMade){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    fun memberAssignedSuccess(user:User)
    {
        hideProgressDialog()
        mAssignedMembersList.add(user)
        anyChangesMade=true
        setUpMembersList(mAssignedMembersList)

        SendNotificationToUserAsyncTask(mBoardDetails.name,user.fcmToken).execute()
    }

    private inner class SendNotificationToUserAsyncTask(val boardName: String,val token:String): AsyncTask<Any, Void, String>(){
        override fun doInBackground(vararg p0: Any?): String {
                var result:String
                var connection:HttpURLConnection?=null
                try {
                    val url=URL(Constants.FCM_BASE_URL)
                    connection=url.openConnection() as HttpURLConnection
                    connection.doInput=true
                    connection.doOutput=true
                    connection.instanceFollowRedirects=false
                    connection.requestMethod="POST"

                    connection.setRequestProperty("Content-Type","application/json")
                    connection.setRequestProperty("charset","utf-8")
                    connection.setRequestProperty("Accept","application/json")

                    connection.setRequestProperty(Constants.FCM_AUTHORIZATION,"${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}")
                    connection.useCaches=false

                    val wr= DataOutputStream(connection.outputStream)
                    val jsonRequest=JSONObject()
                    val dataObject=JSONObject()
                    dataObject.put(Constants.FCM_KEY_TITLE, "Assigned to the board $boardName")
                    dataObject.put(Constants.FCM_KEY_MESSAGE,"You have been assigned to the board by ${mAssignedMembersList[0].name}")

                    jsonRequest.put(Constants.FCM_KEY_DATA,dataObject)
                    jsonRequest.put(Constants.FCM_KEY_TO,token)

                    wr.writeBytes(jsonRequest.toString())
                    wr.flush()
                    wr.close()

                    var httpResult:Int=connection.responseCode
                    if(httpResult==HttpURLConnection.HTTP_OK){
                        val inputStream=connection.inputStream

                        val reader=BufferedReader(InputStreamReader(inputStream))

                        val sb=StringBuilder()
                        var line:String?
                        try {
                            while(reader.readLine().also { line=it }!=null){
                                sb.append(line+"\n")
                            }
                        }catch (e:IOException){
                            e.printStackTrace()
                        }finally {
                            try {
                                inputStream.close()
                            }catch (e:IOException){
                                e.printStackTrace()
                            }
                        }
                        result=sb.toString()
                    }else{
                        result=connection.responseMessage
                    }

                }catch (e:SocketTimeoutException){
                    result="Connection Timeout"
                }catch (e:Exception){
                    result="Error: "+e.message
                }finally {
                    connection?.disconnect()
                }

                return result
        }

        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog("Please Wait!")
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            hideProgressDialog()
        }

    }
}