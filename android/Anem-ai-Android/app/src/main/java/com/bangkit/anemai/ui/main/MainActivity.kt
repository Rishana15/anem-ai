package com.bangkit.anemai.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bangkit.anemai.databinding.ActivityMainBinding
import com.bangkit.anemai.ui.ViewModelFactory
import com.bangkit.anemai.ui.welcome.WelcomeActivity
import com.bangkit.anemai.utils.ProgressBarHandler

class MainActivity : AppCompatActivity(), ProgressBarHandler {

    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this, application)
    }
    private var hasNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        navigateStartingScreen()
    }

    private fun navigateStartingScreen() {
        viewModel.getSession().observe(this) { user ->
            if (hasNavigated) return@observe

            if (!user.isLogin) {
                hasNavigated = true
                val intent = Intent(this, WelcomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    override fun showLoading(state: Boolean) {
        if (state) {
            binding.loadingScreen.visibility = View.VISIBLE
        } else {
            binding.loadingScreen.visibility = View.GONE
        }
    }
}