package uz.lycr.lesson15firebasechatapp

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.app.AlertDialog
import android.view.WindowManager
import android.view.LayoutInflater
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import uz.lycr.lesson15firebasechatapp.adapters.VpAdapter
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import uz.lycr.lesson15firebasechatapp.databinding.FragmentMainBinding
import uz.lycr.lesson15firebasechatapp.databinding.ItemTabBinding
import uz.lycr.lesson15firebasechatapp.models.UserM

class MainFragment : Fragment() {

    private lateinit var currentUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        currentUser = FirebaseAuth.getInstance().currentUser!!
        val binding = FragmentMainBinding.inflate(inflater, container, false)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val gClient = GoogleSignIn.getClient(requireActivity(), gso)

        val list = ArrayList<String>()
        list.add("Chats")
        list.add("Groups")

        binding.vp.adapter = VpAdapter(this, list.size)

        TabLayoutMediator(binding.tl, binding.vp) { tab, position ->
            val itemBinding = ItemTabBinding.inflate(layoutInflater, null, false)
            itemBinding.tv.text = list[position]
            tab.customView = itemBinding.root
        }.attach()

        binding.ivLogOut.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())

            builder.setMessage("Are you sure to log out your account?")
            builder.setPositiveButton("Yes") { dialog, _ ->
                // Change User state to Offline
                FirebaseDatabase.getInstance().getReference("Users/${currentUser.uid}")
                    .updateChildren(mapOf("online" to false))

                // log out
                gClient.signOut()
                FirebaseAuth.getInstance().signOut()

                findNavController().popBackStack()
                findNavController().navigate(R.id.signInFragment)

                dialog.dismiss()
            }

            builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }

            builder.show()
        }

        FirebaseDatabase.getInstance()
            .getReference("Users/${currentUser.uid}")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue(UserM::class.java)
                    binding.tvUserName.text = value!!.name
                    Picasso.get().load(value.photoUrl).placeholder(R.drawable.avatar_placeholder)
                        .error(R.drawable.no_image).into(binding.civProfileImg)

                    if (value.isOnline!!) {
                        binding.tvState.text = "online"
                        binding.vOnlineIndicator.setBackgroundResource(R.drawable.item_online_indicator)
                    } else {
                        binding.tvState.text = "offline"
                        binding.vOnlineIndicator.setBackgroundResource(R.drawable.item_offline_indicator)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        return binding.root
    }

}