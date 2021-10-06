package uz.lycr.lesson15firebasechatapp.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import uz.lycr.lesson15firebasechatapp.VpFragment

class VpAdapter(fm: Fragment, var pageCount: Int) : FragmentStateAdapter(fm) {

    override fun getItemCount() = pageCount

    override fun createFragment(position: Int): Fragment {
        return VpFragment.newInstance(position)
    }

}