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
import com.qiscus.qiscusmultichannel.util.Const
import com.qiscus.sdk.chat.core.data.model.QMessage
import android.widget.TextView
import com.qiscus.qiscusmultichannel.databinding.ItemMyReplyMcBinding
import org.json.JSONObject
import java.util.regex.Matcher

/**
 * Created on : 28/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class ReplyVH(itemView: View) : BaseViewHolder(itemView) {
    private val qiscusAccount = Const.qiscusCore()?.getQiscusAccount()!!
    private lateinit var message: TextView

    override fun bind(comment: QMessage) {
        super.bind(comment)
        val binding = ItemMyReplyMcBinding.bind(itemView)
        message = binding.message
        val origin = comment.replyTo

        binding.originSender.text =
            if (qiscusAccount.id == origin.sender.id) itemView.context.getString(R.string.qiscus_you_mc) else origin.sender.name

        binding.originComment.text = origin.text
        message.text = comment.text
        binding.icon.visibility = View.VISIBLE
        setUpLinks()
        when (origin.type) {
            QMessage.Type.TEXT -> {
                binding.originImage.visibility = View.GONE
                binding.icon.visibility = View.GONE
            }
            QMessage.Type.IMAGE -> {
                val obj = JSONObject(origin.payload)
                binding.originImage.visibility = View.VISIBLE
                binding.icon.setImageResource(R.drawable.ic_qiscus_gallery)
                binding.originComment.text = if (obj.getString("caption") == "") "Image" else obj.getString("caption")
                Nirmana.getInstance().get()
                    .setDefaultRequestOptions(
                        RequestOptions()
                            .placeholder(R.drawable.ic_qiscus_avatar)
                            .error(R.drawable.ic_qiscus_avatar)
                            .dontAnimate()
                    )
                    .load(origin.attachmentUri)
                    .into(binding.originImage)
            }
            QMessage.Type.FILE -> {
                binding.originImage.visibility = View.GONE
                binding.icon.visibility = View.VISIBLE
                binding.originComment.text = origin.attachmentName
                binding.icon.setImageResource(R.drawable.ic_qiscus_file_mc)
            }
            else -> {
                binding.originImage.visibility = View.GONE
                binding.icon.visibility = View.GONE
                binding.originComment.text = origin.text
            }
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