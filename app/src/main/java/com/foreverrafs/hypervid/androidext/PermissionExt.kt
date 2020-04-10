package com.foreverrafs.hypervid.androidext

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment

@RequiresApi(Build.VERSION_CODES.M)
fun Context.requestStoragePermission(requestCode: Int) {
    val context = this
    val permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE

    if (context.checkSelfPermission(permission) ==
        PackageManager.PERMISSION_DENIED
    ) {
        if (context is Activity) {
            context.requestPermissions(arrayOf(permission), requestCode)
        } else if (context is Fragment) {
            context.requestPermissions(arrayOf(permission), requestCode)
        }
    }
}