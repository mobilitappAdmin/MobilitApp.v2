package com.upc.mobilitappv2.map


import android.content.Context
import android.graphics.Bitmap
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.upc.mobilitappv2.R
import com.upc.mobilitappv2.ui.theme.*
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
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
 * @property context The context associated with the Mapa object.
 */
class Mapa(val context:Context): AppCompatActivity() {
    private lateinit var fullView:View
    private lateinit var mMap:MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay

    var totalCO2 = 0.0
    var totalDistance = 0.0
    private val uiString = mutableStateOf("Total CO2 consumption %.0fg".format(0.0))
    private val uiString2 = mutableStateOf("Total distance %.0fm".format(0.0))
    val mutColor = mutableStateOf(Color(0xFF98D8AA))

    private var partialDistance = 0.0
    private var markersOnThisRoad: Queue<GeoPoint> = LinkedList<GeoPoint>()

    private var selectedIcon = R.drawable.test_yellow
    private var currentIcon = R.drawable.test_yellow
    private var previousIcon = R.drawable.test_yellow
    private var emptyMarker = false
    private var roadIndex = 0
    private var enQueue = mutableStateOf(true)
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
            "walk" to 0.0, "run" to 0.0, "still" to 0.0, "bike" to 0.0,
            "tren" to 9.0, "metro" to 23.7, "tram" to 24.0, "bus" to 40.6,
            "moto" to 50.5, "escooter" to 2.2, "ebike" to 1.52, "car" to 100.0,
        )


     val nameToID: Map<String,Int> =
        mapOf(
            "WALK" to R.drawable.marker_walk, "Run" to R.drawable.marker_run, "STILL" to R.drawable.marker_still, "Bicycle" to R.drawable.marker_bike,
            "Train" to R.drawable.marker_tren, "Metro" to R.drawable.marker_metro, "Tram" to R.drawable.marker_tram, "Bus" to R.drawable.marker_bus,
            "Moto" to R.drawable.marker_moto, "E-Scooter" to R.drawable.marker_escooter, "E-Bike" to R.drawable.marker_ebike, "Car" to R.drawable.marker_car,
        )
    var trams: MutableList<Double> = mutableListOf()

    private val markersMap: MutableMap<GeoPoint, Marker> = mutableMapOf()

    private var savedMarkersForReset : MutableMap<GeoPoint,AuxMarker> = mutableMapOf()
    private var savedRoadsForReset : MutableSet<Polyline> = mutableSetOf()
    private val geoQ: Queue<GeoPoint> = LinkedList<GeoPoint>()
    private val fibPosition = GeoPoint(41.38867, 2.11196)
    private val jardinsPedralbes = GeoPoint(41.387540, 2.117864)

    inner class AuxMarker{
        var position = GeoPoint(41.38867, 2.11196)
        var icon :Drawable? = transformDrawable(ContextCompat.getDrawable(context, R.drawable.test_red), 13.0 / 18 )
        var title :String = ""
        var rotation = 0.0f
        var anchorU = 0.5f
        var anchorV = 1.0f

    }
    init {
        //binding = FragContainerBinding.inflate(layoutInflater)
        fullView  = LayoutInflater.from(context).inflate(R.layout.map_layout, null)
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        mMap = fullView.findViewById<MapView>(R.id.map)
        mMap.post(
            Runnable { mMap.controller.setZoom(6.0)
                mMap.controller.animateTo(myLocationOverlay.myLocation)
                mMap.controller.setZoom(18.0)
            })
        mMap.setUseDataConnection(true)
        mMap.setTileSource(TileSourceFactory.MAPNIK)
        mMap.setMultiTouchControls(true)
        mMap.maxZoomLevel = 20.0
        mMap.minZoomLevel = 6.0
        mMap.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        //val map = view.findViewById(R.id.map) as MapView
        initializeMap()

    }

    fun getColor(vehicle: String):Color{
        if (!nameToID.contains(vehicle)) return Color.White
        return Color(markerColors[nameToID[vehicle]]!!)
    }

    val transformTable: Map<String, String> = // g/KM
        mapOf(
            "WALK" to "walk",  "Run" to "run", "STILL" to "still","Bicycle" to  "bike",
            "Train" to "tren", "Metro" to "metro",  "Tram" to "tram" , "Bus" to "bus",
            "Moto" to "moto","E-Scooter" to  "escooter", "E-Bike" to "ebike",  "Car" to "car",
        )
    fun getCO2(dist: Double,vehicle: String):Double{
        val ret = 0.0
        if(!co2Table.contains(vehicle)){
            if(!transformTable.contains(vehicle)) return ret
            else  return co2Table[transformTable[vehicle]]!!*dist/1000
        }
        return co2Table[vehicle]!!*dist/1000
    }
    fun shallowCopy(m:Marker):AuxMarker{
        var a = AuxMarker()
        a.position = m.position
        a.icon = m.icon
        a.title = m.title
        a.rotation = m.rotation
        return a
    }
    fun resetView(){
        //for(m in markersMap) m.value.remove(mMap)
        fullView  = LayoutInflater.from(context).inflate(R.layout.map_layout, null)
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        mMap.removeAllViews()
        mMap = fullView.findViewById(R.id.map) as MapView
        mMap.post(
            Runnable { mMap.controller.setZoom(6.0)
                mMap.controller.animateTo(myLocationOverlay.myLocation)
                mMap.controller.setZoom(18.0)
            })
        mMap.setUseDataConnection(true)
        mMap.setTileSource(TileSourceFactory.MAPNIK)
        mMap.setMultiTouchControls(true)
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
        /*for(m in markersMap){
            mMap.overlays.add(m.value)
            m.value.infoWindow = CustomInfoWindow(mMap)
        }*/
        for(m in savedMarkersForReset) reADD(m.value)
        mMap.invalidate()
        //val map = view.findViewById(R.id.map) as MapView
        //initializeMap()
    }

    private fun reADD(a:AuxMarker){
        val marker =
            object : Marker(mMap) {
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
        savedMarkersForReset.clear()
        savedRoadsForReset.clear()
        markersMap.clear()
        geoQ.clear()
        markersOnThisRoad.clear()
        mMap.overlays.add(myLocationOverlay)
        mMap.invalidate()
        roadIndex = 0
        trams.clear()
        totalCO2 = 0.0
        totalDistance = 0.0
        partialDistance = 0.0
        uiString.value = "Total CO2 consumption %.0fg".format(0.0)
        uiString2.value = "Total distance %.0fm".format(0.0)
        mutColor.value = Color(0xFF98D8AA)
    }
    fun startTrip() {
        clear()
        mMap.controller.animateTo(myLocationOverlay.myLocation)
        mMap.controller.setZoom(18.0)

    }
    private fun updateMarker(
        position: GeoPoint,
        dist: Double = 0.0,
        vehicle: String = "car",
        co2: Double
    ) {
        if (!markersMap.contains(position)) return
        var title = "Distancia total recorreguda en $vehicle: "

        val consum = (co2 * dist / 1000.0)

        title =
            if (dist < 1000.0) title + "%.0fm".format(dist) else title + "%.1fKm".format(dist/1000)
        title =
            if (consum < 1000.0) "$title\n Consum de CO2: %.1fg".format(consum) else "$title\n Consum de CO2: %.2fKg".format(consum/1000)

        var m: AuxMarker;

        markersMap[position]?.title = title
        savedMarkersForReset[position]?.title = title
        return
        //return consum
    }

    fun addMarker(position: GeoPoint, drawable: Int, useMapPosition: Boolean = false) {
        if (markersMap.contains(position)) removeMarker(position)
        // avoid multiple still markers on the same spot

        if( drawable != currentIcon) trams.add(0.0)
        //Log.d("trams","$trams")

        previousIcon = if (geoQ.isEmpty()) drawable else currentIcon
        currentIcon = drawable

        if(geoQ.isEmpty() and (drawable == R.drawable.marker_still)) return




        try{
            val marker =
                object : Marker(mMap) {
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
            var p = position
            if(useMapPosition) p = myLocationOverlay.myLocation
            marker.position = p
            //var scale = if (map.zoomLevelDouble != 0.0) (map.zoomLevelDouble * 100.0).roundToInt() / 100.0 else 200.0
            val scale = 18.0
            marker.title = context.resources.getResourceEntryName(drawable)
            marker.icon = transformDrawable(ContextCompat.getDrawable(context, drawable), 13.0 / scale)
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
        savedMarkersForReset[position]!!.icon = transformDrawable(ContextCompat.getDrawable(context, drawable), 13.0 / 18.0)

        if (geoQ.size > 1) geoQ.remove()
        geoQ.add(position)

        // avoid multiple still markers on the same spot

         pathing()
        if(drawable == R.drawable.marker_still) geoQ.clear()


        mMap.invalidate()

    }

    fun removeMarker(position: GeoPoint) {
        if (markersMap.contains(position)) {
            if(mMap!=null) markersMap[position]?.remove(mMap)
            markersMap.remove(position)
            geoQ.remove(position)
            savedMarkersForReset.remove(position)
            mMap.invalidate()
        }
    }


    private fun addTwin(marker: Marker, flip: Boolean = false) {
        val marker2 =
            object : Marker(mMap) {
                override fun onSingleTapConfirmed(event: MotionEvent?, mapView: MapView?): Boolean {
                    return super.onSingleTapConfirmed(event, mapView)
                }

                override fun onLongPress(event: MotionEvent?, mapView: MapView?): Boolean {
                    val touched = hitTest(event, mapView)
                    if (touched) {
                        //removeMarker(this.position)
                        //updateMarker(position,"alo")
                    }
                    return super.onLongPress(event, mapView)
                }
            }
//        markerTwins[marker] = marker2
//        markerTwins[marker2] = marker

        var angle = 30f
        if (flip) angle = -angle
        marker.rotation = angle
        marker2.rotation = -angle
        marker2.position = marker.position
        marker2.position.latitude += 0.0000001
        marker2.title =
            context.resources.getResourceEntryName(currentIcon).replace("marker_", "")
        val scale =
            if (mMap.zoomLevelDouble != 0.0) (mMap.zoomLevelDouble * 100.0).roundToInt() / 100.0 else 200.0
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
        savedMarkersForReset[marker.position] = shallowCopy(marker)
        savedMarkersForReset[marker2.position] = shallowCopy(marker2)

        if(mMap!= null){
            mMap.overlays.add(marker2)
            mMap.invalidate()
        }
        //mapOverlays.add(marker2)
        if (geoQ.size > 1) geoQ.remove()
        geoQ.add(marker2.position)

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

                //quick fix to make trains go straight
                if(listOf(R.drawable.marker_metro,R.drawable.marker_tram,R.drawable.marker_tren).contains(prevIcon))
                {
                    line  = Polyline()
                    line.setPoints(waypoints)
                }
                Log.d("Previous RoadIcon", context.resources.getResourceEntryName(prevIcon))
                line.outlinePaint.color = markerColors[prevIcon]!!
                line.outlinePaint.strokeWidth = 10.0f

                //road index per a que les carreteres mes noves solapin a les velles si es creuen i no al reves
                mMap.overlays.add(roadIndex, line)
                var poly = Polyline()
                for(p in line.actualPoints){
                    poly.addPoint(p);
                }
                poly.outlinePaint.color = markerColors[prevIcon]!!
                poly.outlinePaint.strokeWidth = 10.0f
                savedRoadsForReset.add(poly)
                ++roadIndex
                mMap.invalidate()

                val vehicle = context.resources.getResourceEntryName(prevIcon)
                    .replace("marker_", "").replace("_crooked", "")
                val co2 = (co2Table[vehicle] ?: 0.0)

                totalCO2 += (co2 / 1000.0) * line.distance
                totalDistance += line.distance
                partialDistance += line.distance

                if(currIcon == R.drawable.marker_still)trams[trams.size-2] += line.distance
                else trams[trams.size-1] += line.distance

                for (g in markersOnThisRoad) updateMarker(g, dist = partialDistance, vehicle, co2)
                updateMarker(startPoint, dist = partialDistance, vehicle, co2)
                updateMarker(endPoint, dist = partialDistance, vehicle, co2)

                //change middle marker to a sphere, same color as the road
                if ((markersOnThisRoad.size > 0)) {
                    markersMap[startPoint]!!.icon = transformDrawable(
                        ContextCompat.getDrawable(context, R.drawable.bolita),
                        scaleFactor = 0.6,
                        hue = markerColors[prevIcon]!!
                    )
                    markersMap[startPoint]!!.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    savedMarkersForReset[startPoint]!!.icon = markersMap[startPoint]!!.icon
                    savedMarkersForReset[startPoint]!!.anchorV = Marker.ANCHOR_CENTER


                }

                if ((prevIcon != currIcon)) markersOnThisRoad.clear()

                //center the markers on the route
                val p = line.actualPoints
                if (markersOnThisRoad.size > 0){
                    markersMap[startPoint]!!.position = p[0]
                    savedMarkersForReset[startPoint]!!.position = p[0]

                }
                markersMap[endPoint]!!.position = p[p.size - 1]
                savedMarkersForReset[endPoint]!!.position = p[p.size - 1]
                mMap.invalidate()

                // adding a second marker on the destination, if changing to a new vehicle
                if ((prevIcon != currIcon)) {
                    if(currIcon == R.drawable.marker_still){
                        markersMap[endPoint]!!.icon  = transformDrawable(ContextCompat.getDrawable(context, prevIcon), 13.0 / 18)
                    }
                    else{
                        savedMarkersForReset.remove(endPoint)
                        markersMap[endPoint]?.let {
                            addTwin(
                                it,
                                p[p.size - 2].longitude > endPoint.longitude
                            )
                        }
                    }

                }

                //trigger recomposition for  walking periods, when CO2 doesn't increase but distance does
                uiString.value = ""
                if (totalCO2 < 1000) uiString.value =
                    "Total CO2 consumption %.1fg".format(totalCO2)
                else uiString.value = "Total CO2 consumption %.2fKg".format(totalCO2/1000)

                if (totalDistance < 1000) uiString2.value =
                    "Total distance %.0fm".format(totalDistance)
                else uiString2.value = "Total distance %.1fKm".format(totalDistance/1000)
                if ((prevIcon != currIcon)) partialDistance = 0.0
                else markersOnThisRoad.add(startPoint)
                mutColor.value =
                    if (totalCO2 * 1000 / (totalDistance) > 45) Color(0xFFFF6D60)
                    else if (totalCO2 * 1000 / (totalDistance) > 10) Color(0xFFF7D06E)
                    else Color(0xFF98D8AA)


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
               /* override fun drawMyLocation(canvas: Canvas?, pj: Projection?, lastFix: Location?) {
                    if (this.isFollowLocationEnabled) rotateMap(lastFix)
                    super.drawMyLocation(canvas, pj, lastFix)
                }

                private fun rotateMap(lastFix: Location?) {
                    lastFix?.let {
                        if (it.speed > 0.2) {
                            map.mapOrientation = -it.bearing
                        }
                    }
                }*/

                override fun onLongPress(e: MotionEvent?, mapView: MapView?): Boolean {
                    val proj = mapView!!.projection
                    val loc = proj.fromPixels(e!!.x.toInt(), e.y.toInt()) as GeoPoint
                    //if (!emptyMarker) addMarker(loc, selectedIcon)
                    //else {
                    //  if (geoQ.size > 1) geoQ.remove()
                    //    geoQ.add(loc)
                    //}
                    return super.onLongPress(e, mapView)
                }
            }

        myLocationOverlay.enableMyLocation()
        myLocationOverlay.isDrawAccuracyEnabled = true
        myLocationOverlay.enableFollowLocation()
        myLocationOverlay.runOnFirstFix {
            runOnUiThread {
                mMap.controller.animateTo(myLocationOverlay.myLocation)
                mMap.controller.setZoom(18.0)
            }
        }
        mMap.overlays.add(myLocationOverlay)
        mMap.invalidate()
    }


    @Composable
    fun ButtonCenterMap() {
        Button(onClick = {

            mMap.controller.animateTo(myLocationOverlay.myLocation)
            mMap.controller.setZoom(18.0)
        },modifier = Modifier
            .height(40.dp)
            .width(150.dp)
            .padding(end = 1.dp))
        {
            Image(
                painterResource(id = R.drawable.yellowguy),
                contentDescription = "Center",
                modifier = Modifier.size(22.dp)
            )

            //Text(text = "Add to cart",Modifier.padding(start = 10.dp))
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
    fun debugLayout() {
        Column {
            Modifier.fillMaxWidth()
            Box(){
                Modifier
                    .background(Color.White)
                    .fillMaxWidth()

                Column() {
                    Text(
                        text = uiString.value,//Text(text = "Soy un mapa :)",
                        Modifier
                            .background(Color.White)
                            .padding(top = 16.dp, bottom = 5.dp)
                            .fillMaxWidth(),
                        fontWeight = FontWeight.Bold,
                        color = mutColor.value,
                        //backgroundColor = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = uiString2.value,
                        Modifier
                            .background(Color.White)
                            .fillMaxWidth(),
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
                Image(
                    painter = painterResource(R.drawable.eco),
                    contentDescription = "Center",
                    modifier = Modifier
                        .size(60.dp)
                        .padding(top = 8.dp, start = 3.dp),
                    colorFilter = ColorFilter.tint(mutColor.value)
                )


            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White),
                horizontalArrangement = Arrangement.SpaceEvenly,

                ) {
                ButtonCenterMap()
                // MAKE PATH BETWEEN THE TWO LAST ADDED POINTS
                Button(onClick = {
                    pathing()
                }, shape = CircleShape) { Text(text = "MakePath", Modifier.padding(start = 10.dp)) }
                Button(onClick = { clear() }, shape = CircleShape) {
                    Text(
                        text = "Clear map",
                        Modifier.padding(start = 10.dp)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Button(
                    onClick = { selectedIcon = R.drawable.test_yellow;emptyMarker = false },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)
                ) {
                    Image(
                        painterResource(id = R.drawable.test_yellow),
                        contentDescription = "Center",
                        modifier = Modifier.size(20.dp)
                    )
                }
                Button(
                    onClick = { selectedIcon = R.drawable.test_red;emptyMarker = false },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)
                ) {
                    Image(
                        painterResource(id = R.drawable.test_red),
                        contentDescription = "Center",
                        modifier = Modifier.size(20.dp)
                    )
                }


                val color =
                    if (!enQueue.value) MaterialTheme.colors.primary else MaterialTheme.colors.secondary
                Button(
                    onClick = { enQueue.value = !enQueue.value;},
                    colors = ButtonDefaults.buttonColors(backgroundColor = color),
                    shape = CircleShape
                ) {
                    Text(
                        text = "(Q)",
                        Modifier.size(25.dp),
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }


                var expanded by remember { mutableStateOf(false) }
                Button(
                    onClick = { emptyMarker = false;expanded = !expanded },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black),
                    shape = CircleShape
                )
                {
                    Text(
                        text = "(+)",
                        Modifier.size(25.dp),
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.requiredSizeIn(maxHeight = 200.dp)
                    )
                    {
                        val listItems = markerColors.keys.toList()
                        listItems.forEachIndexed { itemIndex, itemValue ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedIcon = itemValue
                                    expanded = false
                                },

                                ) {
                                Image(
                                    painterResource(id = itemValue),
                                    contentDescription = "Center",
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                }
                Button(
                    onClick = { emptyMarker = true },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black),
                    shape = CircleShape
                ) {
                    Text(
                        text = "(*)",
                        Modifier.size(25.dp),
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }

            }
//            Row(modifier = Modifier.fillMaxWidth().background(color = Color.White), horizontalArrangement = Arrangement.SpaceEvenly,){
//                Button(onClick = {selectedIcon = R.drawable.test_yellow;emptyMarker=false},   shape = CircleShape,colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)) { Image(painterResource(id = R.drawable.test_yellow), contentDescription ="Center", modifier = Modifier.size(20.dp)) }
//                Button(onClick = {selectedIcon = R.drawable.test_red;emptyMarker=false},   shape = CircleShape,colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)) { Image(painterResource(id = R.drawable.test_red), contentDescription ="Center", modifier = Modifier.size(20.dp)) }
//                Button(onClick = {selectedIcon = R.drawable.test_purple;emptyMarker=false},   shape = CircleShape,colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)) { Image(painterResource(id = R.drawable.test_purple), contentDescription ="Center", modifier = Modifier.size(20.dp)) }
//                Button(onClick = {selectedIcon = R.drawable.test_cyan;emptyMarker=false},   shape = CircleShape,colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)) { Image(painterResource(id = R.drawable.test_cyan), contentDescription ="Center", modifier = Modifier.size(20.dp)) }
//            }
            DrawMap()


        }
    }
    @Composable
    fun APPLayout() {
        Column {
            Modifier.fillMaxWidth()
            Box(){
                Modifier
                    .fillMaxWidth()

                Column() {
                    Text(
                        text = uiString.value,//Text(text = "Soy un mapa :)",
                        Modifier
                            .padding(top = 8.dp, bottom = 5.dp)
                            .fillMaxWidth(),
                        fontWeight = FontWeight.Bold,
                        color = mutColor.value,
                        //backgroundColor = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = uiString2.value,
                        Modifier
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                Image(
                    painter = painterResource(R.drawable.eco),
                    contentDescription = "Center",
                    modifier = Modifier
                        .size(60.dp)
                        .padding(top = 8.dp, start = 3.dp),
                    colorFilter = ColorFilter.tint(mutColor.value)
                )


            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.Center,

                ) {
                ButtonCenterMap()
                Button(onClick = { clear() },modifier = Modifier
                    .height(40.dp)
                    .width(150.dp)
                    .padding(start = 1.dp))
                {
                    Text(
                        text = "Clear map",
                        Modifier.padding(start = 10.dp)
                    )
                }
            }
            DrawMap()


        }
    }
    @Composable
    fun newAPPLayout() {
        Box(
            Modifier
                .fillMaxSize()
                .padding(10.dp)
                .clip(shape = RoundedCornerShape(15.dp))

        )// this will be the map
        {
            DrawMap()
            // buttons
            Column(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 20.dp, end = 15.dp)){
                Button(onClick = {  clear() },shape= CircleShape, modifier = Modifier.size(50.dp), contentPadding = PaddingValues(0.dp),colors = ButtonDefaults.buttonColors(backgroundColor = Color.White)) {
                    Icon(painter = painterResource(R.drawable.icons8_broom_26) , contentDescription = "close", modifier = Modifier.size(25.dp),tint = Orange,

                        )
                }
                Spacer(modifier = Modifier.size(10.dp))
                Button(onClick = { mMap.controller.animateTo(myLocationOverlay.myLocation);mMap.controller.setZoom(18.0) },shape= CircleShape, modifier = Modifier.size(50.dp), contentPadding = PaddingValues(0.dp),colors = ButtonDefaults.buttonColors(backgroundColor = Orange)){
                    Icon(painter = painterResource(R.drawable.baseline_explore_24) , contentDescription = "close", modifier = Modifier.size(30.dp),tint = Color.White,

                        )
                }
            }

            //DATA
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 20.dp,start= 10.dp)){
                var background = Color.White.copy(alpha = 0.7f)
                var co2 = formatData(totalCO2,"CO2")
                var  distance = formatData(totalDistance,"distance")
                Row(Modifier.clip(shape = RoundedCornerShape(topStart = 15.dp,topEnd = 15.dp)).background(background).padding(start = 10.dp,end = 10.dp)){
                    Icon( painter = painterResource(R.drawable.eco), contentDescription = "distance", modifier = Modifier
                        .size(25.dp)
                        .align(CenterVertically),tint = mutColor.value)
                    Text(co2,modifier = Modifier
                        .padding(start = 8.dp,top = 5.dp)
                        .align(CenterVertically), fontWeight = FontWeight.Bold,color = mutColor.value)
                }
                Row(Modifier.padding(end = 3.dp).clip(shape = RoundedCornerShape(bottomStart = 15.dp,bottomEnd = 15.dp, topEnd = 15.dp)).background(background).padding(start = 10.dp,end = 10.dp)){
                    Icon(painter = painterResource(R.drawable.icons8_ruler_24), contentDescription = "distance", modifier = Modifier
                        .size(25.dp)
                        .align(CenterVertically),tint = Orange)
                    Text(distance,modifier = Modifier
                        .padding(start = 8.dp,top = 5.dp,end = 8.dp)
                        .align(CenterVertically), fontWeight = FontWeight.Bold, color = Orange)
                }


            }

        }
    }
    fun formatData(data: Double, type:String = ""):String{
        var s = ""
        if(type=="CO2"){
            if(data == 0.0) s = "%.0fg".format(data)
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


@Preview
@Composable
fun appTest(){
    Column(Modifier.fillMaxSize().background(Color.White)){
        Box(Modifier.fillMaxSize().background(LightOrange).weight(2f))
        Box(Modifier.fillMaxSize().weight(3f)){newAPPLayout()}
    }

}
@Composable
fun newAPPLayout() {
    Box(
        Modifier
            .fillMaxSize()
            .padding(10.dp)
            .clip(shape = RoundedCornerShape(15.dp))

    )// this will be the map
    {
        Box(Modifier.fillMaxSize().background(SofterGray)){}//DrawMap()
        //Buttons
        Column(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 15.dp)){
            Button(onClick = {  },shape= CircleShape, modifier = Modifier.size(50.dp), contentPadding = PaddingValues(0.dp),colors = ButtonDefaults.buttonColors(backgroundColor = Color.White)) {
                Icon(painter = painterResource(R.drawable.icons8_broom_26) , contentDescription = "close", modifier = Modifier.size(25.dp),tint = Orange,

                    )
            }
            Spacer(modifier = Modifier.size(10.dp))
            Button(onClick = {  },shape= CircleShape, modifier = Modifier.size(50.dp), contentPadding = PaddingValues(0.dp),colors = ButtonDefaults.buttonColors(backgroundColor = Orange)){
                Icon(painter = painterResource(R.drawable.baseline_explore_24) , contentDescription = "close", modifier = Modifier.size(30.dp),tint = Color.White,

                    )
            }
        }
        //CO2 and Distance
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)){
            var background = Color.White.copy(alpha = 0.8f)
            Row(Modifier.clip(shape = RoundedCornerShape(topStart = 15.dp,topEnd = 15.dp)).background(background).padding(start = 10.dp,end = 10.dp)){
                Icon( painter = painterResource(R.drawable.eco), contentDescription = "distance", modifier = Modifier
                    .size(25.dp)
                    .align(CenterVertically),tint = Color(0xFF98D8AA))
                Text("327.2g",modifier = Modifier
                    .padding(start = 8.dp,top = 1.dp)
                    .align(CenterVertically), fontWeight = FontWeight.Bold,color = Color(0xFF98D8AA))
            }
            Row(Modifier.padding(end = 3.dp).clip(shape = RoundedCornerShape(bottomStart = 15.dp,bottomEnd = 15.dp, topEnd = 15.dp)).background(background).padding(start = 10.dp,end = 10.dp)){
                Icon(painter = painterResource(R.drawable.icons8_ruler_24), contentDescription = "distance", modifier = Modifier
                    .size(25.dp)
                    .align(Bottom),tint = Orange)
                Text("27.32Km",modifier = Modifier
                    .padding(start = 8.dp)
                    .align(CenterVertically), fontWeight = FontWeight.Bold)
            }


        }

    }
}





