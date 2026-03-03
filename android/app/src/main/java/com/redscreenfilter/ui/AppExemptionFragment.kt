package com.redscreenfilter.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.redscreenfilter.R
import com.redscreenfilter.data.ExemptedAppsManager
import com.redscreenfilter.databinding.FragmentAppExemptionBinding
import com.redscreenfilter.ui.adapter.InstalledAppsAdapter
import kotlinx.coroutines.launch

/**
 * App Exemption Fragment
 * 
 * Displays list of installed apps with checkboxes to toggle exemption from overlay.
 * Includes search functionality to filter apps.
 */
class AppExemptionFragment : Fragment() {
    
    private var _binding: FragmentAppExemptionBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var exemptedAppsManager: ExemptedAppsManager
    private lateinit var appsAdapter: InstalledAppsAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppExemptionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        exemptedAppsManager = ExemptedAppsManager.getInstance(requireContext())
        
        setupRecyclerView()
        setupSearch()
        loadApps()
    }
    
    /**
     * Setup RecyclerView for app list
     */
    private fun setupRecyclerView() {
        appsAdapter = InstalledAppsAdapter { packageName, isExempt ->
            exemptedAppsManager.toggleAppExemption(packageName, isExempt)
        }
        
        binding.recyclerViewApps.apply {
            adapter = appsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    
    /**
     * Setup search functionality
     */
    private fun setupSearch() {
        binding.searchViewApps.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    loadApps()
                } else {
                    searchApps(newText)
                }
                return true
            }
        })
    }
    
    /**
     * Load all installed apps
     */
    private fun loadApps() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            val apps = exemptedAppsManager.getInstalledApps()
            appsAdapter.submitList(apps)
            binding.progressBar.visibility = View.GONE
        }
    }
    
    /**
     * Search apps by name or package
     */
    private fun searchApps(query: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val results = exemptedAppsManager.searchApps(query)
            appsAdapter.submitList(results)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
