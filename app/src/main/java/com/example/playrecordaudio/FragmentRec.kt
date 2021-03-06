package com.example.playrecordaudio

import android.app.Dialog
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.playrecordaudio.model.ModelAudio
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_rec.*
import kotlinx.android.synthetic.main.save.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class FragmentRec: Fragment() {
    private var rec = false
    private var click = true
    private val path_files = File(Environment.getExternalStorageDirectory().toString() + "/.PlayRecordAudio/")
    private var saveModel: ModelAudio? = null
    private var mediaRecorder = MediaRecorder()
    private var saveFile: File? = null
    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private var isPlay = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rec, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Сейчашняя дата
        date.text = SimpleDateFormat("dd.MM.yyyy").format(Date())
        //Проверка на сущестование главного пути в локалке
        if (!path_files.exists()) {
            path_files.mkdir()
        }
        // Начало/Конец записи
        work_rec.setOnClickListener {
            //Проверка на пустоту имени
            if (name.text.isNotEmpty()) {
                //Проверка на возможность клика
                if (click) {
                    //Если запись шла - останавливаем
                    if (rec) {
                        stopRec()
                    //Иначе запускаем запись
                    } else {
                        // Делаем клик на 1/2 секунды не возможным
                        click = false
                        Handler().postDelayed({
                            click = true
                        }, 500)
                        //Берём сейчашнюю запись
                        saveModel = startRec()
                    }
                }
            }else{
                Toast.makeText(requireContext(), "Заполните поле имени", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startRec(): ModelAudio{
        val origin_date = Date()
        val id = SimpleDateFormat("ddMMyyyyHHmmss").format(origin_date)
        val name_file = "${name.text.toString()}&$id.mp3"
        val file = File(path_files.absolutePath, name_file)
        try {
            file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Инициализация записи голоса
        initRec()

        mediaRecorder.setOutputFile(file.toString())

        try {
            vibrate()   //Вибрация
            mediaRecorder.prepare() //Подготовка к записи
            mediaRecorder.start()
            work_rec.setImageResource(R.drawable.pause) //Меняем вид кнопки
            rec = true
            saveFile = file     //Указываем файл на сохраненние
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        //Возрашяем сейчашний файл
        return ModelAudio(name.text.toString(), id, date.text.toString(), file.toString(), InfoUser.avtor_id)
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
            mediaRecorder.release() //Перезапускаем
            work_rec.setImageResource(R.drawable.micro)     //Меняем вид кнопки
            rec = false
            vibrate()       //Вибрация
            toSave(saveFile!!)      //На сохранение
        }
    }

    private fun vibrate (){
        val v = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        v.vibrate(50)
    }

    private fun toSave(file: File){
        //Диалог для потвержденния записанного
        val dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.save)
        //Если пользователь хочет сохранить
        dialog.save.setOnClickListener {
            //Убираем диалог
            dialog.dismiss()
            //Убараем название файла из поля
            name.setText("")
            //Сохраняем в базе
            FirebaseDatabase.getInstance().reference.child("files").child(saveModel!!.id.toString()).setValue(saveModel
            ) { _: DatabaseError?, _: DatabaseReference ->
                Toast.makeText(requireContext(), "Сохраненно", Toast.LENGTH_SHORT).show()
            }
        }
        val date_: TextView = dialog.findViewById(R.id.date_dialog)
        val name_: TextView = dialog.findViewById(R.id.name_dialog)
        val play : ImageView = dialog.findViewById(R.id.play_save)
        //Поля для текста
        date_.text = date.text.toString()
        name_.text = name.text.toString()
        //Если ползователь не хочет сохранять
        dialog.close.setOnClickListener {
            //Удаляем аудио файл
            if (file.exists()) {
                file.delete()
            }
            //Закрываем диалог
            dialog.dismiss()
        }
        //Для воспроизводства записанного
        mediaPlayer = MediaPlayer.create(requireContext(), Uri.parse(saveModel!!.path))
        mediaPlayer.setOnCompletionListener {
            reset()
            play.setImageResource(R.drawable.play)
        }
        play.setOnClickListener {
            if (!isPlay){
                play()
                play.setImageResource(R.drawable.pause)
            }else{
                pause()
                play.setImageResource(R.drawable.play)
            }
        }
        //Клик вне диалога запрещённ
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun play(){
        mediaPlayer.start()
        isPlay = true
    }

    private fun pause(){
        mediaPlayer.pause()
        isPlay = false
    }

    private fun reset(){
        try {
            isPlay = false
            mediaPlayer.seekTo(0)       //В начало записи
        }catch (t: Throwable) {
            if (t.message != "") {
                Toast.makeText(requireContext(), t.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}