package org.cwcc.ani.map.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.cwcc.ani.map.R
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.location.LocationComponent
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.permissions.PermissionsListener
import org.maplibre.android.location.permissions.PermissionsManager
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.HeatmapLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.sources.Source
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import java.net.URI
import java.net.URISyntaxException
import java.text.SimpleDateFormat
import java.util.Locale

//!
class OnLineMapActivity : AppCompatActivity(),OnMapReadyCallback {
    private val mapView: MapView by lazy { findViewById(R.id.onLineMapView) }
    private lateinit var maplibreMap: MapLibreMap
    private var lastLocation: Location? = null

    private lateinit var mLocationMgr:LocationManager
    private var permissionsManager: PermissionsManager? = null
    private var locationComponent: LocationComponent? = null
    private var isTianditu =false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        setContentView(R.layout.activity_on_line_map)

        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync(this)
        findViewById<FloatingActionButton>(R.id.btnMapChooser).setOnClickListener {
            locationComponent!!.applyStyle(
                LocationComponentOptions.builder(
                    this@OnLineMapActivity
                )
                    .pulseEnabled(true)
                    .build()
            )

           val loc =  locationComponent!!.lastKnownLocation
            //mapView.
            if(isTianditu)
            {
                maplibreMap.setStyle(Style.Builder().fromUri("asset://raster_style_forest.json"))
                isTianditu = false
            }
            else
            {
                maplibreMap.setStyle(Style.Builder().fromUri("asset://raster_style_tdt.json"))
                isTianditu = true
            }
            getEarthQuakeDataFromUSGS()


        }

        checkPermissions()


        //修改地图视图
        findViewById<FloatingActionButton>(R.id.btnMapViewChange).setOnClickListener{ view: View? ->
            val cameraPosition = CameraPosition.Builder()
                .target(nextLatLng)
                .zoom(14.0)
                .tilt(60.0)
                //.bearing(0.0)
                .build()
            maplibreMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }
    /* ANCHOR: onMapReady */
    @SuppressLint("MissingPermission")
    override fun onMapReady(map: MapLibreMap) {
        this.maplibreMap = map
        maplibreMap.setStyle(Style.Builder().fromUri("asset://raster_style_forest.json")){style: Style ->
            locationComponent = maplibreMap.locationComponent
            val locationComponentOptions =
                LocationComponentOptions.builder(this@OnLineMapActivity)
                    .pulseEnabled(true)
                    .build()
            val locationComponentActivationOptions =
                buildLocationComponentActivationOptions(style, locationComponentOptions)
            locationComponent!!.activateLocationComponent(locationComponentActivationOptions)
            locationComponent!!.isLocationComponentEnabled = true
            locationComponent!!.cameraMode = CameraMode.TRACKING
            locationComponent!!.forceLocationUpdate(lastLocation)
        }
        maplibreMap.uiSettings.isAttributionEnabled = false
        maplibreMap.uiSettings.isLogoEnabled = false
        val uiSettings = this@OnLineMapActivity.maplibreMap.uiSettings
        uiSettings.setAllGesturesEnabled(true)
    }


