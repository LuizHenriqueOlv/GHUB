package com.luiz.ghub.ui.auth

import android.os.Bundle
import android.widget.Button
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.luiz.ghub.R
import com.luiz.ghub.databinding.FragmentRegister2Binding
import com.luiz.ghub.databinding.FragmentRegisterBinding

class Register2Fragment : Fragment() {
    private var _binding: FragmentRegister2Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegister2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()

    }

    private fun initListeners() {
        binding.btnFinish.setOnClickListener {
            findNavController().navigate(R.id.action_register2Fragment_to_homeFragment)
        }

        binding.btnCadastroGoogle2.setOnClickListener {
            findNavController().navigate(R.id.action_register2Fragment_to_homeFragment)
        }


        binding.btnReturnLogin2.setOnClickListener {
            findNavController().navigate(R.id.action_register2Fragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
