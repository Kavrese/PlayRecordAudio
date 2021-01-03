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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.delete_file_dialog.*
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
    var list_now_play: MutableList<ModelAudio> = mutableListOf()
    var playAll = false
    var positionPlay = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_head, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Запускаем автоматическое сравнение файлов из локалки и из firebase
        autoStartInstallFireBaseFiles(getLocalFiles(), list_all)
        work_img.setOnClickListener {
            if (now_model != null) {
                //Если файл выбран
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
            //Показываем диалог сортировки
            initSortDialog(false)
            dialog_sort!!.show()
        }

        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                //Перемещяем до указанных координат
                if (now_model != null){
                    mediaPlayer.seekTo(p1)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                //Если файл не выбран
                if (now_model == null){
                    p0!!.progress = 0
                    Toast.makeText(requireContext(), "Выберите файл",Toast.LENGTH_LONG).show()
                }
            }

        })

        //Клик на выброчное аоспроизведение
        ver_menu.setOnClickListener {
            if (list_selected.size == 0) {
                //Если нет выбранных файлов запускаем диалог сортировки с checkbox's
                initSortDialog(true)
                dialog_sort!!.show()
                Toast.makeText(requireContext(), "Сначало выберите файлы", Toast.LENGTH_LONG).show()
            }else{
                //Если есть выбраные файлы, то запускаем диалог с действия над ними
                initSelectedDialog()
                dialog_selected!!.show()
            }
        }
        //Воспроизводство всех файлов по очереди
        show_all.setOnClickListener {
            if (!playAll) {
                //Если ещё не начато воспроизводство
                list_now_play = list_all
                //Запуск всех файлов последовательно
                startPlayMN()
            }else{
                Toast.makeText(requireContext(), "Уже играет", Toast.LENGTH_SHORT).show()
            }
        }

        calendar.setOnClickListener {
            //Запуск диалога календаря
            initCalendarDialog()
            dialog_calendar!!.show()
        }
    }

    //Метод воспроизвлдства множества файлов
    private fun startPlayMN (){
        positionPlay = 0
        //Выбираем файл для вопроизводства
        now_model = list_now_play[positionPlay]
        //Инициализируем с новым фалом
        initMediaPlayer()
        playAll = true
        //Показываем инфу об файле
        setInfoModelToViews(now_model!!)
        //Начинаем проигрывать
        play()
    }

    private fun initMediaPlayer(){
        if (now_model != null) {
            //Если есть что проигрывать
            mediaPlayer = MediaPlayer.create(requireContext(), Uri.parse(now_model!!.path))
            //Данные об файле в seekbar
            seekBar.max = mediaPlayer.duration
            seekBar.progress = 0
            mediaPlayer.setOnCompletionListener {
                //При завершении меняем всё
                resetPlay()
                //Если есть что дальше вопроизводить - воспроизводис
                if (playAll && (positionPlay < list_now_play.size - 1) ){
                    //Меняем позицию текущего файда
                    positionPlay += 1
                    now_model = list_now_play[positionPlay]
                    initMediaPlayer()
                    setInfoModelToViews(now_model!!)
                    play()
                }
            }
        }else{
            Toast.makeText(requireContext(), "Выберите файл",Toast.LENGTH_LONG).show()
        }
    }

    //Метод перезапуска текущего файла
    private fun resetPlay(){
        try {
            isPlay = false
            //Меняем вид кнопки
            work_img.setImageResource(R.drawable.play)
            //Перемещяем в начало
            mediaPlayer.seekTo(0)
        }catch (t: Throwable) {
            if (t.message != "") {
                Toast.makeText(requireContext(), t.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    //Метод остановки воспроизведения текущего файла
    private fun stopPLay (){
        try {
            isPlay = false
            work_img.setImageResource(R.drawable.play)
            mediaPlayer.stop()
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
        //Пауза с двайным кликом для переигры аудио
        activ_double_click_pause = !activ_double_click_pause
        if (activ_double_click_pause){
            //Если по достижению 150 милисек будет обнаружен второй клик, то перезапускаем
            Handler().postDelayed(
                {
                    if (!activ_double_click_pause){
                        //Обнаружен второй клик
                        mediaPlayer.seekTo(0)
                        Toast.makeText(requireContext(), "Reset", Toast.LENGTH_LONG).show()
                    }else{
                        //Обычная пауза
                        mediaPlayer.pause()
                        isPlay = false
                        work_img.setImageResource(R.drawable.play)
                        activ_double_click_pause = false
                    }
                }, 150
            )
        }
    }
    //Диалог сортировки
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

        //Дальше клики на фильтры
        dateUp.setOnClickListener {
            list_now_play = Sort().dateFile(list_all.toTypedArray().clone().toMutableList(), Sort().UP)
            newListToRecFile(rec, show_checkBox)
        }

        dateDown.setOnClickListener {
            list_now_play = Sort().dateFile(list_all.toTypedArray().clone().toMutableList(), Sort().DOWN)
            newListToRecFile(rec, show_checkBox)
        }

        clear.setOnClickListener {
            list_now_play = list_all
            newListToRecFile(rec, show_checkBox)
        }

        alphabetUp.setOnClickListener {
            list_now_play = Sort().alphabetFile(list_all.toTypedArray().clone().toMutableList(), Sort().UP)
            newListToRecFile(rec, show_checkBox)
        }

        alphabetDown.setOnClickListener {
            list_now_play = Sort().alphabetFile(list_all.toTypedArray().clone().toMutableList(), Sort().DOWN)
            newListToRecFile(rec, show_checkBox)
        }

        rec.apply {
            adapter = AdapterFiles(
                list_now_play,
                this@FragmentHead,
                show_checkBox
            )
            layoutManager = LinearLayoutManager(requireContext())
        }
        //Кнопка назад
        back.setOnClickListener {
            dialog_sort!!.hide()
        }
        //Клик вне диалога запрещённ
        dialog_sort!!.setCanceledOnTouchOutside(false)
    }

    //Новый лист для сейчашний действий
    private fun newListToRecFile(rec: RecyclerView, show_checkBox: Boolean){
        rec.adapter = AdapterFiles(
            list_now_play,
            this@FragmentHead,
            show_checkBox
        )
    }

    //Новый лист для месяцев
    private fun newListToRecMonth(rec: RecyclerView,list: MutableList<ModelMonth>){
        rec.adapter = AdapterMonth(
            list,
            this@FragmentHead
        )
    }
    //Диалог календаря
    private fun initCalendarDialog(){
        dialog_calendar = Dialog(requireContext())
        dialog_calendar!!.setContentView(R.layout.calendar_dialog)
        val rec = dialog_calendar!!.findViewById<RecyclerView>(R.id.rec)
        val back = dialog_calendar!!.findViewById<ImageView>(R.id.back)
        val textFileUp = dialog_calendar!!.findViewById<TextView>(R.id.textFileUp)
        val textFileDown = dialog_calendar!!.findViewById<TextView>(R.id.textFileDown)
        val textAlphabetUp = dialog_calendar!!.findViewById<TextView>(R.id.textAlphabetUp)
        val textAlphabetDown = dialog_calendar!!.findViewById<TextView>(R.id.textAlphabetDown)
        val clear = dialog_calendar!!.findViewById<TextView>(R.id.clear)
        var now_list_month = generateListFileToListMonth(list_all)
        val list_month_all = now_list_month.toTypedArray().clone().toMutableList()

        rec.apply {
            adapter = AdapterMonth(now_list_month,this@FragmentHead)
            layoutManager = LinearLayoutManager(requireContext())
        }

        clear.setOnClickListener {
            now_list_month = list_month_all.toTypedArray().clone().toMutableList()
            newListToRecMonth(rec, now_list_month)
        }

        back.setOnClickListener {
            dialog_calendar!!.hide()
        }

        //Дальше идут фильтры
        textFileUp.setOnClickListener {
            now_list_month = Sort().countFileMonth(now_list_month.toTypedArray().clone().toMutableList(), Sort().UP)
            newListToRecMonth(rec, now_list_month)
        }

        textFileDown.setOnClickListener {
            now_list_month = Sort().countFileMonth(now_list_month.toTypedArray().clone().toMutableList(), Sort().DOWN)
            newListToRecMonth(rec, now_list_month)
        }

        textAlphabetUp.setOnClickListener {
            now_list_month = Sort().alphabetMonth(now_list_month.toTypedArray().clone().toMutableList(), Sort().UP)
            newListToRecMonth(rec, now_list_month)
        }

        textAlphabetDown.setOnClickListener {
            now_list_month = Sort().alphabetMonth(now_list_month.toTypedArray().clone().toMutableList(), Sort().DOWN)
            newListToRecMonth(rec, now_list_month)
        }
    }

    //Генерация листа месяцев с кол-во файлов для них
    private fun generateListFileToListMonth(list_file: MutableList<ModelAudio>): MutableList<ModelMonth>{
        val list_month = mutableListOf<ModelMonth>()
        for (i in 0..11){
            val cal = Calendar.getInstance()
            cal.set(Calendar.MONTH, i)
            //Название месяца
            val month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale("Ru"))
            val list_file_month = mutableListOf<ModelAudio>()
            //Поиск файлов это-го месяца по его номеру
            for (f in list_file){
                val numMonthF = Integer.parseInt(f.date!!.substring(3,5)) - 1
                if(numMonthF == i){
                    //Лист файлов это-го месяца
                    list_file_month.add(f)
                }
            }
            //Добывляем новый месяц
            list_month.add(ModelMonth(month, list_file_month.size, list_file_month))
        }
        return list_month
    }

    private fun initSelectedDialog(){
        //Диаолог выбора над выбранами файлами
        dialog_selected = Dialog(requireContext(), R.style.DialogTheme)
        dialog_selected!!.setContentView(R.layout.edit_selected_items)
        val refresh = dialog_selected!!.findViewById<LinearLayout>(R.id.refresh)
        val delete = dialog_selected!!.findViewById<LinearLayout>(R.id.delete)
        val play = dialog_selected!!.findViewById<LinearLayout>(R.id.play)

        refresh.setOnClickListener {
            //Сброс выбраных файлов
            list_selected.clear()
            list_now_play = list_all
            dialog_selected!!.hide()
        }
        delete.setOnClickListener {
            //Удаление выбранных файлов
            for (i in list_selected){
                list_now_play.removeAt(i)
            }
            list_selected.clear()
            dialog_selected!!.hide()
        }
        play.setOnClickListener {
            //Воспроизведение выбранных файлов
            val list_ = mutableListOf<ModelAudio>()
            //list_selected - лист с позициами выбранных файлов
            //Достаём эти файлы
            for (i in list_selected){
                list_.add(list_now_play[i])
            }
            //Клонируем все элементы в сейчашний лист
            list_now_play = list_
            //Начинаем их воспроизводство
            startPlayMN()
            dialog_selected!!.hide()
        }
    }
    //Метод взятия файлов из локалки
    private fun getLocalFiles(): MutableList<ModelAudio> {
        val list_= mutableListOf<ModelAudio>()
        //Лист всех файлов
        val listFiles = path_files.listFiles()
        for (i in  0..listFiles.size - 1){
            val file = listFiles[i].name    //Сам файл
            val name = file.substringBefore("&")        //Имя файла
            val id = file.substringAfter("&").substringBefore(".")  //Id Файла
            val date = id.substring(0, 8)   //Дата файла
            val new_date = SimpleDateFormat("ddMMyyyy").parse(date)     //Дата файла в ввиде типа данных Date()
            list_.add(ModelAudio(name, id, SimpleDateFormat("dd.MM.yyyy").format(new_date), listFiles[i].toString()))
        }
        return list_
    }

    //Метод автоматического взятия и сравнения файлов bd и local
    private fun autoStartInstallFireBaseFiles(local_files: MutableList<ModelAudio>, list: MutableList<ModelAudio>){
        //Получаем файлы
        FirebaseDatabase.getInstance().reference.child("files").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val firebase_files: MutableList<ModelAudio> = mutableListOf()
                for (sn: DataSnapshot in snapshot.children){
                    //Получаем каждый файл в читаемом ввиде
                    firebase_files.add(sn.getValue(ModelAudio::class.java)!!)
                }
                //Синхронизируем файлы двух списков
                synFireBaseAndLocalFiles(local_files, firebase_files, list)
            }
        })
    }
    //Метод синхронизации файлов двух списков
    private fun synFireBaseAndLocalFiles(local_files: MutableList<ModelAudio>, firebase_files: MutableList<ModelAudio>, list: MutableList<ModelAudio>){
        //Лист файлов на удаление
        val list_to_delete = mutableListOf<ModelAudio>()
        //Поиск фалов в локалке, которых нет в firebase
        for (i in local_files){
            var find = false
            for (j in firebase_files){
                if (i.path == j.path){
                    find = true
                    list.add(i)
                    break
                }
            }
            if (!find){
                //Если файл не нашди в firebase, то в лист удаления
                list_to_delete.add(i)
            }
        }

        if (list_to_delete.size != 0){
            //Если лист на удаление не пустой, то вызываем dialog
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.delete_file_dialog)
            //Пользователь хочет удалить эти файлы
            dialog.delete.setOnClickListener {
                //Удаляем эти файлы
                deleteFromList(list_to_delete)
                list_now_play = list.toTypedArray().clone().toMutableList()
                dialog.dismiss()
            }
            //Пользователь хочет оставить эти файлы
            dialog.close.setOnClickListener {
                for (i in list_to_delete){
                    list.add(i)
                }
                dialog.dismiss()
                list_now_play = list.toTypedArray().clone().toMutableList()
            }
            dialog.show()
        }
    }

    // Метод удаление файлов из листа
    private fun deleteFromList (list_to_delete: MutableList<ModelAudio>){
        for (i in list_to_delete){
            val file = i.path
            //Если файл существует - удаляем
            if(File(file!!).exists()){
                File(file).delete()
            }else{
                Toast.makeText(requireContext(), "Error delete path $file", Toast.LENGTH_LONG).show()
            }
        }
    }

    //Метод вставки инфы из model во view's
    private fun setInfoModelToViews (model: ModelAudio){
        name_file.text = model.name.toString()
        date_file.text = model.date.toString()
    }

    //Метод интерфейса получения выбранных файлов из диалога сортировки
    override fun getListSelectedFromSortDialog(list: MutableList<Int>) {
        list_selected = list
    }
    //Метод интерфейса для получения листа выбранного месяца из диалога календаря
    override fun getListClickMonth(list: MutableList<ModelAudio>) {
        dialog_calendar!!.hide()
        list_now_play = list
        //После получения файлов открываем диалог сортировки
        initSortDialog(false)
        dialog_sort!!.show()
    }

    ////Метод интерфейса для получения model выбранного из диалога сортировки на проигрывание
    override fun getModelFromSortDialog(model: ModelAudio) {
        //Если играет файл - останавиваем
        if (isPlay) {
            stopPLay()
        }
        dialog_sort!!.hide()
        now_model = model
        initMediaPlayer()
        setInfoModelToViews(now_model!!)
    }
}