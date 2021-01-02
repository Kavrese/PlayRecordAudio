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
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playrecordaudio.adapters.AdapterFiles
import com.example.playrecordaudio.adapters.AdapterMonth
import com.example.playrecordaudio.model.ModelAudio
import com.example.playrecordaudio.model.ModelMonth
import kotlinx.android.synthetic.main.fragment_head.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FragmentHead: Fragment(), getListener {
    private val path_files = File(Environment.getExternalStorageDirectory().toString() + "/.PlayRecordAudio/")
    var list_all: MutableList<ModelAudio> = mutableListOf()
    var now_model: ModelAudio? = null
    var mediaPlayer:MediaPlayer = MediaPlayer()
    var isPlay = false
    var list_selected = mutableListOf<Int>()
    var activ_double_click_pause = false
    var dialog_sort:Dialog? = null
    var dialog_selected:Dialog? = null
    var dialog_calendar:Dialog? = null
    var list_now: MutableList<ModelAudio> = mutableListOf()
    var playAll = false
    var positionPlay = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_head, container, false)
    }

    override fun getModelFromSortDialog(model: ModelAudio) {
        if (isPlay) {
            stopPlay()
        }
        dialog_sort!!.hide()
        now_model = model
        initMediaPlayer()
        setInfoModelToViews(now_model!!)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        list_all = getLocalFiles()
        list_now = list_all
        work_img.setOnClickListener {
            if (now_model != null) {
                if (isPlay) {
                    pause()
                } else {
                    play()
                }
            }else{
                Toast.makeText(requireContext(), "Выберите файл (см. Воспроизвести всё)",Toast.LENGTH_LONG).show()
            }
        }
        filter.setOnClickListener {
            initSortDialog(false)
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
                    Toast.makeText(requireContext(), "Выберите файл",Toast.LENGTH_LONG).show()
                }
            }

        })

        ver_menu.setOnClickListener {
            if (list_selected.size == 0) {
                initSortDialog(true)
                dialog_sort!!.show()
                Toast.makeText(requireContext(), "Сначало выберите файлы", Toast.LENGTH_LONG).show()
            }else{
                initSelectedDialog()
                dialog_selected!!.show()
            }
        }

        show_all.setOnClickListener {
            list_now = list_all
            startPlayMN()
        }

        calendar.setOnClickListener {
            initCalendarDialog()
            dialog_calendar!!.show()
        }
    }

    private fun startPlayMN (){
        positionPlay = 0
        now_model = list_now[positionPlay]
        initMediaPlayer()
        playAll = true
        setInfoModelToViews(now_model!!)
        play()
    }

    private fun initMediaPlayer(){
        if (now_model != null) {
            mediaPlayer = MediaPlayer.create(requireContext(), Uri.parse(now_model!!.path))
            seekBar.max = mediaPlayer.duration
            seekBar.progress = 0
            mediaPlayer.setOnCompletionListener {
                stopPlay()
                if (playAll && (positionPlay < list_now.size - 1) ){
                    positionPlay += 1
                    now_model = list_now[positionPlay]
                    initMediaPlayer()
                    setInfoModelToViews(now_model!!)
                    play()
                }
            }
        }else{
            Toast.makeText(requireContext(), "Выберите файл",Toast.LENGTH_LONG).show()
        }
    }

    private fun stopPlay(){
        try {
            isPlay = false
            work_img.setImageResource(R.drawable.play)
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

    private fun initSortDialog(show_checkBox: Boolean){
        dialog_sort = Dialog(requireContext())
        dialog_sort!!.setContentView(R.layout.sort_dialog)
        val rec = dialog_sort!!.findViewById<RecyclerView>(R.id.rec_sort)
        val back = dialog_sort!!.findViewById<ImageView>(R.id.back)

        val clear = dialog_sort!!.findViewById<TextView>(R.id.clear)
        val dateUp = dialog_sort!!.findViewById<TextView>(R.id.textDateUp)
        val dateDown = dialog_sort!!.findViewById<TextView>(R.id.textDateDown)
        val alphabetUp = dialog_sort!!.findViewById<TextView>(R.id.textAlphabetUp)
        val alphabetDown = dialog_sort!!.findViewById<TextView>(R.id.textAlphabetDown)

        dateUp.setOnClickListener {
            list_now = Sort().dateFile(list_all.toTypedArray().clone().toMutableList(), Sort().UP)
            newListToRec(rec, show_checkBox)
        }

        dateDown.setOnClickListener {
            list_now = Sort().dateFile(list_all.toTypedArray().clone().toMutableList(), Sort().DOWN)
            newListToRec(rec, show_checkBox)
        }

        clear.setOnClickListener {
            list_now = list_all
            newListToRec(rec, show_checkBox)
        }

        alphabetUp.setOnClickListener {
            list_now = Sort().alphabetFile(list_all.toTypedArray().clone().toMutableList(), Sort().UP)
            newListToRec(rec, show_checkBox)
        }

        alphabetDown.setOnClickListener {
            list_now = Sort().alphabetFile(list_all.toTypedArray().clone().toMutableList(), Sort().DOWN)
            newListToRec(rec, show_checkBox)
        }

        rec.apply {
            adapter = AdapterFiles(
                list_now,
                this@FragmentHead,
                show_checkBox
            )
            layoutManager = LinearLayoutManager(requireContext())
        }
        back.setOnClickListener {
            dialog_sort!!.hide()
        }
        dialog_sort!!.setCanceledOnTouchOutside(false)
    }

    private fun newListToRec(rec: RecyclerView, show_checkBox: Boolean){
        rec.adapter = AdapterFiles(
            list_now,
            this@FragmentHead,
            show_checkBox
        )
    }

    private fun initCalendarDialog(){
        dialog_calendar = Dialog(requireContext())
        dialog_calendar!!.setContentView(R.layout.calendar_dialog)
        val rec = dialog_calendar!!.findViewById<RecyclerView>(R.id.rec_cal)
        val back = dialog_calendar!!.findViewById<ImageView>(R.id.back)
        rec.apply {
            adapter = AdapterMonth(generateListFileToListMonth(list_all),this@FragmentHead)
            layoutManager = LinearLayoutManager(requireContext())
        }
        back.setOnClickListener {
            dialog_calendar!!.hide()
        }
    }

    private fun generateListFileToListMonth(list_file: MutableList<ModelAudio>): MutableList<ModelMonth>{
        val list_month = mutableListOf<ModelMonth>()
        for (i in 0..11){
            val cal = Calendar.getInstance()
            cal.set(Calendar.MONTH, i)
            val month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale("Ru"))
            val list_file_month = mutableListOf<ModelAudio>()
            for (f in list_file){
                val numMonthF = Integer.parseInt(f.date!!.substring(3,5)) - 1
                if(numMonthF == i){
                    list_file_month.add(f)
                }
            }
            list_month.add(ModelMonth(month, list_file_month.size, list_file_month))
        }
        return list_month
    }

    private fun initSelectedDialog(){
        dialog_selected = Dialog(requireContext(), R.style.DialogTheme)
        dialog_selected!!.setContentView(R.layout.edit_selected_items)
        val refresh = dialog_selected!!.findViewById<LinearLayout>(R.id.refresh)
        val delete = dialog_selected!!.findViewById<LinearLayout>(R.id.delete)
        val play = dialog_selected!!.findViewById<LinearLayout>(R.id.play)
        refresh.setOnClickListener {
            list_selected.clear()
            dialog_selected!!.hide()
        }
        delete.setOnClickListener {
            for (i in list_selected){
                list_now.removeAt(i)
            }
            list_selected.clear()
            dialog_selected!!.hide()
        }
        play.setOnClickListener {
            val list_ = mutableListOf<ModelAudio>()
            for (i in list_selected){
                list_.add(list_now[i])
            }
            list_now = list_
            startPlayMN()
            dialog_selected!!.hide()
        }
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

    private fun setInfoModelToViews (model: ModelAudio){
        name_file.text = model.name.toString()
        date_file.text = model.date.toString()
    }

    override fun getListSelectedFromSortDialog(list: MutableList<Int>) {
        list_selected = list
    }

    override fun getListClickMonth(list: MutableList<ModelAudio>) {
        dialog_calendar!!.hide()
        list_now = list
        startPlayMN()
    }
}