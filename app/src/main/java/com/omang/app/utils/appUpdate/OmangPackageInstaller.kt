package com.omang.app.utils.appUpdate

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams
import android.net.Uri
import android.os.Build
import androidx.documentfile.provider.DocumentFile
import com.omang.app.utils.deviceCallbacks.DeviceReceivers
import timber.log.Timber
import java.io.File

object OmangPackageInstaller {

    private const val REQUEST_INSTALL_PACKAGE = 12345

    fun installPackage(context: Context, file: File) {
        Timber.v(" apk file %s", file)
        Timber.v(" app update initiated  ${Build.VERSION.SDK_INT}")
        Timber.v(" package name          ${context.packageName}")

        var session: PackageInstaller.Session? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            } else {
                val inputStream =
                    context.applicationContext.contentResolver.openInputStream(Uri.fromFile(file))
                val length =
                    DocumentFile.fromSingleUri(context.applicationContext, Uri.fromFile(file))!!
                        .length()
                val installer = context.applicationContext.packageManager.packageInstaller
                val params = SessionParams(SessionParams.MODE_FULL_INSTALL)
                val sessionId = installer.createSession(params)
                session = installer.openSession(sessionId)
                val outputStream = session.openWrite(file.name, 0, length)
                val buf = ByteArray(1024)
                var len: Int
                while (inputStream!!.read(buf).also { len = it } > 0) {
                    outputStream.write(buf, 0, len)
                }
                session.fsync(outputStream)
                outputStream.close()
                inputStream.close()

                val intent = Intent(context.applicationContext, DeviceReceivers::class.java)

                val pi = PendingIntent.getBroadcast(
                    context.applicationContext,
                    REQUEST_INSTALL_PACKAGE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                session.commit(pi.intentSender)
                session.close()

            }

        } catch (e: Exception) {
            e.printStackTrace()

        } finally {
            session?.abandon()

        }
    }

}