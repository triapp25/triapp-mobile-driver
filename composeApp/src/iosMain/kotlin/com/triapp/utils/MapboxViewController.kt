package com.triapp.utils

import platform.UIKit.UIViewController
import com.triapp.domain.model.Coordinate

interface MapboxViewController {
    fun onUpdateLocation(coordinate: Coordinate)
}

var mapboxViewController: (() -> UIViewController)? = null

