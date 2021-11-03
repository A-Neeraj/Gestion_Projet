package com.neeraj.gestion_projet.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.neeraj.gestion_projet.activities.*
import com.neeraj.gestion_projet.models.Board
import com.neeraj.gestion_projet.models.User
import com.neeraj.gestion_projet.utils.Constants
import com.neeraj.gestion_projet.activities.TaskListActivity
import com.sun.xml.bind.v2.runtime.reflect.opt.Const

class FirestoreClass {
    private val mFirestore= FirebaseFirestore.getInstance()

    fun registerUser(activity: Activity, userInfo: User){
        val userRef=mFirestore.collection(Constants.users).document(getCurrentUserID()).get();
        userRef.addOnSuccessListener {
            doc->if(doc.exists()){
                val loggedInUser=doc.toObject(User::class.java)!!
            Log.i("Hi",loggedInUser.name.toString())
            when(activity){
                is IntroActivity->{
                    activity.signInSuccessful(loggedInUser)
                }
                is MainActivity->{
                    activity.updateNavigationUserDetails(loggedInUser,true)
                }
            }
        }
            else
            {
                when(activity){
                    is IntroActivity->{
                        mFirestore.collection(Constants.users).document(getCurrentUserID()).set(userInfo,
                            SetOptions.merge()).addOnSuccessListener {
                            activity.registerSuccessful()
                        }
                }
            }
        }
        }.addOnFailureListener{
            e->
            when(activity){
                is IntroActivity->{
                    activity.hideProgressDialog()
                }
                is MainActivity->{
                    activity.hideProgressDialog()
                }
            }
            Log.e("SignIn User","Error",e)
        }
    }
    fun loadUserData(activity: Activity,readBoardsList:Boolean=false)
    {
        mFirestore.collection(Constants.users).document(getCurrentUserID()).get().addOnSuccessListener {
            document->val loggedInUser=document.toObject(User::class.java)!!
            when(activity){
                is MyProfileActivity->{
                    activity.setUserData(loggedInUser)
                }
                is MainActivity -> {
                    activity.updateNavigationUserDetails(loggedInUser,readBoardsList)
                }
            }
        }.addOnFailureListener{
            e->Log.e("Data Error","Error",e)
        }
    }

    fun addUpdateTaskList(activity: Activity,board: Board)
    {
        val taskListHashMap=HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST]=board.taskList

        mFirestore.collection(Constants.boards).document(board.docId).update(taskListHashMap).addOnSuccessListener {
            if(activity is TaskListActivity)
                activity.addUpdateTaskListSuccess()
            else if(activity is CardDetailsActivity)
                activity.addUpdateTaskListSuccess()
        }.addOnFailureListener {
            exception ->
            if(activity is TaskListActivity)
                activity.hideProgressDialog()
            else if(activity is CardDetailsActivity)
                activity.hideProgressDialog()
            Log.e(activity.javaClass.simpleName, "error occurred")
        }
    }

    fun getAssignedMembers(activity: Activity,assignedTo:ArrayList<String>)
    {
        mFirestore.collection(Constants.users).whereIn(Constants.ID,assignedTo).get()
            .addOnSuccessListener {
                doc -> Log.e(activity.javaClass.simpleName,doc.documents.toString())

                val usersList:ArrayList<User> = ArrayList()
                for( i in doc.documents) {
                    val user = i.toObject(User::class.java)!!
                    usersList.add(user)
                }
                if(activity is MembersActivity)
                    activity.setUpMembersList(usersList)
                else if(activity is TaskListActivity)
                    activity.boardMembersDetailsList(usersList)

            }.addOnFailureListener {
                e->
                if(activity is MembersActivity)
                    activity.hideProgressDialog()
                else if(activity is TaskListActivity)
                    activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error occurred",e)
            }
    }

    fun getBoardDetails(activity: TaskListActivity, docId:String)
    {
        mFirestore.collection(Constants.boards)
            .document(docId)
            .get().addOnSuccessListener {
                    doc->
                val board=doc.toObject(Board::class.java)!!
                board.docId=doc.id
                activity.boardDetails(board)
            }.addOnFailureListener {
                    e-> activity.hideProgressDialog()
            }
    }

    fun getBoardsList(activity: MainActivity)
    {
        mFirestore.collection(Constants.boards)
            .whereArrayContains(Constants.ASSIGNED_TO,getCurrentUserID())
            .get().addOnSuccessListener {
                doc->
                val boardsList:ArrayList<Board> = ArrayList()
                for(i in doc.documents)
                {
                    val board = i.toObject(Board::class.java)!!
                    board.docId=i.id
                    boardsList.add(board)
                }
                activity.populateBoards(boardsList)
        }.addOnFailureListener {
            e-> activity.hideProgressDialog()
            }
    }

    fun updateUserData(activity: Activity,userHasMap:HashMap<String,Any>)
    {
        mFirestore.collection(Constants.users).document(getCurrentUserID()).update(userHasMap)
            .addOnSuccessListener {
                Toast.makeText(activity,"Profile Updated Successfully",Toast.LENGTH_SHORT).show()
                when(activity){
                    is MainActivity->{
                        activity.tokenUpdateSuccess()
                    }
                    is MyProfileActivity->{
                        activity.profileUpdateSuccess()
                    }
                }
            }.addOnFailureListener {
                e->
                when(activity) {
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Toast.makeText(activity,"Error while updating profile",Toast.LENGTH_SHORT).show()
            }
    }

    fun createBoard(activity: CreateBoardActivity,board: Board)
    {
        mFirestore.collection(Constants.boards).document().set(board, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(activity,"Board Created Successfully",Toast.LENGTH_SHORT).show()
                activity.boardCreated()
            }.addOnFailureListener {
                e->
                activity.hideProgressDialog()
                Log.e("Error while creating",e.toString())
            }
    }

    fun getCurrentUserID():String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    fun getMemberDetails(activity: MembersActivity, email: String)
    {
        mFirestore.collection(Constants.users)
            .whereEqualTo(Constants.EMAIL,email)
            .get()
            .addOnSuccessListener {
                doc -> if(doc.size()>0){
                    val user=doc.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
            }
                else{
                    activity.hideProgressDialog()
                    activity.showError("Member not found!")
            }
            }.addOnFailureListener {
                    e->activity.hideProgressDialog()
                Toast.makeText(activity,"Error while getting details",Toast.LENGTH_SHORT).show()
            }
    }

    fun assignMemberToBoard(activity: MembersActivity,board: Board,user:User)
    {
        val assignedTo=HashMap<String,Any>()
        assignedTo[Constants.ASSIGNED_TO]=board.assignedTo
        mFirestore.collection(Constants.boards).document(board.docId).update(assignedTo)
            .addOnSuccessListener {
                activity.memberAssignedSuccess(user)
            }
            .addOnFailureListener {
                e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while adding member",e)
            }
    }
}
