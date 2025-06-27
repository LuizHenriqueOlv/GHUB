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

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnRecover = view.findViewById<TextView>(R.id.btnRecover)
        btnRecover.paintFlags = btnRecover.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        // Navegar para RegisterFragment
        btnRecover.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }



    }
}
