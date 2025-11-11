import UIKit
import UserNotifications
import FirebaseCore
import FirebaseMessaging
import ComposeApp

class AppDelegate: UIResponder, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {

        FirebaseApp.configure()

        // Inicializa KMP
        //KMPInitializerKt.onDidFinishLaunchingWithOptions()

        // Permissão de notificações
        //notificationManager.requestPermission { granted in
        //    if granted {
        //                DispatchQueue.main.async {
        //                    UIApplication.shared.registerForRemoteNotifications()
        //                }
        //            }
        //}

        UNUserNotificationCenter.current().delegate = self
        Messaging.messaging().delegate = self

        return true
    }

    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        print("Firebase registration token: \(fcmToken ?? "")")
        // envie para o backend se precisar
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        let content = notification.request.content
        //notificationManager.showNotification(title: content.title, message: content.body)
        completionHandler([.banner, .sound])
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        completionHandler()
    }
}
