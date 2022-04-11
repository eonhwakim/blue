package com.example.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity




class MainActivity : AppCompatActivity() {
    private val REQUEST_ENABLE_BT=1
    private var mBluetoothAdapter: BluetoothAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnBluetoothOff: Button = findViewById(R.id.btnBluetoothOff)
        val btnBluetoothOn: Button = findViewById(R.id.btnBluetoothOn)
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        //1. Bluetooth on/off
        btnBluetoothOn.setOnClickListener {
            blueToothOn()
        }

        btnBluetoothOff.setOnClickListener {
            blueToothOff()
        }



    }


    private fun blueToothOn() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(
                applicationContext,
                "This device doesn't support bluetooth service",
                Toast.LENGTH_SHORT
            ).show()
        }
        if (mBluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
        //        } else if (mBluetoothAdapter?.isEnabled() == true) {
//            Toast.makeText(applicationContext, "Already On", Toast.LENGTH_SHORT).show()
//        } else {
//            //Toast.makeText(getApplicationContext(), "Bluetooth On",Toast.LENGTH_SHORT).show();
//            //Ask user
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            startActivityForResult(enableBtIntent!!, REQUEST_ENABLE_BT)
//            //Intercept Status changed (by.broadcast)
//            val BTIntent = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
//            registerReceiver(mBroadCastReceiver, BTIntent)
//        }
    }

    private fun blueToothOff() {
        if (!mBluetoothAdapter!!.isEnabled) {
            Toast.makeText(applicationContext, "Already OFF", Toast.LENGTH_SHORT).show()
        } else if (mBluetoothAdapter?.isEnabled() == true) {
            //Toast.makeText(getApplicationContext(), "Bluetooth Off",Toast.LENGTH_SHORT).show();
            mBluetoothAdapter.disable()
        }
    }

    // Create a BroadcastReceiver
    private val mBroadCastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_ON -> {
                        Toast.makeText(applicationContext, "Bluetooth On", Toast.LENGTH_SHORT)
                            .show()
                    }
                    BluetoothAdapter.STATE_OFF -> {
                        Toast.makeText(applicationContext, "Bluetooth Off", Toast.LENGTH_SHORT)
                            .show()
                    }
                    BluetoothAdapter.STATE_TURNING_ON -> Toast.makeText(
                        applicationContext,
                        "Bluetooth turning On",
                        Toast.LENGTH_SHORT
                    ).show()
                    BluetoothAdapter.STATE_TURNING_OFF -> Toast.makeText(
                        applicationContext,
                        "Bluetooth turning Off",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } //end if
        } //end onReceive
    }

    override fun onDestroy() {
        Toast.makeText(applicationContext, "onDestroy called", Toast.LENGTH_SHORT).show()
        Log.d("onDestroy", "onDestroy called")
        super.onDestroy()
        unregisterReceiver(mBroadCastReceiver)
    }
}
