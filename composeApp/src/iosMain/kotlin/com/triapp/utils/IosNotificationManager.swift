import Foundation
import UserNotifications
import UIKit

@objc class IosNotificationManager: NSObject {
    private let notificationCenter = UNUserNotificationCenter.current()

    @objc func requestPermission(completion: @escaping (Bool) -> Void) {
        notificationCenter.requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            DispatchQueue.main.async {
                completion(granted)
            }
        }
    }

    @objc func showNotificationWithTitle(_ title: String, message: String) {
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = message
        content.sound = .default

        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false)
        let request = UNNotificationRequest(identifier: UUID().uuidString, content: content, trigger: trigger)

        notificationCenter.add(request) { error in
            if let error = error {
                print("Erro ao mostrar notificação: \(error)")
            }
        }
    }
}
