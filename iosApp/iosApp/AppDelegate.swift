import UIKit
import UserNotifications
import FirebaseCore
import FirebaseMessaging
import ComposeApp

class AppDelegate: UIResponder, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {

        FirebaseApp.configure()

        // Inicializa o Koin usando a função definida em KoinIOS.kt
        KoinIOSKt.initKoin()

        UNUserNotificationCenter.current().delegate = self
        Messaging.messaging().delegate = self

        // Registro para notificações remotas
        application.registerForRemoteNotifications()

        return true
    }

    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        print("Firebase registration token: \(fcmToken ?? "")")
        // Aqui você enviaria o token para seu backend para atualizar o usuário
    }

    // Exibe notificação mesmo com app aberto
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([.banner, .sound, .badge])
    }

    // Ação ao clicar na notificação
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        completionHandler()
    }
}