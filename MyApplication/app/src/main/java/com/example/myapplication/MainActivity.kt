package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList


class MainActivity : AppCompatActivity() {
    private val REQUEST_ENABLE_BT = 1

    //리사이클러뷰
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var recyclerViewAdapter : RecyclerViewAdapter

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var scanning: Boolean = false
    private var devicesArr = ArrayList<BluetoothDevice>()
    private val SCAN_PERIOD = 1000
    private val handler = Handler()



// Scan Callback ----------------------------------------------
//    private val mLeScanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    object : ScanCallback() {
//        override fun onScanFailed(errorCode: Int) {
//            super.onScanFailed(errorCode)
//            Log.d("scanCallback", "Scan Failed : " + errorCode)
//        }
//
//        @SuppressLint("MissingPermission")
//        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
//            super.onBatchScanResults(results)
//            results?.let{
//                // results is not null
//                for (result in it){
//                    if (!devicesArr.contains(result.device) && result.device.name!=null) devicesArr.add(result.device)
//                }
//
//            }
//        }
//
//        @SuppressLint("MissingPermission")
//        override fun onScanResult(callbackType: Int, result: ScanResult?) {
//            super.onScanResult(callbackType, result)
//            result?.let {
//                // result is not null
//                if (!devicesArr.contains(it.device) && it.device.name!=null) devicesArr.add(it.device)
//            }
//        }
//
//    }
//ble 스캔    ----------------------------------------------------------------------------------------
//    @SuppressLint("MissingPermission")
//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    private fun scanDevice(state:Boolean) = if(state){
//        handler.postDelayed({ //절대로 반복해서 스캔해서는 안 되고, 스캔에 시간 제한을 설정해야 한다
//            scanning = false
//            bluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
//        }, SCAN_PERIOD)
//        scanning = true
//        devicesArr.clear()
//        bluetoothAdapter?.bluetoothLeScanner?.startScan(mLeScanCallback)
//    }else{
//        scanning = false
//        bluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
//    }
//
////permission 확인-------------------------------------------------------------------------------------
//    private fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
//            for (permission in permissions) {
//                if (ActivityCompat.checkSelfPermission(context, permission)
//                    != PackageManager.PERMISSION_GRANTED) {
//                    return false
//                }
//            }
//        }
//        return true
//    }
//    // Permission check
//    @RequiresApi(Build.VERSION_CODES.M)
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String?>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            REQUEST_ALL_PERMISSION -> {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
//                } else {
//                    requestPermissions(PERMISSIONS, REQUEST_ALL_PERMISSION)
//                    Toast.makeText(this, "Permissions must be granted", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }

//onCreate start-----------------------------------------------------------------------------------
    @SuppressLint("MissingPermission")
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //initialize
        val turnOnBtn: Button = findViewById(R.id.turnOnBtn)
        val turnOffBtn: Button = findViewById(R.id.turnOffBtn)
        val scanBtn: Button = findViewById(R.id.scanBtn)
        val bluetoothStatusTv: TextView = findViewById(R.id.bluetoothStatusTv)
        // 블루투스 어댑터 가져오기
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()



//check if bluetooth is available or not
        if(bluetoothAdapter == null){
            bluetoothStatusTv.text = "Bluetooth is not available"
        }else{
            bluetoothStatusTv.text = "Bluetooth is available"
        }

// 리사이클러뷰
        viewManager = LinearLayoutManager(this)
        recyclerViewAdapter = RecyclerViewAdapter(devicesArr)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.layoutManager = viewManager

        checkPermissions()
// Bluetooth on
        turnOnBtn.setOnClickListener {
            if(bluetoothAdapter?.isEnabled == true){
                Toast.makeText(this, "already on", Toast.LENGTH_LONG).show()
            }else{
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(intent, REQUEST_ENABLE_BT)

            }
        }
// Bluetooth off
        turnOffBtn.setOnClickListener {
            if(bluetoothAdapter?.isEnabled == false){
                Toast.makeText(this, "already off", Toast.LENGTH_LONG).show()
            }else{
                bluetoothAdapter?.disable()
                Toast.makeText(this, "bluetooth turned off", Toast.LENGTH_LONG).show()

                devicesArr.clear()
                recyclerViewAdapter.notifyDataSetChanged()
            }
        }
//ble Bluetooth scan
    // 1. permission 검사함수를 통해 필요 permission 을 요청한 후
    // 2. scanDevice(true) 을 통해 블루투스 디바이스 스캔을 실행
//        scanBtn.setOnClickListener { v: View? -> // Scan Button Onclick
//            if (!hasPermissions(this, PERMISSIONS)) {
//                requestPermissions(PERMISSIONS, REQUEST_ALL_PERMISSION)
//            }
//            scanDevice(true) //잘못된 코드!! 퍼미션 허용 상관없이 스캔이 돼버림 !
//        }
        scanBtn.setOnClickListener {
            bluetoothAdapter?.startDiscovery()						//기기 탐색

            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(receiver, filter)					//탐색 결과 리시버

            recyclerViewAdapter.notifyDataSetChanged()	//recyclerView itemUpdate
        }

    }
//onCreate end--------------------------------------------------------------------------------------
    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(context : Context, intent : Intent){
            val action = intent.action
            when(action){
                BluetoothDevice.ACTION_FOUND -> {
                    val device : BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!

                    devicesArr.add(device)	//recyclerView item추가
                }
            }
        }
    }
    class RecyclerViewAdapter(private  val myDataset: ArrayList<BluetoothDevice>):
            RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>() {
                class MyViewHolder(val linearView: LinearLayout):RecyclerView.ViewHolder(linearView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            //create a new view
            val linearView = LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_item, parent, false) as LinearLayout
            return MyViewHolder(linearView)
        }

        @SuppressLint("MissingPermission")
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val itemName : TextView = holder.linearView.findViewById(R.id.item_name)
            val itemAddress : TextView = holder.linearView.findViewById(R.id.item_address)
            itemName.text = myDataset[position].name
            itemAddress.text = myDataset[position].address
        }

        override fun getItemCount(): Int {
            return myDataset.size
        }
   }

    @RequiresApi(Build.VERSION_CODES.M)
    fun checkPermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            requestPermissions(arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT
            ),1)
        }else{
            requestPermissions(arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_FINE_LOCATION

            ),1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()


        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
    }


}

private fun Handler.postDelayed(function: () -> Unit?, scanPeriod: Int) {

}

