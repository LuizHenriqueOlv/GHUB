package com.luiz.ghub.ui

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.luiz.ghub.R
import com.luiz.ghub.databinding.FragmentProjectUpdatesBinding
import com.luiz.ghub.models.ProjectUpdate
import com.luiz.ghub.ui.adapter.UpdatesAdapter
import java.util.Date

class ProjectUpdatesFragment : Fragment() {

    private var _binding: FragmentProjectUpdatesBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    private var projectId: String = ""
    private var ownerUid: String = ""

    private var tempImageUri: Uri? = null
    private var imgPreviewDialog: ImageView? = null
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            tempImageUri = uri
            imgPreviewDialog?.setImageURI(uri)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProjectUpdatesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        projectId = arguments?.getString("project_id") ?: return
        ownerUid = arguments?.getString("owner_uid") ?: ""

        setupRecyclerView()
        checkPermissionAndSetupFab()
        fetchUpdates()
        initCloudinary()
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

    private fun setupRecyclerView() {
        binding.rvUpdates.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun checkPermissionAndSetupFab() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null && currentUserId == ownerUid) {
            binding.fabAddUpdate.isVisible = true
            binding.fabAddUpdate.setOnClickListener {
                showAddUpdateDialog()
            }
        } else {
            binding.fabAddUpdate.isVisible = false
        }
    }

    private fun fetchUpdates() {
        db.collection("projects").document(projectId).collection("updates")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val updatesList = mutableListOf<ProjectUpdate>()
                for (doc in documents) {
                    val update = doc.toObject(ProjectUpdate::class.java)
                    updatesList.add(update)
                }
                binding.rvUpdates.adapter = UpdatesAdapter(updatesList)

                binding.txtEmptyUpdates.isVisible = updatesList.isEmpty()
            }
    }


    private fun showAddUpdateDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_update, null)
        val edtTitle = dialogView.findViewById<EditText>(R.id.edtUpdateTitle)
        val edtDesc = dialogView.findViewById<EditText>(R.id.edtUpdateDesc)
        val btnAddImg = dialogView.findViewById<View>(R.id.btnAddImage)
        imgPreviewDialog = dialogView.findViewById(R.id.imgUpdatePreview)

        tempImageUri = null

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Publicar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        btnAddImg.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val title = edtTitle.text.toString()
            val desc = edtDesc.text.toString()

            if (title.isNotEmpty() && desc.isNotEmpty()) {
                if (tempImageUri != null) {
                    uploadImageAndPublish(title, desc)
                    dialog.dismiss()
                } else {
                    publishUpdate(title, desc, emptyList())
                    dialog.dismiss()
                }
            } else {
                Toast.makeText(context, "Preencha título e descrição", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImageAndPublish(title: String, desc: String) {
        Toast.makeText(context, "Publicando...", Toast.LENGTH_SHORT).show()

        MediaManager.get().upload(tempImageUri)
            .unsigned("piGHUB")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as String
                    publishUpdate(title, desc, listOf(url))
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    Toast.makeText(context, "Erro imagem: ${error.description}", Toast.LENGTH_SHORT).show()
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    private fun publishUpdate(title: String, desc: String, imgs: List<String>) {
        val update = hashMapOf(
            "title" to title,
            "desc" to desc,
            "imgs" to imgs,
            "createdAt" to Timestamp.now()
        )

        db.collection("projects").document(projectId).collection("updates")
            .add(update)
            .addOnSuccessListener {
                Toast.makeText(context, "Update publicado!", Toast.LENGTH_SHORT).show()
                fetchUpdates()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(projectId: String, ownerUid: String): ProjectUpdatesFragment {
            val fragment = ProjectUpdatesFragment()
            val args = Bundle()
            args.putString("project_id", projectId)
            args.putString("owner_uid", ownerUid)
            fragment.arguments = args
            return fragment
        }
    }
}