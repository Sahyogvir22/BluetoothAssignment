package com.example.bluetoothassignment

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.example.bluetoothassignment.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private lateinit var bluetoothEnableIntent: Intent
    var deviceList = ArrayList<String>()
    lateinit var deviceAdapter: ArrayAdapter<String>
    private var requestCode = 1
    private val REQUEST_BLUETOOTH_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bluetoothEnableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

        binding?.bluetoothSwitch?.setOnClickListener{
            if (binding?.bluetoothSwitch?.isChecked == true){
                if (bluetoothAdapter==null){
                    Toast.makeText(applicationContext, "Bluetooth not supported on this device", Toast.LENGTH_LONG).show()
                    binding?.bluetoothSwitch?.isChecked = false
                }
                else{
                    if (!bluetoothAdapter.isEnabled){
                        startActivityForResult(bluetoothEnableIntent,requestCode)
                    }
                }
            }
            else{
                if (bluetoothAdapter.isEnabled){
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestBluetoothPermissions()
                    }
                    bluetoothAdapter.disable()
                    binding?.availableDevicesTV?.visibility = View.GONE
                    binding?.bluetoothLV?.visibility = View.GONE
                }
            }
        }

        val intentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(broadcastReceiver,intentFilter)

        deviceAdapter = ArrayAdapter(applicationContext,R.layout.device_item,deviceList)
        binding?.bluetoothLV?.adapter = deviceAdapter

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==1){
            if (resultCode== RESULT_OK){
                binding?.availableDevicesTV?.visibility = View.VISIBLE
                binding?.bluetoothLV?.visibility = View.VISIBLE
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestBluetoothPermissions()
                }
                bluetoothAdapter.startDiscovery()
            }
            else if (resultCode== RESULT_CANCELED){
                binding?.bluetoothSwitch?.isChecked = false
            }
        }
    }

    private var broadcastReceiver : BroadcastReceiver = object : BroadcastReceiver(){

        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (BluetoothDevice.ACTION_FOUND == action){
                val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestBluetoothPermissions()
                }
                deviceList.add(device.name)
                deviceAdapter.notifyDataSetChanged()
            }
        }

    }

    private fun requestBluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), REQUEST_BLUETOOTH_PERMISSION)
        }
    }
}