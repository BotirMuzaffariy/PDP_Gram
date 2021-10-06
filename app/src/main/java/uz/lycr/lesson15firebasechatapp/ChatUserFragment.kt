package uz.lycr.lesson15firebasechatapp

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.text.ClipboardManager
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.marginBottom
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import uz.lycr.lesson15firebasechatapp.adapters.ChatUserAdapter
import uz.lycr.lesson15firebasechatapp.databinding.DialogDeleteItemBinding
import uz.lycr.lesson15firebasechatapp.databinding.FragmentChatUserBinding
import uz.lycr.lesson15firebasechatapp.models.MessageM
import uz.lycr.lesson15firebasechatapp.models.UserM
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatUserFragment : Fragment() {

    private var user: UserM = UserM()

    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var listener: ValueEventListener
    private lateinit var fbDatabase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var uReference: DatabaseReference
    private lateinit var seenListener: ValueEventListener
    private lateinit var binding: FragmentChatUserBinding

    private lateinit var list: ArrayList<MessageM>
    private lateinit var chatUserAdapter: ChatUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            user = it.getSerializable("user") as UserM
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
        uReference = fbDatabase.getReference("Users")
        reference = fbDatabase.getReference("UsersMessages")
        binding = FragmentChatUserBinding.inflate(inflater, container, false)

        listener = uReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot.children

                for (child in children) {

                    val value = child.getValue(UserM::class.java)

                    if (value != null && value.uid == user.uid) {
                        binding.tvName.text = value.name
                        binding.tvState.text =
                            if (value.isOnline == true) "online" else "last seen recently"
                        Picasso.get().load(value.photoUrl)
                            .placeholder(R.drawable.avatar_placeholder)
                            .into(binding.ivProfil)
                    }
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

        binding.ivSend.setOnClickListener {
            val message = binding.etMessage.text.toString()

            if (message.trim() == "") {
                Toast.makeText(
                    requireContext(),
                    "The empty message can not be send",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val key = reference.push().key
                val date = SimpleDateFormat("dd.MM.yyyy HH:mm").format(Date())
                val messageM = MessageM(key, date, message, false, currentUser.uid, user.uid)

                reference.child("${currentUser.uid}_&_${user.uid}/$key").setValue(messageM)
                reference.child("${user.uid}_&_${currentUser.uid}/$key").setValue(messageM)
            }

            binding.etMessage.setText("")
        }

        // Read all messages
        reference.child("${currentUser.uid}_&_${user.uid}")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    list.clear()
                    val children = snapshot.children

                    for (child in children) {
                        val value = child.getValue(MessageM::class.java)

                        if (value != null) {
                            list.add(value)
                        }
                    }

                    chatUserAdapter = ChatUserAdapter(list,
                        listOf("${currentUser.photoUrl}", "${user.photoUrl}"),
                        currentUser.uid,
                        object : ChatUserAdapter.MyChatUserAdapterClickListener {
                            override fun onOptionsClick(
                                view: View,
                                message: MessageM,
                                type: Int
                            ) {
                                showPopup(view, message, type)
                            }
                        })
                    binding.rv.adapter = chatUserAdapter
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        seenMessage(user.uid!!)

        return binding.root
    }

    private fun showPopup(view: View, message: MessageM, type: Int) {
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
                        var isDouble = true
                        val builder = AlertDialog.Builder(requireContext())
                        val inflate = DialogDeleteItemBinding.inflate(layoutInflater)

                        builder.setView(inflate.root)
                        val dialog = builder.create()

                        inflate.chb.text = "Also delete ${user.name!!.split(" ")[0]}"
                        inflate.tvCancel.setOnClickListener { dialog.dismiss() }
                        inflate.chb.setOnCheckedChangeListener { _, isChecked ->
                            isDouble = isChecked
                        }

                        inflate.tvDelete.setOnClickListener {
                            val child = reference.child("${user.uid}_&_${currentUser.uid}")
                                .child(message.id!!)

                            reference.child("${currentUser.uid}_&_${user.uid}").child(message.id!!)
                                .removeValue()
                            if (isDouble && child.key != null) child.removeValue()
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
                                reference.child("${currentUser.uid}_&_${user.uid}").child(message.id!!).updateChildren(mapOf("message" to msg))
                                reference.child("${user.uid}_&_${currentUser.uid}").child(message.id!!).updateChildren(mapOf("message" to msg))

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
            popup.menuInflater.inflate(R.menu.popup_left_menu_chat, popup.menu)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) popup.setForceShowIcon(true)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.m_chat_copy -> {
                        setClipboard(message.message!!)
                        Toast.makeText(requireContext(), "Message copied", Toast.LENGTH_SHORT)
                            .show()
                        return@setOnMenuItemClickListener true
                    }
                    R.id.m_chat_copy_paste -> {
                        setClipboard(message.message!!)
                        binding.etMessage.setText(message.message)
                        binding.etMessage.selectAll()
                        return@setOnMenuItemClickListener true
                    }
                    R.id.m_chat_delete -> {
                        val builder = AlertDialog.Builder(requireContext())
                        val inflate = DialogDeleteItemBinding.inflate(layoutInflater)

                        builder.setView(inflate.root)
                        val dialog = builder.create()

                        inflate.chb.visibility = View.GONE
                        inflate.tvNote.visibility = View.VISIBLE
                        inflate.tvNote.text =
                            "Note: this message will not be deleted from ${user.name!!.split(" ")[0]}"
                        inflate.tvNote.setPadding(0, 4, 0, 14)
                        inflate.tvCancel.setOnClickListener { dialog.dismiss() }

                        inflate.tvDelete.setOnClickListener {
                            reference.child("${currentUser.uid}_&_${user.uid}").child(message.id!!)
                                .removeValue()
                            dialog.dismiss()
                        }

                        dialog.show()
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

    private fun seenMessage(userId: String) {
        seenListener = reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val key = child.key
                    if (key == "${currentUser.uid}_&_${userId}") {
                        for (child1 in child.children) {
                            val message = child1.getValue(MessageM::class.java)!!
                            if (message.receiverUid == currentUser.uid && !message.isSeen!!) {
                                child1.ref.updateChildren(mapOf("seen" to true))
                                reference.child("${userId}_&_${currentUser.uid}/${message.id}")
                                    .updateChildren(mapOf("seen" to true))
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onPause() {
        uReference.removeEventListener(listener)
        reference.removeEventListener(seenListener)
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