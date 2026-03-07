//
//  RedScreenFilterApp.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//  Enhanced in Phase 35-40% with AppDelegate integration
//

import SwiftUI

@main
struct RedScreenFilterApp: App {
    
    // Connect AppDelegate for background task registration
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    @Environment(\.scenePhase) private var scenePhase
    @State private var isShowingSplash = true
    
    var body: some Scene {
        WindowGroup {
            ZStack {
                MainView()
                    .opacity(isShowingSplash ? 0 : 1)

                if isShowingSplash {
                    LaunchSplashView {
                        withAnimation(.easeOut(duration: 0.2)) {
                            isShowingSplash = false
                        }
                    }
                    .transition(.opacity)
                }
            }
            .preferredColorScheme(.dark)
            .onOpenURL { url in
                WidgetActionCoordinator.shared.handleIncomingURL(url)
            }
            .onChange(of: scenePhase) { phase in
                if phase == .active {
                    WidgetActionCoordinator.shared.processPendingAction()
                }
            }
        }
    }
}
