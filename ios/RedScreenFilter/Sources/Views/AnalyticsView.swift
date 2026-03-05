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
        NavigationView {
            VStack(spacing: 20) {
                // Period Picker
                Picker("Period", selection: $selectedPeriod) {
                    Text("Today").tag(0)
                    Text("Week").tag(1)
                    Text("Month").tag(2)
                }
                .pickerStyle(.segmented)
                .padding()
                
                // Stats Cards
                VStack(spacing: 15) {
                    StatCard(title: "Usage Time", value: "0h 0m")
                    StatCard(title: "Average Opacity", value: "50%")
                    StatCard(title: "Most Used Preset", value: "Standard")
                    StatCard(title: "Current Streak", value: "0 days")
                }
                .padding()
                
                Spacer()
                
                Text("Analytics dashboard coming in Phase 85-90%")
                    .foregroundColor(.secondary)
                    .padding()
            }
            .navigationTitle("Analytics")
        }
    }
}

struct StatCard: View {
    let title: String
    let value: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.caption)
                .foregroundColor(.secondary)
            Text(value)
                .font(.title2)
                .fontWeight(.bold)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(10)
    }
}

struct AnalyticsView_Previews: PreviewProvider {
    static var previews: some View {
        AnalyticsView()
    }
}
