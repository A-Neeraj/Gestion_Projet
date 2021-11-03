package com.neeraj.gestion_projet.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.neeraj.gestion_projet.R
import com.neeraj.gestion_projet.adapters.LabelColorListItemAdapter
import com.neeraj.gestion_projet.adapters.MembersListItemAdapter
import com.neeraj.gestion_projet.models.User
import kotlinx.android.synthetic.main.dialog_list.*
import kotlinx.android.synthetic.main.dialog_list.view.*

abstract class MembersListDialog(context: Context, private var list:ArrayList<User>, private val title:String="")
    :Dialog(context){
    private var adapter: MembersListItemAdapter?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        val view=LayoutInflater.from(context).inflate(R.layout.dialog_list,null)
        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(view)

        super.onCreate(savedInstanceState)
    }

    private fun setUpRecyclerView(view: View)
    {
        view.tvTitle.text=title
        if(list.size>0)
        {
            view.rvList.layoutManager=LinearLayoutManager(context)
            adapter= MembersListItemAdapter(context,list)
            view.rvList.adapter=adapter


            adapter!!.setOnClickListener(object :MembersListItemAdapter.OnClickListener{
            override fun onClick(position: Int,user: User,action:String) {
                dismiss()
                onIemSelected(user,action)
            }
        })
    }}

    protected abstract fun onIemSelected(user: User,action:String)
}