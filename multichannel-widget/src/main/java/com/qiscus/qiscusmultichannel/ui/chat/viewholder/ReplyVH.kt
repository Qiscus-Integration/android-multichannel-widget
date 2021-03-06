package com.qiscus.qiscusmultichannel.ui.chat.viewholder

import android.annotation.SuppressLint
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.util.PatternsCompat
import com.bumptech.glide.request.RequestOptions
import com.qiscus.nirmana.Nirmana
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.ui.webView.WebViewHelper
import com.qiscus.sdk.chat.core.custom.QiscusCore
import com.qiscus.sdk.chat.core.custom.data.model.QiscusComment
import kotlinx.android.synthetic.main.item_my_reply_mc.view.*
import java.util.regex.Matcher

/**
 * Created on : 28/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class ReplyVH(itemView: View) : BaseViewHolder(itemView) {
    private val qiscusAccount = QiscusCore.getQiscusAccount()
    private val message = itemView.message
    override fun bind(comment: QiscusComment) {
        super.bind(comment)
        val origin = comment.replyTo

        itemView.origin_sender?.text =
            if (qiscusAccount.email == origin.senderEmail) itemView.context.getString(R.string.qiscus_you_mc) else origin.sender

        itemView.origin_comment?.text = origin.message
        itemView.message.text = comment.message
        itemView.icon.visibility = View.VISIBLE
        setUpLinks()
        when (origin.type) {
            QiscusComment.Type.TEXT -> {
                itemView.origin_image.visibility = View.GONE
                itemView.icon.visibility = View.GONE
            }
            QiscusComment.Type.IMAGE -> {
                itemView.origin_image.visibility = View.VISIBLE
                itemView.icon.setImageResource(R.drawable.ic_qiscus_gallery)
                itemView.origin_comment.text = if (origin.caption == "") "Image" else origin.caption
                Nirmana.getInstance().get()
                    .setDefaultRequestOptions(
                        RequestOptions()
                            .placeholder(R.drawable.ic_qiscus_avatar)
                            .error(R.drawable.ic_qiscus_avatar)
                            .dontAnimate()
                    )
                    .load(origin.attachmentUri)
                    .into(itemView.origin_image)
            }
            QiscusComment.Type.FILE -> {
                itemView.origin_image.visibility = View.GONE
                itemView.icon.visibility = View.VISIBLE
                itemView.origin_comment.text = origin.attachmentName
                itemView.icon.setImageResource(R.drawable.ic_qiscus_file_mc)
            }
            else -> {
                itemView.origin_image.visibility = View.GONE
                itemView.icon.visibility = View.GONE
                itemView.origin_comment.text = origin.message
            }
        }
    }

    @SuppressLint("DefaultLocale")
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
            (text as Spannable).setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            val s: SpannableString = SpannableString.valueOf(text)
            s.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            message.text = s
        }
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
}