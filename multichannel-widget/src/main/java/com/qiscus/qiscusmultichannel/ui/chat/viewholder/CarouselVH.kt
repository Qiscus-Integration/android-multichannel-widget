package com.qiscus.qiscusmultichannel.ui.chat.viewholder

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.qiscus.qiscusmultichannel.ui.chat.CarouselAdapter
import com.qiscus.qiscusmultichannel.R
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.qiscusmultichannel.databinding.ItemCarouselMcBinding
import org.json.JSONObject

/**
 * Created on : 15/02/20
 * Author     : arioki
 * Name       : Yoga Setiawan
 * GitHub     : https://github.com/arioki
 */

class CarouselVH(itemView: View) : BaseViewHolder(itemView) {
    override fun bind(comment: QMessage) {
        super.bind(comment)
        val payload = JSONObject(comment.payload)
        val binding = ItemCarouselMcBinding.bind(itemView)
        payload.getJSONArray("cards")?.let {
            val adapter = CarouselAdapter(it, comment)
            val rvCarousel = binding.rvCarousel
            rvCarousel.layoutManager =
                LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            rvCarousel.adapter = adapter

        }
    }

}
