package com.foreverrafs.hyperdownloader.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.foreverrafs.hyperdownloader.R
import com.google.gson.Gson
import timber.log.Timber
import java.io.File

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}


fun Fragment.showToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
}

fun View.disable() {
    this.isEnabled = false
}

fun View.enable() {
    this.isEnabled = true
}

fun Context.shareFile(path: String, packageName: String = "") {
    val context = this

    val uri = FileProvider.getUriForFile(
        context,
        context.applicationContext.packageName + ".provider",
        File(path)
    )

    val videoShare = Intent(Intent.ACTION_SEND).apply {
        if (packageName.isNotEmpty())
            setPackage(packageName)

        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        putExtra(Intent.EXTRA_STREAM, uri)
        type = "*/*"
    }

    try {
        context.startActivity(videoShare)
    } catch (exception: ActivityNotFoundException) {
        Toast.makeText(context, "Required App not found", Toast.LENGTH_SHORT).show()
        Timber.e("$packageName is not installed")
    }
}

fun List<Any>.toJson(): String {
    val gson = Gson()
    return gson.toJson(this)
}

fun ImageView.load(uri: String) {
    Glide.with(this.context).load(uri).into(this)
}

fun ImageView.load(uri: Uri) {
    Glide.with(this.context).load(uri).into(this)
}

fun ImageView.load(image: Bitmap) {
    Glide.with(context)
        .load(image)
        .placeholder(R.drawable.ic_video)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}