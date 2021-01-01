package com.example.playrecordaudio

import com.example.playrecordaudio.model.ModelAudio
import java.util.*
import java.util.Arrays

class Sort {
    val UP = 0
    val DOWN = 1
    fun alphabet (list: MutableList<ModelAudio>, mode: Int): MutableList<ModelAudio>{
        var list_string = mutableListOf<String>()
        val list_res = mutableListOf<ModelAudio>()
        for (i in list){
            list_string.add(i.name!!)
        }
        val two_list = list_string.toTypedArray()
        Arrays.sort(two_list)
        list_string = two_list.toMutableList()

        for (i in list_string){
            for (j in list){
                if (i == j.name!!){
                    list_res.add(j)
                    list.remove(j)
                    break
                }
            }
        }

        if (mode == UP){
            return list_res
        }else{
            val list_res_reversed = mutableListOf<ModelAudio>()
            for (i in 0 until list_res.size){
                list_res_reversed.add(list_res[list_res.size - 1 - i])
            }
            return list_res_reversed
        }
    }

    fun date(list: MutableList<ModelAudio>, mode: Int){


    }
}