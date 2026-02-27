package com.bangkit.anemai.ui.welcome

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bangkit.anemai.R
import com.bangkit.anemai.databinding.ActivityWelcomeBinding
import com.bangkit.anemai.data.sevice.ApiConfig
import com.bangkit.anemai.utils.ProgressBarHandler
import kotlinx.coroutines.launch

class WelcomeActivity : AppCompatActivity(), ProgressBarHandler {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.welcome_activity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.hide()

        // Wake up the server as soon as the welcome screen loads
        lifecycleScope.launch {
            try {
                ApiConfig.getApiService().getArticles()
            } catch (e: Exception) {
                // Ignore — just waking the server up
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