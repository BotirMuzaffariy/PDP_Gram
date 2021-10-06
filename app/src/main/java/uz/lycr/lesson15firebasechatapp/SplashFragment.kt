package uz.lycr.lesson15firebasechatapp

import android.view.View
import android.os.Bundle
import android.os.Looper
import android.os.Handler
import android.view.ViewGroup
import android.view.WindowManager
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.fragment.findNavController

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        Handler(Looper.getMainLooper()).postDelayed({
            if (FirebaseAuth.getInstance().currentUser == null) {
                findNavController().navigate(R.id.action_splashFragment_to_signInFragment)
            } else {
                findNavController().navigate(R.id.action_splashFragment_to_mainFragment)
            }
        }, 100)

        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

}