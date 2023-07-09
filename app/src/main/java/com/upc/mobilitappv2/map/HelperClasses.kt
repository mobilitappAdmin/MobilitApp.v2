package com.upc.mobilitappv2.map

import android.content.Context
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
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

