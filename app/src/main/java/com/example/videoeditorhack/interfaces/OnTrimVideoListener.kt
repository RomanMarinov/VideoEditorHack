package com.example.videoeditorhack.interfaces

import android.net.Uri

interface OnTrimVideoListener {
    fun onTrimStarted()
    fun getResult(uri: Uri)
    fun cancelAction()
    fun onError(message: String)
}
