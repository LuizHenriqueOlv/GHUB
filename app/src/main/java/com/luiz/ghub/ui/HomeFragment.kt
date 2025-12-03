package com.luiz.ghub.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.luiz.ghub.R
import com.luiz.ghub.databinding.FragmentHomeBinding
import com.luiz.ghub.models.Project
import com.luiz.ghub.ui.adapter.ProjectAdapter

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore

    private var highlightProject: Project? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        fetchProjects()
        initListeners()
    }

    private fun setupRecyclerView() {
        binding.rvProjects.layoutManager = GridLayoutManager(requireContext(), 2)
    }

    private fun fetchProjects() {
        db.collection("projects")
            .get()
            .addOnSuccessListener { documents ->
                val projectList = mutableListOf<Project>()

                for (document in documents) {
                    val project = document.toObject(Project::class.java)
                    project.id = document.id
                    projectList.add(project)
                }

                if (projectList.isNotEmpty()) {
                    highlightProject = projectList[0]
                    setupHighlight(highlightProject!!)

                    val listForRecycler = projectList.drop(1)

                    binding.rvProjects.adapter = ProjectAdapter(listForRecycler) { selectedProject ->
                        val bundle = Bundle()
                        bundle.putParcelable("project_data", selectedProject)
                        findNavController().navigate(R.id.action_homeFragment_to_projectDetailFragment, bundle)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("HomeFragment", "Erro ao buscar: ", exception)
            }
    }

    private fun setupHighlight(project: Project) {
        if (project.cover.isNotEmpty()) {
            val secureUrl = project.cover.replace("http:", "https:")
            Glide.with(requireContext())
                .load(secureUrl)
                .centerCrop()
                .placeholder(R.drawable.enterthegungeon)
                .into(binding.imageView2)
        }

        val buttons = listOf(binding.button2, binding.button3, binding.button4)
        buttons.forEachIndexed { index, button ->
            if (index < project.gens.size) {
                button.text = project.gens[index]
                button.isVisible = true
            } else {
                button.isVisible = false
            }
        }
    }

    private fun initListeners() {
        binding.imageView2.setOnClickListener {
            highlightProject?.let { project ->
                val bundle = Bundle()
                bundle.putParcelable("project_data", project)
                findNavController().navigate(R.id.action_homeFragment_to_projectDetailFragment, bundle)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}