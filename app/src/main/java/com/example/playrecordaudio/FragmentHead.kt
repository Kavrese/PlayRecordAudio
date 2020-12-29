package com.example.playrecordaudio

import android.app.Dialog
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.playrecordaudio.model.ModelAudio
import kotlinx.android.synthetic.main.fragment_head.*
import kotlinx.android.synthetic.main.sort_dialog.*

class FragmentHead(): Fragment() {
    var list_all: MutableList<ModelAudio> = mutableListOf()
    var now_model: ModelAudio? = null
    var mediaPlayer:MediaPlayer? = null
    var isPlay = false
    var dialog_sort:Dialog? = null
    var list_now = list_all
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_head, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initDialog()
        work_img.setOnClickListener {
            if (isPlay){
                pause()
            }else{
                if (mediaPlayer == null){
                    initMediaPlayer()
                }
                play()
            }
        }
        show_all.setOnClickListener {
            dialog_sort!!.show()
        }
    }

    private fun initMediaPlayer(){
            if (now_model == null) {
                mediaPlayer = MediaPlayer()
                mediaPlayer = MediaPlayer.create(requireContext(), Uri.parse("/storage/emulated/0/.PlayRecordAudio/29122020121709.mp3"))
                mediaPlayer!!.setOnCompletionListener {
                    work_img.setImageResource(R.drawable.play)
                    stopPlay()
                }
            }else{
                Toast.makeText(requireContext(), "Выберите файл (см. Показать всё)",Toast.LENGTH_LONG).show()
            }
    }

    private fun choosePath(path: String){
        mediaPlayer = MediaPlayer.create(requireContext(), Uri.parse(path))
    }

    private fun stopPlay(){
        try {
            mediaPlayer!!.prepare()
            mediaPlayer!!.seekTo(0)
        }catch (t: Throwable) {
            Toast.makeText(requireContext(), t.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun play(){
        mediaPlayer!!.start()
        isPlay = true
        work_img.setImageResource(R.drawable.pause)
    }

    private fun pause(){
        mediaPlayer!!.pause()
        isPlay = false
        work_img.setImageResource(R.drawable.play)
    }

    private fun initDialog(){
        dialog_sort = Dialog(requireContext())
        dialog_sort!!.setContentView(R.layout.sort_dialog)
        val rec = dialog_sort!!.findViewById<RecyclerView>(R.id.rec_sort)
        val back = dialog_sort!!.findViewById<ImageView>(R.id.back)
        back.setOnClickListener {
            dialog_sort!!.hide()
        }
        dialog_sort!!.setCanceledOnTouchOutside(false)
    }

    private fun getBDFiles(){

    }

    private fun getLocalFiles(){

    }
}