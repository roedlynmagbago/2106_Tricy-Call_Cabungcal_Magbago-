package com.example.nasugbutricycleapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.nasugbutricycleapp.gis.GISManager
import com.example.nasugbutricycleapp.service.AuthService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Point

class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mapLibreMap: MapLibreMap? = null

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val fineGranted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseGranted =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (fineGranted || coarseGranted) {
                getCurrentLocation()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapLibre.getInstance(this)
        setContentView(R.layout.map)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)

        setupProfileAndNavigation()

        findViewById<FloatingActionButton>(R.id.btn_current_location)?.setOnClickListener {
            checkLocationPermission()
        }

        mapView.getMapAsync { map ->
            mapLibreMap = map
            map.setPadding(0, 160, 0, 220)

            val nasugbu = LatLng(14.0669, 120.6318)

            map.cameraPosition = CameraPosition.Builder()
                .target(nasugbu)
                .zoom(13.0)
                .build()

            map.setStyle(
                "https://tiles.openfreemap.org/styles/liberty"
            ) {
                // Load your GIS layers after the map style is ready.
                GISManager.loadGISLayers(map)

                // Ask for location permission after the map is ready.
                checkLocationPermission()
            }
        }
    }

    private fun setupProfileAndNavigation() {
        val tvHeaderUserName = findViewById<TextView>(R.id.tv_header_user_name)
        val ivProfile = findViewById<ImageView>(R.id.iv_profile)
        val btnBook = findViewById<MaterialButton>(R.id.btn_book)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        AuthService.getCurrentUser { user, _ ->
            runOnUiThread {
                if (user != null) {
                    val displayName = user.name.ifBlank {
                        user.username.ifBlank {
                            user.email.substringBefore("@")
                        }
                    }
                    if (tvHeaderUserName != null && displayName.isNotBlank()) {
                        tvHeaderUserName.text = "Welcome, $displayName"
                    }
                }
            }
        }

        btnBook?.setOnClickListener {
            startActivity(Intent(this, BookRideActivity::class.java))
        }

        ivProfile?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.nav_book -> {
                    startActivity(Intent(this, BookRideActivity::class.java))
                    true
                }
                R.id.nav_home -> {
                    true
                }
                else -> true
            }
        }
    }

    private fun checkLocationPermission() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            getCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun getCurrentLocation() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) return

        val map = mapLibreMap ?: return
        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            if (location == null || isFinishing || isDestroyed) return@addOnSuccessListener

            val currentLocation = LatLng(
                location.latitude,
                location.longitude
            )

            map.cameraPosition = CameraPosition.Builder()
                .target(currentLocation)
                .zoom(16.0)
                .build()

            map.getStyle { style ->
                val point = Point.fromLngLat(
                    location.longitude,
                    location.latitude
                )

                val sourceId = "current-location-source"
                val layerId = "current-location-layer"

                // Avoid adding duplicate sources or layers.
                if (style.getSource(sourceId) == null) {
                    style.addSource(
                        GeoJsonSource(sourceId, point)
                    )
                } else {
                    (style.getSource(sourceId) as? GeoJsonSource)
                        ?.setGeoJson(point)
                }

                if (style.getLayer(layerId) == null) {
                    val layer = CircleLayer(
                        layerId,
                        sourceId
                    ).withProperties(
                        PropertyFactory.circleRadius(9f),
                        PropertyFactory.circleColor("#00BCD4"),
                        PropertyFactory.circleStrokeWidth(3f),
                        PropertyFactory.circleStrokeColor("#FFFFFF")
                    )

                    style.addLayer(layer)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }
}
