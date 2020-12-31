package com.example.playrecordaudio

import android.app.Dialog
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playrecordaudio.model.ModelAudio
import kotlinx.android.synthetic.main.fragment_head.*
import java.io.File
import java.text.SimpleDateFormat
import kotlin.concurrent.thread

class FragmentHead(): Fragment(), OpenListener {
    private val path_files = File(Environment.getExternalStorageDirectory().toString() + "/.PlayRecordAudio/")
    var list_all: MutableList<ModelAudio> = mutableListOf()
    var now_model: ModelAudio? = null
    var mediaPlayer:MediaPlayer = MediaPlayer()
    var isPlay = false
    var activ_double_click_pause = false
    var dialog_sort:Dialog? = null
    var list_now: MutableList<ModelAudio> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_head, container, false)
    }

    override fun openFromSortDialog(model: ModelAudio) {
        if (isPlay) {
            stopPlay()
        }
        dialog_sort!!.hide()
        now_model = model
        initMediaPlayer()
        name_file.text = now_model!!.name.toString()
        date_file.text = now_model!!.date.toString()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        list_all = getLocalFiles()
        list_now = list_all
        initDialog()
        work_img.setOnClickListener {
            if (now_model != null) {
                if (isPlay) {
                    pause()
                } else {
                    play()
                }
            }else{
                Toast.makeText(requireContext(), "Выберите файл (см. Показать всё)",Toast.LENGTH_LONG).show()
            }
        }
        show_all.setOnClickListener {
            dialog_sort!!.show()
        }
        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (now_model != null){
                    mediaPlayer.seekTo(p1)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                if (now_model == null){
                    p0!!.progress = 0
                    Toast.makeText(requireContext(), "Выберите файл (см. Показать всё)",Toast.LENGTH_LONG).show()
                }
            }

        })
    }

    private fun initMediaPlayer(){
            if (now_model != null) {
                mediaPlayer = MediaPlayer.create(requireContext(), Uri.parse(now_model!!.path))
                seekBar.max = mediaPlayer.duration
                seekBar.progress = 0
                mediaPlayer.setOnCompletionListener {
                    work_img.setImageResource(R.drawable.play)
                    stopPlay()
                }
            }else{
                Toast.makeText(requireContext(), "Выберите файл (см. Показать всё)",Toast.LENGTH_LONG).show()
            }
    }

    private fun stopPlay(){
        try {
            isPlay = false
            mediaPlayer.seekTo(0)

        }catch (t: Throwable) {
            if (t.message != "") {
                Toast.makeText(requireContext(), t.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun play(){
        mediaPlayer.start()
        isPlay = true
        work_img.setImageResource(R.drawable.pause)
    }

    private fun pause(){
        activ_double_click_pause = !activ_double_click_pause
        if (activ_double_click_pause){
            Handler().postDelayed(
                {
                    if (!activ_double_click_pause){
                        mediaPlayer.seekTo(0)
                       // mediaPlayer.start()
                        Toast.makeText(requireContext(), "Reset", Toast.LENGTH_LONG).show()
                    }else{
                        mediaPlayer.pause()
                        isPlay = false
                        work_img.setImageResource(R.drawable.play)
                        activ_double_click_pause = false
                    }
                }, 150
            )
        }
    }

    private fun initDialog(){
        dialog_sort = Dialog(requireContext())
        dialog_sort!!.setContentView(R.layout.sort_dialog)
        val rec = dialog_sort!!.findViewById<RecyclerView>(R.id.rec_sort)
        rec.apply {
            adapter = AdapterFiles(list_now, this@FragmentHead)
            layoutManager = LinearLayoutManager(requireContext())
        }

        val back = dialog_sort!!.findViewById<ImageView>(R.id.back)
        back.setOnClickListener {
            dialog_sort!!.hide()
        }
        dialog_sort!!.setCanceledOnTouchOutside(false)
    }

    private fun getLocalFiles(): MutableList<ModelAudio> {
        val list_= mutableListOf<ModelAudio>()
        val listFiles = path_files.listFiles()
        for (i in  0..listFiles.size - 1){
            val file = listFiles[i].name
            val name = file.substringBefore("&")
            val date = file.substringAfter("&").substringBefore(".").substring(0, 8)
            val new_date = SimpleDateFormat("ddMMyyyy").parse(date)
            list_.add(ModelAudio(name, SimpleDateFormat("dd.MM.yyyy").format(new_date), listFiles[i].toString()))
        }
        return list_
    }
}