package com.mobi.mobilitapp.map

import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow

class CustomInfoWindow(mapView: MapView?) :
    MarkerInfoWindow(org.osmdroid.bonuspack.R.layout.bonuspack_bubble, mapView){
    init {

    }
    override fun onOpen(item: Any?) {
        closeAllInfoWindowsOn(mapView)
        mapView.setOnClickListener {
            close()
        }

    }
    override fun onDetach() {
        close()
        super.onDetach()
    }

}

