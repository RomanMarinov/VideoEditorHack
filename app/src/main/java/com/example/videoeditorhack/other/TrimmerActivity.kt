package com.example.videoeditorhack.other

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract.Data
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.videoeditorhack.R
import com.example.videoeditorhack.databinding.ActivityTrimmerBinding
import com.example.videoeditorhack.interfaces.OnTrimVideoListener
import com.example.videoeditorhack.interfaces.OnVideoListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class TrimmerActivity : AppCompatActivity(), OnTrimVideoListener, OnVideoListener {

    lateinit var binding: ActivityTrimmerBinding

    private val progressDialog: VideoProgressIndeterminateDialog by lazy { VideoProgressIndeterminateDialog(this, "Cropping video. Please wait...") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_trimmer)
       // setContentView(R.layout.activity_trimmer)
Log.d("4444", " TrimmerActivity loaded")
      //  setupPermissions {
            val extraIntent = intent
            var path = ""
            extraIntent.getStringExtra(MainActivity.EXTRA_VIDEO_PATH)?.let {

            if (extraIntent != null) path = it
            }



            binding.videoTrimmer.setTextTimeSelectionTypeface(FontsHelper[this, FontsConstants.SEMI_BOLD])
                    .setOnTrimVideoListener(this)
                    .setOnVideoListener(this)
                    .setVideoURI(Uri.parse(path))
                    .setVideoInformationVisibility(true)
                    .setMaxDuration(10)
                    .setMinDuration(2)
                    .setDestinationPath(Environment.getExternalStorageDirectory().toString() + File.separator + "Zoho Social" + File.separator + "Videos" + File.separator)


        binding.back.setOnClickListener {
            binding.videoTrimmer.onCancelClicked()
        }

        binding.save.setOnClickListener {
            binding.videoTrimmer.onSaveClicked()
        }
    }

    override fun onTrimStarted() {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(baseContext, "Started Trimming", Toast.LENGTH_SHORT).show()
            progressDialog.show()
        }

//        RunOnUiThread(this).safely {
//
//        }
    }

    override fun getResult(uri: Uri) {

        CoroutineScope(Dispatchers.Main).launch {

            Toast.makeText(baseContext, "Video saved at ${uri.path}", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(baseContext, uri)


            val duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
            val width = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toLong()
            val height = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toLong()
            val values = ContentValues()
            values.put(MediaStore.Video.Media.DATA, uri.path)
            values.put(MediaStore.Video.VideoColumns.DURATION, duration)
            values.put(MediaStore.Video.VideoColumns.WIDTH, width)
            values.put(MediaStore.Video.VideoColumns.HEIGHT, height)

            contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)?.let {
                val id = ContentUris.parseId(it)
                Log.e("VIDEO ID", id.toString())
            }
        }

//        RunOnUiThread(this).safely {
//
//
//        }
    }

    override fun cancelAction() {

        CoroutineScope(Dispatchers.Main).launch {
            binding.videoTrimmer.destroy()
            finish()
        }
//        RunOnUiThread(this).safely {
//
//        }
    }

    override fun onError(message: String) {
        Log.e("ERROR", message)
    }

    override fun onVideoPrepared() {

        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(baseContext, "onVideoPrepared", Toast.LENGTH_SHORT).show()
        }

//        RunOnUiThread(this).safely {
//
//        }
    }

//    lateinit var doThis: () -> Unit
//    private fun setupPermissions(doSomething: () -> Unit) {
//        val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//        val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
//        doThis = doSomething
//        if (writePermission != PackageManager.PERMISSION_GRANTED && readPermission != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 101)
//        } else doThis()
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        when (requestCode) {
//            101 -> {
//                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                    PermissionsDialog(this@TrimmerActivity, "To continue, give Zoho Social access to your Photos.").show()
//                } else doThis()
//            }
//        }
//    }
}
