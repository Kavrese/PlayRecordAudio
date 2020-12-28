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

        shape_head.setOnClickListener {
            setFragment(FragmentHead())
            motion_win.transitionToEnd()
            main = false
        }

        shape_rec.setOnClickListener {
         //   motion_win.transitionToEnd()
          //  main = false
        }
    }

    private fun setFragment(fragment: Fragment){
        val fr = supportFragmentManager
        val tr = fr.beginTransaction()
        tr.hide(nowFragment!!)
        tr.add(R.id.frameLayout,fragment)
        tr.commit()
        nowFragment = fragment
    }

    override fun onBackPressed() {
        if (!main){
            motion_win.transitionToStart()
            Handler().postDelayed({
                val fr = supportFragmentManager
                val tr = fr.beginTransaction()
                tr.hide(nowFragment!!)
                tr.commit()
                main = true
            }, 1100)
        }else{
            exitProcess(1)
        }
    }
}