package com.mobi.mobilitapp.map


import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.mobi.mobilitapp.R
import com.mobi.mobilitapp.ui.theme.*
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*
import kotlin.math.roundToInt


/**
 * Represents a Mapa object that provides functionality related to maps.
 *
 * @author Miquel Gotanegra
 * @property context The application context.
 */
class Mapa(val context:Context,sharedPreferences: SharedPreferences? = null): AppCompatActivity() {
    private lateinit var fullView:View
    private lateinit var mMap:MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var myRotationOverlay : RotationGestureOverlay
    private var preferences = sharedPreferences
    val markerScale = 18.0
    var totalCO2 = 0.0
    var totalDistance = 0.0
    var CO2String = mutableStateOf(formatData(totalCO2,"CO2"))
    var DistanceString = mutableStateOf(formatData(totalDistance,"distance"))

    // dimensions of the drawn map, used in zoomToBB
    var mapHeight = 0.dp
    var mapWidth = 0.dp


    private val uiString = mutableStateOf(context.getString(R.string.co2cons) + " %.0fg".format(0.0))
    private val uiString2 = mutableStateOf(context.getString(R.string.distcons)+" %.0fm".format(0.0))
    val mutColor = mutableStateOf(ecoGreen)
    val battery = mutableStateOf("Regular")
    val triggerRecomposition = mutableStateOf("trigger")


    private var partialDistance = 0.0
    private var markersOnThisRoad: Queue<GeoPoint> = LinkedList<GeoPoint>()

    var selectedIcon = R.drawable.marker_car
    private var currentIcon = R.drawable.test_yellow
    private var previousIcon = R.drawable.test_yellow
    private var emptyMarker = false
    private var roadIndex = 0
    private var onTrip = mutableStateOf(false)
    private var onCruise = mutableStateOf(false)

    //instead of blindly believing the route generated, draw a straight line if the path seems unlikely
    private var useHeuristic = true
    fun <K, V> Map<K, V>.inverseMap() = map { Pair(it.value, it.key) }.toMap()
    var trams: MutableList<Pair<String,Double>> = mutableListOf()

    private val markersMap: MutableMap<GeoPoint, Marker> = mutableMapOf()

    private var savedMarkersForReset : MutableMap<GeoPoint,AuxMarker> = mutableMapOf()
    private var savedRoadsForReset : ArrayList<Polyline> = ArrayList<Polyline>()
    private val geoQ: Queue<GeoPoint> = LinkedList<GeoPoint>()
    private val fibPosition = GeoPoint(41.38867, 2.11196)
    private val jardinsPedralbes = GeoPoint(41.387540, 2.117864)

    private val markerColors: Map<Int, Int> =
        mapOf(
            R.drawable.marker_bike to cyan.toArgb(),
            R.drawable.marker_bus to blue.toArgb(),
            R.drawable.marker_car to red.toArgb(),
            R.drawable.marker_ebike to green.toArgb(),
            R.drawable.marker_escooter to green.toArgb(),
            R.drawable.marker_metro to purpl.toArgb(),
            R.drawable.marker_moto to red.toArgb(),
            R.drawable.marker_run to yellowWalk.toArgb(),
            R.drawable.marker_still to yellowWalk.toArgb(),
            R.drawable.marker_tram to greenTram.toArgb(),
            R.drawable.marker_tren to orangeRodalies.toArgb(),
            R.drawable.marker_walk to yellowWalk.toArgb(),
            R.drawable.test_red to red.toArgb(),
            R.drawable.test_yellow to yellowWalk.toArgb(),
            R.drawable.test_purple to purpl.toArgb(),
            R.drawable.test_cyan to cyan.toArgb()
        )

    val co2Table: Map<String, Double> = // g/KM
        mapOf(
            "walk" to 0.0,  "run" to 0.0, "still" to 0.0, "bike" to 0.0,
            "tren" to 9.0,  "metro" to 23.7, "tram" to 24.0, "bus" to 40.6,
            "moto" to 50.5, "escooter" to 2.2, "ebike" to 1.52, "car" to 100.0,
        )


