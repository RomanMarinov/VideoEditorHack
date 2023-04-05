package com.example.videoeditorhack.other

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.videoeditorhack.R
import com.example.videoeditorhack.utils.FileUtils
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util


class MainActivity : AppCompatActivity() {
    private lateinit var mediaSource: MediaSource
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("4444", " MainActivity loaded")
      //  trimmerButton.setOnClickListener { pickFromGallery(REQUEST_VIDEO_TRIMMER) }
     //   cropperButton.setOnClickListener { pickFromGallery(REQUEST_VIDEO_CROPPER) }


//        val urlType = URLType.HLS
//        val codec265 = "https://dvr4.baza.net/(baza.net).bazanet.ogorodnyj.pereulok.7.(vhod.v.ofis)-7bf166f80e/index.m3u8?token=3.lo4ORUCjAAAAAAAAAfsABfbGP2XS39zMPCRnv46Gq_-81p2SO20rr5g5"
        val codec264 = "https://sputnikdvr1.baza.net/e344f4bb-fd97-4f7d-b12c-2aab350abcdd/index.m3u8?token=MWFkZTQwZGY4NWEwZmJmMjBjNTNlMGNiNzI1Mjc1MTI1N2M4ZWY3Ny4xNjc3MTY0MTIx"
//        val original = "https://Zvun8_uBLxVeIGMNgt3NhqhFJIY@cloud.vsaas.io/magazine-8234e895e9"
//
//        urlType.url = codec264
//
//        exoPlayer.seekTo(0)
//
//        when (urlType) {
//            URLType.MP4 -> {
//                val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
//                    this, Util.getUserAgent(this, getApplicationName()))
//                mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
//                    .createMediaSource(MediaItem.fromUri(Uri.parse(urlType.url)))
//            }
//            URLType.HLS -> {
//                val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
//                    context, Util.getUserAgent(context, getApplicationName()))
//                mediaSource = HlsMediaSource.Factory(dataSourceFactory)
//                    .createMediaSource(MediaItem.fromUri(Uri.parse(urlType.url)))
//            }
//        }
        startTrimActivity(Uri.parse(codec264))



    }
//
//    private fun getApplicationName(): String {
//        val pm: PackageManager = this.applicationContext.packageManager
//        val applicationInfo: ApplicationInfo? =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                pm.getApplicationInfo(this.packageName, PackageManager.ApplicationInfoFlags.of(0))
//            } else {
//                null
//            }
//        return applicationInfo.toString()
//    }

//    private fun pickFromGallery(intentCode: Int) {
//        setupPermissions {
//            val intent = Intent()
//            intent.setTypeAndNormalize("video/*")
//            intent.action = Intent.ACTION_GET_CONTENT
//            intent.addCategory(Intent.CATEGORY_OPENABLE)
//            startActivityForResult(Intent.createChooser(intent, getString(R.string.label_select_video)), intentCode)
//        }
//    }

//    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (resultCode == Activity.RESULT_OK) {
//            if (requestCode == REQUEST_VIDEO_TRIMMER) {
//                val selectedUri = data!!.data
//                if (selectedUri != null) {
//                    startTrimActivity(selectedUri)
//                } else {
//                    Toast.makeText(this@MainActivity, R.string.toast_cannot_retrieve_selected_video, Toast.LENGTH_SHORT).show()
//                }
//            } else if (requestCode == REQUEST_VIDEO_CROPPER) {
//                val selectedUri = data!!.data
//                if (selectedUri != null) {
//                    startCropActivity(selectedUri)
//                } else {
//                    Toast.makeText(this@MainActivity, R.string.toast_cannot_retrieve_selected_video, Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data)
//    }

    private fun startTrimActivity(uri: Uri) {
        val intent = Intent(this, TrimmerActivity::class.java)
        intent.putExtra(EXTRA_VIDEO_PATH, FileUtils.getPath(this, uri))
        startActivity(intent)
    }

    private fun startCropActivity(uri: Uri) {
        val intent = Intent(this, CropperActivity::class.java)
        intent.putExtra(EXTRA_VIDEO_PATH, FileUtils.getPath(this, uri))
        startActivity(intent)
    }

    companion object {
        private const val REQUEST_VIDEO_TRIMMER = 0x01
        private const val REQUEST_VIDEO_CROPPER = 0x02
        internal const val EXTRA_VIDEO_PATH = "EXTRA_VIDEO_PATH"
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

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        when (requestCode) {
//            101 -> {
//                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                    PermissionsDialog(this@MainActivity, "To continue, give Zoho Social access to your Photos.").show()
//                } else doThis()
//            }
//        }
//    }
}
