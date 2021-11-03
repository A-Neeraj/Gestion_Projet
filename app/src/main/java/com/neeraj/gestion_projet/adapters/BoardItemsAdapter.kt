package com.neeraj.gestion_projet.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.neeraj.gestion_projet.R
import com.neeraj.gestion_projet.models.Board
import kotlinx.android.synthetic.main.activity_create_board.view.*
import kotlinx.android.synthetic.main.item_board.view.*

open class BoardItemsAdapter(private val context: Context, private var list: ArrayList<Board>):RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener:OnClickListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return myViewHolder(LayoutInflater.from(context).inflate(R.layout.item_board,parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model=list[position]
        if(holder is myViewHolder)
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(holder.itemView.board_image)

            holder.itemView.tv_name.text=model.name
            holder.itemView.tv_created_by.text="Created by ${model.createdBy}";

            holder.itemView.setOnClickListener {
                if(onClickListener!=null)
                    onClickListener!!.onClick(position,model)
            }
    }

    interface OnClickListener{
        fun onClick(position: Int,model: Board)
    }

    fun setOnClickListener(onClickListener: OnClickListener)
    {
        this.onClickListener=onClickListener
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class myViewHolder(view:View) : RecyclerView.ViewHolder(view)
}
