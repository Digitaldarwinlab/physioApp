package com.darwin.physioai.coreapp.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.darwin.physioai.R
import com.darwin.physioai.coreapp.ui.fragments.*
import com.darwin.physioai.coreapp.utils.NetworkUtil
import com.darwin.physioai.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var bottomNavigationView: BottomNavigationView
    private var binding : ActivityMainBinding? = null
    private lateinit var navController : NavController
    private lateinit var networkUtil: NetworkUtil


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        networkUtil = NetworkUtil(this)

        if(NetworkUtil.Variables.checkvalue.toString() == "true"){
            binding?.apply {
                internetError.root.visibility = View.GONE
                fragmentContainer.visibility = View.VISIBLE
            }
        }else{
            binding?.apply {
                internetError.root.visibility = View.VISIBLE
                fragmentContainer.visibility = View.GONE
            }
        }

        networkUtil.observe(this, {
            if(it == true){
                binding?.apply {
                    internetError.root.visibility = View.GONE
                    fragmentContainer.visibility = View.VISIBLE
                }
            }else{
                binding?.apply {
                    internetError.root.visibility = View.VISIBLE
                    fragmentContainer.visibility = View.GONE
                }
            }
        })
        setupViews()
    }

    fun setupViews() {
        bottomNavigationView = binding?.bottomNavView!!
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment?
        navController = navHostFragment?.navController!!
        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.login -> hideBottomNavigation()
                R.id.forget -> hideBottomNavigation()
                R.id.instructionsFragment -> hideBottomNavigation()
                else -> showBottomNavigation()
            }
        }

        bottomNavigationView.setOnItemReselectedListener {
            when(it.itemId) {
                R.id.home -> Home()
                R.id.schedule -> Schedule()
                R.id.profile -> Profile()
                R.id.achievement -> Acheivement()
                R.id.tutorials -> Tutorials()
            }
        }
    }


    fun showBottomNavigation() {
        binding?.apply {
            bottomNavView.visibility = View.VISIBLE
        }
    }

    fun hideBottomNavigation() {
        binding?.apply {
            bottomNavView.visibility = View.GONE
        }
    }

    private var backPressedOnce = false

    override fun onBackPressed() {
        if (navController.graph.startDestination == navController.currentDestination?.id)
        {
            if (backPressedOnce)
            {
                super.onBackPressed()
                return
            }

            backPressedOnce = true
            Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show()

            lifecycleScope.launch{
                delay(2000)
                backPressedOnce = false
            }
        }
        else {
            super.onBackPressed()
        }
    }
}