package com.luiz.ghub.ui

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.luiz.ghub.R
import com.luiz.ghub.databinding.FragmentUserProfileBinding
import com.luiz.ghub.models.Project
import com.luiz.ghub.models.User
import com.luiz.ghub.ui.adapter.ProfileProjectAdapter

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    private lateinit var binding: FragmentUserProfileBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var currentUser: User? = null
    private var isMyProfile = false

    private val DEFAULT_BANNER_URL = "https://i.pinimg.com/736x/bb/39/81/bb3981929b9f6f450fe7b1efbeaf9884.jpg"

    private var tempProfileUri: Uri? = null
    private var tempBannerUri: Uri? = null

    private val pickProfileMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            tempProfileUri = uri
            binding.imgProfile.setImageURI(uri)
            uploadImageToCloudinary(uri, isBanner = false)
        }
    }

    private val pickBannerMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            tempBannerUri = uri
            binding.imgBanner.setImageURI(uri)
            uploadImageToCloudinary(uri, isBanner = true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserProfileBinding.bind(view)
        initCloudinary()

        val userFromArgs = arguments?.getParcelable<User>("user_data")

        if (userFromArgs != null) {
            currentUser = userFromArgs
            isMyProfile = false
            setupUI(currentUser!!)
        } else {
            isMyProfile = true
            loadMyProfile()
        }

        setupClickListeners()
    }

    private fun initCloudinary() {
        try {
            val config = HashMap<String, String>()
            config["cloud_name"] = "dqudqpdbd"
            config["secure"] = "true"
            MediaManager.init(requireContext(), config)
        } catch (e: Exception) { }
    }

    private fun loadMyProfile() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId != null) {
            db.collection("users").document(currentUserId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val myUser = document.toObject(User::class.java)

                        if (myUser != null) {
                            val userWithId = myUser.copy(id = document.id)

                            currentUser = userWithId
                            setupUI(userWithId)
                        }
                    }
                }
        }
    }

    private fun setupUI(user: User) {
        binding.txtName.text = user.name ?: "Sem nome"

        if (!user.professions.isNullOrEmpty()) {
            binding.txtProfessions.text = user.professions.joinToString(", ")
        } else {
            binding.txtProfessions.text = "Membro"
        }

        if (!user.photoURL.isNullOrEmpty()) {
            val secureUrl = user.photoURL?.replace("http:", "https:")
            Glide.with(this).load(secureUrl).circleCrop().placeholder(R.drawable.account).into(binding.imgProfile)
        }

        val bannerToLoad = if (!user.bannerURL.isNullOrEmpty()) {
            user.bannerURL?.replace("http:", "https:")
        } else {
            DEFAULT_BANNER_URL
        }

        Glide.with(this)
            .load(bannerToLoad)
            .centerCrop()
            .placeholder(R.drawable.gradient_bottom_up)
            .into(binding.imgBanner)

        setupSkills(user)

        fetchUserProjects(user.id)

        if (isMyProfile) {
            binding.btnEditProfile.isVisible = true
            enableEditingMode()
        } else {
            binding.btnEditProfile.isVisible = false
        }
    }

    private fun setupSkills(user: User) {
        binding.chipGroupSkills.removeAllViews()

        user.skills?.forEach { skill ->
            val chip = Chip(requireContext())
            chip.text = skill

            chip.setChipBackgroundColorResource(R.color.black)
            chip.setTextColor(resources.getColor(android.R.color.white, null))


            binding.chipGroupSkills.addView(chip)
        }
    }

    private fun fetchUserProjects(userId: String) {
        db.collection("projects").whereEqualTo("ownerUid", userId).get()
            .addOnSuccessListener { documents ->
                val projectList = mutableListOf<Project>()
                for (doc in documents) {
                    val project = doc.toObject(Project::class.java)
                    project.id = doc.id
                    projectList.add(project)
                }
                setupProjectsList(projectList)
            }
    }

    private fun setupProjectsList(projects: List<Project>) {
        if (projects.isNotEmpty()) {
            binding.rvUserProjects.isVisible = true
            binding.txtEmptyProjects.isVisible = false

            val adapter = ProfileProjectAdapter(projects) { selectedProject ->
                val bundle = Bundle()
                bundle.putParcelable("project_data", selectedProject)
                findNavController().navigate(R.id.action_userProfileFragment_to_projectDetailFragment, bundle)
            }
            binding.rvUserProjects.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            binding.rvUserProjects.adapter = adapter
        } else {
            binding.rvUserProjects.isVisible = false
            binding.txtEmptyProjects.isVisible = true
        }
    }

    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener {
            showEditDataDialog()
        }
    }

    private fun enableEditingMode() {
        binding.imgProfile.setOnClickListener {
            pickProfileMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.imgBanner.setOnClickListener {
            pickBannerMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun showEditDataDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Editar Perfil")

        val layout = android.widget.LinearLayout(requireContext())
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val inputName = EditText(requireContext())
        inputName.hint = "Nome"
        inputName.setText(currentUser?.name)
        layout.addView(inputName)

        val inputBio = EditText(requireContext())
        inputBio.hint = "Bio (Opcional)"
        inputBio.setText(currentUser?.bio)
        layout.addView(inputBio)

        builder.setView(layout)

        builder.setPositiveButton("Salvar") { dialog, _ ->
            val newName = inputName.text.toString()
            val newBio = inputBio.text.toString()
            updateUserDataInFirestore(newName, newBio)
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun uploadImageToCloudinary(uri: Uri, isBanner: Boolean) {
        Toast.makeText(context, "Enviando imagem...", Toast.LENGTH_SHORT).show()
        MediaManager.get().upload(uri)
            .unsigned("piGHUB")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val secureUrl = resultData["secure_url"] as String
                    updateImageUrlInFirestore(secureUrl, isBanner)
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    Toast.makeText(context, "Erro upload: ${error.description}", Toast.LENGTH_SHORT).show()
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    private fun updateImageUrlInFirestore(url: String, isBanner: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val field = if (isBanner) "bannerURL" else "photoURL"

        db.collection("users").document(userId)
            .update(field, url)
            .addOnSuccessListener {
                Toast.makeText(context, "Imagem atualizada!", Toast.LENGTH_SHORT).show()
                currentUser = if (isBanner) currentUser?.copy(bannerURL = url) else currentUser?.copy(photoURL = url)
                currentUser?.let { setupUI(it) }
            }
    }

    private fun updateUserDataInFirestore(name: String, bio: String) {
        val userId = auth.currentUser?.uid ?: return
        val updates = mapOf("name" to name, "bio" to bio)

        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "Perfil atualizado!", Toast.LENGTH_SHORT).show()
                binding.txtName.text = name
                currentUser = currentUser?.copy(name = name, bio = bio)
            }
    }
}