package com.darwin.physioai.coreapp.data.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.darwin.physioai.databinding.TreatingItemRecyclerBinding
import com.example.physioai.data.models.TreatingDoctorDetail


class TreatingDocAdapter(private val list: List<TreatingDoctorDetail>) : RecyclerView.Adapter<TreatingDocAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(val binding: TreatingItemRecyclerBinding): RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val user = list[position]
        holder.binding.firstName.text = user.first_name
        holder.binding.lastName.text = user.last_name.toString()
        holder.binding.expertise1.text = user.expertise_1
        holder.binding.epertise2.text = user.expertise_2.toString()
        holder.binding.epertise3.text = user.expertise_3.toString()
        holder.binding.address1.text = user.Address_1.toString()
        holder.binding.address2.text = user.Address_2.toString() + ","
        holder.binding.mobile.text = user.mobile_no.toString()
        holder.binding.whatsappnum.text = user.whatsapp_no.toString()
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val v =  TreatingItemRecyclerBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(v)
    }
}