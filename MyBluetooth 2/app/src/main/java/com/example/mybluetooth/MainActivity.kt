package com.example.mybluetooth

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
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {
    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_PERMISSIONS= 2
    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    //device scan
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var scanning: Boolean = false
    private val SCAN_PERIOD = 1000
    private val handler = Handler()
    private var devicesArr = ArrayList<BluetoothDevice>() //scan result ArrayList 를 만들어 리사이클러뷰에 띄우기 위해 list 추가

    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var recyclerViewAdapter : RecyclerViewAdapter




// 권한 요청------------------------------------------------------------------------------------------

    fun checkPermissions(){
        Log.d("permission", "퍼미션 체크")
        requestPermissions(arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT
        ),1)
    }


    private fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    // Permission 확인
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSIONS -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
                } else {
                    requestPermissions(permissions, REQUEST_PERMISSIONS)
                    Toast.makeText(this, "Permissions must be granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 스캔 콜백 함수
    private val mLeScanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object: ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d("scanCallback", "BLE Scan Failed : " + errorCode)
        }
        @SuppressLint("MissingPermission")
        override fun onBatchScanResults(results: MutableList<ScanResult> ?) {
            super.onBatchScanResults(results)
            results?.let {
                // results is not null
                for(result in it) {
                    if(!devicesArr.contains(result.device) && result.device.name!=null) devicesArr.add(result.device)
                }
            }
        }
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let {
                // result is not null
                if(!devicesArr.contains(it.device) && it.device.name!=null) devicesArr.add(it.device)
                recyclerViewAdapter.notifyDataSetChanged()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun scanDevice(state:Boolean) = if(state) {
        handler.postDelayed({
            scanning = false
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
        }, SCAN_PERIOD)
        scanning = true
        devicesArr.clear()
        bluetoothAdapter?.bluetoothLeScanner?.startScan(mLeScanCallback)
    }
    else {
        scanning = false
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
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
//            bluetoothAdapter?.startDiscovery()                        //기기 탐색
//
//            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
//            registerReceiver(receiver, filter)
//
//            recyclerViewAdapter.notifyDataSetChanged()
                v: View? ->// Scan Button Onclick
            if(!hasPermissions(this, PERMISSIONS)) {
                requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS)
            }
            scanDevice(true)
        }
    }
//onCreate end--------------------------------------------------------------------------------------

//    private val receiver = object : BroadcastReceiver(){
//        override fun onReceive(context : Context, intent : Intent){
//            val action = intent.action
//            when(action){
//                BluetoothDevice.ACTION_FOUND -> {
//                    val device : BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
//                    if (device != null) {
//                        devicesArr.add(device)
//                        Log.d("Test","List add 성공")
//                        Log.d("Test", "$devicesArr")
//                    }
//                }
//            }
//        }
//    }



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



}

fun Handler.postDelayed(function: () -> Unit, scanPeriod: Int) {

}


