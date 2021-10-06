package uz.lycr.lesson15firebasechatapp.adapters

import android.renderscript.Script
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import uz.lycr.lesson15firebasechatapp.R
import uz.lycr.lesson15firebasechatapp.models.GroupM
import uz.lycr.lesson15firebasechatapp.databinding.ItemGroupsBinding
import uz.lycr.lesson15firebasechatapp.models.MessageGroupM
import uz.lycr.lesson15firebasechatapp.models.MessageM
import java.text.SimpleDateFormat
import java.util.*

class GroupRvAdapter(var groupList: List<GroupM>, var listener: OnGroupClickListener) :
    RecyclerView.Adapter<GroupRvAdapter.UserVh>() {

    inner class UserVh(var itemBinding: ItemGroupsBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun onBind(group: GroupM) {
            FirebaseDatabase.getInstance()
                .getReference("GroupsMessages/${group.groupId}")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            var unreadCount = 0
                            val lastMessage =
                                snapshot.children.last().getValue(MessageGroupM::class.java)!!

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
                                val message = child.getValue(MessageGroupM::class.java)!!
                                if (!message.isSeen!! && message.senderUid != FirebaseAuth.getInstance().currentUser!!.uid) unreadCount++
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

            itemBinding.tvName.text = group.name
            if (group.photoUrl != "no") {
                Picasso.get().load(group.photoUrl).placeholder(R.drawable.avatar_placeholder)
                    .error(R.drawable.no_image).into(itemBinding.ivGroup)
            } else itemBinding.ivGroup.setImageResource(R.drawable.avatar_placeholder)

            itemBinding.root.setOnClickListener { listener.onItemClick(group) }
        }
    }

    interface OnGroupClickListener {
        fun onItemClick(group: GroupM)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserVh {
        return UserVh(ItemGroupsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: UserVh, position: Int) {
        holder.onBind(groupList[position])
    }

    override fun getItemCount() = groupList.size

}