package com.kiit.foss.ui

import android.app.DownloadManager
import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import com.kiit.foss.ui.viewmodel.DownloaderViewModel
import com.meghdut.coolyield.BuildConfig
import com.meghdut.coolyield.R
import kotlinx.android.synthetic.main.activity_downloader.*
import java.io.File


class DownloaderActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            AndroidViewModelFactory(application)
        ).get(DownloaderViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloader)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(
            attachmentDownloadCompleteReceive, IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE
            )
        )
        txt_paste.setOnClickListener {
            pasteData()
        }
        txt_download.setOnClickListener {
            viewModel.loadDownloadUrl(edtLink.text.toString())
        }
        downloadProgress.isIndeterminate = true
        downloadProgress.isVisible = false
        viewModel.isDownloading.observe(this, Observer { isDownloading ->
            downloadProgress.isVisible = isDownloading
        })


    }

    private fun pasteData() {
        val clipboardManager: ClipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        var str = ""
        if (clipboardManager.hasPrimaryClip()) {
            try {
                str = clipboardManager.primaryClip?.getItemAt(0)?.text.toString()
            } catch (unused: Exception) {
            }
        }
        edtLink.setText(str)
    }


    /**
     * Used to open the downloaded attachment.
     *
     * @param context    Content.
     * @param downloadId Id of the downloaded file to open.
     */
    private fun openDownloadedAttachment(
        context: Context,
        downloadId: Long
    ) {
        val downloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query()
        query.setFilterById(downloadId)
        val cursor: Cursor = downloadManager.query(query)
        if (cursor.moveToFirst()) {
            val downloadStatus: Int =
                cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            val downloadLocalUri: String =
                cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
            val downloadMimeType: String =
                cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE))
            if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
                openDownloadedAttachment(
                    context,
                    Uri.parse(downloadLocalUri),
                    downloadMimeType
                )
            }
        }
        cursor.close()
    }


    /**
     * Used to open the downloaded attachment.
     *
     *
     * 1. Fire intent to open download file using external application.
     *
     * 2. Note:
     * 2.a. We can't share fileUri directly to other application (because we will get FileUriExposedException from Android7.0).
     * 2.b. Hence we can only share content uri with other application.
     * 2.c. We must have declared FileProvider in manifest.
     * 2.c. Refer - https://developer.android.com/reference/android/support/v4/content/FileProvider.html
     *
     * @param context            Context.
     * @param attachmentUri      Uri of the downloaded attachment to be opened.
     * @param attachmentMimeType MimeType of the downloaded attachment.
     */
    private fun openDownloadedAttachment(
        context: Context,
        attachmentUri: Uri,
        attachmentMimeType: String
    ) {
        var attachmentUri: Uri? = attachmentUri
        if (attachmentUri != null) {
            // Get Content Uri.
            if (ContentResolver.SCHEME_FILE == attachmentUri.scheme) {
                // FileUri - Convert it to contentUri.
                val file = File(attachmentUri.path)
                attachmentUri =
                    FileProvider.getUriForFile(
                        context,
                        "${BuildConfig.APPLICATION_ID}.provider",
                        file
                    )
            }
            val openAttachmentIntent = Intent(Intent.ACTION_VIEW)
            openAttachmentIntent.setDataAndType(attachmentUri, attachmentMimeType)
            openAttachmentIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            try {
                context.startActivity(openAttachmentIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    context,
                    "Unable to open the file ",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }




    /**
     * Attachment download complete receiver.
     *
     *
     * 1. Receiver gets called once attachment download completed.
     * 2. Open the downloaded file.
     */
    private val attachmentDownloadCompleteReceive: BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
                    val downloadId = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID, 0
                    )
//                    emmitDownloadId(downloadId)
                    openDownloadedAttachment(context, downloadId)
                }
            }
        }

}