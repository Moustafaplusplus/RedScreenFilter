import SwiftUI
import UIKit

struct LaunchSplashView: View {
    let onFinished: () -> Void

    @State private var didFinish = false
    @State private var isAnimating = false

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [
                    Color(red: 0.05, green: 0.0, blue: 0.0),
                    Color(red: 0.18, green: 0.0, blue: 0.0),
                    Color.black
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()

            Circle()
                .stroke(Color.red.opacity(0.22), lineWidth: 2)
                .frame(width: isAnimating ? 300 : 210, height: isAnimating ? 300 : 210)
                .blur(radius: 0.5)
                .animation(.easeInOut(duration: 1.2).repeatForever(autoreverses: true), value: isAnimating)

            Circle()
                .trim(from: 0.08, to: 0.78)
                .stroke(Color.white.opacity(0.35), style: StrokeStyle(lineWidth: 3, lineCap: .round))
                .frame(width: 220, height: 220)
                .rotationEffect(.degrees(isAnimating ? 360 : 0))
                .animation(.linear(duration: 2.6).repeatForever(autoreverses: false), value: isAnimating)

            VStack(spacing: 18) {
                logoView
                    .frame(width: 124, height: 124)
                    .scaleEffect(isAnimating ? 1.0 : 0.88)
                    .shadow(color: Color.red.opacity(0.45), radius: 16)
                    .animation(.spring(response: 0.65, dampingFraction: 0.72), value: isAnimating)

                Text("Red Screen Filter")
                    .font(.headline)
                    .foregroundColor(.white.opacity(isAnimating ? 0.95 : 0.65))
                    .animation(.easeInOut(duration: 0.9), value: isAnimating)
            }
        }
        .onAppear {
            isAnimating = true

            DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
                finishIfNeeded()
            }
        }
    }

    @ViewBuilder
    private var logoView: some View {
        if UIImage(named: "AppLogo") != nil {
            Image("AppLogo")
                .resizable()
                .scaledToFit()
                .clipShape(RoundedRectangle(cornerRadius: 28, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: 28, style: .continuous)
                        .stroke(Color.white.opacity(0.25), lineWidth: 1)
                )
        } else {
            ZStack {
                RoundedRectangle(cornerRadius: 28, style: .continuous)
                    .fill(Color.red.opacity(0.85))
                Image(systemName: "eye.fill")
                    .font(.system(size: 40, weight: .semibold))
                    .foregroundColor(.white)
            }
        }
    }

    private func finishIfNeeded() {
        guard !didFinish else { return }
        didFinish = true
        onFinished()
    }
}
