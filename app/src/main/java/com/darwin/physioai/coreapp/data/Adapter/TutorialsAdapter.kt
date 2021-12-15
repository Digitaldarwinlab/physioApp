package com.darwin.physioai.coreapp.data.Adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.MediaController
import androidx.recyclerview.widget.RecyclerView
import com.darwin.physioai.databinding.VideoItemBinding
import com.example.physioai.data.models.DataXX

class TutorialsAdapter(private val cont: Context, private val list: List<DataXX>) : RecyclerView.Adapter<TutorialsAdapter.ViewHolder>() {

    val url = "https://myphysio.digitaldarwin.in/"

    inner class ViewHolder(val binding: VideoItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =  VideoItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = list[position]
        val v = (url + user.video_url)
        holder.binding.tutorialvieoview.setVideoPath(v)
        holder.binding.nameOfExercise.text = user.name.toString()

        val mediaController = MediaController(cont)
        mediaController.setAnchorView(holder.binding.tutorialvieoview)

        holder.binding.tutorialvieoview.setMediaController(mediaController)
        holder.binding.tutorialvieoview.setVideoURI(Uri.parse(v))

        //for video in loop
        holder.binding.tutorialvieoview.setOnPreparedListener { mp ->
            mp.isLooping = true
        }
        holder.binding.tutorialvieoview.start()
    }

    override fun getItemCount(): Int {
        return list.size
    }


}