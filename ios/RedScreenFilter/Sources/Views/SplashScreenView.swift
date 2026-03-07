//
//  SplashScreenView.swift
//  RedScreenFilter
//
//  Created on March 6, 2026.
//  Animated splash screen using Lottie animation from Android
//

import SwiftUI
import Lottie

struct SplashScreenView: View {
    @State private var isActive = false
    @State private var animationProgress: CGFloat = 0
    
    var body: some View {
        if isActive {
            MainView()
        } else {
            ZStack {
                // Background gradient matching app theme
                LinearGradient(
                    colors: [
                        Color(red: 0.93, green: 0.43, blue: 0.43),
                        Color(red: 1.0, green: 0.53, blue: 0.53),
                        Color(red: 1.0, green: 0.68, blue: 0.68)
                    ],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
                .ignoresSafeArea()
                
                VStack(spacing: 30) {
                    // Lottie Animation
                    LottieView(animation: .named("splash_wave"))
                        .playing(loopMode: .loop)
                        .animationSpeed(1.0)
                        .frame(width: 250, height: 250)
                    
                    // App name
                    VStack(spacing: 8) {
                        Text("Red Screen Filter")
                            .font(.system(size: 32, weight: .bold, design: .rounded))
                            .foregroundColor(.white)
                        
                        Text("Eye Health Companion")
                            .font(.system(size: 16, weight: .medium, design: .rounded))
                            .foregroundColor(.white.opacity(0.9))
                    }
                    .padding(.top, 20)
                }
            }
            .onAppear {
                // Transition to main view after animation
                DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                    withAnimation(.easeInOut(duration: 0.5)) {
                        isActive = true
                    }
                }
            }
        }
    }
}

struct SplashScreenView_Previews: PreviewProvider {
    static var previews: some View {
        SplashScreenView()
    }
}
