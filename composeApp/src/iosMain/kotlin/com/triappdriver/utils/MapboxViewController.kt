package com.triappdriver.utils

import platform.UIKit.UIViewController
import com.triappdriver.domain.model.Coordinate

interface MapboxViewController {
    fun onUpdateLocation(coordinate: Coordinate)
}

var mapboxViewController: (() -> UIViewController)? = null

