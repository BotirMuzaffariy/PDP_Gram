package uz.lycr.lesson15firebasechatapp

import android.app.Activity
import java.util.*
import android.view.View
import android.os.Bundle
import android.widget.Toast
import android.view.ViewGroup
import android.app.AlertDialog
import android.content.ClipData
import android.content.Context
import android.os.Build
import java.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import kotlin.collections.ArrayList
import com.google.firebase.database.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import uz.lycr.lesson15firebasechatapp.models.UserM
import uz.lycr.lesson15firebasechatapp.models.GroupM
import androidx.navigation.fragment.findNavController
import com.squareup.picasso.Picasso
import uz.lycr.lesson15firebasechatapp.models.MessageGroupM
import uz.lycr.lesson15firebasechatapp.adapters.ChatGroupAdapter
import uz.lycr.lesson15firebasechatapp.databinding.DialogDeleteItemBinding
import uz.lycr.lesson15firebasechatapp.databinding.FragmentChatGroupBinding
import uz.lycr.lesson15firebasechatapp.models.MessageM

class ChatGroupFragment : Fragment() {

    private var currentGroup = GroupM()

    private lateinit var message: String
    private lateinit var list: ArrayList<MessageGroupM>
    private lateinit var chatGroupAdapter: ChatGroupAdapter

    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var fbDatabase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var listener: ValueEventListener
    private lateinit var gReference: DatabaseReference
    private lateinit var seeListener: ValueEventListener
    private lateinit var binding: FragmentChatGroupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentGroup = it.getSerializable("group") as GroupM
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        list = ArrayList()
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!

        fbDatabase = FirebaseDatabase.getInstance()
        gReference = fbDatabase.getReference("Groups")
        reference = fbDatabase.getReference("GroupsMessages")
        binding = FragmentChatGroupBinding.inflate(inflater, container, false)

        listener = gReference.child(currentGroup.groupId!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val group = snapshot.getValue(GroupM::class.java)!!
                    message = group.about!!
                    binding.tvName.text = group.name
                    if (group.photoUrl != "no") {
                        Picasso.get().load(group.photoUrl)
                            .placeholder(R.drawable.avatar_placeholder).error(R.drawable.no_image)
                            .into(binding.ivGroupImg)
                    } else {
                        binding.ivGroupImg.setImageResource(R.drawable.avatar_placeholder)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        binding.tvName.isSelected = true
        binding.ivBack.setOnClickListener {
            hideKeyboard(requireActivity())
            findNavController().popBackStack()
        }

        binding.ivInfo.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())

            builder.setMessage(message)
            builder.setTitle("About group")
            builder.setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }

            builder.show()
        }

        binding.ivSend.setOnClickListener {
            val message = binding.etMessage.text.toString()

            if (message.trim() != "") {
                val key = reference.push().key
                val date = SimpleDateFormat("dd.MM.yyyy HH:mm").format(Date())
                val messageGroup = MessageGroupM(
                    key,
                    date,
                    message,
                    currentUser.uid,
                    currentUser.displayName,
                    currentUser.photoUrl?.toString(),
                    false
                )
                reference.child("${currentGroup.groupId}/$key").setValue(messageGroup)
            } else {
                Toast.makeText(
                    requireContext(),
                    "The empty message can not be send",
                    Toast.LENGTH_SHORT
                ).show()
            }

            binding.etMessage.setText("")
        }

        // Read all messages
        reference.child(currentGroup.groupId!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                snapshot.children.forEach { list.add(it.getValue(MessageGroupM::class.java)!!) }

                chatGroupAdapter = ChatGroupAdapter(list, currentUser.uid, object : ChatGroupAdapter.MyChatGroupAdapterClickListener{
                    override fun onOptionsClick(view: View, message: MessageGroupM, type: Int) {
                        showPopup(view, message, type)
                    }
                })
                binding.rv.adapter = chatGroupAdapter
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        seenMessage()

        return binding.root
    }

    private fun showPopup(view: View, message: MessageGroupM, type: Int) {
        val popup = PopupMenu(requireContext(), view)

        if (type == 1) {
            popup.menuInflater.inflate(R.menu.popup_right_menu, popup.menu)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) popup.setForceShowIcon(true)

            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.m_r_copy -> {
                        setClipboard(message.message!!)
                        Toast.makeText(requireContext(), "Message copied", Toast.LENGTH_SHORT)
                            .show()
                        return@setOnMenuItemClickListener true
                    }
                    R.id.m_r_copy_paste -> {
                        setClipboard(message.message!!)
                        binding.etMessage.setText(message.message)
                        binding.etMessage.selectAll()
                        return@setOnMenuItemClickListener true
                    }
                    R.id.m_r_delete -> {
                        val builder = AlertDialog.Builder(requireContext())
                        val inflate = DialogDeleteItemBinding.inflate(layoutInflater)

                        builder.setView(inflate.root)
                        val dialog = builder.create()

                        inflate.chb.visibility = View.GONE
                        inflate.tvAnswer.text = "The message is deleted for all members of the group"
                        inflate.tvCancel.setOnClickListener { dialog.dismiss() }

                        inflate.tvDelete.setOnClickListener {
                            reference.child(currentGroup.groupId!!).child(message.id!!).removeValue()
                            dialog.dismiss()
                        }

                        dialog.show()
                        return@setOnMenuItemClickListener true
                    }
                    R.id.m_r_edit -> {
                        binding.ivEdit.visibility = View.VISIBLE
                        binding.ivSend.visibility = View.INVISIBLE
                        binding.etMessage.setText(message.message)
                        binding.etMessage.selectAll()

                        binding.ivEdit.setOnClickListener {
                            val msg = binding.etMessage.text.toString()
                            if (msg.trim().isNotEmpty()) {
                                reference.child(currentGroup.groupId!!).child(message.id!!).updateChildren(mapOf("message" to msg))

                                binding.etMessage.setText("")
                                binding.ivSend.visibility = View.VISIBLE
                                binding.ivEdit.visibility = View.INVISIBLE
                                Toast.makeText(requireContext(), "Successfully edited", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "The empty message can not be edited",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        return@setOnMenuItemClickListener true
                    }
                    else -> {
                        return@setOnMenuItemClickListener false
                    }
                }
            }
        } else if (type == 2) {
            popup.menuInflater.inflate(R.menu.popup_left_menu_group, popup.menu)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) popup.setForceShowIcon(true)

            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.m_group_copy -> {
                        setClipboard(message.message!!)
                        Toast.makeText(requireContext(), "Message copied", Toast.LENGTH_SHORT)
                            .show()
                        return@setOnMenuItemClickListener true
                    }
                    R.id.m_group_copy_paste -> {
                        setClipboard(message.message!!)
                        binding.etMessage.setText(message.message)
                        binding.etMessage.selectAll()
                        return@setOnMenuItemClickListener true
                    }
                    else -> {
                        return@setOnMenuItemClickListener false
                    }
                }
            }
        }

        popup.show()
    }

    private fun setClipboard(text: String) {
        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun seenMessage() {
        seeListener = reference.child(currentGroup.groupId!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        val message = child.getValue(MessageGroupM::class.java)!!
                        if (message.senderUid != currentUser.uid && !message.isSeen!!) {
                            child.ref.updateChildren(mapOf("seen" to true))
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    override fun onPause() {
        gReference.removeEventListener(listener)
        reference.removeEventListener(seeListener)
        super.onPause()
    }

    private fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

}