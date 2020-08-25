package com.qiscus.qiscuschat.ui.chat.image

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.qiscus.integrations.multichannel_sample.R
import com.qiscus.nirmana.Nirmana
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom
import com.qiscus.sdk.chat.core.data.model.QiscusPhoto
import kotlinx.android.synthetic.main.activity_send_image_confirmation_mc.*

class SendImageConfirmationActivity : AppCompatActivity() {

    lateinit var qiscusChatRoom: QiscusChatRoom
    lateinit var qiscusPhoto: QiscusPhoto

    companion object {
        val EXTRA_ROOM = "extra_room"
        val EXTRA_PHOTOS = "extra_photos"
        val EXTRA_CAPTIONS = "extra_captions"

        fun generateIntent(
            context: Context,
            qiscusChatRoom: QiscusChatRoom,
            qiscusPhoto: QiscusPhoto
        ): Intent {
            val intent = Intent(context, SendImageConfirmationActivity::class.java)
            intent.putExtra(EXTRA_ROOM, qiscusChatRoom)
            intent.putExtra(EXTRA_PHOTOS, qiscusPhoto)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_image_confirmation_mc)

        qiscusChatRoom = intent.getParcelableExtra(EXTRA_ROOM)

        if (!this::qiscusChatRoom.isInitialized) {
            finish()
            return
        }

        qiscusPhoto = intent.getParcelableExtra(EXTRA_PHOTOS)
        if (this::qiscusPhoto.isInitialized) {
            initPhotos()
        } else {
            finish()
            return
        }

        initRoomChat()

        buttonSend.setOnClickListener { confirm() }
        btn_back.setOnClickListener { finish() }
    }

    private fun initPhotos() {
        Nirmana.getInstance().get()
            .setDefaultRequestOptions(
                RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            )
            .load(qiscusPhoto.photoFile)
            .into(ivImage)
    }

    private fun confirm() {
        val data = Intent()
        data.putExtra(
            EXTRA_PHOTOS,
            qiscusPhoto
        )
        data.putExtra(EXTRA_CAPTIONS, etCaption.text.toString())
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    private fun initRoomChat() {
        tvTitle.text = qiscusChatRoom.name

        Nirmana.getInstance().get()
            .load(qiscusChatRoom.avatarUrl)
            .apply(
                RequestOptions()
                    .dontAnimate()
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
            )
            .into(avatar)
    }

}