    val nameToID: Map<String,Int> =
        mapOf(
            "WALK" to R.drawable.marker_walk, "Run" to R.drawable.marker_run, "STILL" to R.drawable.marker_still, "Bicycle" to R.drawable.marker_bike,
            "Train" to R.drawable.marker_tren, "Metro" to R.drawable.marker_metro, "Tram" to R.drawable.marker_tram, "Bus" to R.drawable.marker_bus,
            "Moto" to R.drawable.marker_moto, "E-Scooter" to R.drawable.marker_escooter, "E-Bike" to R.drawable.marker_ebike, "Car" to R.drawable.marker_car,
        )

    val transformTable: Map<String, String> = // g/KM
        mapOf(
            "WALK" to "walk",  "Run" to "run", "STILL" to "still","Bicycle" to  "bike",
            "Train" to "tren", "Metro" to "metro",  "Tram" to "tram" , "Bus" to "bus",
            "Moto" to "moto","E-Scooter" to  "escooter", "E-Bike" to "ebike",  "Car" to "car",
        )

    inner class AuxMarker{
        var position = GeoPoint(41.38867, 2.11196)
        var icon :Drawable? = transformDrawable(ContextCompat.getDrawable(context, R.drawable.test_red), 13.0 / 18 )
        var title :String = ""
        var rotation = 0.0f
        var anchorU = 0.5f
        var anchorV = 1.0f

    }
    inner class MyMarker(mapView: MapView?): Marker(mapView){
        override fun onSingleTapConfirmed(event: MotionEvent?, mapView: MapView?): Boolean {
            return super.onSingleTapConfirmed(event, mapView)
        }

        override fun onLongPress(event: MotionEvent?, mapView: MapView?): Boolean {
            val touched = hitTest(event, mapView)
            if (touched) {
                //removeMarker(this.position)
                //addTwin(this)
                //updateMarker(position,"alo")
            }
            return super.onLongPress(event, mapView)
        }
    }
    init {
        resetView()

    }
    fun triggerRecompose(){
        triggerRecomposition.value = ""
    }
    fun getColor(vehicle: String):Color{
        if (!nameToID.contains(vehicle)) return Color.White
        return Color(markerColors[nameToID[vehicle]]!!)
    }
    fun getCO2(dist: Double,vehicle: String):Double{
        val ret = 0.0
        if(!co2Table.contains(vehicle)){
            if(!transformTable.contains(vehicle)) return ret
            else  return co2Table[transformTable[vehicle]]!!*dist/1000
        }
        return co2Table[vehicle]!!*dist/1000
    }
    fun resetView(){
        //for(m in markersMap) m.value.remove(mMap)
        fullView  = LayoutInflater.from(context).inflate(R.layout.map_layout, null)
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        mMap = fullView.findViewById(R.id.map) as MapView
        mMap.post(
            Runnable { center()
            })
        mMap.setUseDataConnection(true)
        mMap.setTileSource(TileSourceFactory.MAPNIK)
        mMap.setMultiTouchControls(true)
        myRotationOverlay = RotationGestureOverlay(mMap)
        myRotationOverlay.isEnabled
        mMap.overlays.add(myRotationOverlay)

        mMap.maxZoomLevel = 20.0
        mMap.minZoomLevel = 6.0
        mMap.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

        // a polyline has an atribute MapView that can be null, but once added to a map the polyline 's mapview is set
        // and cannot be added to a diferent map, so i make a clone to add to the current map
        for(r in savedRoadsForReset){
            val poly = Polyline()
            for(p in r.actualPoints) poly.addPoint(p)
            poly.outlinePaint.color = r.color
            poly.outlinePaint.strokeWidth = 10.0f
            mMap.overlays.add(0,poly)
        }
        initializeMap()

        for(m in savedMarkersForReset) reADD(m.value)
        mMap.invalidate()
    }

