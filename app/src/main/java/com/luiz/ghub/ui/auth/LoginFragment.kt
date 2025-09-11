package com.luiz.ghub.ui.auth

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.luiz.ghub.R
import com.luiz.ghub.databinding.FragmentLoginBinding
import com.luiz.ghub.databinding.FragmentSplashBinding

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()
        }

    private fun initListeners() {

        binding.btnLogin.setOnClickListener {
            findNavController().navigate(R.id.action_global_homeFragment)
        }

        binding.btnGoogle.setOnClickListener {
            findNavController().navigate(R.id.action_global_homeFragment)
        }

        binding.btnCadastro.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
    }
}
