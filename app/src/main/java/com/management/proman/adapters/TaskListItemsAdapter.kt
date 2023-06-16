package com.management.proman.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.view.setMargins
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.management.proman.R
import com.management.proman.activities.TaskListActivity
import com.management.proman.models.Card
import com.management.proman.models.Task
import org.w3c.dom.Text
import java.util.*
import kotlin.collections.ArrayList

open class TaskListItemsAdapter(private val context: Context, private var list: ArrayList<Task>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mPositionDraggedFrom = -1
    private var mPositionDraggedTo = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)

        val layoutParams = LinearLayout.LayoutParams((parent.width * 0.7).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)

        layoutParams.setMargins((15.toDp()).toPx(), 0, (40.toDp()).toPx(), 0)

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val model = list[position]
        if (holder is MyViewHolder) {
            if (position == list.size - 1) {
                val tvAddTaskList = holder.itemView.findViewById<TextView>(R.id.tv_add_task_list)
                val llTaskItem = holder.itemView.findViewById<LinearLayout>(R.id.ll_task_item)

                tvAddTaskList.visibility = View.VISIBLE
                llTaskItem.visibility = View.GONE
            } else {
                val tvAddTaskList = holder.itemView.findViewById<TextView>(R.id.tv_add_task_list)
                val llTaskItem = holder.itemView.findViewById<LinearLayout>(R.id.ll_task_item)

                tvAddTaskList.visibility = View.GONE
                llTaskItem.visibility = View.VISIBLE
            }

            val tvTaskListTitle = holder.itemView.findViewById<TextView>(R.id.tv_task_list_title)
            val tvAddTaskList = holder.itemView.findViewById<TextView>(R.id.tv_add_task_list)
            val cvAddTaskListName =
                holder.itemView.findViewById<CardView>(R.id.cv_add_task_list_name)

            tvTaskListTitle.text = model.title

            tvAddTaskList.setOnClickListener {
                tvAddTaskList.visibility = View.GONE
                cvAddTaskListName.visibility = View.VISIBLE

            }

            val ibCloseListName = holder.itemView.findViewById<ImageButton>(R.id.ib_close_list_name)
            ibCloseListName.setOnClickListener {
                tvAddTaskList.visibility = View.VISIBLE
                cvAddTaskListName.visibility = View.GONE
            }

            val ibDoneListName = holder.itemView.findViewById<ImageButton>(R.id.ib_done_list_name)
            ibDoneListName.setOnClickListener {
                val etTaskListName = holder.itemView.findViewById<TextView>(R.id.et_task_list_name)
                val listName = etTaskListName.text.toString()

                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.createTaskList(listName)
                    }
                } else {
                    Toast.makeText(context, "Please enter a list name!", Toast.LENGTH_LONG).show()
                }
            }
        }

        val ibEditListName = holder.itemView.findViewById<ImageButton>(R.id.ib_edit_list_name)
        ibEditListName.setOnClickListener {
            val etEditTaskListName =
                holder.itemView.findViewById<EditText>(R.id.et_edit_task_list_name)
            val llTitleView = holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view)
            val cvEditTaskListName =
                holder.itemView.findViewById<CardView>(R.id.cv_edit_task_list_name)

            llTitleView.visibility = View.GONE
            cvEditTaskListName.visibility = View.VISIBLE

        }

        val ibCloseEditableView =
            holder.itemView.findViewById<ImageButton>(R.id.ib_close_editable_view)
        ibCloseEditableView.setOnClickListener {
            val llTitleView = holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view)
            val cvEditTaskListName =
                holder.itemView.findViewById<CardView>(R.id.cv_edit_task_list_name)

            llTitleView.visibility = View.VISIBLE
            cvEditTaskListName.visibility = View.GONE
        }

        val ibDoneEditListName =
            holder.itemView.findViewById<ImageButton>(R.id.ib_done_edit_list_name)
        ibDoneEditListName.setOnClickListener {
            val etEditTaskListName =
                holder.itemView.findViewById<EditText>(R.id.et_edit_task_list_name)

            val listName = etEditTaskListName.text.toString()

            if (listName.isNotEmpty()) {
                if (context is TaskListActivity) {
                    context.updateTaskList(position, listName, model)
                }
            } else {
                Toast.makeText(context, "Please enter a list name!", Toast.LENGTH_LONG).show()
            }
        }

        val ibDeleteList = holder.itemView.findViewById<ImageButton>(R.id.ib_delete_list)
        ibDeleteList.setOnClickListener {
            //TODO add alert dialog
            if (context is TaskListActivity) {
                context.deleteTaskList(position)
            }
        }

        val tvAddCard = holder.itemView.findViewById<TextView>(R.id.tv_add_card)
        val cvAddCard = holder.itemView.findViewById<CardView>(R.id.cv_add_card)
        tvAddCard.setOnClickListener {
            tvAddCard.visibility = View.GONE
            cvAddCard.visibility = View.VISIBLE
        }

        val ibCloseCardName = holder.itemView.findViewById<ImageButton>(R.id.ib_close_card_name)
        ibCloseCardName.setOnClickListener {
            tvAddCard.visibility = View.VISIBLE
            cvAddCard.visibility = View.GONE

        }

        val ibDoneCardName = holder.itemView.findViewById<ImageButton>(R.id.ib_done_card_name)
        ibDoneCardName.setOnClickListener {
            val etCardName = holder.itemView.findViewById<EditText>(R.id.et_card_name)

            val cardName = etCardName.text.toString()

            if (cardName.isNotEmpty()) {
                if (context is TaskListActivity) {
                    context.addCardToTaskList(position, cardName)
                }
            } else {
                Toast.makeText(context, "Please enter a card name!", Toast.LENGTH_LONG).show()
            }
        }

        val rvCardList = holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list)
        rvCardList.layoutManager = LinearLayoutManager(context)

        rvCardList.setHasFixedSize(true)

        val adapter = CardListItemsAdapter(context, model.cards)
        rvCardList.adapter = adapter

        adapter.setOnClickListener(object : CardListItemsAdapter.OnClickListener {
            override fun onClick(cardPosition: Int) {
                if (context is TaskListActivity) {
                    context.cardDetails(position, cardPosition)
                }
            }
        })

        val dividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        rvCardList.addItemDecoration(dividerItemDecoration)

        val helper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                dragged: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val draggedPosition = dragged.adapterPosition
                val targetPosition = target.adapterPosition

                if (mPositionDraggedFrom == -1) {
                    mPositionDraggedFrom == draggedPosition
                }
                mPositionDraggedTo = targetPosition
                Collections.swap(list[position].cards, draggedPosition, targetPosition)
                adapter.notifyItemMoved(draggedPosition, targetPosition)
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)

                if (mPositionDraggedFrom != -1 && mPositionDraggedTo != -1 && mPositionDraggedFrom != mPositionDraggedTo) {
                    (context as TaskListActivity).updateCardsInTaskList(position, list[position].cards)
                }

                mPositionDraggedTo = -1
                mPositionDraggedFrom = -1
            }
        })
        helper.attachToRecyclerView(rvCardList)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view)
}