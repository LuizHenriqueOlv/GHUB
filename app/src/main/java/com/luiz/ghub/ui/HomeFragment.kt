package com.luiz.ghub.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.luiz.ghub.R
import com.luiz.ghub.databinding.FragmentHomeBinding
import com.luiz.ghub.databinding.FragmentLoginBinding
import com.luiz.ghub.databinding.FragmentRegister2Binding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        binding.imgRight.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_authentication)
        }
    }

}
