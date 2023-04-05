package com.example.videoeditorhack.other

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.example.videoeditorhack.R

class VideoProgressIndeterminateDialog(private var ctx: Context, private var message: String) : Dialog(ctx) {

    lateinit var progressBar: ProgressBar
    lateinit var messageLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.progress_loading_indeterminate)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(false)
        setCanceledOnTouchOutside(false)

        progressBar = findViewById(R.id.progress)
        messageLabel = findViewById(R.id.messageLabel)
        messageLabel.text = message

        messageLabel.typeface = FontsHelper[ctx, FontsConstants.SEMI_BOLD]
    }
}