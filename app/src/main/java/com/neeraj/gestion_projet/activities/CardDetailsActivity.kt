package com.neeraj.gestion_projet.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telecom.Call
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.google.common.collect.ArrayTable
import com.neeraj.gestion_projet.R
import com.neeraj.gestion_projet.adapters.CardMemberListItemsAdapter
import com.neeraj.gestion_projet.dialogs.LabelColorListDialog
import com.neeraj.gestion_projet.dialogs.MembersListDialog
import com.neeraj.gestion_projet.firebase.FirestoreClass
import com.neeraj.gestion_projet.models.*
import com.neeraj.gestion_projet.utils.Constants
import kotlinx.android.synthetic.main.activity_card_details.*
import kotlinx.android.synthetic.main.activity_members.*
import kotlinx.android.synthetic.main.item_task.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {

    private lateinit var mBoardDetails: Board
    private var mTaskListPosition=-1
    private var mCardListPosition=-1
    private var mSelectedColor=""
    private lateinit var mMembersDetailList:ArrayList<User>
    private var mSelectedDueDateMilliseconds: Long=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)
        getIntentData()
        setUpActionBar()
        setUpSelectedMembersList()

        et_name_card_details.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name)
        et_name_card_details.setSelection(et_name_card_details.text.toString().length)
        mSelectedColor=mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].labelColor

        if(mSelectedColor.isNotEmpty())
            setColor()

        btn_update_card_details.setOnClickListener {
            if(et_name_card_details.text.toString().isNotEmpty())
                updateCardDetails()
            else
                Toast.makeText(this,"Please enter a card name",Toast.LENGTH_SHORT).show()
        }

        tv_select_label_color.setOnClickListener {
            labelColorsListDialog()
        }

        tv_select_members.setOnClickListener {
            membersListDialog()
        }

        mSelectedDueDateMilliseconds=mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].dueDate
        if(mSelectedDueDateMilliseconds>0){
            val simpleDateFormat=SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH)
            val selectedDate=simpleDateFormat.format(Date(mSelectedDueDateMilliseconds))
            tv_select_due_date.text=selectedDate
        }

        tv_select_due_date.setOnClickListener {
            showDatePicker()
        }
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar_card_details_activity)
        val actionBar=supportActionBar
        if(actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_colour_back)
            actionBar.title=mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name
        }
        toolbar_card_details_activity.setNavigationOnClickListener{
            onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card,menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun colorsList():ArrayList<String>{
        val colorList:ArrayList<String> = ArrayList()
        colorList.add("#43C86F")
        colorList.add("#0C90F1")
        colorList.add("#F72400")
        colorList.add("#7A8089")
        colorList.add("#D57C1D")
        colorList.add("#770000")
        colorList.add("#0022F8")
        return colorList
    }

    private fun labelList():ArrayList<String>{
        val label:ArrayList<String> = ArrayList()
        label.add("Label 1")
        label.add("Label 2")
        label.add("Label 3")
        label.add("Label 4")
        label.add("Label 5")
        label.add("Label 6")
        label.add("Label 7")
        return label
    }

    private fun setColor(){
        tv_select_label_color.text=""
        tv_select_label_color.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_delete_card -> {
                alertDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getIntentData() {
        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails=intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        if(intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            mTaskListPosition=intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION,-1)
        }
        if(intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mCardListPosition=intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION,-1)
        }
        if(intent.hasExtra(Constants.BOARD_MEMBERS_LIST)){
            mMembersDetailList=intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    private fun alertDialog() {
        val builder=AlertDialog.Builder(this)
        builder.setTitle("Delete ${mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name}")
        builder.setMessage("Are you sure?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes"){dialogInterface, which->
            dialogInterface.dismiss()
            deleteCards()
        }
        builder.setNegativeButton("No"){dialogInterface, which->
            dialogInterface.dismiss()
        }
        var alertDialog: AlertDialog =builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun membersListDialog(){
        var cardAssignedMember=mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo
        if(cardAssignedMember.size>0){
            for(i in mMembersDetailList.indices){
                for(j in cardAssignedMember){
                    if(mMembersDetailList[i].id==j){
                        mMembersDetailList[i].selected=true
                    }
                }
            }
        }else{
            for(i in mMembersDetailList.indices){
                mMembersDetailList[i].selected=false
            }
        }

        val listDialog= object : MembersListDialog(this,mMembersDetailList,"Select Member"){
            override fun onIemSelected(user: User, action: String) {
                if(action==Constants.SELECT){
                    if(!mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.contains(user.id)){
                        mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.add(user.id)
                    }
                }else{
                    mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.remove(user.id)
                    for(i in mMembersDetailList.indices){
                        if(mMembersDetailList[i].id==user.id){
                            mMembersDetailList[i].selected=false
                        }
                    }
                }
                setUpSelectedMembersList()
            }
        }.show()
    }

    private fun updateCardDetails(){
        val card=Card(
            et_name_card_details.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo,
            mSelectedColor,
            mSelectedDueDateMilliseconds
        )

        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition]=card

        showProgressDialog("Please Wait!")
        FirestoreClass().addUpdateTaskList(this@CardDetailsActivity,mBoardDetails)
    }

    private fun deleteCards() {
        val cardList:ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards

        cardList.removeAt(mCardListPosition)

        val taskList:ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size-1)

        taskList[mTaskListPosition].cards=cardList

        showProgressDialog("Please Wait!")
        FirestoreClass().addUpdateTaskList(this@CardDetailsActivity,mBoardDetails)
    }

    private fun labelColorsListDialog(){
        val colorsList:ArrayList<String> = colorsList()
        val labelList:ArrayList<String> = labelList()
        val listDialog=object :LabelColorListDialog(this,colorsList,labelList,"Select Label Color",mSelectedColor){
            override fun onIemSelected(color: String) {
                mSelectedColor=color
                setColor()
            }
        }
        listDialog.show()
    }

    private fun setUpSelectedMembersList(){
        val cardAssignedMembersList=mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo

        val selectedMembersList:ArrayList<SelectedMembers> = ArrayList()

        for(i in mMembersDetailList.indices) {
            for (j in cardAssignedMembersList) {
                if (mMembersDetailList[i].id == j) {
                    val selectedMember = SelectedMembers(
                        mMembersDetailList[i].id,
                        mMembersDetailList[i].image,
                    )
                    selectedMembersList.add(selectedMember)
                }
            }
        }
            if(selectedMembersList.size>0){
                selectedMembersList.add(SelectedMembers("",""))
                tv_select_members.visibility=View.GONE
                rv_selected_members_list.visibility=View.VISIBLE

                rv_selected_members_list.layoutManager=GridLayoutManager(this,6)

                val adapter=CardMemberListItemsAdapter(this,selectedMembersList,true)
                rv_selected_members_list.adapter=adapter
                adapter.setOnClickListener(
                    object: CardMemberListItemsAdapter.OnClickListener{
                        override fun onClick() {
                            membersListDialog()
                        }
                    }
                )
            }else{
                tv_select_members.visibility=View.VISIBLE
                rv_selected_members_list.visibility=View.GONE
            }
    }

    private fun showDatePicker(){
        val c=Calendar.getInstance()
        val year=c.get(Calendar.YEAR)
        val month=c.get(Calendar.MONTH)
        val day=c.get(Calendar.DAY_OF_MONTH)
        val dpd=DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                val sMonthOfYear =
                    if ((monthOfYear + 1) > 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"
                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                tv_select_due_date.text = selectedDate

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                val theDate = sdf.parse(selectedDate)
                mSelectedDueDateMilliseconds = theDate!!.time
            },
            year,
            month,
            day
        )
        dpd.show()
    }
}