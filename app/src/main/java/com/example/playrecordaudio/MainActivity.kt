package com.example.playrecordaudio

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.playrecordaudio.model.ModelAudio
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(), ActualList{
    private var nowFragment: Fragment? = FragmentHead()
    var main = true
    var list: MutableList<ModelAudio> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPer()
        //Клик на вкладку прослушивания
        shape_head.setOnClickListener {
            setFragment(FragmentHead())
        }

        //Клик на вкладку записи
        shape_rec.setOnClickListener {
            setFragment(FragmentRec())
        }
    }

    //Метод установки нового fragment
    private fun setFragment(fragment: Fragment){
        if (checkPer()) {
            val fr = supportFragmentManager
            val tr = fr.beginTransaction()
            //Добавляем новый fragment
            tr.add(R.id.frameLayout, fragment)
            tr.commit()
            //Какой сейчас фрагмент
            nowFragment = fragment
            //Запускаем анимацию появления fragment
            motion_win.transitionToEnd()
            //Сейчас не в главном меню
            main = false
        }else{
            Toast.makeText(applicationContext, "Предоставте разрешенния",Toast.LENGTH_LONG).show()
        }
    }

    override fun onBackPressed() {
        if (!main){
            //Если не в меню - запускаем анимацию сворачивания fragment
            motion_win.transitionToStart()
            main = true
        }else{
            //Выходим из приложенния
            exitProcess(1)
        }
    }

    private fun checkPer(): Boolean{
        return if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.VIBRATE)
            ActivityCompat.requestPermissions(this, permissions,0)
            false
        }else{
            true
        }
    }

    override fun newElem(elem: ModelAudio) {
        list.add(elem)
    }
}