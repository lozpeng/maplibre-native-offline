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
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
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
    companion object {
        private const val SAVED_STATE_LOCATION = "saved_state_location"
        private const val TAG = "Mbgl-OnLineMapActivity"

        private val LAT_LNG_FORBIDEN_CITY = LatLng(39.91,116.39)
        private val LAT_LNG_XIANG_SHAN = LatLng(39.990246,116.189141)
    }
}