package com.luiz.ghub.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.luiz.ghub.R
import com.luiz.ghub.databinding.FragmentProjectDetailBinding
import com.luiz.ghub.models.Project
import java.text.NumberFormat
import java.util.Locale

class ProjectDetailFragment : Fragment() {

    private var _binding: FragmentProjectDetailBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var currentProject: Project? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentProject = arguments?.getParcelable("project_data")

        if (currentProject != null) {
            setupHeader(currentProject!!)
            setupViewPager(currentProject!!)
            initListeners()
        }
    }

    private fun setupHeader(project: Project) {
        binding.txtDetailTitle.text = project.title

        val ptBr = Locale("pt", "BR")
        val foundsFormatted = NumberFormat.getCurrencyInstance(ptBr).format(project.founds)
        val goalFormatted = NumberFormat.getCurrencyInstance(ptBr).format(project.goal)

        binding.txtGoalInfo.text = "$foundsFormatted / $goalFormatted"

        val progress = if (project.goal > 0) (project.founds / project.goal * 100).toInt() else 0
        binding.progressGoal.progress = progress

        if (project.cover.isNotEmpty()) {
            val secureUrl = project.cover.replace("http:", "https:")
            Glide.with(this).load(secureUrl).centerCrop().into(binding.imgDetailCover)
        }

        binding.chipGroupGens.removeAllViews()
        project.gens.forEach { genre ->
            val chip = Chip(requireContext())
            chip.text = genre
            chip.setChipBackgroundColorResource(android.R.color.black)
            chip.setTextColor(resources.getColor(android.R.color.white, null))
            binding.chipGroupGens.addView(chip)
        }
    }

    private fun initListeners() {
        binding.btnDonate.setOnClickListener {
            showDonateDialog()
        }
    }

    private fun showDonateDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_donate, null)
        val edtDonateValue = dialogView.findViewById<TextInputEditText>(R.id.edtDonateValue)
        val btnConfirmDonate = dialogView.findViewById<MaterialButton>(R.id.btnConfirmDonate)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnConfirmDonate.setOnClickListener {
            val valueStr = edtDonateValue.text.toString()
            val value = valueStr.toDoubleOrNull()

            if (value != null && value > 0) {
                processDonation(value)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Por favor, insira um valor válido.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun processDonation(amount: Double) {
        val projectId = currentProject?.id ?: return
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(requireContext(), "Você precisa estar logado para doar.", Toast.LENGTH_SHORT).show()
            return
        }

        val projectRef = db.collection("projects").document(projectId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(projectRef)
            val currentFounds = snapshot.getDouble("founds") ?: 0.0
            val newFounds = currentFounds + amount

            transaction.update(projectRef, "founds", newFounds)
            newFounds
        }.addOnSuccessListener { newFounds ->
            Toast.makeText(requireContext(), "Doação realizada com sucesso!", Toast.LENGTH_LONG).show()

            currentProject?.let {
                val updatedProject = it.copy(founds = newFounds)
                currentProject = updatedProject
                setupHeader(updatedProject)
            }

        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Erro ao processar doação", Toast.LENGTH_SHORT).show()
            Log.e("ProjectDetail", "Erro na transação", e)
        }
    }

    private fun setupViewPager(project: Project) {
        val adapter = ViewPagerAdapter(this, project)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Sobre"
                1 -> tab.text = "Updates"
            }
        }.attach()
    }

    inner class ViewPagerAdapter(fragment: Fragment, private val project: Project) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ProjectOverviewFragment.newInstance(project)
                1 -> ProjectUpdatesFragment.newInstance(project.id, project.ownerUid)
                else -> ProjectOverviewFragment.newInstance(project)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}