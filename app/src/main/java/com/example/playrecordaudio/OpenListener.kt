package com.example.playrecordaudio

import com.example.playrecordaudio.model.ModelAudio

interface OpenListener {
    fun openFromSortDialog(model: ModelAudio){}
}