    private fun reADD(a:AuxMarker){
        val marker = MyMarker(mMap)
        marker.icon = a.icon
        marker.title = a.title
        marker.position = a.position
        marker.rotation = a.rotation
        marker.setAnchor(a.anchorU,a.anchorV)
        markersMap[a.position] = marker
        mMap.overlays.add(marker)
    }

    private fun transformDrawable(
        icon: Drawable?,
        scaleFactor: Double,
        flip: Boolean = false,
        hue: Int = -1
    ): Drawable? {
        if (icon == null) return icon
        val b = (icon as BitmapDrawable).bitmap
        var sizeX = (icon.getIntrinsicWidth() * scaleFactor).roundToInt()
        var sizeY = (icon.getIntrinsicHeight() * scaleFactor).roundToInt()
        if (sizeX < 1) sizeX = 1
        if (sizeY < 1) sizeY = 1
        if (flip) sizeX = -sizeX
        val bitmapResized = Bitmap.createScaledBitmap(b, sizeX, sizeY, false)
        val res = BitmapDrawable(context.resources, bitmapResized)
        if (hue != -1) res.setColorFilter(hue, PorterDuff.Mode.MULTIPLY)
        return res
    }

    fun Double.format(digits: Int) = "%.${digits}f".format(this)
    fun clear() {
        InfoWindow.closeAllInfoWindowsOn(mMap)
        mMap.overlays.clear()
        markersMap.clear()
        savedMarkersForReset.clear()
        savedRoadsForReset.clear()
        markersMap.clear()
        geoQ.clear()
        markersOnThisRoad.clear()
        mMap.overlays.add(myLocationOverlay)
        mMap.overlays.add(myRotationOverlay)
        mMap.invalidate()
        currentIcon = 80085
        lastPos = GeoPoint(0.0,0.0)
        roadIndex = 0
        trams.clear()
        totalCO2 = 0.0
        totalDistance = 0.0
        partialDistance = 0.0
        CO2String = mutableStateOf(formatData(totalCO2,"CO2"))
        DistanceString = mutableStateOf(formatData(totalDistance,"distance"))
        uiString.value = context.getString(R.string.co2cons) + " %.0fg".format(0.0)
        uiString2.value = context.getString(R.string.co2cons) + " %.0fg".format(0.0)
        mutColor.value = ecoGreen
    }
    fun startTrip() {
        clear()
        center()
        onTrip.value = true
        onCruise.value = true

 
    }
    fun endTrip() {
        onTrip.value = false
        onCruise.value = false
        zoomToBB()
    }

    fun zoomToBB(){
        myLocationOverlay.disableFollowLocation()
        if(savedRoadsForReset.isEmpty() and savedMarkersForReset.isEmpty()) return
        var l = mutableListOf<GeoPoint>()
        savedRoadsForReset.map{l.addAll(it.actualPoints)}
        l.addAll(savedMarkersForReset.keys)
        var b = BoundingBox.fromGeoPoints(l)

        var ratio = (mapHeight/mapWidth).toDouble()

        var latPad = (b.latitudeSpan/5) * (1/ratio)
        var lonPad = (b.longitudeSpan/10) * ratio
        var bb = BoundingBox.fromGeoPoints(listOf(GeoPoint(b.latNorth+latPad,b.lonEast+lonPad),GeoPoint(b.latSouth-latPad,b.lonWest-lonPad)))
        mMap.zoomToBoundingBox(bb,true)
    }

    fun center(){
        mMap.controller.animateTo(myLocationOverlay.myLocation,18.7,500.toLong(),if(onTrip.value and (myLocationOverlay.lastFix != null))myLocationOverlay.lastFix!!.bearing  else 0.0f)
        myLocationOverlay.enableFollowLocation()
    }
    fun centerTo(loc:GeoPoint){
        mMap.controller.animateTo(loc,18.7,500.toLong())
    }

