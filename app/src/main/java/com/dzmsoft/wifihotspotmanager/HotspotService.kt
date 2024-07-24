package com.dzmsoft.wifihotspotmanager

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.Manifest
import java.lang.reflect.Method

class HotspotService : Service() {

    override fun onCreate() {
        super.onCreate()
        Log.d("HotspotService", "服务已启动")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sharedPreferences = getSharedPreferences("HotspotPrefs", Context.MODE_PRIVATE)
        val hotspotName = sharedPreferences.getString("hotspotName", "")
        val hotspotPassword = sharedPreferences.getString("hotspotPassword", "")
        val hotspotEnabled = sharedPreferences.getBoolean("hotspotEnabled", false)
        Log.d("HotspotService", "热点名称: $hotspotName, 热点密码: $hotspotPassword, 启用热点: $hotspotEnabled")

        if (hotspotEnabled) {
            // 设置WiFi热点
            setWifiHotspot(hotspotName, hotspotPassword)
        } else {
            // 关闭WiFi热点
            stopWifiHotspot()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopWifiHotspot()
        Log.d("HotspotService", "服务已销毁")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun setWifiHotspot(ssid: String?, password: String?) {
        if (ssid.isNullOrEmpty() || password.isNullOrEmpty()) {
            Log.e("HotspotService", "SSID或密码为空，无法设置热点")
            return
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("HotspotService", "缺少必要的权限")
            return
        }

        try {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiConfig = WifiConfiguration().apply {
                SSID = ssid
                preSharedKey = password
                allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA2_PSK)
                // 强制设置为2.4GHz频段
                val apBandField = WifiConfiguration::class.java.getDeclaredField("apBand")
                apBandField.isAccessible = true
                apBandField.setInt(this, 0) // 0表示2.4GHz频段
            }

            val method: Method = wifiManager.javaClass.getDeclaredMethod("startSoftAp", WifiConfiguration::class.java)
            method.isAccessible = true
            method.invoke(wifiManager, wifiConfig)
            Log.d("HotspotService", "WiFi热点已启动: SSID=$ssid, 密码=$password")
        } catch (e: Exception) {
            Log.e("HotspotService", "设置WiFi热点失败", e)
        }
    }

    private fun stopWifiHotspot() {
        try {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val method: Method = wifiManager.javaClass.getDeclaredMethod("stopSoftAp")
            method.isAccessible = true
            method.invoke(wifiManager)
            Log.d("HotspotService", "WiFi热点已关闭")
        } catch (e: Exception) {
            Log.e("HotspotService", "关闭WiFi热点失败", e)
        }
    }
}