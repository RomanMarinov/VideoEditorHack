package com.example.videoeditorhack.view

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnErrorListener
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import com.example.videoeditorhack.R
import com.example.videoeditorhack.databinding.ViewTrimmerBinding
import com.example.videoeditorhack.interfaces.OnProgressVideoListener
import com.example.videoeditorhack.interfaces.OnRangeSeekBarListener
import com.example.videoeditorhack.interfaces.OnTrimVideoListener
import com.example.videoeditorhack.interfaces.OnVideoListener
import com.example.videoeditorhack.utils.RealPathUtil
import com.example.videoeditorhack.utils.TrimVideoUtils
import com.example.videoeditorhack.utils.UiThreadExecutor
import com.example.videoeditorhack.utils.VideoOptions
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

import kotlinx.coroutines.*
import java.io.File
import java.lang.ref.WeakReference
import java.util.*


class VideoTrimmer @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaSource: MediaSource
    private lateinit var icon_video_play: ImageView
    private lateinit var layout_surface_view: RelativeLayout

    lateinit var binding: ViewTrimmerBinding


    ////////////////////////////
    private lateinit var mSrc: Uri
    private var mFinalPath: String? = null

    private var mMaxDuration: Int = -1
    private var mMinDuration: Int = -1
    private var mListeners: ArrayList<OnProgressVideoListener> = ArrayList()

    private var mOnTrimVideoListener: OnTrimVideoListener? = null
    private var mOnVideoListener: OnVideoListener? = null

    private var mDuration = 0f
    private var mTimeVideo = 0f
    private var mStartPosition = 0f

    private var mEndPosition = 0f
    private var mResetSeekBar = true
    private val mMessageHandler = MessageHandler(this)

    private var destinationPath: String
        get() {
            if (mFinalPath == null) {
                val folder = Environment.getExternalStorageDirectory()
                mFinalPath = folder.path + File.separator
            }
            return mFinalPath ?: ""
        }
        set(finalPath) {
            mFinalPath = finalPath
        }

    init {
        init(context)
    }

    private fun init(context: Context) {

        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_trimmer, this, true)

//        LayoutInflater.from(context).inflate(R.layout.view_trimmer, this, true)
        setUpListeners()
        setUpMargins()

        ///////////////////////////////
        exoPlayer = ExoPlayer.Builder(context).build()
        // exoPlayer.addListener(errorPlayerListener)
     //   exoPlayer.addListener(touchPlayer)
        binding.playerView.player = exoPlayer

        createMediaSource()

        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        ///////////////////////////////////
    }

    //////////////////////////////
    private fun createMediaSource() {
        val urlType = URLType.HLS
        val codec265 = "https://dvr4.baza.net/(baza.net).bazanet.ogorodnyj.pereulok.7.(vhod.v.ofis)-7bf166f80e/index.m3u8?token=3.lo4ORUCjAAAAAAAAAfsABfbGP2XS39zMPCRnv46Gq_-81p2SO20rr5g5"
        val codec264 = "https://sputnikdvr1.baza.net/e344f4bb-fd97-4f7d-b12c-2aab350abcdd/index.m3u8?token=MWFkZTQwZGY4NWEwZmJmMjBjNTNlMGNiNzI1Mjc1MTI1N2M4ZWY3Ny4xNjc3MTY0MTIx"
        val original = "https://Zvun8_uBLxVeIGMNgt3NhqhFJIY@cloud.vsaas.io/magazine-8234e895e9"

        urlType.url = codec264

        exoPlayer.seekTo(0)

        when (urlType) {
            URLType.MP4 -> {
                val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                    context, Util.getUserAgent(context, getApplicationName()))
                mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(Uri.parse(urlType.url)))
            }
            URLType.HLS -> {
                val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                    context, Util.getUserAgent(context, getApplicationName()))
                mediaSource = HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(Uri.parse(urlType.url)))
            }
        }
    }

    private fun getApplicationName(): String {
        val pm: PackageManager = context.applicationContext.packageManager
        val applicationInfo: ApplicationInfo? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(context.packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                null
            }
        return applicationInfo.toString()
    }

    ////////////////////////////////





    private fun setUpListeners() {
        mListeners = ArrayList()
        mListeners.add(object : OnProgressVideoListener {
            override fun updateProgress(time: Float, max: Float, scale: Float) {
                updateVideoProgress(time)
            }
        })

        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                onClickVideoPlayPause()
                return true
            }
        })
        // playerView
