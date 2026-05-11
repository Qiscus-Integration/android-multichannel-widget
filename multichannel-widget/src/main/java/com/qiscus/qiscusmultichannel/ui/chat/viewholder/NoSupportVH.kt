package com.qiscus.qiscusmultichannel.ui.chat.viewholder

import android.view.View
import com.qiscus.qiscusmultichannel.R
import com.qiscus.sdk.chat.core.data.model.QMessage
import android.widget.TextView
import com.qiscus.qiscusmultichannel.databinding.ItemMessageNotSupportedMcBinding

/**
 * Created on : 17/02/20
 * Author     : arioki
 * Name       : Yoga Setiawan
 * GitHub     : https://github.com/arioki
 */

class NoSupportVH(val view: View) : BaseViewHolder(view) {
    override fun bind(comment: QMessage) {
        super.bind(comment)
        val binding = ItemMessageNotSupportedMcBinding.bind(view)
        binding.message.text = view.context.getString(R.string.qiscus_type_not_support_mc).toString()
    }
}