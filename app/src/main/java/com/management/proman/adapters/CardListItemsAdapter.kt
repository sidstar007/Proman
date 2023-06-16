package com.management.proman.adapters

import android.content.Context
import android.graphics.Color
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.management.proman.R
import com.management.proman.activities.TaskListActivity
import com.management.proman.models.Card
import com.management.proman.models.SelectedMembers

open class CardListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Card>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_card,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {

            val tvCardName = holder.itemView.findViewById<TextView>(R.id.tv_card_name)
            tvCardName.text = model.name
            if (model.labelColor.isNotEmpty()) {
                val viewLabelColor = holder.itemView.findViewById<View>(R.id.view_label_color)
                viewLabelColor.visibility = View.VISIBLE
                viewLabelColor.setBackgroundColor(Color.parseColor(model.labelColor))
            }

            if ((context as TaskListActivity).mAssignedMemberDetailList.size > 0 ) {
                val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

                for (i in context.mAssignedMemberDetailList.indices) {
                    for (j in model.assignedTo) {
                        if (context.mAssignedMemberDetailList[i].id == j) {
                            val selectedMember = SelectedMembers(context.mAssignedMemberDetailList[i].id, context.mAssignedMemberDetailList[i].image)
                            selectedMembersList.add(selectedMember)
                        }
                    }
                }

                if (selectedMembersList.size > 0) {
                    if (selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy) {
                        val rvCardSelectedMembersList = holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list)
                        rvCardSelectedMembersList.visibility = View.GONE
                    }
                    else {
                        val rvCardSelectedMembersList = holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list)
                        rvCardSelectedMembersList.visibility = View.VISIBLE
                        rvCardSelectedMembersList.layoutManager = GridLayoutManager(context, 6)
                        val adapter = CardMemberListItemsAdapter(context, selectedMembersList, false)
                        rvCardSelectedMembersList.adapter = adapter
                        adapter.setOnClickListener(object : CardMemberListItemsAdapter.OnClickListener{
                            override fun onClick() {
                                if (onClickListener != null) {
                                    onClickListener!!.onClick(position)
                                }
                            }
                        })
                    }
                }
                else {
                    val rvCardSelectedMembersList = holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list)
                    rvCardSelectedMembersList.visibility = View.GONE
                }
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int)
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}