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
    
    var body: some Scene {
        WindowGroup {
            MainView()
        }
    }
}
