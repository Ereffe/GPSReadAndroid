package edu.ppsm.lab03

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationListenerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity(), LocationListenerCompat{
    protected var locMan:LocationManager? = null
    protected var tvLon:TextView? = null
    protected var tvLat:TextView? = null
    protected var tvHgt:TextView? = null

    protected var compass:CCompass? = null
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    companion object { private var sensorManager: SensorManager? = null}

    private val  sensorListener: SensorEventListener = object : SensorEventListener {
        var gValue: FloatArray? = FloatArray(3)
        var mValue: FloatArray? = FloatArray(3)
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        override fun onSensorChanged(event: SensorEvent?) {
            if(event!!.sensor.type == Sensor.TYPE_ACCELEROMETER) gValue = event.values.clone()
            if(event!!.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) mValue = event.values.clone()
            if (gValue!= null && mValue != null){
                val R = FloatArray(9)
                val success = SensorManager.getRotationMatrix(R,null,gValue,mValue)
                if(success){
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(R,orientation)
                    compass!!.updateData(orientation[0])
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        isGranted: Boolean ->
        if(isGranted){
            Toast.makeText(applicationContext, R.string.permissionGranted,Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            return
        }
        locMan!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,500L,20f,this)
        sensorManager!!.registerListener(sensorListener,accelerometer,SensorManager.SENSOR_DELAY_NORMAL,SensorManager.SENSOR_DELAY_UI)
        sensorManager!!.registerListener(sensorListener,magnetometer,SensorManager.SENSOR_DELAY_NORMAL,SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        locMan!!.removeUpdates(this)
        sensorManager!!.unregisterListener(sensorListener,accelerometer)
        sensorManager!!.unregisterListener(sensorListener,magnetometer)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        locMan = getSystemService(LOCATION_SERVICE) as LocationManager
        tvLon = findViewById(R.id.tvLon)
        tvLat = findViewById(R.id.tvLat)
        tvHgt = findViewById(R.id.tvHgt)

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        compass = CCompass(this)
        val par = LinearLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        compass!!.layoutParams = par
        val compassTitle = TextView(this)
        compassTitle.setText(R.string.compassText)
        compassTitle.layoutParams = par
        val layout = findViewById<LinearLayout>(R.id.internalLayout)
        layout.addView(compassTitle)
        layout.addView(compass)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)


    }


    private fun formatPosition(value: Double, loc:Char):String {
        val deg = value.toInt()
        val min = ((value-deg)*60).toInt()
        val sec = (value-deg-min/60)*3600
        return "%3d\u00b0 %2d' %5.3f\" %c".format(deg,min,sec,loc)
    }

    override fun onLocationChanged(location: Location) {
        var v = location.longitude
        tvLon!!.text = formatPosition(Math.abs(v), if(v>=0)'E' else 'W')
        v = location.latitude
        tvLat!!.text = formatPosition(Math.abs(v), if(v>=0)'N' else 'S')
        tvHgt!!.text = " %4.1f m".format(location.altitude)
    }
}