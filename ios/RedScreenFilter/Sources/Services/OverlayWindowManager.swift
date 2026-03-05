//
//  OverlayWindowManager.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import UIKit
import SwiftUI

class OverlayWindowManager {
    static let shared = OverlayWindowManager()
    
    private var overlayWindow: UIWindow?
    private var overlayView: UIView?
    private let prefsManager = PreferencesManager.shared
    
    // MARK: - Public Methods
    
    func showOverlay(opacity: Float) {
        createOverlayIfNeeded()
        updateOpacity(opacity)
        overlayWindow?.isHidden = false
    }
    
    func hideOverlay() {
        overlayWindow?.isHidden = true
    }
    
    func updateOpacity(_ opacity: Float) {
        guard let overlayView = overlayView else { return }
        
        let variant = prefsManager.getColorVariantEnum()
        let validatedOpacity = ColorUtility.validatedOpacity(opacity, for: variant)
        let color = ColorUtility.uiColor(for: variant, opacity: validatedOpacity)
        overlayView.backgroundColor = color
        prefsManager.setOpacity(validatedOpacity)
    }
    
    // MARK: - Private Methods
    
    private func createOverlayIfNeeded() {
        guard overlayWindow == nil else { return }
        
        let window = UIWindow(frame: UIScreen.main.bounds)
        window.windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene
        window.windowLevel = .alert - 1  // Below alerts but above content
        window.backgroundColor = .clear
        window.isUserInteractionEnabled = false
        
        let overlayView = UIView(frame: UIScreen.main.bounds)
        let variant = prefsManager.getColorVariantEnum()
        let color = ColorUtility.uiColor(for: variant, opacity: prefsManager.getOpacity())
        overlayView.backgroundColor = color
        
        window.addSubview(overlayView)
        window.isHidden = false
        
        self.overlayWindow = window
        self.overlayView = overlayView
    }
    
    // Color handling is now delegated to ColorUtility for consistency
}
