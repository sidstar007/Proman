package com.management.proman.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.management.proman.R
import com.management.proman.models.User
import com.management.proman.utils.Constants


open class MemberListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<User>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_member,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {

            val ivMemberImage = holder.itemView.findViewById<ImageView>(R.id.iv_member_image)
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(ivMemberImage)

            val tvMemberName = holder.itemView.findViewById<TextView>(R.id.tv_member_name)
            val tvMemberEmail = holder.itemView.findViewById<TextView>(R.id.tv_member_email)

            tvMemberName.text = model.name
            tvMemberEmail.text = model.email

            if (model.selected) {
                val ivSelectedMember = holder.itemView.findViewById<ImageView>(R.id.iv_selected_member)
                ivSelectedMember.visibility = View.VISIBLE
            }
            else {
                val ivSelectedMember = holder.itemView.findViewById<ImageView>(R.id.iv_selected_member)
                ivSelectedMember.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if (onClickListener !=  null) {
                    if (model.selected) {
                        onClickListener!!.onClick(position, model, Constants.UN_SELECT)
                    }
                    else if (!model.selected) {
                        onClickListener!!.onClick(position, model, Constants.SELECT)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener  = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, user: User, action: String)
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}