package com.manav.commo

import android.app.Activity
import android.app.ActivityManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import java.util.*
import java.util.regex.Pattern

class CommonUtils {

    companion object {

        private lateinit var progressDialog: ProgressDialog
        private lateinit var customprogressDialog: AlertDialog
        private lateinit var dailogbox: AlertDialog

        fun isNetworkConnected(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return cm.activeNetworkInfo != null
        }

        fun hideWindowStatusBar(window: Window) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = Color.TRANSPARENT
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }
        }

        fun getSystemStatusBarHeight(context: Context): Int {
            val id = context.resources.getIdentifier(
                "status_bar_height", "dimen", "android"
            )
            return if (id > 0) context.resources.getDimensionPixelSize(id) else id
        }

        fun shareText(context: Context, title: String?, url: String?) {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, title)
            shareIntent.putExtra(Intent.EXTRA_TEXT, url)
            context.startActivity(Intent.createChooser(shareIntent, "Share via"))
        }

        fun shareVideo(context: Context, title: String?, videoPath: String?) {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            val screenshotUri = Uri.parse(videoPath)
            sharingIntent.type = "video/*"
            sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri)
            context.startActivity(Intent.createChooser(sharingIntent, title))
        }

        fun showProgress(activity: Activity?, message: String?) {
            progressDialog = ProgressDialog(activity)
            progressDialog.setMessage(message)
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()
        }

        fun dismissProgress() {
            if (progressDialog != null && progressDialog.isShowing) {
                progressDialog.dismiss()
            }
        }

        fun isMyServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    Log.i("isMyServiceRunning?", true.toString() + "")
                    return true
                }
            }
            Log.i("isMyServiceRunning?", false.toString() + "")
            return false
        }

        fun toast(context: Context?, msg: String?) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }

        fun sendSMS(srcNumber: String, message: String?, context: Activity) {
            val sms_uri = Uri.parse("smsto:$srcNumber")
            val intent = Intent(Intent.ACTION_SENDTO, sms_uri)
            intent.putExtra("sms_body", message)
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            }
        }

        // this will hide the bottom mobile navigation controll
        fun fullScreen(activity: Activity) {
            activity.requestWindowFeature(Window.FEATURE_NO_TITLE)
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

            // This work only for android 4.4+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                activity.window.decorView.systemUiVisibility = flags
                val decorView = activity.window.decorView
                decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                    if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                        decorView.systemUiVisibility = flags
                    }
                }
            }
        }

        fun hideKeyboard(activity: Activity) {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            var view = activity.currentFocus
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun isEmailValid(email: String?): Boolean {
            val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
            val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(email)
            return matcher.matches()
        }

        fun getMultipartBody(filePath: String?, key: String?): MultipartBody.Part? {
            val compressedFile = File(filePath)
            val requestFile: RequestBody =
                RequestBody.create(MediaType.parse("multipart/form-data"), compressedFile)
            return MultipartBody.Part.createFormData(key, compressedFile.name, requestFile)
        }

        fun getTextRequestBody(text: String?): RequestBody? {
            return RequestBody.create(MediaType.parse("text/plain"), text)
        }

        var alertDialogPositveButton: AlertDialogPositveButton? = null

        interface AlertDialogPositveButton {
            fun onAlertDialogPositveButtonClicked()
        }

        fun alertDialog(
            context: Context?,
            title: String?,
            message: String?,
            positiveButton: String?,
            negativeButton: String?,
            alertDialogPositveButton: AlertDialogPositveButton
        ) {
            val al = AlertDialog.Builder(
                context!!
            )
            al.setTitle(title).setPositiveButton(
                positiveButton
            ) { dialog, which -> alertDialogPositveButton.onAlertDialogPositveButtonClicked() }
                .setNegativeButton(
                    negativeButton
                ) { dialog, which -> dialog.dismiss() }.setMessage(message).show()
        }

        fun permissionsToRequest(
            context: Context,
            wantedPermissions: ArrayList<String>
        ): ArrayList<String>? {
            val result = ArrayList<String>()
            for (perm in wantedPermissions) {
                if (!hasPermission(context, perm)) {
                    result.add(perm)
                }
            }
            return result
        }

        fun hasPermission(context: Context, permission: String?): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.checkSelfPermission(permission!!) == PackageManager.PERMISSION_GRANTED
            } else true
        }

        fun mapToJsonObject(map: Map<*, *>?): JSONObject? {
            return JSONObject(map)
        }

        fun strinBuilderToString(arr: Array<String?>): String? {
            val sb = StringBuilder()
            var prefix = ""
            for (str in arr) {
                sb.append(prefix)
                prefix = ","
                sb.append(str)
            }
            println("WithCommas : $sb")
            return sb.toString()
        }

        /*
    fun makeTextViewResizable(
        tv: TextView,
        maxLine: Int,
        expandText: String,
        spanHexaDeciColor: String,
        viewMore: Boolean
    ) {
        if (tv.tag == null) {
            tv.tag = tv.text
        }
        val vto = tv.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val obs = tv.viewTreeObserver
                obs.removeGlobalOnLayoutListener(this)
                if (maxLine == 0) {
                    val lineEndIndex = tv.layout.getLineEnd(0)
                    val text = tv.text.subSequence(0, lineEndIndex - expandText.length + 1)
                        .toString() + " " + expandText
                    tv.text = text
                    tv.movementMethod = LinkMovementMethod.getInstance()
                    tv.setText(
                        addClickablePartTextViewResizable(
                            Html.fromHtml(tv.text.toString()),
                            tv,
                            maxLine,
                            expandText,
                            spanHexaDeciColor,
                            viewMore
                        ), TextView.BufferType.SPANNABLE
                    )
                } else if (maxLine > 0 && tv.lineCount >= maxLine) {
                    val lineEndIndex = tv.layout.getLineEnd(maxLine - 1)
                    val text = tv.text.subSequence(0, lineEndIndex - expandText.length + 1)
                        .toString() + " " + expandText
                    tv.text = text
                    tv.movementMethod = LinkMovementMethod.getInstance()
                    tv.setText(
                        addClickablePartTextViewResizable(
                            Html.fromHtml(tv.text.toString()), tv, maxLine, expandText,
                            spanHexaDeciColor, viewMore
                        ), TextView.BufferType.SPANNABLE
                    )
                } else {
                    val lineEndIndex = tv.layout.getLineEnd(tv.layout.lineCount - 1)
                    val text =
                        tv.text.subSequence(0, lineEndIndex).toString() + " " + expandText
                    tv.text = text
                    tv.movementMethod = LinkMovementMethod.getInstance()
                    tv.setText(
                        addClickablePartTextViewResizable(
                            Html.fromHtml(tv.text.toString()), tv, lineEndIndex, expandText,
                            spanHexaDeciColor, viewMore
                        ), TextView.BufferType.SPANNABLE
                    )
                }
            }
        })
    }


    private fun addClickablePartTextViewResizable(
        strSpanned: Spanned, tv: TextView,
        maxLine: Int, spanableText: String, spanHexaColor: String, viewMore: Boolean
    ): SpannableStringBuilder? {
        val str = strSpanned.toString()
        val ssb = SpannableStringBuilder(strSpanned)
        if (str.contains(spanableText)) {
            ssb.setSpan(object : MySpannable(false, spanHexaColor) {
                fun onClick(widget: View?) {
                    if (viewMore) {
                        tv.layoutParams = tv.layoutParams
                        tv.setText(tv.tag.toString(), TextView.BufferType.SPANNABLE)
                        tv.invalidate()
                        makeTextViewResizable(tv, -1, "See Less", spanHexaColor, false)
                    } else {
                        tv.layoutParams = tv.layoutParams
                        tv.setText(tv.tag.toString(), TextView.BufferType.SPANNABLE)
                        tv.invalidate()
                        makeTextViewResizable(tv, 3, ".. See More", spanHexaColor, true)
                    }
                }
            }, str.indexOf(spanableText), str.indexOf(spanableText) + spanableText.length, 0)
        }
        return ssb
    }

    class MySpannable(isUnderline: Boolean, spanHexaColor: String?) :
        ClickableSpan() {
        private var isUnderline = true
        override fun updateDrawState(ds: TextPaint) {
            ds.isUnderlineText = isUnderline
            ds.color = Color.parseColor("#a6822d")
        }

        override fun onClick(widget: View) {}

        init {
            this.isUnderline = isUnderline
        }
    }
    */

    }
}