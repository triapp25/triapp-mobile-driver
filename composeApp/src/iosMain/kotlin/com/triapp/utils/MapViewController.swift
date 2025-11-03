package com.triapp.utils

import MapboxMaps
import shared

struct MapViewController: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let resourceOptions = ResourceOptions(accessToken: "SUA_CHAVE_MAPBOX")
        let mapInitOptions = MapInitOptions(resourceOptions: resourceOptions)
        let mapView = MapView(frame: .zero, mapInitOptions: mapInitOptions)
        return UIViewController().apply { $0.view = mapView }
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
