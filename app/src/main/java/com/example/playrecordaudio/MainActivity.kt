package com.example.playrecordaudio

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private var nowFragment: Fragment? = FragmentHead()
    var main = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        val fr = supportFragmentManager
        val tr = fr.beginTransaction()
        //Добавляем новый fragment
        tr.add(R.id.frameLayout,fragment)
        tr.commit()
        //Какой сейчас фрагмент
        nowFragment = fragment
        //Запускаем анимацию появления fragment
        motion_win.transitionToEnd()
        //Сейчас не в главном меню
        main = false
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
}