//        video_loader.setOnErrorListener { _, what, _ ->
//            mOnTrimVideoListener?.onError("Something went wrong reason : $what")
//            false
//        }
// playerView

//        video_loader.setOnTouchListener { _, event ->
//            gestureDetector.onTouchEvent(event)
//            true
//        }



        val touchPlayer =
            object : OnTouchListener, Player.Listener {
                override fun onTouch(v: View?, event: MotionEvent): Boolean {
                    gestureDetector.onTouchEvent(event)
                    return true
                }

            }
        val errorPlayer =
            object : OnErrorListener, Player.Listener {
                override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                    mOnTrimVideoListener?.onError("Something went wrong reason : $what")
                    return false
                }


            }
        exoPlayer.addListener(touchPlayer)
        exoPlayer.addListener(errorPlayer)

//// playerView
//        video_loader.setOnErrorListener { _, what, _ ->
//            mOnTrimVideoListener?.onError("Something went wrong reason : $what")
//            false
//        }
//// playerView
//        video_loader.setOnTouchListener { _, event ->
//            gestureDetector.onTouchEvent(event)
//            true
//        }

        binding.handlerTop.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                onPlayerIndicatorSeekChanged(progress, fromUser)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                onPlayerIndicatorSeekStart()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                onPlayerIndicatorSeekStop(seekBar)
            }
        })

        binding.timeLineBar.addOnRangeSeekBarListener(object : OnRangeSeekBarListener {
            override fun onCreate(rangeSeekBarView: RangeSeekBarView, index: Int, value: Float) {
            }

            override fun onSeek(rangeSeekBarView: RangeSeekBarView, index: Int, value: Float) {
                binding.handlerTop.visibility = View.GONE
                onSeekThumbs(index, value)
            }

            override fun onSeekStart(rangeSeekBarView: RangeSeekBarView, index: Int, value: Float) {
            }

            override fun onSeekStop(rangeSeekBarView: RangeSeekBarView, index: Int, value: Float) {
                onStopSeekThumbs()
            }
        })
// exoPlayer