    fun cruise(){
        onCruise.value = !onCruise.value
        if(onCruise.value) center()
        else myLocationOverlay.disableFollowLocation()
    }
    private fun updateMarker(
        position: GeoPoint,
        dist: Double = 0.0,
        vehicle: String = "car",
        co2: Double
    ) {
        var title = context.getString(R.string.m1)+" $vehicle: "

        val consum = (co2 * dist / 1000.0)

        title =
            if (dist < 1000.0) title + "%.0fm".format(dist) else title + "%.1fKm".format(dist/1000)
        title =
            if (consum < 1000.0) "$title\n " +context.getString(R.string.m2) + " %.1fg".format(consum) else "$title\n "+context.getString(R.string.m2)+": %.2fKg".format(consum/1000)


        if (markersMap.contains(position))markersMap[position]?.title = title
        savedMarkersForReset[position]?.title = title
        return
        //return consum
    }
    var lastPos = GeoPoint(0.0,0.0)
    fun addMarker(position: GeoPoint, drawable: Int, useMapPosition: Boolean = false) {
        //if (markersMap.contains(position)) removeMarker(position)
        // avoid multiple still markers on the same spot

        if(!onTrip.value) return

        //if screen is out of focus addMarker gets called 3 times ????
        if(position == lastPos) return
        lastPos = position


        Log.d("addMarker","addingMarker")
        if( drawable != currentIcon) trams.add(Pair(nameToID.inverseMap()[drawable]!!,0.0))
        //Log.d("trams","$trams")

        previousIcon = if (geoQ.isEmpty()) drawable else currentIcon
        currentIcon = drawable

        if(geoQ.isEmpty() and (drawable == R.drawable.marker_still)) return

        try{
            val marker = MyMarker(mMap)
            var p = position
            if(useMapPosition) p = myLocationOverlay.myLocation
            marker.position = p
            //var scale = if (map.zoomLevelDouble != 0.0) (map.zoomLevelDouble * 100.0).roundToInt() / 100.0 else 200.0

            marker.title = context.resources.getResourceEntryName(drawable)
            marker.icon = transformDrawable(ContextCompat.getDrawable(context, drawable), 13.0 / markerScale)
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            //marker.infoWindow = CustomInfoWindow(mMap)
            markersMap[p] = marker
            mMap.overlays.add(marker)
            mMap.invalidate()
        }catch (e: Exception) {
            e.printStackTrace()
        }
        savedMarkersForReset[position] = AuxMarker()
        savedMarkersForReset[position]!!.position = position
        savedMarkersForReset[position]!!.title = context.resources.getResourceEntryName(drawable)
        savedMarkersForReset[position]!!.icon = transformDrawable(ContextCompat.getDrawable(context, drawable), 13.0 / markerScale)

        if (geoQ.size > 1) geoQ.remove()
        geoQ.add(position)

        // avoid multiple still markers on the same spot

        pathing()
        if(drawable == R.drawable.marker_still) geoQ.clear()

        mMap.invalidate()

        if(battery.value == "Low") zoomToBB()

    }

    fun removeMarker(position: GeoPoint) {
        if (markersMap.contains(position)) {
            try{
                markersMap[position]?.remove(mMap)
                markersMap.remove(position)
                mMap.invalidate()
            }catch(e: Exception){}
            geoQ.remove(position)
            savedMarkersForReset.remove(position)

        }
    }



