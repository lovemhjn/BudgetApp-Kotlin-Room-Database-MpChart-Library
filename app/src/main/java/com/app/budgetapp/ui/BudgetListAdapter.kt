package com.app.budgetapp.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import androidx.recyclerview.widget.RecyclerView
import com.app.budgetapp.R
import com.app.budgetapp.db.RoomDb
import com.app.budgetapp.model.Budget
import com.app.budgetapp.utils.Db
import com.app.budgetapp.utils.getMonth
import com.app.budgetapp.utils.getYear
import kotlinx.android.synthetic.main.item_budget.view.*

class BudgetListAdapter(private val list:ArrayList<Budget>,val listener:BudgetListener) : RecyclerView.Adapter<BudgetListAdapter.ViewHolder>(){

    val options = arrayOf("Expenses","Transfer","Delete","Add Funds")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_budget,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bindData(position)

    inner class ViewHolder(private val view:View):RecyclerView.ViewHolder(view){

        fun bindData(position:Int){
            view.apply {
                list[position].let {
                    tvName.text = it.category
                    val fund = context.Db().fundsDao().getFund(it.id,context.getMonth(),context.getYear())
                    tvInitialBudget.text = "Initial Budget\n${context.getString(R.string.currency)} ${fund?.initialBudget?:0.0}"
                    tvRemainingBudget.text = "Balance\n${context.getString(R.string.currency)} ${fund?.remainingBudget?:0.0}"

                    imgMenu.setOnClickListener {v->

                        val listPopupWindow = ListPopupWindow(context)
                        listPopupWindow.setAdapter(ArrayAdapter(context,android.R.layout.simple_list_item_1,options))
                        listPopupWindow.anchorView = v
                        listPopupWindow.width = width/2


                        listPopupWindow.setOnItemClickListener { _, _, position, _ ->
                            when (position) {

                                0 -> {
                                    listPopupWindow.dismiss()
                                    val intent = Intent(context,ViewBudgetActivity::class.java)
                                    intent.putExtra("BUDGET_ID",it.id)
                                    context.startActivity(intent)
                                }
                                1 -> {
                                    listPopupWindow.dismiss()
                                    val intent = Intent(context,TransferBudgetActivity::class.java)
                                    intent.putExtra("BUDGET_ID",it.id)
                                    context.startActivity(intent)
                                }
                                2 -> {
                                    listPopupWindow.dismiss()
                                    RoomDb.getDatabase(context).expenseDao().deleteExpenses(it.id)
                                    RoomDb.getDatabase(context).fundsDao().deleteFunds(it.id)
                                    RoomDb.getDatabase(context).budgetDao().deleteBudget(it)
                                    listener.deleteBudget()
                                    list.remove(it)
                                    notifyDataSetChanged()
                                }
                                else -> {
                                    listPopupWindow.dismiss()
                                    val intent = Intent(context,CreateBudgetActivity::class.java)
                                    intent.putExtra("BUDGET_ID",it.id)
                                    context.startActivity(intent)
                                }
                            }
                        }
                        listPopupWindow.show()

                    }

                }
            }
        }
    }

    interface BudgetListener{
        fun deleteBudget()
    }

}