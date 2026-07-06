import SwiftUI
import Shared

struct ContentView: View {
    var body: some View {
        VStack(spacing: 8) {
            Text("Vedra")
                .font(.largeTitle)
            Text("SwiftUI app coming in phase 18 — shared Kotlin data layer is ready.")
                .font(.footnote)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
        }
        .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
