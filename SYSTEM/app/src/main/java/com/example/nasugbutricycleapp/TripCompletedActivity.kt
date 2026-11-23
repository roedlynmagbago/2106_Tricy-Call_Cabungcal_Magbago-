package com.example.nasugbutricycleapp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.nasugbutricycleapp.gis.GISManager
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

class TripCompletedActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var mapLibreMap: MapLibreMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapLibre.getInstance(this)
        setContentView(R.layout.activity_trip_completed)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        // Setup interactive map view
        mapView.getMapAsync { map ->
            mapLibreMap = map

            // Focus map on Nasugbu Central Town route
            val nasugbuTown = LatLng(14.072, 120.633)

            map.cameraPosition = CameraPosition.Builder()
                .target(nasugbuTown)
                .zoom(14.5)
                .build()

            map.setStyle("https://tiles.openfreemap.org/styles/liberty") {
                GISManager.loadGISLayers(map)
            }
        }

        setupHeaderAndNavigation()
    }

    private fun setupHeaderAndNavigation() {
        val bottomNav = findViewById<LinearLayout>(R.id.bottom_nav)
        val navHome = bottomNav?.getChildAt(0)
        val navBookRide = bottomNav?.getChildAt(1)
        val navProfile = bottomNav?.getChildAt(2)

        navHome?.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
            finish()
        }

        navBookRide?.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
            finish()
        }

        navProfile?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
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
