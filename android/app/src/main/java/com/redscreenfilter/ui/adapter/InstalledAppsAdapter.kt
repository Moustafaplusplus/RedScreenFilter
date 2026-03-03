package com.redscreenfilter.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.redscreenfilter.R
import com.redscreenfilter.data.InstalledApp
import com.redscreenfilter.databinding.ItemInstalledAppBinding

/**
 * RecyclerView Adapter for displaying installed apps with exemption checkboxes
 */
class InstalledAppsAdapter(
    private val onExemptionChanged: (packageName: String, isExempt: Boolean) -> Unit
) : RecyclerView.Adapter<InstalledAppsAdapter.AppViewHolder>() {
    
    private val apps = mutableListOf<InstalledApp>()
    
    /**
     * Update the list of apps to display
     */
    fun submitList(newApps: List<InstalledApp>) {
        apps.clear()
        apps.addAll(newApps)
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemInstalledAppBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AppViewHolder(binding, onExemptionChanged)
    }
    
    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(apps[position])
    }
    
    override fun getItemCount() = apps.size
    
    /**
     * ViewHolder for individual app items
     */
    class AppViewHolder(
        private val binding: ItemInstalledAppBinding,
        private val onExemptionChanged: (packageName: String, isExempt: Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(app: InstalledApp) {
            binding.apply {
                // Set app icon
                if (app.icon != null) {
                    imageViewAppIcon.setImageDrawable(app.icon)
                } else {
                    imageViewAppIcon.setImageResource(android.R.drawable.ic_menu_info_details)
                }
                
                // Set app name
                textViewAppName.text = app.appName
                
                // Set package name (smaller, secondary text)
                textViewPackageName.text = app.packageName
                
                // Set checkbox state
                checkboxExempt.isChecked = app.isExempted
                
                // Handle checkbox change
                checkboxExempt.setOnCheckedChangeListener { _, isChecked ->
                    onExemptionChanged(app.packageName, isChecked)
                }
                
                // Also allow clicking on the whole item to toggle
                root.setOnClickListener {
                    checkboxExempt.toggle()
                }
            }
        }
    }
}
