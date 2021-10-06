package uz.lycr.lesson15firebasechatapp

import android.net.Uri
import android.view.View
import android.os.Bundle
import android.widget.Toast
import android.view.ViewGroup
import android.app.AlertDialog
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import uz.lycr.lesson15firebasechatapp.models.UserM
import uz.lycr.lesson15firebasechatapp.models.GroupM
import androidx.navigation.fragment.findNavController
import uz.lycr.lesson15firebasechatapp.adapters.UserRvAdapter
import uz.lycr.lesson15firebasechatapp.adapters.GroupRvAdapter
import androidx.activity.result.contract.ActivityResultContracts
import uz.lycr.lesson15firebasechatapp.databinding.FragmentVpBinding
import uz.lycr.lesson15firebasechatapp.databinding.ItemDialogAddGroupBinding
import uz.lycr.lesson15firebasechatapp.databinding.ItemDialogLoadingBinding

class VpFragment : Fragment() {

    private var imgUri = Uri.EMPTY
    private var position: Int = -1
    private val ARG_PARAM1 = "position"

    private lateinit var currentUserUid: String
    private lateinit var binding: FragmentVpBinding
    private lateinit var dialogBinding: ItemDialogAddGroupBinding

    private lateinit var listener: ValueEventListener

    private lateinit var reference: DatabaseReference
    private lateinit var firebaseDatabase: FirebaseDatabase

    private lateinit var userList: ArrayList<UserM>
    private lateinit var groupList: ArrayList<GroupM>
    private lateinit var chatUserList: ArrayList<UserM>

    private lateinit var userAdapter: UserRvAdapter
    private lateinit var groupAdapter: GroupRvAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            position = it.getInt(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        userList = ArrayList()
        groupList = ArrayList()
        chatUserList = ArrayList()

        firebaseDatabase = FirebaseDatabase.getInstance()
        currentUserUid = FirebaseAuth.getInstance().currentUser!!.uid
        binding = FragmentVpBinding.inflate(inflater, container, false)

        when (position) {
            0 -> {
                reference = firebaseDatabase.getReference("Users")
                listener = reference.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        userList.clear()

                        for (user in snapshot.children) {
                            val value = user.getValue(UserM::class.java)
                            if (value!!.uid != currentUserUid) userList.add(value)
                        }

                        userAdapter = UserRvAdapter(userList,
                            object : UserRvAdapter.OnUserClickListener {
                            override fun onItemClick(user: UserM) {
                                val bundle = Bundle()
                                bundle.putSerializable("user", user)
                                findNavController().navigate(
                                    R.id.action_mainFragment_to_chatUserFragment,
                                    bundle
                                )
                            }
                        })

                        binding.rv.adapter = userAdapter
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                    }
                })
            }
            1 -> {
                binding.cvAddGroup.visibility = View.VISIBLE
                reference = firebaseDatabase.getReference("Groups")
                listener = reference.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        groupList.clear()

                        for (child in snapshot.children) {
                            val value = child.getValue(GroupM::class.java)
                            if (value != null) {
                                groupList.add(value)
                            }
                        }

                        groupAdapter =
                            GroupRvAdapter(groupList, object : GroupRvAdapter.OnGroupClickListener {
                                override fun onItemClick(group: GroupM) {
                                    val bundle = Bundle()
                                    bundle.putSerializable("group", group)
                                    findNavController().navigate(
                                        R.id.action_mainFragment_to_chatGroupFragment,
                                        bundle
                                    )
                                }
                            })

                        binding.rv.adapter = groupAdapter
                        binding.rv.setHasFixedSize(true)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        binding.rv.setHasFixedSize(true)

        binding.cvAddGroup.setOnClickListener {
            var photoUrl = "no"
            imgUri = Uri.EMPTY
            val builder = AlertDialog.Builder(requireContext())
            dialogBinding = ItemDialogAddGroupBinding.inflate(layoutInflater)

            builder.setView(dialogBinding.root)
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

            builder.setPositiveButton("Create") { dialog, _ ->
                val name = dialogBinding.etName.text.toString()
                val about = dialogBinding.etAbout.text.toString()

                if (name.trim().isNotEmpty() && about.trim().isNotEmpty()) {
                    val key = reference.push().key!!
                    if (imgUri == Uri.EMPTY) {
                        val group = GroupM(name, about, key, currentUserUid, photoUrl)
                        reference.child(key).setValue(group)
                        dialog.dismiss()
                    } else {
                        val loadingBuilder = AlertDialog.Builder(requireContext())
                        val loadingBinding = ItemDialogLoadingBinding.inflate(layoutInflater)
                        loadingBuilder.setView(loadingBinding.root)
                        val loadDialog = loadingBuilder.create()
                        loadDialog.show()

                        FirebaseStorage.getInstance().getReference("images/$key").putFile(imgUri)
                            .addOnSuccessListener {
                                if (it.task.isSuccessful) {
                                    it.metadata?.reference?.downloadUrl!!.addOnSuccessListener { uri ->
                                        photoUrl = uri.toString()
                                        val group =
                                            GroupM(name, about, key, currentUserUid, photoUrl)
                                        reference.child(key).setValue(group)
                                        loadDialog.dismiss()
                                    }
                                }
                            }

                    }
                } else {
                    Toast.makeText(requireContext(), "Enter the full details", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            dialogBinding.ivGroupImg.setOnClickListener {
                getGalleryContent.launch("image/*")
            }

            builder.show()
        }

        return binding.root
    }

    private val getGalleryContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                imgUri = it
                dialogBinding.ivGroupImg.setImageURI(it)
            }
        }

    companion object {
        @JvmStatic
        fun newInstance(param1: Int) =
            VpFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                }
            }
    }

}