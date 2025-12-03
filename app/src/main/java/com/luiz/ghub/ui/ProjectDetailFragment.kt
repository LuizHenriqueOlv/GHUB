package com.luiz.ghub.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayoutMediator
import com.luiz.ghub.R
import com.luiz.ghub.databinding.FragmentProjectDetailBinding
import com.luiz.ghub.models.Project
import java.text.NumberFormat
import java.util.Locale

class ProjectDetailFragment : Fragment() {

    private var _binding: FragmentProjectDetailBinding? = null
    private val binding get() = _binding!!

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

        // Formatação de Moeda
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
            Toast.makeText(context, "Função de doar...", Toast.LENGTH_SHORT).show()
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