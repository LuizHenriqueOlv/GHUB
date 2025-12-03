package com.luiz.ghub

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.luiz.ghub.databinding.ActivityMainBinding
import com.luiz.ghub.models.User

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment),
            binding.drawerLayout
        )

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val screensWithoutBars = setOf(
                R.id.splashFragment,
                R.id.loginFragment,
                R.id.register2Fragment,
                R.id.authentication
            )

            if (destination.id in screensWithoutBars) {
                binding.toolbar.visibility = View.GONE
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            } else {
                binding.toolbar.visibility = View.VISIBLE
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

                if (destination.id == R.id.homeFragment) {
                    updateDrawerHeader()
                }
            }
        }
    }

    private fun updateDrawerHeader() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val headerView = binding.navView.getHeaderView(0)

        val txtName = headerView.findViewById<TextView>(R.id.txtHeaderName)
        val txtEmail = headerView.findViewById<TextView>(R.id.txtHeaderEmail)
        val imgProfile = headerView.findViewById<ImageView>(R.id.imgHeaderProfile)

        val btnLogout = headerView.findViewById<View>(R.id.btnLogoutHeader)

        btnLogout?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            try {
                navController.navigate(R.id.action_global_to_login)
            } catch (e: Exception) {
                navController.navigate(R.id.authentication)
            }

            binding.drawerLayout.close()
        }

        if (currentUser != null) {
            txtEmail.text = currentUser.email

            FirebaseFirestore.getInstance().collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val user = document.toObject(User::class.java)

                        if (user != null) {
                            txtName.text = user.name ?: "Usuário sem nome"

                            if (!user.photoURL.isNullOrEmpty()) {
                                val secureUrl = user.photoURL?.replace("http:", "https:")

                                if (secureUrl != null) {
                                    Glide.with(this)
                                        .load(secureUrl)
                                        .circleCrop()
                                        .placeholder(R.drawable.account)
                                        .into(imgProfile)
                                }
                            }
                        }
                    }
                }
        } else {
            txtName.text = "G-Hub"
            txtEmail.text = ""
            imgProfile.setImageResource(R.drawable.account)
        }
    }
}