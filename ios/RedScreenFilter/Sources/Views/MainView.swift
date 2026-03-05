//
//  MainView.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import SwiftUI

struct MainView: View {
    @StateObject private var viewModel = OverlayViewModel()
    @State private var selectedTab: Int = 0
    
    var body: some View {
        TabView(selection: $selectedTab) {
            // Tab 1: Main Control
            MainControlView(viewModel: viewModel)
                .tabItem {
                    Label("Control", systemImage: "circle.fill")
                }
                .tag(0)
            
            // Tab 2: Presets
            PresetsView(viewModel: viewModel)
                .tabItem {
                    Label("Presets", systemImage: "square.grid.2x2")
                }
                .tag(1)
            
            // Tab 3: Settings
            SettingsView(viewModel: viewModel)
                .tabItem {
                    Label("Settings", systemImage: "gear")
                }
                .tag(2)
            
            // Tab 4: Analytics
            AnalyticsView()
                .tabItem {
                    Label("Analytics", systemImage: "chart.bar")
                }
                .tag(3)
        }
        .preferredColorScheme(nil)
    }
}

struct MainControlView: View {
    @ObservedObject var viewModel: OverlayViewModel
    
    var body: some View {
        VStack(spacing: 30) {
            Text("Red Screen Filter")
                .font(.title)
                .fontWeight(.bold)
            
            Spacer()
            
            // Toggle Button
            Button(action: viewModel.toggleOverlay) {
                ZStack {
                    Circle()
                        .fill(viewModel.isEnabled ? Color.red : Color.gray)
                        .frame(width: 120, height: 120)
                    
                    Text(viewModel.isEnabled ? "ON" : "OFF")
                        .font(.title)
                        .fontWeight(.bold)
                        .foregroundColor(.white)
                }
            }
            
            // Opacity Slider
            VStack(alignment: .leading, spacing: 10) {
                HStack {
                    Text("Opacity")
                    Spacer()
                    Text(viewModel.opacity.percentageString)
                        .fontWeight(.bold)
                }
                
                Slider(value: $viewModel.opacity, in: 0...1)
                    .onChange(of: viewModel.opacity) { newValue in
                        viewModel.updateOpacity(newValue)
                    }
            }
            .padding()
            .background(Color(.systemGray6))
            .cornerRadius(10)
            
            Spacer()
        }
        .padding()
    }
}

struct PresetsView: View {
    @ObservedObject var viewModel: OverlayViewModel
    
    var body: some View {
        VStack {
            Text("Presets")
                .font(.title2)
                .fontWeight(.bold)
            
            Text("More presets coming in Phase 35-40%")
                .foregroundColor(.secondary)
            
            Spacer()
        }
        .padding()
    }
}

struct MainView_Previews: PreviewProvider {
    static var previews: some View {
        MainView()
    }
}
