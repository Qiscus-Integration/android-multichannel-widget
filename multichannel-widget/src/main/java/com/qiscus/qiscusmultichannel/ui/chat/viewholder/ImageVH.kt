package com.qiscus.qiscusmultichannel.ui.chat.viewholder

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.core.util.PatternsCompat
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.qiscus.nirmana.Nirmana
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.ui.webView.WebViewHelper
import com.qiscus.qiscusmultichannel.util.DateUtil
import com.qiscus.qiscusmultichannel.util.QiscusImageUtil
import com.qiscus.qiscusmultichannel.util.showToast
import com.qiscus.sdk.chat.core.QiscusCore
import com.qiscus.sdk.chat.core.data.model.QiscusComment
import com.qiscus.sdk.chat.core.data.remote.QiscusApi
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil
import com.qiscus.sdk.chat.core.util.QiscusDateUtil
import com.qiscus.sdk.chat.core.util.QiscusFileUtil
import org.json.JSONObject
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.util.*
import java.util.regex.Matcher

/**
 * Created on : 22/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
open class ImageVH(itemView: View) : BaseViewHolder(itemView) {
    private val thumbnail: ImageView = itemView.findViewById(R.id.thumbnail)
    private val message: TextView = itemView.findViewById(R.id.message)
    private val sender: TextView? = itemView.findViewById(R.id.sender)
    private val dateOfMessage: TextView? = itemView.findViewById(R.id.dateOfMessage)
    private val downloadContainer: ViewGroup? = itemView.findViewById(R.id.layout_img_cover)
    private val tvProgress: TextView? = itemView.findViewById(R.id.tv_progress)
    private val pbProgress: ProgressBar? = itemView.findViewById(R.id.pb_download)
    private val btnDownload: ImageView? = itemView.findViewById(R.id.iv_download)
    private var downloadListener: DownloadFileListener? = null

    override fun bind(comment: QiscusComment) {
        super.bind(comment)

        val content = setJsonContent(comment)
        val caption = parsingJson(content, "caption")
        var url = parsingJson(content, "url")
        var filename = parsingJson(content, "file_name")

        if (url == "") {
            url = setFileAttachment(comment)
            filename = setNameFromFileAttachment(comment)
        }

        if (url.startsWith("http")) { //We have sent it
            showSentImage(comment, url)
        } else { //Still uploading the image
            showSendingImage(url)
        }

        if (caption.isEmpty()) {
            message.visibility = View.GONE
        } else {
            message.visibility = View.VISIBLE
            message.text = caption
        }

        val chatRoom = QiscusCore.getDataStore().getChatRoom(comment.roomId)

        sender?.visibility = if (chatRoom.isGroup) View.VISIBLE else View.GONE
        pbProgress?.visibility = View.GONE

        dateOfMessage?.text = DateUtil.toFullDate(comment.time)

        btnDownload?.setOnClickListener {
            val localPath = QiscusCore.getDataStore().getLocalPath(comment.id)
            if (localPath != null) {
                itemView.context.showToast("Image already in the gallery")
            } else {
                pbProgress?.progress = 0
                tvProgress?.text = "0 %"

                pbProgress?.visibility = View.VISIBLE
                btnDownload.visibility = View.GONE
                tvProgress?.visibility = View.VISIBLE
                pbProgress?.visibility = View.VISIBLE

                downloadFile(comment, filename, url)
            }
        }

        thumbnail.setOnClickListener {
            val localPath = QiscusCore.getDataStore().getLocalPath(comment.id)
            if (localPath != null) {
                dialogViewImage(url, comment.sender, caption, comment.time)
            }
        }
        setUpLinks()
    }

    private fun setJsonContent(comment: QiscusComment): JSONObject? {
        lateinit var content: JSONObject
        try {
            content = JSONObject(comment.extraPayload)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        return content
    }

    private fun parsingJson(content: JSONObject?, param: String): String {
        var text = ""
        try {
            text = content!!.getString(param)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        return text
    }

    private fun downloadFile(qiscusComment: QiscusComment, fileName: String, URLImage: String) {
        QiscusApi.getInstance()
            .downloadFile(URLImage, fileName) {
                // here you can get the progress total downloaded
                val progress = it.toInt()
                val percentage = "$it %"
                QiscusAndroidUtil.runOnUIThread {
                    pbProgress?.progress = progress
                    tvProgress?.text = percentage
                }
            }
            .doOnNext { file ->
                // here we update the local path of file
                QiscusImageUtil.addImageToGallery(file)
                QiscusFileUtil.notifySystem(file)
                QiscusCore.getDataStore()
                    .addOrUpdateLocalPath(qiscusComment.roomId, qiscusComment.id, file.absolutePath)

            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                itemView.context.showToast("success save image to gallery")
                showLocalImage(it)
            }, {
                //on error
            })
    }

    override fun setNeedToShowDate(showDate: Boolean) {
        dateOfMessage?.visibility = if (showDate) View.VISIBLE else View.GONE
    }

    private fun showSendingImage(url: String) {
        val localPath = File(url)
        showLocalImage(localPath)
    }

    private fun showSentImage(comment: QiscusComment, url: String) {
        val localPath = QiscusCore.getDataStore().getLocalPath(comment.id)
        localPath?.let {
            showLocalImage(it)
        } ?: run {
            downloadContainer?.visibility = View.VISIBLE

            Nirmana.getInstance().get()
                .setDefaultRequestOptions(
                    RequestOptions()
                        .placeholder(R.drawable.ic_qiscus_add_image)
                        .error(R.drawable.ic_qiscus_add_image)
                        .dontAnimate()
                        .transform(CenterCrop(), RoundedCorners(16))
                    //                        .optionalTransform(RoundedCorners(16))
                )
                .load(QiscusImageUtil.generateBlurryThumbnailUrl(url))
                .into(thumbnail)
        }
    }

    private fun showLocalImage(localPath: File) {
        if (downloadListener != null) {
            downloadListener!!.onDownloaded(localPath)
            downloadContainer?.visibility = View.VISIBLE
            btnDownload?.visibility = View.GONE
        } else {
            downloadContainer?.visibility = View.GONE
            btnDownload?.visibility = View.VISIBLE
        }

        pbProgress?.visibility = View.GONE
        tvProgress?.visibility = View.GONE

        Nirmana.getInstance().get()
            .setDefaultRequestOptions(
                RequestOptions()
                    .placeholder(R.drawable.ic_qiscus_add_image)
                    .error(R.drawable.ic_qiscus_add_image)
                    .dontAnimate()
                    .transform(CenterCrop(), RoundedCorners(16))
                //                    .transforms(CenterCrop(), RoundedCorners(16))
            )
            .load(localPath)
            .into(thumbnail)
    }

    @SuppressLint("InflateParams")
    private fun dialogViewImage(
        localPath: String,
        sender: String,
        description: String,
        date: Date
    ) {
        val mCotext = itemView.context
        val mDialog =
            LayoutInflater.from(mCotext).inflate(R.layout.image_dialog_view_mc, null)

        val imageView = mDialog.findViewById<ImageView>(R.id.ivDialogView)
        val ibDialogView = mDialog.findViewById<ImageButton>(R.id.ibDialogView)
        val tvSender = mDialog.findViewById<TextView>(R.id.tv_view_sender)
        val tvDescription = mDialog.findViewById<TextView>(R.id.tv_view_description)
        val tvDate = mDialog.findViewById<TextView>(R.id.tv_view_date)
        /*val btnShare = mDialog.findViewById<ImageButton>(R.id.ibShareDialogView)*/
        tvSender.text = sender
        tvDescription.text = description
        tvDate.text = QiscusDateUtil.toFullDateFormat(date)

        Nirmana.getInstance().get()
            .setDefaultRequestOptions(
                RequestOptions()
                    .placeholder(R.drawable.ic_qiscus_add_image)
                    .error(R.drawable.ic_qiscus_add_image)
                    .dontAnimate()
                    .dontTransform()
            )
            .load(localPath)
            .into(imageView)
        val dialogBuilder = AlertDialog.Builder(mCotext, R.style.CustomeDialogFull)
            .setView(mDialog)
        val dialog = dialogBuilder.show()

        /*btnShare.setOnClickListener {
            //shareImage()
        }*/
        ibDialogView.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun shareImage(fileImage: File) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/jpg"
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileImage))
        } else {
            QiscusCore.getApps().packageName
            intent.putExtra(
                Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(
                    itemView.context,
                    QiscusCore.getApps().packageName,
                    fileImage
                )
            )
        }
    }

    @SuppressLint("DefaultLocale", "RestrictedApi")
    private fun setUpLinks() {
        val text = message.text.toString().toLowerCase()
        val matcher: Matcher = PatternsCompat.AUTOLINK_WEB_URL.matcher(text)
        while (matcher.find()) {
            val start: Int = matcher.start()
            if (start > 0 && text[start - 1] == '@') {
                continue
            }
            val end: Int = matcher.end()
            clickify(start, end, object : ClickSpan.OnClickListener {
                override fun onClick() {
                    var url = text.substring(start, end)
                    if (!url.startsWith("http")) {
                        url = "http://$url"
                    }
                    WebViewHelper.launchUrl(itemView.context, Uri.parse(url))
                }
            })
        }
    }

    private fun clickify(start: Int, end: Int, listener: ClickSpan.OnClickListener) {
        val text: CharSequence = message.text.toString()
        val span = ClickSpan(listener)
        if (start == -1) {
            return
        }
        if (text is Spannable) {
            text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            val s: SpannableString = SpannableString.valueOf(text)
            s.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            message.text = s
        }
    }

    private fun setFileAttachment(comment: QiscusComment): String {
        val message = comment.message.trim()
        val fileNameBeginIndex = message.indexOf(']') + 1
        val fileNameEndIndex = message.lastIndexOf("[/file]")
        return message.substring(fileNameBeginIndex, fileNameEndIndex).trim()
    }

    private fun setNameFromFileAttachment(comment: QiscusComment): String {
        val message = comment.message.trim()
        val fileNameEndIndex = message.lastIndexOf("[/file]")
        val fileNameBeginIndex = message.lastIndexOf('/', fileNameEndIndex) + 1
        return message.substring(fileNameBeginIndex, fileNameEndIndex).trim()
    }

    open fun isVideoMode(downloadListener: DownloadFileListener) {
        this.downloadListener = downloadListener
    }

    private class ClickSpan(private val listener: OnClickListener?) :
        ClickableSpan() {

        interface OnClickListener {
            fun onClick()
        }

        override fun onClick(widget: View) {
            listener?.onClick()
        }

    }

    interface DownloadFileListener {
        fun onDownloaded(localPath: File?)
    }
}