    private fun addTwin(pos: GeoPoint, flip: Boolean = false) {
        var angle = 30f
        if (flip) angle = -angle
        var m2 = AuxMarker()
        savedMarkersForReset[pos]!!.rotation = angle
        m2.rotation = -angle
        m2.position = savedMarkersForReset[pos]!!.position
        m2.position.latitude += 0.0000001
        m2.title = context.resources.getResourceEntryName(currentIcon).replace("marker_", "")
        savedMarkersForReset[pos]!!.icon = transformDrawable(ContextCompat.getDrawable(context, previousIcon), 13.0 / markerScale, flip = (context.resources.getResourceEntryName(previousIcon).contains("crooked") and (angle > 0)))
        m2.icon = transformDrawable(ContextCompat.getDrawable(context, currentIcon), 13.0 / markerScale, flip = (context.resources.getResourceEntryName(currentIcon).contains("crooked") and (angle < 0)))
        m2.anchorV = 1.0f
        savedMarkersForReset[m2.position] = m2
        try{
            val marker2 = MyMarker(mMap)
            var  marker = markersMap[pos]!!
            marker.rotation = angle
            marker2.rotation = -angle
            marker2.position = marker.position
            marker2.position.latitude += 0.0000001
            marker2.title =
                context.resources.getResourceEntryName(currentIcon).replace("marker_", "")
            val scale = markerScale
            //if (mMap.zoomLevelDouble != 0.0) (mMap.zoomLevelDouble * 100.0).roundToInt() / 100.0 else 200.0
            marker.icon = transformDrawable(
                ContextCompat.getDrawable(context, previousIcon),
                13.0 / scale,
                flip = (context.resources.getResourceEntryName(previousIcon)
                    .contains("crooked") and (angle > 0))
            )
            marker2.icon = transformDrawable(
                ContextCompat.getDrawable(context, currentIcon),
                13.0 / scale,
                flip = (context.resources.getResourceEntryName(currentIcon)
                    .contains("crooked") and (angle < 0))
            )

            marker2.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            //marker2.infoWindow = CustomInfoWindow(mMap)
            markersMap[marker2.position] = marker2
            mMap.overlays.add(marker2)
            mMap.invalidate()
        }catch(e:Exception){}
        //mapOverlays.add(marker2)
        if (geoQ.size > 1) geoQ.remove()
        geoQ.add(m2.position)

    }

