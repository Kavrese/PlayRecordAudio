package com.example.playrecordaudio

import com.example.playrecordaudio.model.ModelAudio

interface getListener {
    fun getModelFromSortDialog(model: ModelAudio){}
    fun getListSelectedFromSortDialog(list: MutableList<Int>){}
    fun getListClickMonth(list: MutableList<ModelAudio>)
}