package uz.lycr.lesson15firebasechatapp

import android.os.Bundle
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import uz.lycr.lesson15firebasechatapp.models.UserM

class MainActivity : AppCompatActivity() {

    private lateinit var currentUserUid: String
    private var currentUser: FirebaseUser? = null
    private lateinit var uReference: DatabaseReference
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onSupportNavigateUp(): Boolean {
        return Navigation.findNavController(this, R.id.my_nav_host).navigateUp()
    }

    override fun onResume() {
        currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            currentUserUid = currentUser!!.uid
            firebaseDatabase = FirebaseDatabase.getInstance()
            uReference = firebaseDatabase.getReference("Users")

            // Change User state to Online
            uReference.child(currentUserUid).updateChildren(mapOf("online" to true))

        }

        super.onResume()
    }

    override fun onPause() {
        currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            currentUserUid = FirebaseAuth.getInstance().currentUser!!.uid
            firebaseDatabase = FirebaseDatabase.getInstance()
            uReference = firebaseDatabase.getReference("Users")

            // Change User state to Offline
            uReference.child(currentUserUid).updateChildren(mapOf("online" to false))
        }

        super.onPause()
    }

}