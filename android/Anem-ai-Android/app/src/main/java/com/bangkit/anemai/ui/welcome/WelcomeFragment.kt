package com.bangkit.anemai.ui.welcome

import android.os.Bundle
import android.transition.ChangeBounds
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.bangkit.anemai.R
import com.bangkit.anemai.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {

    private lateinit var binding: FragmentWelcomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWelcomeBinding.inflate(layoutInflater)
        sharedElementEnterTransition = ChangeBounds().apply { duration = 750 }
        sharedElementReturnTransition = ChangeBounds().apply { duration = 750 }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
        setupAction(view)
    }

    private fun setupAction(view: View) {
        val extrasLogin = FragmentNavigatorExtras(
            binding.cardLogin to "card_login",
            binding.headlineWelcome to "headline_welcome",
            binding.bodycopyWelcome to "bodycopy_welcome",
            binding.ivWelcome to "iv_welcome"
        )
        val extrasRegister = FragmentNavigatorExtras(
            binding.cardRegister to "card_register",
            binding.headlineWelcome to "headline_welcome",
            binding.bodycopyWelcome to "bodycopy_welcome",
            binding.ivWelcome to "iv_welcome"
        )
        binding.btnLogin.setOnClickListener {
            view.findNavController().navigate(R.id.action_welcomeFragment_to_loginFragment, null, null, extrasLogin)
        }
        binding.btnRegister.setOnClickListener {
            view.findNavController().navigate(R.id.action_welcomeFragment_to_registerFragment, null, null, extrasRegister)
        }
    }
}