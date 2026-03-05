//
//  AnalyticsView.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import SwiftUI

struct AnalyticsView: View {
    @State private var selectedPeriod: Int = 0
    
    var body: some View {
        ZStack {
            RsfTheme.colors.background
                .ignoresSafeArea()
            
            NavigationView {
                VStack(spacing: RsfTheme.spacing.lg) {
                    // Title
                    RsfSectionHeader("Analytics", subtitle: "Usage insights")
                        .padding(RsfTheme.spacing.md)
                    
                    // Period Picker
                    Picker("Period", selection: $selectedPeriod) {
                        Text("Today").tag(0)
                        Text("Week").tag(1)
                        Text("Month").tag(2)
                    }
                    .pickerStyle(.segmented)
                    .accentColor(RsfTheme.colors.primary)
                    .padding(RsfTheme.spacing.md)
                    
                    // Stats Cards
                    ScrollView {
                        VStack(spacing: RsfTheme.spacing.md) {
                            RsfStatCard(label: "Usage Time", value: "0h 0m", icon: "clock.fill")
                            RsfStatCard(label: "Average Opacity", value: "50%", icon: "slider.horizontal.3")
                            RsfStatCard(label: "Most Used Preset", value: "Standard", icon: "star.fill")
                            RsfStatCard(label: "Current Streak", value: "0 days", icon: "flame.fill")
                        }
                        .padding(RsfTheme.spacing.md)
                    }
                    
                    // Coming Soon Message
                    VStack(spacing: RsfTheme.spacing.md) {
                        Image(systemName: "chart.line.uptrend.xyaxis")
                            .font(.system(size: 32))
                            .foregroundColor(RsfTheme.colors.primary.opacity(0.5))
                        
                        Text("Analytics dashboard coming in Phase 85-90%")
                            .font(.body)
                            .foregroundColor(RsfTheme.colors.onSurfaceVariant)
                            .multilineTextAlignment(.center)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(RsfTheme.spacing.lg)
                    
                    Spacer()
                }
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .principal) {
                        Text("Analytics")
                            .font(.headline)
                            .fontWeight(.semibold)
                            .foregroundColor(RsfTheme.colors.onBackground)
                    }
                }
            }
        }
    }
}

struct AnalyticsView_Previews: PreviewProvider {
    static var previews: some View {
        AnalyticsView()
    }
}
