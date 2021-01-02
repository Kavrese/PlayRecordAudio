package com.example.playrecordaudio

import com.example.playrecordaudio.model.ModelAudio
import com.example.playrecordaudio.model.ModelMonth
import java.text.SimpleDateFormat
import java.util.*
import java.util.Arrays

class Sort {
    val UP = 0
    val DOWN = 1
    fun alphabetFile (list: MutableList<ModelAudio>, mode: Int): MutableList<ModelAudio>{
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

        if (mode == DOWN){
            return list_res
        }else{
            val list_res_reversed = mutableListOf<ModelAudio>()
            for (i in 0 until list_res.size){
                list_res_reversed.add(list_res[list_res.size - 1 - i])
            }
            return list_res_reversed
        }
    }

    fun dateFile(list: MutableList<ModelAudio>, mode: Int): MutableList<ModelAudio>{
        val formartSim = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val list_res = mutableListOf<ModelAudio>()
        for (i in 0 until list.size){
            var maxPos = 0
            var minPos = 0
            val max = Calendar.getInstance()
            max.time = Date(0, 0, 0, 0, 0, 0)
            val min = Calendar.getInstance()
            min.time = Date(9999, 12, 30, 23,59,59)
            val now = Calendar.getInstance()
            for (j in 0 until list.size){
                now.time = formartSim.parse(list[j].date!!)!!
                if (now > max) {
                    max.time = now.time
                    maxPos = j
                }
                if (now < min) {
                    min.time = now.time
                    minPos = j
                }
            }

            if (mode == UP){
                list_res.add(list[minPos])
                list.removeAt(minPos)
            }else{
                list_res.add(list[maxPos])
                list.removeAt(maxPos)
            }
        }
        return list_res
    }

    fun countFileMonth(list: MutableList<ModelMonth>, mode: Int): MutableList<ModelMonth>{
        val list_res: MutableList<ModelMonth> = mutableListOf()
        for (i in 0 until list.size){
            var maxPos = 0
            var minPos = 0
            var maxCount = 0
            var minCount = 999
            for (j in 0 until list.size){
                val nowCount = list[j].count!!
                if (nowCount > maxCount){
                    maxCount = nowCount
                    maxPos = j
                }
                if (nowCount < minCount){
                    minCount = nowCount
                    minPos = j
                }
            }

            if (mode == UP){
                list_res.add(list[minPos])
                list.removeAt(minPos)
            }else{
                list_res.add(list[maxPos])
                list.removeAt(maxPos)
            }
        }
        return list_res
    }

    fun alphabetMonth (list: MutableList<ModelMonth>, mode: Int): MutableList<ModelMonth>{
        var list_string = mutableListOf<String>()
        val list_res = mutableListOf<ModelMonth>()
        for (i in list){
            list_string.add(i.name_month!!)
        }
        val two_list = list_string.toTypedArray()
        Arrays.sort(two_list)
        list_string = two_list.toMutableList()

        for (i in list_string){
            for (j in list){
                if (i == j.name_month){
                    list_res.add(j)
                    list.remove(j)
                    break
                }
            }
        }

        return if (mode == DOWN){
            list_res
        }else{
            val list_res_reversed = mutableListOf<ModelMonth>()
            for (i in 0 until list_res.size){
                list_res_reversed.add(list_res[list_res.size - 1 - i])
            }
            list_res_reversed
        }
    }
}