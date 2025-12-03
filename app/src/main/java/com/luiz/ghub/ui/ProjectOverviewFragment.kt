package com.luiz.ghub.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.luiz.ghub.R
import com.luiz.ghub.databinding.FragmentProjectOverviewBinding
import com.luiz.ghub.models.Project
import com.luiz.ghub.models.ProjectMember
import com.luiz.ghub.models.User
import com.luiz.ghub.ui.adapter.MemberAdapter

class ProjectOverviewFragment : Fragment(R.layout.fragment_project_overview) {

    private lateinit var binding: FragmentProjectOverviewBinding
    private val db = FirebaseFirestore.getInstance()
    private val membersDisplayList = mutableListOf<User>()
    private lateinit var memberAdapter: MemberAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProjectOverviewBinding.bind(view)

        val project = arguments?.getParcelable<Project>("project_data")

        if (project != null) {
            setupUI(project)
        }
    }

    private fun setupUI(project: Project) {
        binding.txtDetailDesc.text = project.desc
        setupMembersList(project)
    }

    private fun setupMembersList(project: Project) {
        membersDisplayList.clear()

        memberAdapter = MemberAdapter(membersDisplayList) { selectedUser ->
            val bundle = Bundle()
            bundle.putParcelable("user_data", selectedUser)
            findNavController().navigate(R.id.action_projectDetailFragment_to_userProfileFragment, bundle)
        }

        binding.rvMembers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMembers.adapter = memberAdapter

        fetchProjectMembers(project)
    }

    private fun fetchProjectMembers(project: Project) {
        db.collection("projects").document(project.id).collection("members")
            .get()
            .addOnSuccessListener { result ->
                val uidsToFetch = mutableListOf<String>()
                if (project.ownerUid.isNotEmpty()) uidsToFetch.add(project.ownerUid)

                for (document in result) {
                    val member = document.toObject(ProjectMember::class.java)
                    if (member.memberUid.isNotEmpty() && member.memberUid != project.ownerUid) {
                        uidsToFetch.add(member.memberUid)
                    }
                }
                uidsToFetch.forEach { uid -> fetchUserData(uid) }
            }
    }

    private fun fetchUserData(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        val userWithId = user.copy(id = document.id)
                        membersDisplayList.add(userWithId)
                        memberAdapter.notifyDataSetChanged()
                    }
                }
            }
    }

    companion object {
        fun newInstance(project: Project): ProjectOverviewFragment {
            val fragment = ProjectOverviewFragment()
            val args = Bundle()
            args.putParcelable("project_data", project)
            fragment.arguments = args
            return fragment
        }
    }
}