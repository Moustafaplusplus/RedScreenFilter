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
        
        let color = getColorForVariant(prefsManager.getColorVariant())
        overlayView.backgroundColor = color.withAlphaComponent(CGFloat(opacity))
        prefsManager.setOpacity(opacity)
    }
    
    // MARK: - Private Methods
    
    private func createOverlayIfNeeded() {
        guard overlayWindow == nil else { return }
        
        let window = UIWindow(frame: UIScreen.main.bounds)
        window.windowLevel = .alert - 1  // Below alerts but above content
        window.backgroundColor = .clear
        window.isUserInteractionEnabled = false
        
        let overlayView = UIView(frame: UIScreen.main.bounds)
        let color = getColorForVariant(prefsManager.getColorVariant())
        overlayView.backgroundColor = color.withAlphaComponent(CGFloat(prefsManager.getOpacity()))
        
        window.addSubview(overlayView)
        window.makeKeyAndVisible()
        
        self.overlayWindow = window
        self.overlayView = overlayView
    }
    
    private func getColorForVariant(_ variant: String) -> UIColor {
        switch variant {
        case "red_orange":
            return UIColor(red: 1.0, green: 0.39, blue: 0, alpha: 1.0)
        case "red_pink":
            return UIColor(red: 1.0, green: 0, blue: 0.39, alpha: 1.0)
        case "high_contrast":
            return UIColor(red: 1.0, green: 0, blue: 0, alpha: 1.0)
        default:  // red_standard
            return UIColor(red: 1.0, green: 0, blue: 0, alpha: 1.0)
        }
    }
}
