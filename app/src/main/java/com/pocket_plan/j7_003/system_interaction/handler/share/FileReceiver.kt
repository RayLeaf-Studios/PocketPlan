package com.pocket_plan.j7_003.system_interaction.handler.share

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.system_interaction.handler.notifications.NotificationHandler
import kotlinx.android.synthetic.main.fragment_settings_backup.*
import java.io.File
import java.io.FileNotFoundException
import java.lang.Exception

class FileReceiver : Activity() {
    private val lifeCycleCallback = Callback()
    private val discCallBack = DiscoveryCallback()
    private var payloadCallback = PayloadCallBack()
    private lateinit var payload: Payload
    private val userName = "test"
    private lateinit var file: File
    private lateinit var connectionClient: ConnectionsClient
    private var endpoint = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.fragment_settings_backup)
//        if (intent.action == Intent.ACTION_SEND) {
//            if (intent.type == "application/json") {
                (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri).let { it ->
                    val inputStream = contentResolver.openInputStream(it)!!
                    val bufferReader = inputStream.bufferedReader()
                    val l = bufferReader.use {it.readText()}
                    file = File.createTempFile("send", "json")
                    file.writeText(l)

                    connectionClient = Nearby.getConnectionsClient(this)
//                    NotificationHandler.createNotification("debug", "debug", 9, "debug", l.toString(), R.drawable.ic_action_settings, "none", this)
//                    Log.e("permissions", "Bluetooth:\t${ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)}")
//                    Log.e("permissions", "Bluetooth Admin:\t${ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)}")
//                    Log.e("permissions", "Acc Wifi:\t${ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)}")
//                    Log.e("permissions", "Chg Wifi:\t${ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE)}")
//                    Log.e("permissions", "C Loc:\t${ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)}")
//                    Log.e("permissions", "F Log:\t${ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)}")
//
                    payload= Payload.fromFile(file)
//                }
//            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 50)
        }

        super.onCreate(savedInstanceState)
        btnDisconnect.setOnLongClickListener {
            finish()
            true
        }

        btnDisconnect.setOnClickListener {
            try {
                stopAdvertising()
                stopDiscovery()
            } catch (e: Exception) {/* no-op */}
        }

        btnConnect.setOnClickListener {
            startAdvertising()
        }

        btnConnect.setOnLongClickListener {
            startDiscovery()
            true
        }

        btnSend.setOnClickListener {
            try {
                val payload = Payload.fromFile(this@FileReceiver.file)
                connectionClient.sendPayload(endpoint, payload)
            } catch (f: FileNotFoundException) {
                Log.e("onConnectionResult", "file not found")
            }
        }
    }

    private fun startAdvertising() {
        val adOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build()
        connectionClient.startAdvertising(
                userName, packageName, lifeCycleCallback, adOptions)
            .addOnSuccessListener {
                Log.e("startAdvertising", "advertising...")
            }.addOnFailureListener {
                Log.e("startAdvertising", "can't advertise")
            }
    }

    private fun stopAdvertising() {
        connectionClient.stopAdvertising()
        Log.e("stopAdvertising", "stopped advertising")
    }

    private fun startDiscovery() {
        val options = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build()

        connectionClient.startDiscovery(packageName, discCallBack, options)
            .addOnSuccessListener {
                Log.e("startDiscovery", "discovering")
            }
            .addOnFailureListener { e ->
                Log.e("startDiscovery", "can't discover because: $e")
            }
    }

    private fun stopDiscovery() {
        connectionClient.stopDiscovery()
        Log.e("stopDiscovery", "stopped discovering")
    }

    private inner class Callback: ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(p0: String, p1: ConnectionInfo) {
            connectionClient.acceptConnection(p0, payloadCallback)
            Log.e("onConnectionInitiated", "connection initiated")
            // TODO authentication like bluetooth pairing (check tokens)
        }

        override fun onConnectionResult(p0: String, p1: ConnectionResolution) {
            when (p1.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    Log.e("onConnectionResult", "connection accepted")
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> Log.e("onConnectionResult", "connection rejected")
                ConnectionsStatusCodes.STATUS_ERROR -> Log.e("onConnectionResult", "connection error")
                else -> Log.e("onConnectionResult", "unknown statuscode")
            }
        }

        override fun onDisconnected(p0: String) {
            Log.e("onDisconnected", "disconnected")
        }
    }

    private inner class DiscoveryCallback: EndpointDiscoveryCallback() {
        override fun onEndpointFound(p0: String, p1: DiscoveredEndpointInfo) {
            endpoint = p0
            Log.e("onEndpointFound", "endpoint found")
            connectionClient.requestConnection(userName, p0, lifeCycleCallback)
                .addOnSuccessListener {
                    Log.e("requestConnection", "granted")
                }.addOnFailureListener {
                    Log.e("requestConnection", "denied")
                }
        }

        override fun onEndpointLost(p0: String) {
            Log.e("onEndpointLost", "endpoint lost")
        }
    }

    private inner class PayloadCallBack: PayloadCallback() {
        override fun onPayloadReceived(p0: String, p1: Payload) {
            Log.e("onPayloadReceived", "receiving $p0, ${p1.type}")
            Toast.makeText(this@FileReceiver, "receiver?", Toast.LENGTH_LONG).show()
        }

        override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
//            TODO("Not yet implemented")
        }

    }
}
