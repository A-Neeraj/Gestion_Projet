package com.neeraj.gestion_projet.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.neeraj.gestion_projet.R
import com.neeraj.gestion_projet.adapters.LabelColorListItemAdapter
import kotlinx.android.synthetic.main.dialog_list.*
import kotlinx.android.synthetic.main.dialog_list.view.*

abstract class LabelColorListDialog(context: Context,private var list:ArrayList<String>,private var label:ArrayList<String>,private val title:String="",private var mSelectedColor:String="")
    :Dialog(context){
    private var adapter: LabelColorListItemAdapter?=null
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
        view.rvList.layoutManager=LinearLayoutManager(context)
        adapter= LabelColorListItemAdapter(context,list,label,mSelectedColor)
        view.rvList.adapter=adapter

        adapter!!.onItemClickListener=object :LabelColorListItemAdapter.OnItemClickListener{
            override fun onClick(position: Int, color: String) {
                dismiss()
                onIemSelected(color)
            }

        }
    }

    protected abstract fun onIemSelected(color:String)
}