package com.xayah.librootservice.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageInfo
import android.os.IBinder
import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import android.widget.Toast
import com.topjohnwu.superuser.ipc.RootService
import com.xayah.librootservice.IRemoteRootService
import com.xayah.librootservice.impl.RemoteRootServiceImpl
import com.xayah.librootservice.parcelables.StatFsParcelable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RemoteRootService(private val context: Context) {
    private var mService: IRemoteRootService? = null
    private var mConnection: ServiceConnection? = null
    private var isFirstConnection = true

    class RemoteRootService : RootService() {
        override fun onBind(intent: Intent): IBinder = RemoteRootServiceImpl()
    }

    private suspend fun bindService(): IRemoteRootService = suspendCoroutine { continuation ->
        if (mService == null) {
            mConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    mService = IRemoteRootService.Stub.asInterface(service)
                    continuation.resume(mService!!)
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    mService = null
                    mConnection = null
                    val msg = "Service disconnected."
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    continuation.resumeWithException(RemoteException(msg))
                }

                override fun onBindingDied(name: ComponentName) {
                    mService = null
                    mConnection = null
                    val msg = "Binding died."
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    continuation.resumeWithException(RemoteException(msg))
                }

                override fun onNullBinding(name: ComponentName) {
                    mService = null
                    mConnection = null
                    val msg = "Null binding."
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    continuation.resumeWithException(RemoteException(msg))
                }
            }
            val intent = Intent().apply {
                component = ComponentName(context.packageName, RemoteRootService::class.java.name)
                addCategory(RootService.CATEGORY_DAEMON_MODE)
            }
            RootService.bind(intent, mConnection!!)
        } else {
            mService
        }
    }

    /**
     * Destroy the service.
     */
    fun destroyService(killDaemon: Boolean = false) {
        if (killDaemon)
            if (mConnection != null)
                RootService.unbind(mConnection!!)
        mConnection = null
        mService = null
    }

    private suspend fun getService(): IRemoteRootService {
        return withContext(Dispatchers.Main) {
            if (mService == null) {
                val msg = "Service is null."
                if (isFirstConnection)
                    isFirstConnection = false
                else
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                bindService()
            } else if (mService!!.asBinder().isBinderAlive.not()) {
                mService = null
                val msg = "Service is dead."
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                bindService()
            } else {
                mService!!
            }
        }
    }

    suspend fun readStatFs(path: String): StatFsParcelable = getService().readStatFs(path)

    suspend fun mkdirs(path: String): Boolean = getService().mkdirs(path)

    suspend fun writeText(text: String, path: String, context: Context): Boolean {
        var state = true
        val tmpFilePath = "${context.cacheDir.path}/tmp"
        val tmpFile = File(tmpFilePath)
        tmpFile.writeText(text)
        if (getService().mkdirs(path).not()) state = false
        if (getService().copyTo(tmpFilePath, path, true).not()) state = false
        tmpFile.deleteRecursively()
        return state
    }


    suspend fun exists(path: String): Boolean = getService().exists(path)

    suspend fun getInstalledPackagesAsUser(flags: Int, userId: Int): List<PackageInfo> {
        val pfd = getService().getInstalledPackagesAsUser(flags, userId)
        val stream = ParcelFileDescriptor.AutoCloseInputStream(pfd)
        val bytes = stream.readBytes()
        val parcel = Parcel.obtain()

        parcel.unmarshall(bytes, 0, bytes.size)
        parcel.setDataPosition(0)

        val packages = mutableListOf<PackageInfo>()
        parcel.readTypedList(packages, PackageInfo.CREATOR)
        parcel.recycle()
        return packages
    }

    suspend fun getPackageSourceDir(packageName: String, userId: Int): List<String> = getService().getPackageSourceDir(packageName, userId)
}