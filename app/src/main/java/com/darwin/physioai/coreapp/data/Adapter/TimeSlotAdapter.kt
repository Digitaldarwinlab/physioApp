package com.darwin.physioai.coreapp.data.Adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.darwin.physioai.databinding.TimeSlotItemBinding
import com.example.physioai.data.models.DataXX
import com.example.physioai.data.models.TimeSlotMobileX


class TimeSlotAdapter(private val list: List<TimeSlotMobileX>, private val onItemClickInterface: OnItemClickInterface) : RecyclerView.Adapter<TimeSlotAdapter.ViewHolder>() {

    var timedatalist :ArrayList<DataXX>? = null
    private var row_index : Int? = 0

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnItemClickInterface {
        fun onLeadClicked(timeSlotMobile: ArrayList<DataXX>, position: Int)
    }

    inner class ViewHolder(val binding: TimeSlotItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val user = list[position]
        holder.binding.time.text = user.time
        Log.d("LogList", user.data.toString())

        if (row_index == position){
            onItemClickInterface.onLeadClicked(user.data as ArrayList<DataXX>, position)
        }

        if (row_index == position){
            holder.binding.timelay.setBackgroundColor(Color.parseColor("#41928d"))
        }else{
            holder.binding.timelay.setBackgroundColor(Color.parseColor("#58C1BA"))
        }

        holder.binding.timelay.setOnClickListener {
            row_index = position;
            onItemClickInterface.onLeadClicked(user.data as ArrayList<DataXX>, position)
            notifyDataSetChanged();
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val v =  TimeSlotItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(v)
    }
}