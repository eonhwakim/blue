package com.example.mybluetoothkotlin


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mybluetoothkotlin.databinding.ActivityMainBinding
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val REQUEST_CODE_ENABLE_BT: Int = 1
    private val REQUEST_CODE_DISCOVERABLE_BT: Int = 2

    //val testTable = mutableListOf<BluetoothDevice>()
    private var devicesArr = ArrayList<BluetoothDevice>() //scan result ArrayList 를 만들어 리사이클러뷰에 띄우기 위해 list 추가
    private lateinit var recyclerViewAdapter : RecyclerViewAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager


    //bluetooth adapter
    lateinit var bAdapter: BluetoothAdapter

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()
//        val bluetoothStatusTv = findViewById<TextView>(R.id.bluetoothStatusTv)
//        val turnOnBtn = findViewById<Button>(R.id.turnOnBtn)
//        val turnOffBtn = findViewById<Button>(R.id.turnOffBtn)
//        val discoverableBtn = findViewById<Button>(R.id.discoverableBtn)
//        val pairedBtn = findViewById<Button>(R.id.pairedBtn)
//        val pairedTv =findViewById<TextView>(R.id.pairedTv)

        // 1. 블루투스 어댑터 가져오기
        val bAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        viewManager = LinearLayoutManager(this)
        recyclerViewAdapter =  RecyclerViewAdapter(devicesArr)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.layoutManager = viewManager


//check if bluetooth is available or not
        if(bAdapter == null){
            binding.bluetoothStatusTv.text = "Bluetooth is not available"
        }else{
            binding.bluetoothStatusTv.text = "Bluetooth is available"
        }

//turn on bluetooth
        binding.turnOnBtn.setOnClickListener {
            if(bAdapter!!.isEnabled){
                Toast.makeText(this, "already on", Toast.LENGTH_LONG).show()
            }else{
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(intent, REQUEST_CODE_ENABLE_BT)

            }
        }
//turn off bluetooth
        binding.turnOffBtn.setOnClickListener {
            if(!bAdapter!!.isEnabled){
                Toast.makeText(this, "already off", Toast.LENGTH_LONG).show()
            }else{
                bAdapter!!.disable()
                Toast.makeText(this, "bluetooth turned off", Toast.LENGTH_LONG).show()
            }
        }
// 기기 검색
        binding.discoverableBtn.setOnClickListener {
            if(!bAdapter!!.isDiscovering){
                Toast.makeText(this, "making your device discoverable", Toast.LENGTH_LONG).show()
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                startActivityForResult(intent, REQUEST_CODE_DISCOVERABLE_BT)
            }
        }
// 페어링 된 기기 찾는 것
        binding.pairedBtn.setOnClickListener {
            if(bAdapter!!.isEnabled){
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

// 주변 기기 찾는 코드 !!
        // Register for broadcasts when a device is discovered.

                                     
        binding.scanBtn.setOnClickListener {
            bAdapter?.startDiscovery()                        //기기 탐색

            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(receiver, filter)
            }
        }
//onCreate end----------------------------

    private val receiver = object : BroadcastReceiver() {
         @SuppressLint("MissingPermission")
         override fun onReceive(context: Context, intent: Intent) {
             Log.d("Test","들어옴")

             val action = intent.action
             when (action) {
                   BluetoothDevice.ACTION_FOUND -> {
                       val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                       Log.d("device","device add")
                       if (device != null) {
                           devicesArr.add(device)
//
//                           for(i in testTable) {
//                               Log.d("Test","dfList add 성공")
//
//                               binding.scanTv.append(i.toString())
//                           }
//
                       }


//                       if (device != null) {
//                           testTable.addAll(listOf(device))
//                           Log.d("Test","List add 성공")
//                           Log.d("Test", "$testTable")
//
//                           //binding.pairedTv.append("\nDevice : $deviceName2 , $deviceAddress2")
//                           Log.d("Test","List add 성공")
//                       }

                   }
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

    @RequiresApi(Build.VERSION_CODES.M)
    fun checkPermissions(){
        requestPermissions(arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE
        ),2)
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
            val itemAddress: TextView = holder.linearView.findViewById(R.id.item_address)
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