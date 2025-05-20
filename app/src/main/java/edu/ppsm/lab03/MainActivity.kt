package edu.ppsm.lab03

import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationListenerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity(), LocationListenerCompat{
    protected var locMan:LocationManager? = null
    protected var tvLon:TextView? = null;
    protected var tvLat:TextView? = null;
    protected var tvHgt:TextView? = null;


    override fun onCreate(savedInstanceState: Bundle?) {

        locMan = getSystemService(LOCATION_SERVICE) as LocationManager
        tvLon = findViewById(R.id.tvLon)
        tvLat = findViewById(R.id.tvLat)
        tvHgt = findViewById(R.id.tvHgt)



        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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
        v = location.longitude
        tvLat!!.text = formatPosition(Math.abs(v), if(v>=0)'N' else 'S')
        tvHgt!!.text = " %4.1f m".format(location.altitude)
    }
}