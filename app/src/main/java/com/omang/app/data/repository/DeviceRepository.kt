package com.omang.app.data.repository

import android.content.ContentResolver
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.google.gson.JsonObject
import com.omang.app.data.NetworkResult
import com.omang.app.data.database.dataAnalytics.AnalyticsEntity
import com.omang.app.data.model.appupdate.AppUpdateResponse
import com.omang.app.data.model.appupdate.AppUpdateResponseData
import com.omang.app.data.model.deleteUpdates.DeleteUpdateRequest
import com.omang.app.data.model.deleteUpdates.DeleteUpdateResponse
import com.omang.app.data.model.deviceUpdate.StatusRequest
import com.omang.app.data.model.feed.FeedPostResponse
import com.omang.app.data.model.feed.FeedRequest
import com.omang.app.data.model.profile.ProfileResponse
import com.omang.app.data.model.updates.DeviceUpdatesData
import com.omang.app.network.RemoteDataSource
import com.omang.app.ui.admin.model.DiagnosisResponse
import com.omang.app.ui.gallery.model.MediaFile
import com.omang.app.utils.ViewUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response
import java.io.File
import javax.inject.Inject


/**
 * Repository to handle the device specific functionalities
 */
class DeviceRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val analyticsRepository: AnalyticsRepository
) {

    //Providing the all the updates(changes) of this device/user
    fun getDeviceUpdates(): Flow<NetworkResult<DeviceUpdatesData>> {
        return flow {
            val deviceUpdatesResponse = remoteDataSource.getDeviceUpdates()
            if (deviceUpdatesResponse.isSuccessful) {
                deviceUpdatesResponse.body()?.deviceUpdatesData?.let { data ->
                    return@flow emit(NetworkResult.Success(data))
                }
            }
            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = deviceUpdatesResponse.raw().toString()
                )
            )
            return@flow emit(
                NetworkResult.Error(
                    deviceUpdatesResponse.message(),
                    deviceUpdatesResponse.code()
                )
            )
        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }

    suspend fun getDeleteUpdates(deleteUpdateRequest: DeleteUpdateRequest) {
        val deleteResponse = remoteDataSource.getDeleteUpdates(deleteUpdateRequest)
        if (!deleteResponse.isSuccessful) {
            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = deleteResponse.raw().toString()
                )
            )
        }
    }

    /**
     * Fetches the media from the in-app directories based on the
     * extensions (jpg/mp4)
     */
    fun fetchMediaFromInAppDirectory(
        path: String,
        extension: String
    ): List<MediaFile> {
        val pictures: MutableList<MediaFile> = ArrayList()

        val inAppFolder = File(path)
        if (inAppFolder.exists() && inAppFolder.isDirectory) {
            val medias: List<File> = inAppFolder.listFiles { file ->
                file.isFile && file.extension in listOf(extension)
            }?.toList() ?: emptyList()

            if (medias.isNotEmpty()) {
                for (mediaFile in medias) {
                    val filePath = mediaFile.absolutePath
                    val image = mediaFile.name
                    val date = mediaFile.lastModified()
                    val picture = MediaFile(filePath, image, date)
                    pictures.add(picture)
                }

                if (medias.isNotEmpty()) {
                    return pictures
                }
            }
        }
        return emptyList()
    }

    /**
     * Fetches the medias from the device folders
     * @param contentResolver Content resolver to fetch the data
     * @param isVideo if this true, fetches the video else image
     */
    fun fetchPhotosOrVideosFromDevice(
        contentResolver: ContentResolver,
        isVideo: Boolean = false
    ): List<MediaFile> {
        val imagesList = mutableListOf<MediaFile>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_MODIFIED
        )
        val mediaUri = if (isVideo)
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        else
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val cursor = contentResolver.query(
            mediaUri,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use { c ->
            // val idColumn = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val pathColumn = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val dateColumn = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)

            while (c.moveToNext()) {
                val name = c.getString(nameColumn)
                val path = c.getString(pathColumn)
                val createdAt = c.getLong(dateColumn)
                val image = MediaFile(path, name, createdAt, isDeviceFile = true)
                imagesList.add(image)
            }
        }
        return imagesList
    }

    /**
     * Fetches the images from the device folders
     * @param contentResolver Content resolver to fetch the data
     */
    fun fetchPhotosFromDevice(
        contentResolver: ContentResolver
    ): List<MediaFile> {
        val imagesList = mutableListOf<MediaFile>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_MODIFIED
        )
        val mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val cursor = contentResolver.query(
            mediaUri,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use { c ->
            // val idColumn = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val pathColumn = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val dateColumn = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)

            while (c.moveToNext()) {
                val name = c.getString(nameColumn)
                val path = c.getString(pathColumn)
                val createdAt = c.getLong(dateColumn)
                val image = MediaFile(path, name, createdAt, isDeviceFile = true)
                imagesList.add(image)
            }
        }
        return imagesList
    }

    /**
     * Fetches documents from the device folders
     * @param contentResolver Content resolver to fetch the data
     */
    fun fetchDocumentsFromDevice(
        contentResolver: ContentResolver
    ): List<MediaFile> {
        val docList = mutableListOf<MediaFile>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_MODIFIED
        )

        val pdfMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")
        val xlsxMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xlsx")
        val odsMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("ods")
        val txtMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("txt")
        val docxMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("docx")

        val selectionMimeType = "${MediaStore.Files.FileColumns.MIME_TYPE} IN (?, ?, ? , ? , ?)"
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        val mediaUri = MediaStore.Files.getContentUri("external")

        val cursor = contentResolver.query(
            mediaUri,
            projection,
            selectionMimeType,
            arrayOf(pdfMimeType, xlsxMimeType, odsMimeType, txtMimeType, docxMimeType),
            sortOrder
        )

        cursor?.use { c ->
            // val idColumn = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val pathColumn = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val createdColumn = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)

            while (c.moveToNext()) {
                val name = c.getString(nameColumn)
                val path = c.getString(pathColumn)
                val createdAt = c.getLong(pathColumn)
                val image = MediaFile(path, name, createdAt, isDeviceFile = true)
                docList.add(image)
            }
        }
        return docList

    }

    fun deleteFiles(mediaList: List<MediaFile>, contentResolver: ContentResolver) {
        mediaList.forEach { mediaFile ->
            val file = File(mediaFile.urlPath)
            if (mediaFile.isDeviceFile) {
                val where = MediaStore.MediaColumns.DATA + "=?"
                val selectionArgs = arrayOf(
                    file.absolutePath
                )
                val filesUri = MediaStore.Files.getContentUri("external")
                contentResolver.delete(filesUri, where, selectionArgs)
            } else {
                file.delete()
            }
        }
    }

    fun deleteFile(mediaFile: MediaFile, contentResolver: ContentResolver) {
        val file = File(mediaFile.urlPath)
        if (mediaFile.isDeviceFile) {
            val where = MediaStore.MediaColumns.DATA + "=?"
            val selectionArgs = arrayOf(
                file.absolutePath
            )
            val filesUri = MediaStore.Files.getContentUri("external")
            contentResolver.delete(filesUri, where, selectionArgs)
        } else {
            file.delete()
        }
    }

    fun getAppUpdates(
    ): Flow<NetworkResult<AppUpdateResponseData>> {
        return flow {
            emit(NetworkResult.Loading())
            val appUpdateResponse: Response<AppUpdateResponse> =
                remoteDataSource.getAppUpdates()
            if (appUpdateResponse.isSuccessful) {
                appUpdateResponse.body()?.let {
                    try {
                        return@flow emit(NetworkResult.Success(it.appUpdateResponseData!!))
                    } catch (e: Exception) {
                        /*
                        this is the case where the api return null if there are no apk in serve or
                        there is no update for the device
                        */
//                        e.printStackTrace()
                        return@flow emit(
                            NetworkResult.Error(
                                "No update available, skip to go home",
                                3535
                            )
                        )
                    }
                }
            }

            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = appUpdateResponse.raw().toString()
                )
            )

            return@flow emit(
                NetworkResult.Error(
                    appUpdateResponse.message(),
                    appUpdateResponse.code()
                )
            )
        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }

    fun statusUpdate(
        statusRequest: StatusRequest
    ): Flow<NetworkResult<JsonObject>> {
        return flow {
            emit(NetworkResult.Loading())
            val appUpdateResponse: Response<JsonObject> =
                remoteDataSource.statusUpdate(statusRequest)
            if (appUpdateResponse.isSuccessful) {
                appUpdateResponse.body()?.let {
                    return@flow emit(NetworkResult.Success(it))
                }
            }
            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = appUpdateResponse.raw().toString()
                )
            )
            return@flow emit(
                NetworkResult.Error(
                    appUpdateResponse.message(),
                    appUpdateResponse.code()
                )
            )
        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }

    //deletes the available notification by id
    fun deleteNotifications(deleteUpdateRequest: DeleteUpdateRequest?): Flow<NetworkResult<DeleteUpdateResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            val deviceUpdatesResponse = remoteDataSource.deleteNotifications(deleteUpdateRequest)
            if (deviceUpdatesResponse.isSuccessful) {
                deviceUpdatesResponse.body()?.let { data ->
                    return@flow emit(NetworkResult.Success(data))
                }
            }
            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = deviceUpdatesResponse.raw().toString()
                )
            )
            return@flow emit(
                NetworkResult.Error(
                    deviceUpdatesResponse.message(),
                    deviceUpdatesResponse.code()
                )
            )
        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }

    fun postFeed(feedRequest: FeedRequest): Flow<NetworkResult<FeedPostResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            val deviceUpdatesResponse = remoteDataSource.postFeed(feedRequest)
            if (deviceUpdatesResponse.isSuccessful) {
                deviceUpdatesResponse.body()?.let { data ->
                    return@flow emit(NetworkResult.Success(data))
                }
            }
            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = deviceUpdatesResponse.raw().toString()
                )
            )
            return@flow emit(
                NetworkResult.Error(
                    deviceUpdatesResponse.message(),
                    deviceUpdatesResponse.code()
                )
            )
        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }

    fun getDiagnosisStatus(
    ): Flow<NetworkResult<Int>> {
        return flow {
            emit(NetworkResult.Loading())
            val diagnosisResponse: Response<DiagnosisResponse> =
                remoteDataSource.getDiagnosisStatus()
            if (diagnosisResponse.isSuccessful) {
                diagnosisResponse.body()?.let {
                    return@flow emit(NetworkResult.Success(it.statusCode!!))
                }
            }
            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = diagnosisResponse.raw().toString()
                )
            )
            return@flow emit(
                NetworkResult.Error(
                    diagnosisResponse.message(),
                    diagnosisResponse.code()
                )
            )
        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }

    fun getProfileUpdate(): Flow<NetworkResult<ProfileResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            val profileResponse =
                remoteDataSource.getProfileUpdate()
            if (profileResponse.isSuccessful) {
                profileResponse.body()?.let {
                    return@flow emit(NetworkResult.Success(it))
                }
            }
            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = profileResponse.raw().toString()
                )
            )
            return@flow emit(
                NetworkResult.Error(
                    profileResponse.message(),
                    profileResponse.code()
                )
            )
        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }
}