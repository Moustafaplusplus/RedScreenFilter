//
//  OverlayView.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import SwiftUI

/// OverlayView - A full-screen red rectangle with configurable opacity
/// Used as a preview within the app to show what the overlay will look like
/// The actual system overlay is managed by OverlayWindowManager
struct OverlayView: View {
    @Binding var isVisible: Bool
    @Binding var currentOpacity: Float
    @Binding var colorVariant: String
    
    var body: some View {
        if isVisible {
            ZStack {
                // Semi-transparent red overlay
                Color.fromVariant(colorVariant)
                    .ignoresSafeArea()
                    .opacity(Double(currentOpacity))
                    .animation(.easeInOut(duration: 0.3), value: currentOpacity)
                    .animation(.easeInOut(duration: 0.3), value: colorVariant)
                    .animation(.easeInOut(duration: 0.2), value: isVisible)
            }
            .transition(.opacity)
        }
    }
    
    // MARK: - Methods
    
    /// Show the overlay with specified opacity
    func showOverlay(opacity: Float) {
        withAnimation {
            isVisible = true
            currentOpacity = max(0, min(1, opacity))
        }
    }
    
    /// Hide the overlay
    func hideOverlay() {
        withAnimation {
            isVisible = false
        }
    }
    
    /// Update the overlay opacity
    func updateOpacity(_ newOpacity: Float) {
        withAnimation {
            currentOpacity = max(0, min(1, newOpacity))
        }
    }
}

/// PreviewOverlayView - A smaller preview component to show the overlay effect
/// Displays in a constrained frame for UI preview purposes
struct PreviewOverlayView: View {
    let opacity: Float
    let colorVariant: String
    let height: CGFloat = 120
    
    var body: some View {
        ZStack {
            // White background (simulates content beneath overlay)
            Color.white
            
            // Overlay preview
            Color.fromVariant(colorVariant)
                .opacity(Double(opacity))
            
            // Label
            VStack(spacing: 8) {
                Text("Preview")
                    .font(.caption)
                    .fontWeight(.semibold)
                
                HStack(spacing: 4) {
                    Text("Opacity:")
                        .font(.caption2)
                    Text(opacity.percentageString)
                        .font(.caption2)
                        .fontWeight(.bold)
                }
            }
            .foregroundColor(shouldUseDarkText ? .black : .white)
        }
        .frame(height: height)
        .cornerRadius(12)
        .shadow(radius: 4)
    }
    
    // Determine if we should use dark text based on overlay opacity
    private var shouldUseDarkText: Bool {
        return opacity < 0.4
    }
}

// MARK: - Color Variant Helper View

/// OverlayColorPicker - A component to select color variants
struct OverlayColorPicker: View {
    @Binding var selectedVariant: String
    let onVariantChanged: (String) -> Void
    
    private let colorOptions: [(name: String, key: String, description: String)] = [
        ("Red Standard", Constants.Colors.redStandard, "Pure red"),
        ("Red-Orange", Constants.Colors.redOrange, "Warmer tone"),
        ("Red-Pink", Constants.Colors.redPink, "Pink tone"),
        ("High Contrast", Constants.Colors.highContrast, "Maximum red")
    ]
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Color Variant")
                .font(.headline)
                .fontWeight(.semibold)
            
            VStack(spacing: 8) {
                ForEach(colorOptions, id: \.key) { option in
                    HStack(spacing: 12) {
                        // Color preview circle
                        Circle()
                            .fill(Color.fromVariant(option.key))
                            .frame(width: 24, height: 24)
                        
                        VStack(alignment: .leading, spacing: 2) {
                            Text(option.name)
                                .font(.body)
                                .fontWeight(.semibold)
                            Text(option.description)
                                .font(.caption)
                                .foregroundColor(.gray)
                        }
                        
                        Spacer()
                        
                        // Selection indicator
                        if selectedVariant == option.key {
                            Image(systemName: "checkmark.circle.fill")
                                .foregroundColor(.green)
                                .font(.system(size: 20))
                        } else {
                            Circle()
                                .stroke(Color.gray.opacity(0.3), lineWidth: 2)
                                .frame(width: 20, height: 20)
                        }
                    }
                    .contentShape(Rectangle())
                    .onTapGesture {
                        withAnimation {
                            selectedVariant = option.key
                            onVariantChanged(option.key)
                        }
                    }
                }
            }
            .padding()
            .background(Color(.systemGray6))
            .cornerRadius(12)
        }
    }
}

// MARK: - Preview

struct OverlayView_Previews: PreviewProvider {
    static var previews: some View {
        ZStack {
            VStack(spacing: 20) {
                VStack {
                    Text("Overlay Preview")
                        .font(.headline)
                    
                    PreviewOverlayView(
                        opacity: 0.5,
                        colorVariant: Constants.Colors.redStandard
                    )
                }
                
                Spacer()
            }
            .padding()
        }
    }
}
