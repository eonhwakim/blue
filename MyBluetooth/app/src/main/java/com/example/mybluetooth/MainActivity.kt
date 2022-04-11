package com.example.mybluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {
    private val REQUEST_ENABLE_BT = 1

    private var devicesArr = ArrayList<BluetoothDevice>() //scan result ArrayList 를 만들어 리사이클러뷰에 띄우기 위해 list 추가

    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var recyclerViewAdapter : RecyclerViewAdapter



// 권한 요청------------------------------------------------------------------------------------------

    fun checkPermissions(){
        requestPermissions(arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT
        ),1)
    }
//onCreate start------------------------------------------------------------------------------------

    @SuppressLint("MissingPermission")
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnBluetoothOn: Button = findViewById(R.id.btnBluetoothOn)
        val btnBluetoothOff: Button = findViewById(R.id.btnBluetoothOff)
        val deviceBtn: Button = findViewById(R.id.deviceBtn)

        checkPermissions()


        //var bluetoothAdapter: BluetoothAdapter? = null
// 1. 블루투스 어댑터 가져오기
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

        viewManager = LinearLayoutManager(this)
        recyclerViewAdapter =  RecyclerViewAdapter(devicesArr)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.layoutManager = viewManager


// Bluetooth on
        btnBluetoothOn.setOnClickListener {
            if(bluetoothAdapter!!.isEnabled){
                Toast.makeText(this, "already on", Toast.LENGTH_LONG).show()
            }else{
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(intent, REQUEST_ENABLE_BT)

            }
        }
// Bluetooth off
        btnBluetoothOff.setOnClickListener {
            if(!bluetoothAdapter!!.isEnabled){
                Toast.makeText(this, "already off", Toast.LENGTH_LONG).show()
            }else{
                bluetoothAdapter!!.disable()
                Toast.makeText(this, "bluetooth turned off", Toast.LENGTH_LONG).show()
            }
        }

// Bluetooth discovering
        deviceBtn.setOnClickListener {
            bluetoothAdapter?.startDiscovery()                        //기기 탐색

            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(receiver, filter)

            recyclerViewAdapter.notifyDataSetChanged()
        }


    }
//onCreate end--------------------------------------------------------------------------------------

    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(context : Context, intent : Intent){
            val action = intent.action
            when(action){
                BluetoothDevice.ACTION_FOUND -> {
                    val device : BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        devicesArr.add(device)
                        Log.d("Test","List add 성공")
                        Log.d("Test", "$devicesArr")
                    }
                }
            }
        }
    }



    class RecyclerViewAdapter(private val myDataset: ArrayList<BluetoothDevice>) :
        RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>() {

        class MyViewHolder(val linearView: LinearLayout) : RecyclerView.ViewHolder(linearView)

        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): RecyclerViewAdapter.MyViewHolder {
            // create a new view
            val linearView = LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_item, parent, false) as LinearLayout
            return MyViewHolder(linearView)
        }

        // Replace the contents of a view (invoked by the layout manager)
        @SuppressLint("MissingPermission")
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val itemName: TextView = holder.linearView.findViewById(R.id.item_name)
            val itemAddress:TextView = holder.linearView.findViewById(R.id.item_address)
            itemName.text = myDataset[position].name
            itemAddress.text = myDataset[position].address
        }

        override fun getItemCount() = myDataset.size
    }


    override fun onDestroy() {
        super.onDestroy()


        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
    }
}


