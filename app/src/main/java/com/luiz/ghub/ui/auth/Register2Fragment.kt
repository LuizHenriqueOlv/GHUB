package com.luiz.ghub.ui.auth

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.luiz.ghub.R
import com.luiz.ghub.databinding.FragmentRegister2Binding

class Register2Fragment : Fragment() {

    private var _binding: FragmentRegister2Binding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            imageUri = uri
            binding.imageView.setImageURI(uri)
        } else {
            Log.d("PhotoPicker", "Nenhuma imagem selecionada")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegister2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCloudinary()
        initListeners()
    }

    private fun initCloudinary() {
        try {
            val config = HashMap<String, String>()
            config["cloud_name"] = "dqudqpdbd"
            config["secure"] = "true"
            MediaManager.init(requireContext(), config)
        } catch (e: Exception) {
        }
    }

    private fun initListeners() {
        binding.imageView.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnFinish.setOnClickListener {
            val name = binding.edtName.text.toString()
            val dob = binding.edtDob.text.toString()

            if (name.isNotEmpty()) {
                if (imageUri != null) {
                    uploadImageToCloudinary(name, dob)
                } else {
                    saveUserToFirestore(name, dob, "")
                }
            } else {
                Toast.makeText(requireContext(), "Preencha o nome pelo menos", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnReturnLogin2.setOnClickListener {
            findNavController().navigate(R.id.action_register2Fragment_to_loginFragment)
        }
    }

    private fun uploadImageToCloudinary(name: String, dob: String) {
        binding.btnFinish.isEnabled = false
        binding.btnFinish.text = "Enviando imagem..."

        MediaManager.get().upload(imageUri)
            .unsigned("piGHUB")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val secureUrl = resultData["secure_url"] as String
                    Log.d("Cloudinary", "Upload sucesso: $secureUrl")
                    saveUserToFirestore(name, dob, secureUrl)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.e("Cloudinary", "Erro: ${error.description}")
                    binding.btnFinish.isEnabled = true
                    binding.btnFinish.text = "Cadastrar"
                    Toast.makeText(requireContext(), "Erro na imagem: ${error.description}", Toast.LENGTH_LONG).show()
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    private fun saveUserToFirestore(name: String, dob: String, imageUrl: String) {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val userId = user.uid
            val email = user.email ?: ""
            val userMap = hashMapOf(
                "id" to userId,
                "name" to name,
                "email" to email,
                "photoURL" to imageUrl,
                "bio" to "Nascido em: $dob"
            )

            db.collection("users").document(userId)
                .set(userMap)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Bem-vindo!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_register2Fragment_to_homeFragment)
                }
                .addOnFailureListener { e ->
                    binding.btnFinish.isEnabled = true
                    binding.btnFinish.text = "Cadastrar"
                    Toast.makeText(requireContext(), "Erro no banco: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Erro de autenticação.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}