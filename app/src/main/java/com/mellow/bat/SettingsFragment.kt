package com.mellow.bat

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.mellow.bat.databinding.FragmentSettingsBinding


class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(inflater,container,false)
        binding.imageView12.setOnClickListener {
            val navController = Navigation.findNavController(requireActivity(),R.id.fragmentContainerView)
            navController.popBackStack()
        }
        var sounds = requireContext().getSharedPreferences("prefs",Context.MODE_PRIVATE).getBoolean("sounds",false)
        var music = requireContext().getSharedPreferences("prefs",Context.MODE_PRIVATE).getBoolean("music",false)
        binding.switch1.isChecked = music
        binding.switch2.isChecked = sounds
        binding.switch1.setOnCheckedChangeListener { buttonView, isChecked ->
            music = !music
            requireContext().getSharedPreferences("prefs",Context.MODE_PRIVATE).edit().putBoolean("music",music).apply()
        }
        binding.switch2.setOnCheckedChangeListener { buttonView, isChecked ->
            sounds = !sounds
            requireContext().getSharedPreferences("prefs",Context.MODE_PRIVATE).edit().putBoolean("sounds",sounds).apply()
        }
        return binding.root
    }


}