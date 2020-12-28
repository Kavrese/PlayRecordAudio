package com.example.playrecordaudio

import android.app.Dialog
import android.content.Context
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.playrecordaudio.model.ModelAudio
import kotlinx.android.synthetic.main.fragment_rec.*
import kotlinx.android.synthetic.main.save.*
import java.io.File
import java.io.IOException
import java.lang.ClassCastException
import java.text.SimpleDateFormat
import java.util.*


class FragmentRec: Fragment() {
    private var rec = false
    private var click = true
    private val path_files = File(Environment.getExternalStorageDirectory().toString() + "/.PlayRecordAudio/")
    private var saveModel: ModelAudio? = null
    private var mediaRecorder = MediaRecorder()
    private var saveFile: File? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rec, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        date.text = SimpleDateFormat("dd.MM.yyyy").format(Date())
        if (!path_files.exists()) {
            path_files.mkdir()
        }

        initRec()
        work_rec.setOnClickListener {
            if (name.text.isNotEmpty()) {
                if (click) {
                    if (rec) {
                        stopRec()
                    } else {
                        click = false
                        Handler().postDelayed({
                            click = true
                        }, 500)
                        saveModel = startRec()
                    }
                }
            }else{
                Toast.makeText(requireContext(), "Заполните поле имени", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startRec(): ModelAudio{
        val date = SimpleDateFormat("ddMMyyyyHHmmss").format(Date())
        val name_file = "$date.mp3"
        val file = File(path_files.absolutePath, name_file)
        try {
            file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        initRec()

        mediaRecorder.setOutputFile(file.toString())

        try {
            vibrate()
            mediaRecorder.prepare()
            mediaRecorder.start()
            work_rec.setImageResource(R.drawable.pause)
            rec = true
            saveFile = file
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ModelAudio(name.text.toString(), date, file.toString())
    }

    private fun initRec(){
        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
    }

    private fun stopRec(){
        if (rec) {
            mediaRecorder.stop()
            mediaRecorder.release()
            work_rec.setImageResource(R.drawable.micro)
            rec = false
            vibrate()
            toSave(saveFile!!)
            val inter = requireActivity() as ActualList
            try {
                inter.newElem(saveModel!!)
            }catch (ignore: ClassCastException){}
        }
    }

    private fun vibrate (){
        val v = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        v.vibrate(50)
    }

    private fun toSave(file: File){
        val dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.save)
        dialog.save.setOnClickListener {
            dialog.dismiss()
        }
        val date_: TextView = dialog.findViewById(R.id.date_dialog)
        val name_: TextView = dialog.findViewById(R.id.name_dialog)
        date_.text = date.text.toString()
        name_.text = name.text.toString()
        dialog.close.setOnClickListener {
            if (file.exists()) {
                file.delete()
            }
            dialog.dismiss()
        }
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }
}