package uz.lycr.lesson15firebasechatapp.adapters

import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.squareup.picasso.Picasso
import uz.lycr.lesson15firebasechatapp.R
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import uz.lycr.lesson15firebasechatapp.models.UserM
import uz.lycr.lesson15firebasechatapp.databinding.ItemChatsBinding
import uz.lycr.lesson15firebasechatapp.models.MessageM
import java.text.SimpleDateFormat
import java.util.*

class UserRvAdapter(
    var userList: List<UserM>,
    var listener: OnUserClickListener
) : RecyclerView.Adapter<UserRvAdapter.UserVh>() {

    inner class UserVh(var itemBinding: ItemChatsBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun onBind(user: UserM) {
            FirebaseDatabase.getInstance()
                .getReference("UsersMessages/${FirebaseAuth.getInstance().currentUser!!.uid}_&_${user.uid}")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            var unreadCount = 0
                            val lastMessage =
                                snapshot.children.last().getValue(MessageM::class.java)!!

                            if (lastMessage.senderUid == FirebaseAuth.getInstance().currentUser.uid) {
                                if (lastMessage.isSeen!!) {
                                    itemBinding.ivSeen.visibility = View.VISIBLE
                                    itemBinding.ivSeen2.visibility = View.VISIBLE
                                } else {
                                    itemBinding.ivSeen.visibility = View.VISIBLE
                                    itemBinding.ivSeen2.visibility = View.INVISIBLE
                                }
                            } else {
                                itemBinding.ivSeen.visibility = View.INVISIBLE
                                itemBinding.ivSeen2.visibility = View.INVISIBLE
                            }

                            for (child in snapshot.children) {
                                val message = child.getValue(MessageM::class.java)!!
                                if (!message.isSeen!! && message.senderUid == user.uid) unreadCount++
                            }

                            if (lastMessage.date!!.split(" ")[0] == SimpleDateFormat("dd.MM.yyyy HH:mm").format(
                                    Date()
                                ).split(" ")[0]
                            ) {
                                itemBinding.tvTime.text =
                                    "Today: ${lastMessage.date!!.split(" ")[1]}"
                            } else {
                                itemBinding.tvTime.text = lastMessage.date
                            }
                            itemBinding.tvLastMsg.text = lastMessage.message
                            itemBinding.tvUnreadMessages.visibility = View.VISIBLE

                            when {
                                unreadCount > 0 -> itemBinding.tvUnreadMessages.text =
                                    unreadCount.toString()
                                unreadCount > 99 -> itemBinding.tvUnreadMessages.text = "99+"
                                unreadCount == 0 -> itemBinding.tvUnreadMessages.visibility =
                                    View.GONE
                            }
                        } else {
                            itemBinding.tvTime.text = ""
                            itemBinding.tvLastMsg.text = ""
                            itemBinding.ivSeen.visibility = View.INVISIBLE
                            itemBinding.ivSeen2.visibility = View.INVISIBLE
                            itemBinding.tvUnreadMessages.visibility = View.GONE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })

            itemBinding.tvName.text = user.name
            if (user.isOnline!!) itemBinding.vOnlineIndicator.setBackgroundResource(R.drawable.item_online_indicator)
            Picasso.get().load(user.photoUrl).placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.no_image).into(itemBinding.ivProfil)

            itemBinding.root.setOnClickListener { listener.onItemClick(user) }
        }
    }

    interface OnUserClickListener {
        fun onItemClick(user: UserM)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserVh {
        return UserVh(ItemChatsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: UserVh, position: Int) {
        holder.onBind(userList[position])
    }

    override fun getItemCount() = userList.size

}