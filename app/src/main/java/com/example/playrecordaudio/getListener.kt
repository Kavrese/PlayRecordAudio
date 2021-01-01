package com.example.playrecordaudio

import com.example.playrecordaudio.model.ModelAudio

interface getListener {
    fun getModelFromSortDialog(model: ModelAudio){}
    fun getListFromSortDialog(list: MutableList<ModelAudio>){}
    fun getListSelectedFromSortDialog(list: MutableList<Int>){}
}