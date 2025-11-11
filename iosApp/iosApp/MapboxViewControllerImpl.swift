import UIKit
import SwiftUI
import MapboxMaps
import ComposeApp

class MapboxViewControllerImpl: UIViewController, MapboxViewController {
    private var mapView: MapView!
    private var pointAnnotationManager: PointAnnotationManager?
    private var userAnnotation: PointAnnotation?

    override func viewDidLoad() {
        super.viewDidLoad()

        let initOptions = MapInitOptions(styleURI: .streets)
        mapView = MapView(frame: view.bounds, mapInitOptions: initOptions)
        mapView.autoresizingMask = [.flexibleWidth, .flexibleHeight]

        view.addSubview(mapView)

        mapView.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            mapView.topAnchor.constraint(equalTo: view.topAnchor),
            mapView.bottomAnchor.constraint(equalTo: view.bottomAnchor),
            mapView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            mapView.trailingAnchor.constraint(equalTo: view.trailingAnchor)
        ])

        pointAnnotationManager = mapView.annotations.makePointAnnotationManager()
    }

    private func addOrUpdateUserMarker(at coordinate: CLLocationCoordinate2D) {
        guard let manager = pointAnnotationManager else { return }

        if var annotation = userAnnotation {
            annotation.point = Point(coordinate)
            manager.annotations = [annotation]
            userAnnotation = annotation
            return
        }

        var annotation = PointAnnotation(coordinate: coordinate)

        if let image = UIImage(systemName: "mappin.circle.fill") {
            let imageName = "user_marker"

            let size = CGSize(width: 40, height: 40)
            UIGraphicsBeginImageContextWithOptions(size, false, 0.0)
            image.draw(in: CGRect(origin: .zero, size: size))
            let resizedImage = UIGraphicsGetImageFromCurrentImageContext()
            UIGraphicsEndImageContext()

            if let finalImage = resizedImage {
                try? mapView.mapboxMap.addImage(finalImage, id: imageName)
                annotation.image = .init(image: finalImage, name: imageName)
                annotation.iconSize = 1.0
            }
        }

        manager.annotations = [annotation]
        userAnnotation = annotation
    }

    func onUpdateLocation(coordinate: Coordinate) {
        let newCoordinate = CLLocationCoordinate2D(latitude: coordinate.latitude, longitude: coordinate.longitude)

        mapView.camera.ease(
            to: CameraOptions(center: newCoordinate, zoom: 14.0),
            duration: 1.0
        )

        addOrUpdateUserMarker(at: newCoordinate)
    }
}
