package com.app.budgetapp.ui

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.budgetapp.R
import com.app.budgetapp.model.Expenses
import kotlinx.android.synthetic.main.item_expense.view.*

class ExpenseListAdapter(val list:List<Expenses>,val listener:ExpenseEventListener) : RecyclerView.Adapter<ExpenseListAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(position)
    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view){
        fun bindData(position:Int){
            view.apply {
                list[position].let {
                    tvName.text = it.expenseName
                    tvAmount.text = "Amount: ${context.getString(R.string.currency)} ${it.expense}"
                    tvDate.text = it.dateTime

                    if(it.image.isNullOrBlank()){
                        imgReceipt.visibility = View.GONE
                    }else{
                        imgReceipt.visibility = View.VISIBLE
                        val imageBytes = Base64.decode(it.image, Base64.DEFAULT)
                        val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        imgReceipt.setImageBitmap(decodedImage)
                    }

                    imgDel.setOnClickListener {
                        listener.onDelete(position)
                    }
                }
            }
        }
    }

    interface ExpenseEventListener{
        fun onDelete(position:Int)
    }
}