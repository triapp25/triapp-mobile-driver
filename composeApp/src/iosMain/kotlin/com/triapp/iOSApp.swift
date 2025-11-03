import SwiftUI
import shared
import Koin

@main
struct iOSApp: App {

    init() {
        KoinInit.initKoin()
    }

	var body: some Scene {
		WindowGroup {
            MainViewController()
		}
	}
}