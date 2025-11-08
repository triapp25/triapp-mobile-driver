import UIKit
import Firebase
import UserNotifications

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        FirebaseApp.configure()

       notificationManager.requestPermission { granted in
                  if granted {
                      DispatchQueue.main.async {
                          UIApplication.shared.registerForRemoteNotifications()
                      }
                  }
              }

              UNUserNotificationCenter.current().delegate = self

        return true
    }

    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        print("Firebase registration token: \(fcmToken ?? "")")
        // envie para o backend se precisar
    }

    // Recebendo notificação enquanto o app está em primeiro plano
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                   willPresent notification: UNNotification,
                                   withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {

            let content = notification.request.content
            let title = content.title
            let body = content.body

            // Aqui você pode salvar no banco local, se quiser (Room não existe no iOS, use CoreData ou Realm)
            // Chame o NotificationManager para exibir a notificação localmente, se desejar

            notificationManager.showNotification(title: title, message: body)

            completionHandler([.banner, .sound])
        }

        // Recebe notificações quando o usuário interage com elas
        func userNotificationCenter(_ center: UNUserNotificationCenter,
                                   didReceive response: UNNotificationResponse,
                                   withCompletionHandler completionHandler: @escaping () -> Void) {
            // Trate a ação do usuário aqui, se necessário
            completionHandler()
        }
}
