package com.darwin.physioai.coreapp.data.Adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.darwin.physioai.R
import com.darwin.physioai.coreapp.data.models.Data
import com.darwin.physioai.databinding.VisitRecyclerViewBinding

class VisitAdapter(private val cont: Context, private val list: List<Data>) : RecyclerView.Adapter<VisitAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: VisitRecyclerViewBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =  VisitRecyclerViewBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = list[position]
        holder.binding.visitTypev.text = user.visit_type
        holder.binding.locationv.text = user.location
        holder.binding.durationv.text = user.appointment_detail?.duration
        holder.binding.confDetailv.text = user.video_link
        holder.binding.timev.text = user.appointment_detail?.start_time

//        holder.binding.confDetailv.setOnClickListener {
//
//            Navigation.findNavController(it).navigate(R.id.action_schedule_to_agora)
//        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}