    private fun getEarthQuakeDataFromUSGS() {
        val url = "https://earthquake.usgs.gov/fdsnws/event/1/query".toHttpUrl().newBuilder()
            .addQueryParameter("format", "geojson")
            .addQueryParameter("starttime", "2022-01-01")
            .addQueryParameter("endtime", "2023-12-31")
            .addQueryParameter("minmagnitude", "5.8")
            .addQueryParameter("latitude", "24")
            .addQueryParameter("longitude", "121")
            .addQueryParameter("maxradius", "1.5")
            .build()
        val request: Request = Request.Builder().url(url).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Toast.makeText(this@OnLineMapActivity, "Fail to fetch data", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onResponse(call: Call, response: Response) {
                val featureCollection = response.body?.string()
                    ?.let(FeatureCollection::fromJson)
                    ?: return
                // If FeatureCollection in response is not null
                // Then add markers to map
                runOnUiThread { addMarkersToMap(featureCollection) }
            }
        })
    }
    private fun addMarkersToMap(data: FeatureCollection) {
        val bounds = mutableListOf<LatLng>()

        // Get bitmaps for marker icon
        val infoIconDrawable = ResourcesCompat.getDrawable(
            this.resources,
            // Intentionally specify package name
            // This makes copy from another project easier
            R.drawable.maplibre_info_icon_default,
            null
        )!!
        val bitmapBlue = infoIconDrawable.toBitmap()
        val bitmapRed = infoIconDrawable
            .mutate()
            .apply { setTint(Color.RED) }
            .toBitmap()

        // Add symbol for each point feature
        data.features()?.forEach { feature ->
            val geometry = feature.geometry()?.toJson() ?: return@forEach
            val point = Point.fromJson(geometry) ?: return@forEach
            val latLng = LatLng(point.latitude(), point.longitude())
            bounds.add(latLng)

            // Contents in InfoWindow of each marker
            val title = feature.getStringProperty("title")
            val epochTime = feature.getNumberProperty("time")
            val dateString = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.TAIWAN).format(epochTime)

            // If magnitude > 6.0, show marker with red icon. If not, show blue icon instead
            val mag = feature.getNumberProperty("mag")
            val icon = IconFactory.getInstance(this)
                .fromBitmap(if (mag.toFloat() > 6.0) bitmapRed else bitmapBlue)

            // Use MarkerOptions and addMarker() to add a new marker in map
            val markerOptions = MarkerOptions()
                .position(latLng)
                .title(dateString)
                .snippet(title)
                .icon(icon)
            maplibreMap.addMarker(markerOptions)
        }
        // Move camera to newly added annotations
        if(data.features()!!.isNotEmpty()){
            maplibreMap.getCameraForLatLngBounds(LatLngBounds.fromLatLngs(bounds))?.let {
                val newCameraPosition = CameraPosition.Builder()
                    .target(it.target)
                    .zoom(it.zoom - 0.5)
                    .build()
                maplibreMap.cameraPosition = newCameraPosition
            }
        }
        //
        maplibreMap.style!!.addSource(earthquakeSource!!)
        //maplibreMap.style!!.addLayer(eqheatmapLayer)
        maplibreMap.style!!.addLayer(createCircleLayer1())
    }

    /* ANCHOR_END: onMapReady */

    /* ANCHOR: LocationComponentActivationOptions */
    private fun buildLocationComponentActivationOptions(
        style: Style,
        locationComponentOptions: LocationComponentOptions
    ): LocationComponentActivationOptions {
        return LocationComponentActivationOptions
            .builder(this, style)
            .locationComponentOptions(locationComponentOptions)
            .useDefaultLocationEngine(true)
            .locationEngineRequest(
                LocationEngineRequest.Builder(750)
                    .setFastestInterval(750)
                    .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                    .build()
            )
            .build()
    }
    /* ANCHOR_END: LocationComponentActivationOptions */

    private fun loadNewStyle() {
        //maplibreMap.setStyle(Style.Builder().fromUri(Utils.nextStyle()))
    }

    /* ANCHOR: permission */
    private fun checkPermissions() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            mapView.getMapAsync(this)
        } else {
            permissionsManager = PermissionsManager(object : PermissionsListener {
                override fun onExplanationNeeded(permissionsToExplain: List<String>) {
                    Toast.makeText(
                        this@OnLineMapActivity,
                        "You need to accept location permissions.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onPermissionResult(granted: Boolean) {
                    if (granted) {
                        mapView.getMapAsync(this@OnLineMapActivity)
                    } else {
                        finish()
                    }
                }
            })
            permissionsManager!!.requestLocationPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    /* ANCHOR_END: permission */

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    @SuppressLint("MissingPermission")
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
        if (locationComponent != null) {
            outState.putParcelable(SAVED_STATE_LOCATION, locationComponent!!.lastKnownLocation)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
    private var cameraState = false
    private val nextLatLng: LatLng
        private get() {
            cameraState = !cameraState
            return if (cameraState) LAT_LNG_FORBIDEN_CITY else LAT_LNG_XIANG_SHAN
        }


    private val earthquakeSource: Source?
        private get() {
            var source: Source? = null
            try {
                source = GeoJsonSource(EARTHQUAKE_SOURCE_ID, URI(EARTHQUAKE_SOURCE_URL))
            } catch (uriSyntaxException: URISyntaxException) {
                //Timber.e(uriSyntaxException, "That's not an url... ")
            }
            return source
        }
    // Color ramp for heatmap.  Domain is 0 (low) to 1 (high).
    // Begin color ramp at 0-stop with a 0-transparency color
    // to create a blur-like effect.
    // Increase the heatmap weight based on frequency and property magnitude
    // Increase the heatmap color weight weight by zoom level
    // heatmap-intensity is a multiplier on top of heatmap-weight
    // Adjust the heatmap radius by zoom level
    // Transition from heatmap to circle layer by zoom level
    private val eqheatmapLayer: HeatmapLayer
        private get() {
            val layer = HeatmapLayer(EQ_HEATMAP_LAYER_ID, EARTHQUAKE_SOURCE_ID)
            layer.maxZoom = 9f
            layer.sourceLayer = EQ_HEATMAP_LAYER_SOURCE
            layer.setProperties( // Color ramp for heatmap.  Domain is 0 (low) to 1 (high).
                // Begin color ramp at 0-stop with a 0-transparency color
                // to create a blur-like effect.
                PropertyFactory.heatmapColor(
                    Expression.interpolate(
                        Expression.linear(), Expression.heatmapDensity(),
                        Expression.literal(0), Expression.rgba(33, 102, 172, 0),
                        Expression.literal(0.2), Expression.rgb(103, 169, 207),
                        Expression.literal(0.4), Expression.rgb(209, 229, 240),
                        Expression.literal(0.6), Expression.rgb(253, 219, 199),
                        Expression.literal(0.8), Expression.rgb(239, 138, 98),
                        Expression.literal(1), Expression.rgb(178, 24, 43)
                    )
                ), // Increase the heatmap weight based on frequency and property magnitude
                PropertyFactory.heatmapWeight(
                    Expression.interpolate(
                        Expression.linear(),
                        Expression.get("mag"),
                        Expression.stop(0, 0),
                        Expression.stop(6, 1)
                    )
                ), // Increase the heatmap color weight weight by zoom level
                // heatmap-intensity is a multiplier on top of heatmap-weight
                PropertyFactory.heatmapIntensity(
                    Expression.interpolate(
                        Expression.linear(),
                        Expression.zoom(),
                        Expression.stop(0, 1),
                        Expression.stop(9, 3)
                    )
                ), // Adjust the heatmap radius by zoom level
                PropertyFactory.heatmapRadius(
                    Expression.interpolate(
                        Expression.linear(),
                        Expression.zoom(),
                        Expression.stop(0, 2),
                        Expression.stop(9, 20)
                    )
                ), // Transition from heatmap to circle layer by zoom level
                PropertyFactory.heatmapOpacity(
                    Expression.interpolate(
                        Expression.linear(),
                        Expression.zoom(),
                        Expression.stop(7, 1),
                        Expression.stop(9, 0)
                    )
                )
            )
            return layer
        }


    private fun createCircleLayer1(): CircleLayer {
        val circleLayer = CircleLayer(CIRCLE_LAYER_ID, EARTHQUAKE_SOURCE_ID)
        circleLayer.setProperties( // Size circle radius by earthquake magnitude and zoom level
            PropertyFactory.circleRadius(
                Expression.interpolate(
                    Expression.linear(),
                    Expression.zoom(),
                    Expression.literal(7),
                    Expression.interpolate(
                        Expression.linear(),
                        Expression.get("mag"),
                        Expression.stop(1, 1),
                        Expression.stop(6, 4)
                    ),
                    Expression.literal(16),
                    Expression.interpolate(
                        Expression.linear(),
                        Expression.get("mag"),
                        Expression.stop(1, 5),
                        Expression.stop(6, 50)
                    )
                )
            ), // Color circle by earthquake magnitude
            PropertyFactory.circleColor(
                Expression.interpolate(
                    Expression.linear(), Expression.get("mag"),
                    Expression.literal(1), Expression.rgba(33, 102, 172, 0),
                    Expression.literal(2), Expression.rgb(103, 169, 207),
                    Expression.literal(3), Expression.rgb(209, 229, 240),
                    Expression.literal(4), Expression.rgb(253, 219, 199),
                    Expression.literal(5), Expression.rgb(239, 138, 98),
                    Expression.literal(6), Expression.rgb(178, 24, 43)
                )
            ), // Transition from heatmap to circle layer by zoom level
            PropertyFactory.circleOpacity(
                Expression.interpolate(
                    Expression.linear(),
                    Expression.zoom(),
                    Expression.stop(7, 0),
                    Expression.stop(8, 1)
                )
            ),
            PropertyFactory.circleStrokeColor("white"),
            PropertyFactory.circleStrokeWidth(1.0f)
        )
        return circleLayer
    }

    companion object {
        private const val SAVED_STATE_LOCATION = "saved_state_location"
        private const val TAG = "Mbgl-OnLineMapActivity"

        private const val EARTHQUAKE_SOURCE_URL =
            "https://www.mapbox.com/mapbox-gl-js/assets/earthquakes.geojson"
        private const val EARTHQUAKE_SOURCE_ID = "earthquakes"
        private const val EQ_HEATMAP_LAYER_SOURCE = "earthquakes"
        private const val EQ_HEATMAP_LAYER_ID = "earthquakes-heat"
        private const val CIRCLE_LAYER_ID="circle_layer_earthquakes"


        private val LAT_LNG_FORBIDEN_CITY = LatLng(39.91,116.39)
        private val LAT_LNG_XIANG_SHAN = LatLng(39.990246,116.189141)
    }
}