package com.darwin.physioai.coreapp.data.Adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.darwin.physioai.R
import com.darwin.physioai.coreapp.ui.fragments.Schedule
import com.darwin.physioai.databinding.CustomLayoutforCalenderCareplanBinding
import com.example.physioai.data.models.DataXX
import java.text.SimpleDateFormat
import java.util.*

class ExcerciseDetailsAdapter(
    private val cont: Context,
    private val list: List<DataXX>,
    private val time: String? =null
) : RecyclerView.Adapter<ExcerciseDetailsAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(val binding: CustomLayoutforCalenderCareplanBinding): RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = list[position]
        holder.binding.exercisename.text = user.name
        holder.binding.set.text = user.Rep.set.toString()
        holder.binding.rep.text = user.Rep.rep_count.toString()
        holder.binding.exId.text = user.ex_em_id.toString()

        Glide.with(holder.itemView).load("https://myphysio.digitaldarwin.in/"+user.image_url)
            .centerCrop()
            .placeholder(R.drawable.ic_profile_my_team_img)
            .error(R.drawable.ic_profile_my_team_img)
            .into(holder.binding.exerciseimage)

        holder.binding.startexercise.setOnClickListener {
            val bundle: Bundle = Bundle().apply {
                putString("exercise", user.name.toString())
                putString("angle", user.angle_data[0].angle_name)
                putString("pp_cp_id", user.pp_cp_id.toString())
                putString("time", time)
            }
            Navigation.findNavController(it).navigate(R.id.action_schedule_to_instructionsFragment, bundle)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val v =  CustomLayoutforCalenderCareplanBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(v)
    }
}