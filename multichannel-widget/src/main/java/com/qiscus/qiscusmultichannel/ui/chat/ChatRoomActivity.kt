package com.qiscus.qiscusmultichannel.ui.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.qiscus.nirmana.Nirmana
import com.qiscus.qiscusmultichannel.MultichannelWidget
import com.qiscus.qiscusmultichannel.MultichannelWidgetConfig
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.databinding.ActivityChatRoomMcBinding
import com.qiscus.qiscusmultichannel.util.Const
import com.qiscus.sdk.chat.core.data.model.QChatRoom
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.event.QMessageReceivedEvent
import com.qiscus.sdk.chat.core.event.QiscusUserStatusEvent
import com.qiscus.sdk.chat.core.util.QiscusDateUtil
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class ChatRoomActivity : AppCompatActivity(), ChatRoomFragment.CommentSelectedListener,
    ChatRoomFragment.OnUserTypingListener {

    lateinit var qiscusChatRoom: QChatRoom
    private val users: MutableSet<String> = HashSet()
    private var memberList: String = ""
    private lateinit var binding: ActivityChatRoomMcBinding
    private  var runnable =  Runnable{
        binding.tvSubtitle.text = MultichannelWidget.config.getRoomSubtitle() ?: memberList
    }
    private var handler = Handler(Looper.getMainLooper())

    companion object {
        val CHATROOM_KEY = "chatroom_key"

        fun generateIntent(
            context: Context,
            qiscusChatRoom: QChatRoom
        ): Intent {

            val intent = Intent(context, ChatRoomActivity::class.java)
            //intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(CHATROOM_KEY, qiscusChatRoom)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (android.os.Build.VERSION.SDK_INT >= 35) {
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        }
        binding = ActivityChatRoomMcBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        onWindow()

        val room = intent.getParcelableExtra<QChatRoom>(CHATROOM_KEY)

        if (room == null) {
            finish()
            return
        } else {
            this.qiscusChatRoom = room
        }

        binding.btnBack.setOnClickListener { finish() }

        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainer,
                ChatRoomFragment.newInstance(qiscusChatRoom),
                ChatRoomFragment::class.java.name
            )
            .commit()

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        binding.toolbarSelectedComment.btnActionCopy.setOnClickListener { getChatFragment().copyComment() }
        binding.toolbarSelectedComment.btnActionDelete.setOnClickListener { getChatFragment().deleteComment() }
        binding.toolbarSelectedComment.btnActionReply.setOnClickListener { getChatFragment().replyComment() }
        binding.toolbarSelectedComment.btnActionReplyCancel.setOnClickListener { getChatFragment().clearSelectedComment() }
        setBarInfo()

        binding.tvTitle.text = MultichannelWidget.config.getRoomTitle() ?: qiscusChatRoom.name

        val avatar = MultichannelWidget.config.getHardcodedAvatar() ?: qiscusChatRoom.avatarUrl
        Nirmana.getInstance().get()
            .load(getAvatar())
            .into(binding.ivAvatar)
    }

    override fun onResume() {
        super.onResume()
        bindRoomData()
    }

    private fun onWindow() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            binding.toolbar.setPadding(
                binding.toolbar.paddingLeft, systemBars.top,
                binding.toolbar.paddingRight, binding.toolbar.paddingBottom
            )
            v.setPadding(
                systemBars.left, 0, systemBars.right, maxOf(imeInsets.bottom, systemBars.bottom)
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun getChatFragment(): ChatRoomFragment {
        return supportFragmentManager.findFragmentByTag(ChatRoomFragment::class.java.name) as ChatRoomFragment
    }

    override fun onCommentSelected(selectedComment: QMessage) {
        val me = Const.qiscusCore()?.getQiscusAccount()?.getId()
        if (binding.toolbarSelectedComment.root.visibility == View.VISIBLE) {
            binding.toolbarSelectedComment.root.visibility = View.GONE
            getChatFragment().clearSelectedComment()
        } else {
            binding.toolbarSelectedComment.btnActionDelete.visibility =
                if (selectedComment.isMyComment(me)) View.VISIBLE else View.GONE
            binding.toolbarSelectedComment.root.visibility = View.VISIBLE
        }
    }

    override fun onClearSelectedComment(status: Boolean) {
        binding.toolbarSelectedComment.root.visibility = View.INVISIBLE
    }

    override fun onUserTyping(email: String?, isTyping: Boolean) {
        binding.tvSubtitle.text = if (isTyping) "typing..." else getSubtitle()

        if (isTyping) {
            handler.removeCallbacks(runnable)
            handler.postDelayed(runnable, 5000)
        }
    }

    private fun bindRoomData() {
        for (member in qiscusChatRoom.participants) {
            if (member.id != MultichannelWidget.instance.getQiscusAccount().id) {
                users.add(member.id)
                Const.qiscusCore()?.pusherApi?.subscribeUserOnlinePresence(member.id)
            }
        }
    }

    private fun setBarInfo() {
        val listMember: ArrayList<String> = arrayListOf()
        Const.qiscusCore()?.api?.getChatRoomInfo(qiscusChatRoom.id)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe { chatRoom ->
                chatRoom.participants.forEach {
                    listMember.add(it.name)
                }
                this.memberList = listMember.joinToString()
                binding.tvSubtitle.text = getSubtitle()
            }
    }

    @Subscribe
    fun onUserStatusChanged(event: QiscusUserStatusEvent) {
        val last = QiscusDateUtil.getRelativeTimeDiff(event.lastActive)
        if (users.contains(event.user)) {
            //tvSubtitle?.text = if (event.isOnline) "Online" else "Last seen $last"
        }
    }

    @Subscribe
    fun onMessageReceived(event: QMessageReceivedEvent) {
        if (event.qiscusComment.type == QMessage.Type.SYSTEM_EVENT) {
            setBarInfo()
        }

    }

    fun getSubtitle(): String {
        return MultichannelWidget.config.getRoomSubtitle() ?: memberList
    }

    fun getAvatar(): String {
        for (member in qiscusChatRoom.participants) {
            val type = member.extras.getString("type")
            if (type.isNotEmpty() && type == "agent"){
                return member.avatarUrl
            }
        }

        return MultichannelWidgetConfig.getHardcodedAvatar() ?: qiscusChatRoom.avatarUrl
    }

    override fun onDestroy() {
        super.onDestroy()
        for (user in users) {
            Const.qiscusCore()?.pusherApi?.unsubscribeUserOnlinePresence(user)
        }
        EventBus.getDefault().unregister(this)
    }
}
