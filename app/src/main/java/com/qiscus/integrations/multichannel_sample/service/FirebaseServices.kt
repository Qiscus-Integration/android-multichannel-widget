package com.qiscus.integrations.multichannel_sample.service

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.qiscus.integrations.multichannel_sample.ConstCore
import com.qiscus.qiscusmultichannel.MultichannelWidget
import com.qiscus.qiscusmultichannel.util.Const

/**
 * Created on : 26/03/20
 * Author     : Taufik Budi S
 * Github     : https://github.com/tfkbudi
 */
class FirebaseServices : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        MultichannelWidget.instance.registerDeviceToken(ConstCore.qiscusCore1(), p0)
        MultichannelWidget.instance.registerDeviceToken(ConstCore.qiscusCore2(), p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        if (MultichannelWidget.instance.isMultichannelMessage(p0, ConstCore.allQiscusCore())) {
            Log.e("debug", "notif")
            return
        }
    }

    fun getCurrentDeviceToken() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e(
                        "Qiscus", "getCurrentDeviceToken Failed : " +
                                task.exception
                    )
                    return@OnCompleteListener
                }
                if (task.result != null) {
                    val currentToken = task.result!!.token
                    MultichannelWidget.instance.registerDeviceToken(ConstCore.qiscusCore1(), currentToken)
                    MultichannelWidget.instance.registerDeviceToken(ConstCore.qiscusCore2(), currentToken)
                }
            })
    }
}