    fun pathing(){
        if (geoQ.size > 1) {
            val start = geoQ.remove();lifecycleScope.launch {
                geoQ.peek()
                    ?.let {  makePath(start, it) };
            }
        }
    }
    private fun makePath(startPoint: GeoPoint, endPoint: GeoPoint) {

        // saving this so the internet call doesnt produce race conditions
        val prevIcon = previousIcon
        val currIcon = currentIcon
        val thread = Thread {
            try {
                val roadManager = OSRMRoadManager(context, "MY_USER_AGENT")

                if (listOf(
                        R.drawable.marker_still,
                        R.drawable.marker_run,
                        R.drawable.marker_walk
                    ).contains(prevIcon)
                )
                    roadManager.setMean(OSRMRoadManager.MEAN_BY_FOOT)
                else if (listOf(
                        R.drawable.marker_bike,
                        R.drawable.marker_ebike,
                        R.drawable.marker_escooter
                    ).contains(prevIcon)
                )
                    roadManager.setMean(OSRMRoadManager.MEAN_BY_BIKE)
                else
                    roadManager.setMean(OSRMRoadManager.MEAN_BY_CAR)

                val waypoints = ArrayList<GeoPoint>()
                waypoints.add(startPoint)
                waypoints.add(endPoint)
                val road = roadManager.getRoad(waypoints)
                var line = RoadManager.buildRoadOverlay(road)
                var straightLine = Polyline()
                straightLine.setPoints(waypoints)

                val fact = preferences?.getFloat("heuristic_fact",0f)?.toDouble() ?: 2.2
                if((prevIcon == R.drawable.marker_walk) or (useHeuristic and (straightLine.distance * fact < line.distance))) line = straightLine

                Log.d("heuristic fact", fact.toString())

                //quick fix to make trains go straight
                if(listOf(R.drawable.marker_metro,R.drawable.marker_tram,R.drawable.marker_tren).contains(prevIcon)) line  = straightLine

                Log.d("Previous RoadIcon", context.resources.getResourceEntryName(prevIcon))
                line.outlinePaint.color = markerColors[prevIcon]!!
                line.outlinePaint.strokeWidth = 10.0f

                //road index per a que les carreteres mes noves solapin a les velles si es creuen i no al reves

                var poly = Polyline()
                for(p in line.actualPoints){
                    poly.addPoint(p);
                }
                poly.outlinePaint.color = markerColors[prevIcon]!!
                poly.outlinePaint.strokeWidth = 10.0f
                savedRoadsForReset.add(poly)
                ++roadIndex

                try{
                    mMap.overlays.add(roadIndex, line)
                    mMap.invalidate()
                }catch(e: Exception){}

                val vehicle = context.resources.getResourceEntryName(prevIcon)
                    .replace("marker_", "").replace("_crooked", "")
                val co2 = (co2Table[vehicle] ?: 0.0)

                totalCO2 += (co2 / 1000.0) * line.distance
                totalDistance += line.distance
                partialDistance += line.distance

                CO2String.value = formatData(totalCO2,"CO2")
                DistanceString.value = formatData(totalDistance,"distance")

                Log.d("tram","${trams}")
                if(currIcon != prevIcon )trams[trams.size-2]  = trams[trams.size-2].copy(second = trams[trams.size-2].second + line.distance)
                else trams[trams.size-1]  = trams[trams.size-1].copy(second = trams[trams.size-1].second + line.distance)

                for (g in markersOnThisRoad) updateMarker(g, dist = partialDistance, vehicle, co2)
                updateMarker(startPoint, dist = partialDistance, vehicle, co2)
                updateMarker(endPoint, dist = partialDistance, vehicle, co2)

                //change middle marker to a sphere, same color as the road
                if ((markersOnThisRoad.size > 0)) {
                    if(markersMap.contains(startPoint)){
                        markersMap[startPoint]!!.icon = transformDrawable(
                            ContextCompat.getDrawable(context, R.drawable.bolita),
                            scaleFactor = 0.6,
                            hue = markerColors[prevIcon]!!
                        )
                        markersMap[startPoint]!!.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    }

                    savedMarkersForReset[startPoint]!!.icon = transformDrawable(ContextCompat.getDrawable(context, R.drawable.bolita), scaleFactor = 0.6, hue = markerColors[prevIcon]!!)
                    savedMarkersForReset[startPoint]!!.anchorV = Marker.ANCHOR_CENTER


                }

                if ((prevIcon != currIcon)) markersOnThisRoad.clear()

                //center the markers on the route
                val p = line.actualPoints
                Log.d("markersOnThisRoad","${markersOnThisRoad.size},$markersOnThisRoad")
                if (markersOnThisRoad.size > 0){
                    if(markersMap.contains(startPoint))markersMap[startPoint]!!.position = p[0]
                    savedMarkersForReset[startPoint]!!.position = p[0]

                }
                if(markersMap.contains(endPoint))markersMap[endPoint]!!.position = p[p.size - 1]
                savedMarkersForReset[endPoint]!!.position = p[p.size - 1]
                mMap.invalidate()

                // adding a second marker on the destination, if changing to a new vehicle
                if ((prevIcon != currIcon)) {
                    if(currIcon == R.drawable.marker_still){
                        if(markersMap.contains(endPoint)) markersMap[endPoint]!!.icon  = transformDrawable(ContextCompat.getDrawable(context, prevIcon), 13.0 / 18)
                        savedMarkersForReset[endPoint]!!.icon = transformDrawable(ContextCompat.getDrawable(context, prevIcon), 13.0 / 18)
                    }
                    else{
                        addTwin(endPoint,p[p.size - 2].longitude > endPoint.longitude)
                    }

                }

                //trigger recomposition for  walking periods, when CO2 doesn't increase but distance does
                uiString.value = ""
                if (totalCO2 < 1000) uiString.value =
                    context.getString(R.string.co2cons) + " %.1fg".format(totalCO2)
                else uiString.value = context.getString(R.string.co2cons) + " %.2fKg".format(totalCO2/1000)

                if (totalDistance < 1000) uiString2.value =
                    context.getString(R.string.distcons) + " %.0fm".format(totalDistance)
                else uiString2.value = context.getString(R.string.distcons) + " %.1fKm".format(totalDistance/1000)
                if ((prevIcon != currIcon)) partialDistance = 0.0
                else markersOnThisRoad.add(startPoint)
                mutColor.value =
                    if (totalCO2 * 1000 / (totalDistance) > 45) ecoRed
                    else if (totalCO2 * 1000 / (totalDistance) > 10) ecoYellow
                    else ecoGreen


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        thread.start()
    }

    fun initializeMap() {
        //val mapController = map.controller
        val provider = GpsMyLocationProvider(context)
        provider.addLocationSource(LocationManager.NETWORK_PROVIDER)
        myLocationOverlay =
            object : MyLocationNewOverlay(provider, mMap) {
                override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
                    super.onLocationChanged(location, source)
                    location?.let {
                        //uiString.value = map.zoomLevelDouble.toString()//parseLocation(it)
                    }
                }
                override fun drawMyLocation(canvas: Canvas?, pj: Projection?, lastFix: Location?) {
                    // TODO Trip mode WIP
                    if (onTrip.value and this.isFollowLocationEnabled) rotateMap(lastFix)
                    super.drawMyLocation(canvas, pj, lastFix)
                }

                private fun rotateMap(lastFix: Location?) {
                    lastFix?.let {
                        if(lastFix.hasBearing()) mMap.mapOrientation = -it.bearing
                    }
                }
                override fun onLongPress(e: MotionEvent?, mapView: MapView?): Boolean {
                    val proj = mapView!!.projection
                    val loc = proj.fromPixels(e!!.x.toInt(), e.y.toInt()) as GeoPoint
                    //addMarker(loc, selectedIcon)
                    return super.onLongPress(e, mapView)
                }

                override fun disableFollowLocation() {
                    super.disableFollowLocation()
                    if(onTrip.value) onCruise.value  = false
                }

                override fun enableFollowLocation() {
                    super.enableFollowLocation()
                    if(onTrip.value) onCruise.value  = true
                }
            }
        if(battery.value == "Regular"){ // regular
            myLocationOverlay.enableMyLocation()
            myLocationOverlay.isDrawAccuracyEnabled = true
            myLocationOverlay.enableFollowLocation()
            myLocationOverlay.runOnFirstFix {
                runOnUiThread {
                    center()
                }
            }

        }
        else{
            myLocationOverlay.enableMyLocation()
            myLocationOverlay.runOnFirstFix {
                runOnUiThread {
                    center()
                    myLocationOverlay.disableMyLocation()
                    myLocationOverlay.isDrawAccuracyEnabled = false
                    myLocationOverlay.disableFollowLocation()
                }
            }

        }

        mMap.overlays.add(myLocationOverlay)
        mMap.invalidate()
    }


    @Composable
    fun ButtonCenterMap() {
        Button(onClick = {center() },shape= CircleShape, modifier = Modifier.size(50.dp), contentPadding = PaddingValues(0.dp),colors = ButtonDefaults.buttonColors(backgroundColor = Orange)){
            Icon(painter = painterResource(R.drawable.baseline_explore_24) , contentDescription = "Center map", modifier = Modifier.size(30.dp),tint = Color.White,

                )
        }
    }
    @Composable
    fun ButtonClearMap(){
        Button(onClick = {  clear()
            //zoomToBB()
                         },shape= CircleShape, modifier = Modifier.size(50.dp), contentPadding = PaddingValues(0.dp),colors = ButtonDefaults.buttonColors(backgroundColor = Color.White)) {
            Icon(painter = painterResource(R.drawable.broom) , contentDescription = "Clean map", modifier = Modifier.size(25.dp),tint = Orange,

                )
        }
    }
    @Composable
    fun ButtonCruise() {
        var color = if(onCruise.value) Orange else Color.White
        Button(onClick = { cruise() },shape= CircleShape, modifier = Modifier.size(50.dp), contentPadding = PaddingValues(0.dp),colors = ButtonDefaults.buttonColors(backgroundColor = color)){
            Icon(
                painter = painterResource(if(onCruise.value)R.drawable.straight_arrow else R.drawable.tilted_arrow) ,
                contentDescription = "Cruise", modifier = Modifier.size(30.dp),tint = if(onCruise.value) Color.White else Orange,

                )
        }
    }

    @Composable
    fun DrawMap() {
        AndroidView(
            factory = {
                resetView()

                fullView

            },
            modifier = Modifier.fillMaxSize(),//.padding(bottom = 40.dp),
            //esto se ejecuta despues del inflate
            update = {
            }

        )
    }
    @Composable
    fun noMapa(){
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.LightGray)){
            Column(
                Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally){
                Icon(
                    painter = painterResource(R.drawable.hide),
                    contentDescription = "map unaviable",
                    modifier = Modifier
                        .size(200.dp)

                        ,
                    tint = Color.Black
                )
                Spacer(Modifier.size(10.dp))
                Text(modifier = Modifier.padding(start = 20.dp,end = 20.dp), text = context.getString(R.string.mapDisabled) , textAlign = TextAlign.Justify, fontSize = 12.sp, color = Color.Black )
                Spacer(Modifier.size(40.dp))
            }

        }
    }
    @Composable
    fun appLayout() {

        if(triggerRecomposition.value == "trigger"){
            val localDensity = LocalDensity.current

            //check if the preferences have changed
            battery.value = preferences?.getString("battery", "Regular")!!
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .clip(shape = RoundedCornerShape(15.dp))
                    .onGloballyPositioned { coordinates ->
                        mapHeight = with(localDensity) { coordinates.size.height.toDp() }
                        mapWidth = with(localDensity) { coordinates.size.width.toDp() }
                    }

            )// this will be the map
            {
                if (battery.value == "Minimal") {
                    noMapa()
                } else {
                    DrawMap()
                    //Text("H $mapHeight W $mapWidth",modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 2.dp), fontWeight = FontWeight.Bold,color = Color.Black)

                    Column(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 20.dp, end = 15.dp)
                    ) {


                        if (battery.value == "Regular") {
                            if (!onTrip.value) {
                                //ButtonClearMap
                                Spacer(modifier = Modifier.size(10.dp))
                                ButtonCenterMap()
                            } else {
                                //ButtonClearMap()
                                ButtonCruise()
                            }
                        }
                    }

                    //DATA
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 20.dp, start = 10.dp)
                    ) {
                        var background = Color.White.copy(alpha = 0.7f)

                        Row(
                            Modifier
                                .clip(shape = RoundedCornerShape(15.dp))
                                .background(background)
                                .padding(start = 10.dp, end = 10.dp, top = 2.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.eco),
                                contentDescription = "distance",
                                modifier = Modifier
                                    .size(25.dp)
                                    .padding(start = 2.dp)
                                    .align(CenterVertically),
                                tint = mutColor.value
                            )
                            Text(
                                CO2String.value,
                                modifier = Modifier
                                    .padding(start = 8.dp, end = 8.dp, top = 2.dp)
                                    .align(CenterVertically),
                                fontWeight = FontWeight.Bold,
                                color = mutColor.value
                            )
                        }
                        Spacer(Modifier.padding(2.dp))
                        Row(
                            Modifier
                                .padding(end = 3.dp)
                                .clip(shape = RoundedCornerShape(15.dp))
                                .background(background)
                                .padding(start = 10.dp, end = 10.dp, top = 1.dp, bottom = 1.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ruler),
                                contentDescription = "distance",
                                modifier = Modifier
                                    .size(25.dp)
                                    .padding(start = 2.dp)
                                    .align(CenterVertically),
                                tint = Orange
                            )
                            Text(
                                DistanceString.value,
                                modifier = Modifier
                                    .padding(start = 8.dp, end = 8.dp, top = 2.dp)
                                    .align(CenterVertically),
                                fontWeight = FontWeight.Bold,
                                color = Orange
                            )
                        }


                    }
                }
            }
        }
        else triggerRecomposition.value = "trigger"
    }
    fun formatData(data: Double, type:String = ""):String{
        var s = ""
        if(type=="CO2"){
            if(data == 0.0) s =  "%.0fg".format(data)
            else if (data < 1000) s = "%.1fg".format(data)
            else s = "%.2fKg".format(data/1000)
        }
        else if(type=="distance"){
            if (data < 1000) s= "%.0fm".format(data)
            else s ="%.1fKm".format(data/1000)
        }
        return s.replace(",",".")
    }

}



