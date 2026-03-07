//
//  AppLogger.swift
//  RedScreenFilter
//
//  Created on March 6, 2026.
//

import Foundation
import OSLog

enum AppLogger {
    static let lifecycle = LoggerChannel(category: "lifecycle")
    static let notifications = LoggerChannel(category: "notifications")
    static let location = LoggerChannel(category: "location")
}

struct LoggerChannel {
    private let logger: Logger

    init(category: String) {
        logger = Logger(subsystem: Bundle.main.bundleIdentifier ?? "com.redscreenfilter", category: category)
    }

    func debug(_ message: String) {
        logger.debug("\(message, privacy: .public)")
    }

    func info(_ message: String) {
        logger.info("\(message, privacy: .public)")
    }

    func success(_ message: String) {
        logger.notice("\(message, privacy: .public)")
    }

    func warning(_ message: String) {
        logger.warning("\(message, privacy: .public)")
    }

    func error(_ message: String, error: Error? = nil) {
        if let error {
            logger.error("\(message, privacy: .public): \(String(describing: error), privacy: .public)")
        } else {
            logger.error("\(message, privacy: .public)")
        }
    }
}
