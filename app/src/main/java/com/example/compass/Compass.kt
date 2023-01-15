package com.example.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView

class Compass(context:Context) : SensorEventListener {

    private var sensorManager:SensorManager
    private var gSensor:Sensor
    private var mSensor:Sensor
    private val mGravity=FloatArray(3)
    private val mGeomagnetic=FloatArray(3)
    private var azimuth=0f
    private var correctAzimuth=0f

    init {
        sensorManager=context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gSensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mSensor=sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    companion object { private val TAG="Compass" }

    fun start(){
        sensorManager.registerListener(this, gSensor, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop(){ sensorManager.unregisterListener(this) }

    override fun onSensorChanged(event: SensorEvent?) {
        val alpha=0.97f

        synchronized(this){
            if(event?.sensor?.type==Sensor.TYPE_ACCELEROMETER){
                mGravity[0]=alpha*mGravity[0]+(1-alpha) * event.values[0]
                mGravity[1]=alpha*mGravity[1]+(1-alpha) * event.values[1]
                mGravity[2]=alpha*mGravity[2]+(1-alpha) * event.values[2]
            }

            if(event?.sensor?.type==Sensor.TYPE_MAGNETIC_FIELD){
                mGeomagnetic[0]=alpha*mGeomagnetic[0]+(1-alpha) * event.values[0]
                mGeomagnetic[1]=alpha*mGeomagnetic[1]+(1-alpha) * event.values[1]
                mGeomagnetic[2]=alpha*mGeomagnetic[2]+(1-alpha) * event.values[2]
            }

            val R=FloatArray(9)
            val I=FloatArray(9)
            val success=SensorManager.getRotationMatrix(R,I,mGravity,mGeomagnetic)
            if(success){
                val orientation=FloatArray(3)
                SensorManager.getOrientation(R,orientation)
                azimuth=Math.toDegrees(orientation[0].toDouble()).toFloat()
                azimuth=(azimuth+360)%360
                adjustArrow()
            }
        }
    }

    var arrowView: ImageView? = null
    private fun adjustArrow() {
        if(arrowView==null)
            return

        val anim=RotateAnimation(
            -correctAzimuth,-azimuth,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f)
        correctAzimuth=azimuth
        anim.duration=500
        anim.repeatCount = 0
        anim.fillAfter = true

        arrowView!!.startAnimation(anim)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {  }
}
