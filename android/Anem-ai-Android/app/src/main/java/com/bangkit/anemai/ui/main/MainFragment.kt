package com.bangkit.anemai.ui.main

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.LinearLayoutManager
import com.bangkit.anemai.R
import com.bangkit.anemai.data.adapter.ArticleAdapter
import com.bangkit.anemai.data.model.ArticlesResponseItem
import com.bangkit.anemai.databinding.FragmentMainBinding
import com.bangkit.anemai.ui.ViewModelFactory
import com.bangkit.anemai.ui.welcome.WelcomeActivity
import com.bangkit.anemai.utils.Result

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var menuProvider: MenuProvider

    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(requireContext(), requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvArticle.layoutManager = LinearLayoutManager(requireContext())

        viewModel.getArticles().observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> showLoading(true)
                is Result.Success -> {
                    showLoading(false)
                    setupArticle(result.data, view)
                }
                is Result.Error -> {
                    showLoading(false)
                    AlertDialog.Builder(requireContext())
                        .setMessage(getString(R.string.error_try_again))
                        .setPositiveButton(getString(R.string.ok)) { _, _ -> }
                        .show()
                }
                else -> {}
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show() }
        }

        viewModel.userDetail.observe(viewLifecycleOwner) { userDetail ->
            val name = userDetail?.userResult?.name ?: "User"
            binding.tvGreeetingName.text = getString(R.string.greeting_name, name)
        }

        viewModel.getSession().observe(viewLifecycleOwner) { session ->
            session?.id?.takeIf { it.isNotEmpty() }?.let {
                viewModel.getDetailUser(it)
            }
        }

        setupAction(view)
        setupActionBar()
    }

    override fun onStop() {
        super.onStop()
        requireActivity().removeMenuProvider(menuProvider)
    }

    override fun onResume() {
        super.onResume()
        setupActionBar()
    }

    private fun setupAction(view: View) {
        val extras = FragmentNavigatorExtras(binding.bgLayout to "bg_layout")

        binding.btnHistory.setOnClickListener {
            view.findNavController().navigate(
                R.id.action_mainFragment_to_historyFragment, null, null, extras
            )
        }

        binding.btnCheckup.setOnClickListener {
            if (checkPermission(PERMISSION_CAMERA)) {
                view.findNavController().navigate(
                    R.id.action_mainFragment_to_detectionFragment, null, null, extras
                )
            } else {
                requestCameraPermissionLauncher.launch(PERMISSION_CAMERA)
            }
        }

        binding.btnMore.setOnClickListener {
            val toArticleList = MainFragmentDirections.actionMainFragmentToArticleFragment()
            view.findNavController().navigate(toArticleList, extras)
        }
    }

    private fun setupActionBar() {
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(false)
        }

        menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_exit, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_exit -> {
                        viewModel.logout()
                        startActivity(Intent(requireActivity(), WelcomeActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
        }

        requireActivity().addMenuProvider(menuProvider)
    }

    private fun setupArticle(articleList: List<ArticlesResponseItem>, view: View) {
        val extras = FragmentNavigatorExtras(binding.bgLayout to "bg_layout")
        val adapter = ArticleAdapter { article ->
            val toArticleDetail = MainFragmentDirections.actionMainFragmentToArticleDetailFragment()
            toArticleDetail.articleId = article.id.toString()
            view.findNavController().navigate(toArticleDetail, extras)
        }

        binding.rvArticle.adapter = adapter
        val topArticles = articleList.sortedByDescending { it.createdAt }.take(3).toMutableList()
        adapter.submitList(topArticles)
    }

    private fun checkPermission(permission: String) =
        ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                view?.findNavController()?.navigate(R.id.action_mainFragment_to_detectionFragment)
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun showLoading(state: Boolean) {
        binding.shimmerArticle.apply {
            if (state) {
                startShimmer()
                visibility = View.VISIBLE
            } else {
                stopShimmer()
                visibility = View.GONE
            }
        }
    }

    companion object {
        private const val PERMISSION_CAMERA = Manifest.permission.CAMERA
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
