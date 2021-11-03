package com.neeraj.gestion_projet.activities

import android.app.Activity
import android.app.Activity.*
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.neeraj.gestion_projet.R
import com.neeraj.gestion_projet.activities.BaseActivity
import com.neeraj.gestion_projet.adapters.TaskListItemsAdapter
import com.neeraj.gestion_projet.firebase.FirestoreClass
import com.neeraj.gestion_projet.models.Board
import com.neeraj.gestion_projet.models.Card
import com.neeraj.gestion_projet.models.Task
import com.neeraj.gestion_projet.models.User
import com.neeraj.gestion_projet.utils.Constants
import com.sun.xml.bind.v2.runtime.reflect.opt.Const
import kotlinx.android.synthetic.main.activity_task_list.*

class TaskListActivity : BaseActivity() {

    private lateinit var mBoardDetails:Board;
    private lateinit var mBoardDocumentId:String
    lateinit var mAssignedMemberDetailsList:ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            mBoardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }

        showProgressDialog("Please Wait!")
        FirestoreClass().getBoardDetails(this@TaskListActivity, mBoardDocumentId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode== Activity.RESULT_OK && (requestCode== MEMBERS_REQUEST_CODE || requestCode== CARD_DETAILS_REQUEST_CODE))
        {
            showProgressDialog("Please Wait!")
            FirestoreClass().getBoardDetails(this@TaskListActivity, mBoardDocumentId)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun cardDetails(cardPosition:Int, taskPosition:Int)
    {
        val intent=Intent(this,CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION,taskPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION,cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST,mAssignedMemberDetailsList)
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_members->{
                val intent=Intent(this,MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails)
                startActivityForResult(intent, MEMBERS_REQUEST_CODE)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupActionBar() {

        setSupportActionBar(toolbar_task_list_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
//            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mBoardDetails.name
        }

        toolbar_task_list_activity.setNavigationOnClickListener { onBackPressed() }
    }

    fun boardDetails(board: Board) {

        mBoardDetails=board

        hideProgressDialog()
        setupActionBar()


        showProgressDialog("Please Wait!")
        FirestoreClass().getAssignedMembers(this,mBoardDetails.assignedTo)
    }

    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        showProgressDialog("Please Wait!")
        FirestoreClass().getBoardDetails(this,mBoardDetails.docId)
    }

    fun createTaskList(taskListName:String)
    {
        val task=Task(taskListName,FirestoreClass().getCurrentUserID())
        mBoardDetails.taskList.add(0,task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        showProgressDialog("Please Wait!")
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    fun updateTaskList(position:Int,listName:String, model:Task)
    {
        val task=Task(listName,model.createdBy)
        mBoardDetails.taskList[position]=task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        showProgressDialog("Please Wait!")
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    fun deleteTaskList(position: Int)
    {
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        showProgressDialog("Please Wait!")
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    fun addCard(position: Int, cardName:String)
    {
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        val cardAssignedUsersList:ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FirestoreClass().getCurrentUserID())

        val card= Card(cardName,FirestoreClass().getCurrentUserID(),cardAssignedUsersList)

        val cardsList=mBoardDetails.taskList[position].cards
        cardsList.add(card)

        val task=Task(mBoardDetails.taskList[position].title,mBoardDetails.taskList[position].createdBy,cardsList)

        mBoardDetails.taskList[position]=task

        showProgressDialog("Please Wait!")
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    fun boardMembersDetailsList(list:ArrayList<User>)
    {

        mAssignedMemberDetailsList=list
        hideProgressDialog()

        val addTaskList = Task("Add List")
        mBoardDetails.taskList.add(addTaskList)

        rv_task_list.layoutManager =
            LinearLayoutManager(this@TaskListActivity, LinearLayoutManager.HORIZONTAL, false)
        rv_task_list.setHasFixedSize(true)
        val adapter = TaskListItemsAdapter(this@TaskListActivity, mBoardDetails.taskList)
        rv_task_list.adapter = adapter
    }

    fun updateCardsInTaskList(taskListPosition:Int, cards:ArrayList<Card>){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        mBoardDetails.taskList[taskListPosition].cards=cards
        showProgressDialog("Please Wait!")
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)

    }

    companion object{
        const val MEMBERS_REQUEST_CODE=123
        const val CARD_DETAILS_REQUEST_CODE=14
    }
}