package com.qiscus.qiscusmultichannel

import com.qiscus.qiscusmultichannel.util.MultichannelNotificationListener

/**
 * Created on : 05/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
object MultichannelWidgetConfig {
    private var enableLog: Boolean = false
    private var isSessional: Boolean = false
    private var enableNotification: Boolean = true
    var multichannelNotificationListener: MultichannelNotificationListener? = null

    fun setEnableLog(enableLog: Boolean) = apply { this.enableLog = enableLog }
    fun isEnableLog() = enableLog
    fun isSessional() = isSessional
    fun setSessional(isSessional: Boolean) = apply { this.isSessional = isSessional }
    fun setNotificationListener(multichannelNotificationListener: MultichannelNotificationListener?) = apply { this.multichannelNotificationListener = multichannelNotificationListener }
    fun getNotificationListener() = multichannelNotificationListener
    fun setEnableNotification(enableNotification: Boolean) = apply { this.enableNotification = enableNotification }
    fun isEnableNotification() = enableNotification

}