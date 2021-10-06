package uz.lycr.lesson15firebasechatapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import uz.lycr.lesson15firebasechatapp.R
import uz.lycr.lesson15firebasechatapp.databinding.ItemChatGroupLeftBinding
import uz.lycr.lesson15firebasechatapp.databinding.ItemChatRightBinding
import uz.lycr.lesson15firebasechatapp.databinding.ItemChatUserLeftBinding
import uz.lycr.lesson15firebasechatapp.models.MessageGroupM
import uz.lycr.lesson15firebasechatapp.models.MessageM
import java.text.SimpleDateFormat
import java.util.*

class ChatGroupAdapter(var list: List<MessageGroupM>, var currentUserUid: String, var listener: MyChatGroupAdapterClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class LeftVh(var itemBinding: ItemChatGroupLeftBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun onBind(message: MessageGroupM) {
            val today = SimpleDateFormat("dd.MM.yyyy HH:mm").format(Date()).split(" ")[0]

            itemBinding.tvMsg.text = message.message
            itemBinding.tvUserName.text = message.senderName
            Picasso.get().load(message.senderImgUrl).placeholder(R.drawable.avatar_placeholder).into(itemBinding.ivProfil)
            if (message.date!!.split(" ")[0] == today) itemBinding.tvMsgTime.text = "Today: ${message.date!!.split(" ")[1]}"
            else itemBinding.tvMsgTime.text = message.date

            itemBinding.ivOptions.setOnClickListener { listener.onOptionsClick(it, message, 2) }
        }
    }

    inner class RightVh(var itemBinding: ItemChatRightBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun onBind(message: MessageGroupM) {
            val today = SimpleDateFormat("dd.MM.yyyy HH:mm").format(Date()).split(" ")[0]

            if (message.isSeen!!) {
                itemBinding.ivTick1.visibility = View.VISIBLE
                itemBinding.ivTick2.visibility = View.VISIBLE
            } else {
                itemBinding.ivTick1.visibility = View.VISIBLE
                itemBinding.ivTick2.visibility = View.INVISIBLE
            }
            itemBinding.tvMsg.text = message.message
            Picasso.get().load(message.senderImgUrl).placeholder(R.drawable.avatar_placeholder).into(itemBinding.ivProfil)
            if (message.date!!.split(" ")[0] == today) itemBinding.tvMsgTime.text = "Today: ${message.date!!.split(" ")[1]}"
            else itemBinding.tvMsgTime.text = message.date

            itemBinding.ivOptions.setOnClickListener { listener.onOptionsClick(it, message, 1) }
        }
    }

    interface MyChatGroupAdapterClickListener{
        fun onOptionsClick(view:View, message: MessageGroupM, type: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            RightVh(
                ItemChatRightBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            LeftVh(
                ItemChatGroupLeftBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == 1) {
            val rightVh = holder as RightVh
            rightVh.onBind(list[position])
        } else {
            val leftVh = holder as LeftVh
            leftVh.onBind(list[position])}
    }

    override fun getItemCount() = list.size

    override fun getItemViewType(position: Int) =
        if (list[position].senderUid == currentUserUid) 1 else 2

}