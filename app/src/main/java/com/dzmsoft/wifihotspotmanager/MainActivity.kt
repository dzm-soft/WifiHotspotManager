package com.dzmsoft.wifihotspotmanager

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dzmsoft.wifihotspotmanager.ui.theme.WifiHotspotManagerTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    private val requestCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")
        setContent {
            WifiHotspotManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    HotspotConfigScreen()
                }
            }
        }

        // 检查并请求权限
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val requiredPermissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_SETTINGS
        )

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), requestCode)
        }
    }

    @Composable
    fun HotspotConfigScreen() {
        val context = LocalContext.current
        val sharedPreferences = context.getSharedPreferences("HotspotPrefs", Context.MODE_PRIVATE)
        val defaultHotspotName = "wifi-${Random.nextInt(1000, 9999)}"
        var hotspotName by remember { mutableStateOf(sharedPreferences.getString("hotspotName", defaultHotspotName) ?: defaultHotspotName) }
        var hotspotPassword by remember { mutableStateOf(sharedPreferences.getString("hotspotPassword", hotspotName.reversed()) ?: hotspotName.reversed()) }
        var autoStart by remember { mutableStateOf(sharedPreferences.getBoolean("autoStart", false)) }
        var hotspotEnabled by remember { mutableStateOf(sharedPreferences.getBoolean("hotspotEnabled", false)) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = hotspotName,
                onValueChange = {
                    hotspotName = it
                    hotspotPassword = it.reversed() // 将hotspotName倒序作为hotspotPassword
                    sharedPreferences.edit().putString("hotspotName", hotspotName).apply()
                    sharedPreferences.edit().putString("hotspotPassword", hotspotPassword).apply()
                },
                label = { Text("热点名称") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = hotspotPassword,
                onValueChange = {
                    hotspotPassword = it
                    sharedPreferences.edit().putString("hotspotPassword", hotspotPassword).apply()
                },
                label = { Text("热点密码") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = autoStart,
                    onCheckedChange = {
                        autoStart = it
                        sharedPreferences.edit().putBoolean("autoStart", autoStart).apply()
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Text("开机启动")

                Spacer(modifier = Modifier.height(16.dp))
                Switch(
                    checked = hotspotEnabled,
                    onCheckedChange = {
                        hotspotEnabled = it
                        sharedPreferences.edit().putBoolean("hotspotEnabled", hotspotEnabled).apply()
                        // 确保在启动服务之前更新SharedPreferences
                        sharedPreferences.edit().putString("hotspotName", hotspotName).apply()
                        sharedPreferences.edit().putString("hotspotPassword", hotspotPassword).apply()
                        val serviceIntent = Intent(context, HotspotService::class.java)
                        if (hotspotEnabled) {
                            context.startService(serviceIntent)
                        } else {
                            context.stopService(serviceIntent)
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Text("设置热点")
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun HotspotConfigScreenPreview() {
        WifiHotspotManagerTheme {
            HotspotConfigScreen()
        }
    }
}