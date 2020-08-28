package com.qiscus.chat.sample.utils

import android.content.Context
import com.qiscus.qiscusmultichannel.util.MultichannelNotificationListener
import com.qiscus.qiscusmultichannel.util.PNUtil
import com.qiscus.sdk.chat.core.data.model.QiscusComment

class NotificationListener : MultichannelNotificationListener {

    override fun handleMultichannelListener(context: Context?, qiscusComment: QiscusComment?) {
        qiscusComment?.let { context?.let { ctx -> PNUtil.showPn(ctx, it) } }
    }

}