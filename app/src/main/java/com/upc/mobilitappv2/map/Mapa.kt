package com.upc.mobilitappv2.map


import android.content.Context
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.upc.mobilitappv2.BuildConfig
import com.upc.mobilitappv2.R
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
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*
import kotlin.math.roundToInt


class Mapa(val context:Context): AppCompatActivity() {

    private val purpl = android.graphics.Color.parseColor("#9551ea")
    private val blue = android.graphics.Color.parseColor("#2468c4")
    private val cyan = android.graphics.Color.parseColor("#3be7b9")
    private val tram_green = android.graphics.Color.parseColor("#008479")
    private val green = android.graphics.Color.parseColor("#99ca58")
    private val yellow = android.graphics.Color.parseColor("#e7b63c")
    private val rod_orange = android.graphics.Color.parseColor("#ef7d00")
    private val red = android.graphics.Color.parseColor("#e74c3c")

    private lateinit var fullView:View
    private lateinit var map:MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay

    private var totalCO2 = 0.0
    private var totalDistance = 0.0
    private val uiString = mutableStateOf("Total CO2 consumption : ${totalCO2.format(0)}g")
    private val uiString2 = mutableStateOf("Total distance = ${totalDistance.format(0)}m")
    private val mutColor = mutableStateOf(Color(0xFF98D8AA))

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
            R.drawable.marker_bike to cyan,
            R.drawable.marker_bus to blue,
            R.drawable.marker_car to red,
            R.drawable.marker_ebike to green,
            R.drawable.marker_escooter to green,
            R.drawable.marker_metro to purpl,
            R.drawable.marker_moto to red,
            R.drawable.marker_run to yellow,
            R.drawable.marker_still to yellow,
            R.drawable.marker_tram to tram_green,
            R.drawable.marker_tren to rod_orange,
            R.drawable.marker_walk to yellow,
            R.drawable.test_red to red,
            R.drawable.test_yellow to yellow,
            R.drawable.test_purple to purpl,
            R.drawable.test_cyan to cyan
        )

    private val co2Table: Map<String, Double> = // g/KM
        mapOf(
            "walk" to 0.0, "run" to 0.0, "still" to 0.0, "bike" to 0.0,
            "tren" to 9.0, "metro" to 23.7, "tram" to 24.0, "bus" to 40.6,
            "moto" to 50.5, "escooter" to 2.2, "ebike" to 1.52, "car" to 100.0,
        )


    private val markersMap: MutableMap<GeoPoint, Marker> = mutableMapOf()

    private var savedMarkersForReset : MutableMap<GeoPoint,AuxMarker> = mutableMapOf()
    private var savedRoadsForReset : MutableSet<Polyline> = mutableSetOf()

    inner class AuxMarker{
        var position = GeoPoint(41.38867, 2.11196)
        var icon :Drawable? = transformDrawable(ContextCompat.getDrawable(context, R.drawable.test_red), 13.0 / 18 )
        var title :String = ""
        var rotation = 0.0f
        var anchorU = 0.5f
        var anchorV = 1.0f

    }

    private val geoQ: Queue<GeoPoint> = LinkedList<GeoPoint>()
    private val fibPosition = GeoPoint(41.38867, 2.11196)
    private val jardinsPedralbes = GeoPoint(41.387540, 2.117864)


    init {
        //binding = FragContainerBinding.inflate(layoutInflater)
        fullView  = LayoutInflater.from(context).inflate(R.layout.map_layout,null,)
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        map = fullView.findViewById<MapView>(R.id.map)
        map.post(
            Runnable { map.controller.setZoom(6.0)
                map.controller.animateTo(myLocationOverlay.myLocation)
                map.controller.setZoom(18.0)
            })
        map.setUseDataConnection(true)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.maxZoomLevel = 20.0
        map.minZoomLevel = 6.0
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        //val map = view.findViewById(R.id.map) as MapView
        initializeMap()

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
        fullView  = LayoutInflater.from(context).inflate(R.layout.map_layout,null,)
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID;
        map.removeAllViews()
        map = fullView.findViewById(R.id.map) as MapView
        map.post(
            Runnable { map.controller.setZoom(6.0)
                map.controller.animateTo(myLocationOverlay.myLocation)
                map.controller.setZoom(18.0)
            })
        map.setUseDataConnection(true)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.maxZoomLevel = 20.0
        map.minZoomLevel = 6.0
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        initializeMap()

        // a polyline has an atribute MapView that can be null, but once added to a map the polyline 's mapview is set
        // and cannot be added to a diferent map, so i make a clone to add to the current map
        for(r in savedRoadsForReset){
            val poly = Polyline()
            for(p in r.actualPoints) poly.addPoint(p)
            poly.outlinePaint.color = r.color
            poly.outlinePaint.strokeWidth = 10.0f
            map.overlays.add(0,poly)
        }
        for(m in savedMarkersForReset) reADD(m.value)
        map.invalidate()
        //val map = view.findViewById(R.id.map) as MapView
        //initializeMap()
    }

    private fun reADD(a:AuxMarker){
        val marker =
            object : Marker(map) {
                override fun onSingleTapConfirmed(event: MotionEvent?, mapView: MapView?): Boolean {
                    return super.onSingleTapConfirmed(event, mapView)
                }

                override fun onLongPress(event: MotionEvent?, mapView: MapView?): Boolean {
                    val touched = hitTest(event, mapView)
                    if (touched) {
                        removeMarker(this.position)
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
        map.overlays.add(marker)
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

        map.overlays.clear()
        savedMarkersForReset.clear()
        savedRoadsForReset.clear()
        markersMap.clear()
        geoQ.clear()
        markersOnThisRoad.clear()
        map.overlays.add(myLocationOverlay)
        map.invalidate()
        roadIndex = 0
        totalCO2 = 0.0
        totalDistance = 0.0
        partialDistance = 0.0
        uiString.value = "Total CO2 consumption : ${totalCO2.format(0)}g"
        uiString2.value = "Total distance = ${totalDistance.format(0)}m"
        mutColor.value = Color(0xFF98D8AA)
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
            if (dist < 1000.0) title + dist.format(0) + "m" else title + (dist / 1000).format(2) + "Km"
        title =
            if (consum < 1000.0) "$title\n Consum de CO2: ${consum.format(2)} g" else "$title\n Consum de CO2: ${
                (consum / 1000).format(2)
            } g"

        var m: AuxMarker;

        markersMap[position]?.title = title
        savedMarkersForReset[position]?.title = title
        return
        //return consum
    }

    fun addMarker(position: GeoPoint, drawable: Int) {
        if (markersMap.contains(position)) removeMarker(position)

        previousIcon = if (geoQ.isEmpty()) drawable else currentIcon
        currentIcon = drawable
        Log.d("PreviousIcon", context.resources.getResourceEntryName(previousIcon))
        val marker =
            object : Marker(map) {
                override fun onSingleTapConfirmed(event: MotionEvent?, mapView: MapView?): Boolean {
                    return super.onSingleTapConfirmed(event, mapView)
                }

                override fun onLongPress(event: MotionEvent?, mapView: MapView?): Boolean {
                    val touched = hitTest(event, mapView)
                    if (touched) {
                        removeMarker(this.position)
                        //addTwin(this)
                        //updateMarker(position,"alo")
                    }
                    return super.onLongPress(event, mapView)
                }
            }
        marker.position = position
        //var scale = if (map.zoomLevelDouble != 0.0) (map.zoomLevelDouble * 100.0).roundToInt() / 100.0 else 200.0
        val scale = 18.0
        marker.title = context.resources.getResourceEntryName(drawable)
        marker.icon = transformDrawable(ContextCompat.getDrawable(context, drawable), 13.0 / scale)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        markersMap[position] = marker
        savedMarkersForReset[position] = shallowCopy(marker)


        if(map!= null){ // to not create a null exception when the map is not visible
            map.overlays.add(marker)
            map.invalidate()
        }

        if (geoQ.size > 1) geoQ.remove()
        geoQ.add(position)

        pathing()
        map.invalidate()
    }

    fun removeMarker(position: GeoPoint) {
        if (markersMap.contains(position)) {
            if(map!=null) markersMap[position]?.remove(map)
            markersMap.remove(position)
            geoQ.remove(position)
            savedMarkersForReset.remove(position)
            map.invalidate()
        }
    }


    private fun addTwin(marker: Marker, flip: Boolean = false) {
        val marker2 =
            object : Marker(map) {
                override fun onSingleTapConfirmed(event: MotionEvent?, mapView: MapView?): Boolean {
                    return super.onSingleTapConfirmed(event, mapView)
                }

                override fun onLongPress(event: MotionEvent?, mapView: MapView?): Boolean {
                    val touched = hitTest(event, mapView)
                    if (touched) {
                        removeMarker(this.position)
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
            if (map.zoomLevelDouble != 0.0) (map.zoomLevelDouble * 100.0).roundToInt() / 100.0 else 200.0
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
        markersMap[marker2.position] = marker2
        savedMarkersForReset[marker.position] = shallowCopy(marker)
        savedMarkersForReset[marker2.position] = shallowCopy(marker2)

        if(map!= null){
            map.overlays.add(marker2)
            map.invalidate()
        }
        //mapOverlays.add(marker2)
        if (geoQ.size > 1) geoQ.remove()
        geoQ.add(marker2.position)

    }

    fun pathing(){
        if (geoQ.size > 1) {
            val start = geoQ.remove();lifecycleScope.launch {
                geoQ.peek()
                    ?.let { makePath(start, it) };
            }
        }
    }
    private fun makePath(startPoint: GeoPoint, endPoint: GeoPoint): Double {
        var ret = 0.0
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
                map.overlays.add(roadIndex, line)
                var poly = Polyline()
                for(p in line.actualPoints){
                    poly.addPoint(p);
                }
                poly.outlinePaint.color = markerColors[prevIcon]!!
                poly.outlinePaint.strokeWidth = 10.0f
                savedRoadsForReset.add(poly)
                ++roadIndex
                map.invalidate()

                val vehicle = context.resources.getResourceEntryName(prevIcon)
                    .replace("marker_", "").replace("_crooked", "")
                val co2 = (co2Table[vehicle] ?: 0.0)

                totalCO2 += (co2 / 1000.0) * line.distance
                totalDistance += line.distance

                partialDistance += line.distance
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
                map.invalidate()

                // adding a second marker on the destination, if changing to a new vehicle
                if ((prevIcon != currIcon)) {
                    savedMarkersForReset.remove(endPoint)
                    markersMap[endPoint]?.let {
                        addTwin(
                            it,
                            p[p.size - 2].longitude > endPoint.longitude
                        )
                    }
                }

                //trigger recomposition for  walking periods, when CO2 doesn't increase but distance does
                uiString.value = ""
                if (totalCO2 < 1000) uiString.value =
                    "Total CO2 consumption : ${totalCO2.format(1)}g"
                else uiString.value = "Total CO2 consumption : ${(totalCO2 / 1000).format(2)}Kg"

                if (totalDistance < 1000) uiString2.value =
                    "Total distance = ${(totalDistance).format(1)}m"
                else uiString2.value = "Total distance = ${(totalDistance / 1000).format(2)}Km"
                if ((prevIcon != currIcon)) partialDistance = 0.0
                else markersOnThisRoad.add(startPoint)
                mutColor.value =
                    if (totalCO2 * 1000 / (totalDistance) > 60) Color(0xFFFF6D60) else if (totalCO2 * 1000 / (totalDistance) > 25) Color(
                        0xFFF7D06E
                    ) else Color(0xFF98D8AA)
               ret = line.distance
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        thread.start()
        return ret
    }

    fun initializeMap() {
        //val mapController = map.controller

        myLocationOverlay =
            object : MyLocationNewOverlay(GpsMyLocationProvider(context), map) {
                override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
                    super.onLocationChanged(location, source)
                    location?.let {
                        //uiString.value = map.zoomLevelDouble.toString()//parseLocation(it)
                    }
                }

                override fun onLongPress(e: MotionEvent?, mapView: MapView?): Boolean {
                    val proj = mapView!!.projection
                    val loc = proj.fromPixels(e!!.x.toInt(), e.y.toInt()) as GeoPoint
                    if (!emptyMarker) addMarker(loc, selectedIcon)
                    else {
                        if (geoQ.size > 1) geoQ.remove()
                        geoQ.add(loc)
                    }
                    return super.onLongPress(e, mapView)
                }
            }

        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()
        myLocationOverlay.isDrawAccuracyEnabled = true

        myLocationOverlay.runOnFirstFix {
            runOnUiThread {
                map.controller.animateTo(myLocationOverlay.myLocation)
                map.controller.setZoom(18.0)
            }
        }
        map.overlays.add(myLocationOverlay)
        map.invalidate()
    }

    @Composable
    fun ButtonCenterMap() {
        Button(onClick = {

            map.controller.animateTo(myLocationOverlay.myLocation)
            map.controller.setZoom(18.0)
        }, shape = CircleShape) {
            Image(
                painterResource(id = R.drawable.yellowguy),
                contentDescription = "Center",
                modifier = Modifier.size(30.dp)
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
    fun fullLayout() {
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
}


