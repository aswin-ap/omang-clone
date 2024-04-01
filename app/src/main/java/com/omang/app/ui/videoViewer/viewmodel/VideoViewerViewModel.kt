package com.omang.app.ui.videoViewer.viewmodel

import android.app.Application
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.omang.app.data.database.DBConstants
import com.omang.app.data.repository.MediaRepository
import com.omang.app.ui.base.viewmodel.BaseViewModel
import com.omang.app.utils.FileUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject

@HiltViewModel
class VideoViewerViewModel @Inject constructor(
    application: Application,
    private val mediaRepository: MediaRepository,
) : BaseViewModel(application) {

    private var exoPlayer: ExoPlayer? = null
    private val _uiState = MutableLiveData<UIState>()
    val uiState: LiveData<UIState> get() = _uiState

    private var videoFile: File? = null

    var isFullScreen: Boolean = false
    var isFullScreenIcon: Boolean = false

    var vH = 0
    var vW = 0

    fun fetchFile(context: Context, fileUrl: String, menu: Int) {
        if (menu == DBConstants.AnalyticsMenu.GALLERY.value) {
            viewModelScope.launch {
                try {
                    videoFile = File(fileUrl)
                    _uiState.postValue(UIState.Initialized(videoFile!!))

                } catch (e: Exception) {
                    _uiState.postValue(UIState.OnError(e))
                }
            }
        } else {
            viewModelScope.launch {
                try {
                    val fileName = FileUtil.getFileNameFromUrl(fileUrl)
                    mediaRepository.fetchFile(getApplication(), fileName)?.let {
                        videoFile = it
                        _uiState.postValue(UIState.Initialized(it))
                    } ?: kotlin.runCatching {
                        _uiState.postValue(UIState.OnError(FileNotFoundException("File not found")))
                    }
                } catch (e: Exception) {
                    _uiState.postValue(UIState.OnError(e))
                }
            }
        }

        videoFile?.let {
           val result =  getMetaData(context, it.toUri())
            if (result != null) {
                vW = result.first
                vH = result.second
            }
        }
    }

    fun preparePlayer(context: Context, playerView: StyledPlayerView) {
        try {
            exoPlayer = ExoPlayer.Builder(context).build()
            exoPlayer?.playWhenReady = true
            playerView.player = exoPlayer

            val dataSourceFactory = DefaultDataSource.Factory(context)
            val mediaItem = MediaItem.fromUri(Uri.fromFile(videoFile))
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)

            exoPlayer?.apply {
                setMediaSource(mediaSource)
                prepare()
            }
            _uiState.value = UIState.PlayerPrepared(exoPlayer)
        } catch (e: Exception) {
            _uiState.postValue(UIState.OnError(e))
        }
    }

    fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }

    private fun getMetaData(context : Context, uri: Uri): Pair<Int, Int>? {
        val retriever = MediaMetadataRetriever()

        try {
            retriever.setDataSource(context, uri)

            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0

            Timber.e("vH ${height} : vW ${width}")

            return Pair(width, height)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }

        return null
    }

    override fun onCleared() {
        releasePlayer()
        super.onCleared()
    }

    companion object {
        private const val URL = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"
    }
}

sealed class UIState {
    data class Initialized(val file: File) : UIState()
    data class OnError(val exception: Exception) : UIState()
    class PlayerPrepared(val player: ExoPlayer?) : UIState()

}