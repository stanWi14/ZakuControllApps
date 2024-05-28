package com.example.menugrid

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.core.content.ContextCompat
import com.example.menugrid.ui.theme.BlueGato
import com.example.menugrid.ui.theme.MenuGridTheme

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothGatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private var connectionStatus = mutableStateOf("Disconnected")

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (bluetoothAdapter.isEnabled) {
                if (permissions.all { it.value }) {
                    startBLEScan()
                } else {
                    Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show()
            }
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MenuGridTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @Composable
    fun MainScreen() {
        var showMenu by remember { mutableStateOf(false) }
        if (showMenu) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            checkPermissions()
            ShowGridMenu(::sendData, connectionStatus)
        } else {
            MyOnboardingScreen { showMenu = true }
        }
    }

    private fun checkPermissions() {
        val requiredPermissions = arrayOf(
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT
        )

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        } else {
            startBLEScan()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startBLEScan() {
        bluetoothAdapter.bluetoothLeScanner.startScan(bleScanCallback)
        connectionStatus.value = "Scanning..."
    }

    private val bleScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if (result.device.name == "ESP_MASTER") {
                bluetoothAdapter.bluetoothLeScanner.stopScan(this)
                connectToDevice(result.device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            connectionStatus.value = "Scan failed"
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        connectionStatus.value = "Connecting to ${device.name}..."
        bluetoothGatt = device.connectGatt(this, false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            runOnUiThread {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    connectionStatus.value = "Connected to ${gatt?.device?.name}"
                    Toast.makeText(
                        this@MainActivity,
                        "Connected to ${gatt?.device?.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                    gatt?.discoverServices()
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    connectionStatus.value = "Disconnected"
                    Toast.makeText(
                        this@MainActivity,
                        "Disconnected from ${gatt?.device?.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt?.services?.forEach { service ->
                    if (service.uuid.toString() == "0000180a-0000-1000-8000-00805f9b34fb") {
                        service.characteristics.forEach { characteristic ->
                            if (characteristic.uuid.toString() == "00002a57-0000-1000-8000-00805f9b34fb") {
                                writeCharacteristic = characteristic
                                runOnUiThread {
                                    connectionStatus.value = "Ready to communicate"
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Ready to communicate",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendData(data: String) {
        writeCharacteristic?.let {
            it.setValue(data)
            bluetoothGatt?.writeCharacteristic(it)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}

@Composable
fun ShowGridMenu(sendData: (String) -> Unit, connectionStatus: MutableState<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = connectionStatus.value)
        Spacer(modifier = Modifier.height(16.dp))
        LazyVerticalGrid(
            modifier = Modifier
                .height(900.dp)
                .padding(16.dp),
            columns = GridCells.Adaptive(minSize = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                ChooseBoxFormat(
                    painterResource(R.drawable.menuledoff),
                    text = "Send Signal 1", value = "1", sendData = sendData
                )
            }
            item {
                ChooseBoxFormat(
                    painterResource(R.drawable.menusweep),
                    text = "Send Signal 2", value = "2", sendData = sendData
                )
            }
            item {
                ChooseBoxFormat(
                    painterResource(R.drawable.menusetting),
                    text = "Send Signal 3", value = "3", sendData = sendData
                )
            }
            item {
                ChooseBoxFormat(
                    painterResource(R.drawable.menusetting),
                    text = "Send Signal 4", value = "4", sendData = sendData
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ChooseBoxFormat(icon: Painter, text: String, value: String, sendData: (String) -> Unit) {
    Box(
        modifier = Modifier
            .wrapContentSize()
            .aspectRatio(1f)
            .border(
                1.dp,
                Color.Black,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
            .clickable { sendData(value) }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = "Icon",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text)
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun MyOnboardingScreen(onActivateZaku: () -> Unit) {
    var position by remember { mutableStateOf(0f) }
    val infiniteTransition = rememberInfiniteTransition()
    val animatedPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueGato),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))
            Text(
                text = "MS-06R-1A",
                color = Color.White,
                fontSize = 50.sp,
                fontFamily = FontFamily(Font(R.font.orbitronregular)),
            )
            Text(
                text = "ZAKU II",
                color = Color.White,
                fontSize = 75.sp,
                fontFamily = FontFamily(Font(R.font.orbitronextrabold)),
            )
            Text(
                text = "Controller App",
                color = Color.White,
                fontSize = 40.sp,
                fontFamily = FontFamily(Font(R.font.orbitronregular)),
            )
            Spacer(modifier = Modifier.height(75.dp))
            Row {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(70.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .width(250.dp)
                        .height(70.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .offset(x = animatedPosition * (250.dp - 60.dp))
                            .size(60.dp)
                            .background(Color.Red, shape = CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(70.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black),
                )
            }
            Spacer(modifier = Modifier.height(50.dp))
            Text(
                text = "[Activate Zaku]",
                color = Color.White,
                fontSize = 30.sp,
                fontFamily = FontFamily(Font(R.font.orbitronregular)),
                modifier = Modifier
                    .clickable { onActivateZaku() }
                    .padding(8.dp)
            )
        }
    }
}