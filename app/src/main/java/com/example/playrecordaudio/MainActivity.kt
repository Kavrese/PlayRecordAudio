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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(){
    var main = true
    private var nowFragment: Fragment? = FragmentHead()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkAllPer()
        //Клик на вкладку прослушивания
        shape_head.setOnClickListener {
            setFragment(FragmentHead())
        }

        //Клик на вкладку записи
        shape_rec.setOnClickListener {
            setFragment(FragmentRec())
        }
        //Запуск стартовой анимации через 3000 милл
        Handler().postDelayed({
            lin_main.transitionToEnd()
            Handler().postDelayed({
                //Логика показа анимации окна авторизации
                if(FirebaseAuth.getInstance().currentUser == null) {
                    lin_main.setTransition(R.id.auth)
                    lin_main.transitionToEnd()
                }
                val name = FirebaseAuth.getInstance().currentUser!!.email!!.substringBefore("@")
                FirebaseDatabase.getInstance().reference.child("users").child(name).child("id").addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_LONG).show()
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        InfoUser.name = name
                        InfoUser.avtor_id = snapshot.getValue(Int::class.java)
                    }

                })
            }, 1200)

        }, 3000)

        enter.setOnClickListener {
            val pass = password.text.toString()
            val email = email.text.toString()
            if (pass.length >= 6){
                if ("@" in email){
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener {
                            lin_main.transitionToStart()
                        }
                        .addOnCanceledListener {
                            Toast.makeText(this, "Error OnCanceledListener", Toast.LENGTH_LONG).show()
                        }
                }else{
                    Toast.makeText(this, "Email должен иметь @", Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(this, "Пароль должен быть минимум 6 символов", Toast.LENGTH_LONG).show()
            }
        }
    }

    //Метод установки нового fragment
    private fun setFragment(fragment: Fragment){
        if (checkAllPer()) {
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
            //Выходим из приложенния с анимацией
            lin_main.setTransition(R.id.tra_screen)
            lin_main.transitionToStart()
            Handler().postDelayed({
                exitProcess(1)
            }, 2000)
        }
    }
    //Метод проверки разрешений
    private fun checkAllPer(): Boolean{
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 0)
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 2)
        }

        return (ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        }
}