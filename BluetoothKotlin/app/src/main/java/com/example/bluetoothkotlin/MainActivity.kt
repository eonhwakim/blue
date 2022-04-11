package com.example.bluetoothkotlin

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.bluetoothkotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val REQUEST_CODE_ENABLE_BT: Int = 1
    private val REQUEST_CODE_DISCOVERABLE_BT: Int = 2

    //bluetooth adapter
    lateinit var bAdapter: BluetoothAdapter

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


//        val bluetoothStatusTv = findViewById<TextView>(R.id.bluetoothStatusTv)
//        val turnOnBtn = findViewById<Button>(R.id.turnOnBtn)
//        val turnOffBtn = findViewById<Button>(R.id.turnOffBtn)
//        val discoverableBtn = findViewById<Button>(R.id.discoverableBtn)
//        val pairedBtn = findViewById<Button>(R.id.pairedBtn)
//        val pairedTv =findViewById<TextView>(R.id.pairedTv)

        //init bluetooth adapter
        bAdapter = BluetoothAdapter.getDefaultAdapter()
        //check if bluetooth is available or not
        if(bAdapter == null){
            binding.bluetoothStatusTv.text = "Bluetooth is not available"
        }else{
            binding.bluetoothStatusTv.text = "Bluetooth is available"
        }

        //turn on bluetooth
        binding.turnOnBtn.setOnClickListener {
            if(bAdapter.isEnabled){
                Toast.makeText(this, "already on", Toast.LENGTH_LONG).show()
            }else{
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(intent, REQUEST_CODE_ENABLE_BT)

            }
        }
        //turn off bluetooth
        binding.turnOffBtn.setOnClickListener {
            if(!bAdapter.isEnabled){
                Toast.makeText(this, "already off", Toast.LENGTH_LONG).show()
            }else{
                bAdapter.disable()
                Toast.makeText(this, "bluetooth turned off", Toast.LENGTH_LONG).show()
            }
        }
        //discoverable bluetooth
        binding.discoverableBtn.setOnClickListener {
            if(!bAdapter.isDiscovering){
                Toast.makeText(this, "making your device discoverable", Toast.LENGTH_LONG).show()
                var intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                startActivityForResult(intent, REQUEST_CODE_DISCOVERABLE_BT)
            }
        }
        binding.pairedBtn.setOnClickListener {
            if(bAdapter.isEnabled){
                binding.pairedTv.text = "Paired Devices"

                val devices = bAdapter.bondedDevices
                for(device in devices){
                    val deviceName = device.name
                    val deviceAddress = device
                    binding.pairedTv.append("\nDevice : $deviceName , $deviceAddress")
                }
            }else{
                Toast.makeText(this, "turn on bluetooth first", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            REQUEST_CODE_ENABLE_BT ->
                if(requestCode == Activity.RESULT_OK){
                    Toast.makeText(this, "Bluetooth is on",Toast.LENGTH_LONG    ).show()
                }else{
                    Toast.makeText(this, "could not on bluetooth",Toast.LENGTH_LONG    ).show()
                }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}