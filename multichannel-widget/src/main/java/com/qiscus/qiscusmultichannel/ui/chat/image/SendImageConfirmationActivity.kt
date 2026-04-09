package com.qiscus.qiscusmultichannel.ui.chat.image

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.qiscus.nirmana.Nirmana
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.databinding.ActivitySendImageConfirmationMcBinding
import com.qiscus.sdk.chat.core.data.model.QChatRoom
import com.qiscus.sdk.chat.core.data.model.QiscusPhoto

class SendImageConfirmationActivity : AppCompatActivity() {

    lateinit var qiscusChatRoom: QChatRoom
    lateinit var qiscusPhoto: QiscusPhoto
    private lateinit var binding: ActivitySendImageConfirmationMcBinding

    companion object {
        val EXTRA_ROOM = "extra_room2"
        val EXTRA_PHOTOS = "extra_photos2"
        val EXTRA_CAPTIONS = "extra_captions2"

        fun generateIntent(
            context: Context,
            qiscusChatRoom: QChatRoom,
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
        binding = ActivitySendImageConfirmationMcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val room = intent.getParcelableExtra<QChatRoom>(EXTRA_ROOM)

        if (room == null) {
            finish()
            return
        } else {
            this.qiscusChatRoom = room
        }

        val photo = intent.getParcelableExtra<QiscusPhoto>(EXTRA_PHOTOS)
        if (photo != null) {
            this.qiscusPhoto = photo
            initPhotos()
        } else {
            finish()
            return
        }

        binding.buttonSend.setOnClickListener { confirm() }
    }



    private fun initPhotos() {
        Nirmana.getInstance().get()
            .load(qiscusPhoto.photoFile)
            .apply(
                RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
            )
            .into(binding.ivImage)
    }

    private fun confirm() {
        val intent = Intent()
        intent.putExtra(EXTRA_CAPTIONS, binding.etCaption.text.toString())
        intent.putExtra(EXTRA_PHOTOS, qiscusPhoto)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
