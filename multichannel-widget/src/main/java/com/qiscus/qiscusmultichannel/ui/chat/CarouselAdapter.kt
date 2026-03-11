package com.qiscus.qiscusmultichannel.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.ui.chat.viewholder.BaseViewHolder
import com.qiscus.qiscusmultichannel.ui.chat.viewholder.CardVH
import com.qiscus.sdk.chat.core.data.model.QMessage
import org.json.JSONArray
import org.json.JSONObject
import com.qiscus.qiscusmultichannel.databinding.ItemCardMcBinding

/**
 * Created on : 15/02/20
 * Author     : arioki
 * Name       : Yoga Setiawan
 * GitHub     : https://github.com/arioki
 */

class CarouselAdapter(val mData: JSONArray, val qiscusComment: QMessage) :
    RecyclerView.Adapter<BaseViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemCardMcBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val view = binding.root
        binding.dateOfMessage.visibility = View.GONE
        binding.date.visibility = View.GONE
        view.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        return CardVH(view)
    }

    override fun getItemCount(): Int = mData.length()

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val qiscusComment =
            QMessage.generatePostBackMessage(
                qiscusComment.chatRoomId,
                qiscusComment.text,
                mData[position] as JSONObject?
            )
        holder.bind(qiscusComment)
    }
}