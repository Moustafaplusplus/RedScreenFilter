//
//  ContentView.swift
//  RedScreenFilter
//
//  Created on March 5, 2026.
//

import SwiftUI

struct ContentView: View {
    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "circle.hexagongrid.fill")
                .font(.system(size: 80))
                .foregroundColor(.red)
            
            Text("Red Screen Filter")
                .font(.largeTitle)
                .fontWeight(.bold)
            
            Text("Eye Health Companion")
                .font(.subheadline)
                .foregroundColor(.secondary)
            
            Spacer()
                .frame(height: 40)
            
            Text("Ready to build!")
                .font(.body)
                .foregroundColor(.green)
        }
        .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