//        video_loader.setOnPreparedListener { mp -> onVideoPrepared(mp) }
//        video_loader.setOnCompletionListener { onVideoCompleted() }

        val preparedListener = object : OnPreparedListener, Player.Listener {
            override fun onPrepared(mp: MediaPlayer) {
                onVideoPrepared(mp)
            }

        }
        val completionListener = object : OnCompletionListener, Player.Listener {
            override fun onCompletion(mp: MediaPlayer?) {
                onVideoCompleted()
            }


        }
        exoPlayer.addListener(preparedListener)
        exoPlayer.addListener(completionListener)



    }

    private fun onPlayerIndicatorSeekChanged(progress: Int, fromUser: Boolean) {
        val duration = (mDuration * progress / 1000L)
        if (fromUser) {
            if (duration < mStartPosition) setProgressBarPosition(mStartPosition)
            else if (duration > mEndPosition) setProgressBarPosition(mEndPosition)
        }
    }

    private fun onPlayerIndicatorSeekStart() {
        mMessageHandler.removeMessages(SHOW_PROGRESS)
        // exoPlayer
        exoPlayer.pause()
       // video_loader.pause()

        icon_video_play = findViewById(R.id.icon_video_play)

        icon_video_play.visibility = View.VISIBLE
        notifyProgressUpdate(false)
    }

    private fun onPlayerIndicatorSeekStop(seekBar: SeekBar) {
        mMessageHandler.removeMessages(SHOW_PROGRESS)
        // exoPlayer
        exoPlayer.pause()
      //  video_loader.pause()

        icon_video_play = findViewById(R.id.icon_video_play)
        icon_video_play.visibility = View.VISIBLE

        val duration = (mDuration * seekBar.progress / 1000L).toInt()
        // exoPlayer
        exoPlayer.seekTo(duration.toLong())
//        video_loader.seekTo(duration)
        notifyProgressUpdate(false)
    }

    private fun setProgressBarPosition(position: Float) {
        if (mDuration > 0) binding.handlerTop.progress = (1000L * position / mDuration).toInt()
    }

    private fun setUpMargins() {
        val marge = binding.timeLineBar.thumbs[0].widthBitmap
        val lp = binding.timeLineView.layoutParams as LayoutParams
        lp.setMargins(marge, 0, marge, 0)
        binding.timeLineView.layoutParams = lp
    }

    fun onSaveClicked() {
        mOnTrimVideoListener?.onTrimStarted()
        icon_video_play = findViewById(R.id.icon_video_play)
        icon_video_play.visibility = View.VISIBLE
        // exoPlayer
        exoPlayer.pause()
//        video_loader.pause()

        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, mSrc)
        val metaDataKeyDuration = java.lang.Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))

        val file = File(mSrc.path ?: "")

        if (mTimeVideo < MIN_TIME_FRAME) {
            if (metaDataKeyDuration - mEndPosition > MIN_TIME_FRAME - mTimeVideo) mEndPosition += MIN_TIME_FRAME - mTimeVideo
            else if (mStartPosition > MIN_TIME_FRAME - mTimeVideo) mStartPosition -= MIN_TIME_FRAME - mTimeVideo
        }

        val root = File(destinationPath)
        root.mkdirs()
        val outputFileUri = Uri.fromFile(File(root, "t_${Calendar.getInstance().timeInMillis}_" + file.nameWithoutExtension + ".mp4"))
        val outPutPath = RealPathUtil.realPathFromUriApi19(context, outputFileUri)
                ?: File(root, "t_${Calendar.getInstance().timeInMillis}_" + mSrc.path?.substring(mSrc.path!!.lastIndexOf("/") + 1)).absolutePath
        Log.e("SOURCE", file.path)
        Log.e("DESTINATION", outPutPath)
        val extractor = MediaExtractor()
        var frameRate = 24
        try {
            extractor.setDataSource(file.path)
            val numTracks = extractor.trackCount
            for (i in 0..numTracks) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)

                mime?.let {
                    if (it.startsWith("video/")) {
                        if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                            frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE)
                        }
                    }
                }


            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            extractor.release()
        }
        val duration = java.lang.Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))
        Log.e("FRAME RATE", frameRate.toString())
        Log.e("FRAME COUNT", (duration / 1000 * frameRate).toString())
        VideoOptions(context).trimVideo(TrimVideoUtils.stringForTime(mStartPosition), TrimVideoUtils.stringForTime(mEndPosition), file.path, outPutPath, outputFileUri, mOnTrimVideoListener)
    }

    private fun onClickVideoPlayPause() {
            icon_video_play = findViewById(R.id.icon_video_play)
        if (exoPlayer.isPlaying) {
            icon_video_play.visibility = View.VISIBLE
            mMessageHandler.removeMessages(SHOW_PROGRESS)
            exoPlayer.pause()
        } else {
            icon_video_play.visibility = View.GONE
            if (mResetSeekBar) {
                mResetSeekBar = false
                // exoPlayer
                exoPlayer.seekTo(mStartPosition.toInt().toLong())
              //  video_loader.seekTo(mStartPosition.toInt())
            }
            mMessageHandler.sendEmptyMessage(SHOW_PROGRESS)
            exoPlayer.play()
//            video_loader.start()
        }

//        if (video_loader.isPlaying) {
//            icon_video_play.visibility = View.VISIBLE
//            mMessageHandler.removeMessages(SHOW_PROGRESS)
//            video_loader.pause()
//        } else {
//            icon_video_play.visibility = View.GONE
//            if (mResetSeekBar) {
//                mResetSeekBar = false
//                video_loader.seekTo(mStartPosition.toInt())
//            }
//            mMessageHandler.sendEmptyMessage(SHOW_PROGRESS)
//            video_loader.start()
//        }
    }

    fun onCancelClicked() {
        // playerView
        //video_loader.stopPlayback()
        mOnTrimVideoListener?.cancelAction()
    }

    private fun onVideoPrepared(mp: MediaPlayer) {

        layout_surface_view = findViewById(R.id.layout_surface_view)
        icon_video_play = findViewById(R.id.icon_video_play)
        val videoWidth = mp.videoWidth
        val videoHeight = mp.videoHeight
        val videoProportion = videoWidth.toFloat() / videoHeight.toFloat()
        val screenWidth = layout_surface_view.width
        val screenHeight = layout_surface_view.height
        val screenProportion = screenWidth.toFloat() / screenHeight.toFloat()

        // playerView
        val lp = binding.playerView.layoutParams
       // val lp = video_loader.layoutParams

        if (videoProportion > screenProportion) {
            lp.width = screenWidth
            lp.height = (screenWidth.toFloat() / videoProportion).toInt()
        } else {
            lp.width = (videoProportion * screenHeight.toFloat()).toInt()
            lp.height = screenHeight
        }
        // playerView
        binding.playerView.layoutParams = lp
//        video_loader.layoutParams = lp

        icon_video_play.visibility = View.VISIBLE

        // exoPlayer
        mDuration = exoPlayer.duration.toFloat()
        //mDuration = video_loader.duration.toFloat()
        setSeekBarPosition()

        setTimeFrames()

        mOnVideoListener?.onVideoPrepared()
    }

    private fun setSeekBarPosition() {
        when {
            mDuration >= mMaxDuration && mMaxDuration != -1 -> {
                mStartPosition = mDuration / 2 - mMaxDuration / 2
                mEndPosition = mDuration / 2 + mMaxDuration / 2
                binding.timeLineBar.setThumbValue(0, (mStartPosition * 100 / mDuration))
                binding.timeLineBar.setThumbValue(1, (mEndPosition * 100 / mDuration))
            }
            mDuration <= mMinDuration && mMinDuration != -1 -> {
                mStartPosition = mDuration / 2 - mMinDuration / 2
                mEndPosition = mDuration / 2 + mMinDuration / 2
                binding.timeLineBar.setThumbValue(0, (mStartPosition * 100 / mDuration))
                binding.timeLineBar.setThumbValue(1, (mEndPosition * 100 / mDuration))
            }
            else -> {
                mStartPosition = 0f
                mEndPosition = mDuration
            }
        }

        exoPlayer.seekTo(mStartPosition.toInt().toLong())
//        video_loader.seekTo(mStartPosition.toInt())
        mTimeVideo = mDuration
        binding.timeLineBar.initMaxWidth()
    }

    private fun setTimeFrames() {
        val seconds = context.getString(R.string.short_seconds)
        binding.textTimeSelection.text = String.format("%s %s - %s %s", TrimVideoUtils.stringForTime(mStartPosition), seconds, TrimVideoUtils.stringForTime(mEndPosition), seconds)
    }

    private fun onSeekThumbs(index: Int, value: Float) {
        when (index) {
            Thumb.LEFT -> {
                mStartPosition = (mDuration * value / 100L)

                exoPlayer.seekTo(mStartPosition.toInt().toLong())
              //  video_loader.seekTo(mStartPosition.toInt())
            }
            Thumb.RIGHT -> {
                mEndPosition = (mDuration * value / 100L)
            }
        }
        setTimeFrames()
        mTimeVideo = mEndPosition - mStartPosition
    }

    private fun onStopSeekThumbs() {
        mMessageHandler.removeMessages(SHOW_PROGRESS)
        exoPlayer.pause()
//        video_loader.pause()
        icon_video_play.visibility = View.VISIBLE
    }

    private fun onVideoCompleted() {
        exoPlayer.seekTo(mStartPosition.toInt().toLong())
        //video_loader.seekTo(mStartPosition.toInt())
    }

    private fun notifyProgressUpdate(all: Boolean) {
        if (mDuration == 0f) return
        val position = exoPlayer.currentPosition
//        val position = video_loader.currentPosition
        if (all) {
            for (item in mListeners) {
                item.updateProgress(position.toFloat(), mDuration, (position * 100 / mDuration))
            }
        } else {
            mListeners[0].updateProgress(position.toFloat(), mDuration, (position * 100 / mDuration))
        }
    }

    private fun updateVideoProgress(time: Float) {
        if (exoPlayer == null) return
//        if (video_loader == null) return
        if (time <= mStartPosition && time <= mEndPosition) binding.handlerTop.visibility = View.GONE
        else binding.handlerTop.visibility = View.VISIBLE
        if (time >= mEndPosition) {
            mMessageHandler.removeMessages(SHOW_PROGRESS)

            exoPlayer.pause()
//            video_loader.pause()
            icon_video_play.visibility = View.VISIBLE
            mResetSeekBar = true
            return
        }
        setProgressBarPosition(time)
    }

    fun setVideoInformationVisibility(visible: Boolean): VideoTrimmer {
        binding.timeFrame.visibility = if (visible) View.VISIBLE else View.GONE
        return this
    }

    fun setOnTrimVideoListener(onTrimVideoListener: OnTrimVideoListener): VideoTrimmer {
        mOnTrimVideoListener = onTrimVideoListener
        return this
    }

    fun setOnVideoListener(onVideoListener: OnVideoListener): VideoTrimmer {
        mOnVideoListener = onVideoListener
        return this
    }

    fun destroy() {
        BackgroundExecutor.cancelAll("", true)
        UiThreadExecutor.cancelAll("")
    }

    fun setMaxDuration(maxDuration: Int): VideoTrimmer {
        mMaxDuration = maxDuration * 1000
        return this
    }

    fun setMinDuration(minDuration: Int): VideoTrimmer {
        mMinDuration = minDuration * 1000
        return this
    }

    fun setDestinationPath(path: String): VideoTrimmer {
        destinationPath = path
        return this
    }

    fun setVideoURI(videoURI: Uri): VideoTrimmer {
        mSrc = videoURI

        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            context, Util.getUserAgent(context, getApplicationName()))
        mediaSource = HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(mSrc))


        binding.playerView.requestFocus()
//        video_loader.setVideoURI(mSrc)
//        video_loader.requestFocus()
        binding.timeLineView.setVideo(mSrc)
        return this
    }

    fun setTextTimeSelectionTypeface(tf: Typeface?): VideoTrimmer {
        if (tf != null) binding.textTimeSelection.typeface = tf
        return this
    }

    private class MessageHandler internal constructor(view: VideoTrimmer) : Handler() {
        private val mView: WeakReference<VideoTrimmer> = WeakReference(view)
        override fun handleMessage(msg: Message) {
            val view = mView.get()
            if (view == null || view.exoPlayer == null) return
//            if (view == null || view.video_loader == null) return
            view.notifyProgressUpdate(true)
            if (view.exoPlayer.isPlaying) sendEmptyMessageDelayed(0, 10)
//            if (view.video_loader.isPlaying) sendEmptyMessageDelayed(0, 10)
        }
    }

    companion object {
        private const val MIN_TIME_FRAME = 1000
        private const val SHOW_PROGRESS = 2
    }
}
enum class URLType(var url: String) {
    MP4(""),
    HLS("")
}
