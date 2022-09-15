package com.example.intents

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.telephony.SmsManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.intents.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var requestedPermission: Array<String>
    private lateinit var deniedPermission: Array<String>
    private var allGranted: Boolean = false
    lateinit var testView:TextView
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPermissionsLauncher()

        testView = findViewById(R.id.test_view)
        requestedPermission = arrayOf(Manifest.permission.SEND_SMS, Manifest.permission
            .RECEIVE_SMS,Manifest.permission.CAMERA)

        if(requestedPermission.all { hasPermission(it) }){
            Toast.makeText(this, "All Granted", Toast.LENGTH_SHORT).show()
        }else{
            requestPermissions(deniedPermissions(requestedPermission))
        }
        testView.setOnClickListener {
            sendSms()
        }
    }

    private  fun setupPermissionsLauncher(){
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts
                .RequestMultiplePermissions()
        ) { grantResults ->
            if(grantResults.all { hasPermission(it.key) }){
                Toast.makeText(this, "All Granted", Toast.LENGTH_SHORT).show()
            }else{
                if(requestedPermission.any{needAnyRationale(it)}){
                    displayRationale(deniedPermissions(requestedPermission))
                }else{
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri: Uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }

            }
        }
    }
    private fun sendSms(){
        val smsManager = getSystemService(SmsManager::class.java) as SmsManager
        smsManager.sendTextMessage("+9179956612656",null,"Testing Sms",null,null)
        Toast.makeText(this, "sent", Toast.LENGTH_SHORT).show()
    }

    private fun deniedPermissions(permissions: Array<String>): Array<String> =
        permissions.filter {
            !hasPermission(it)
        }.toTypedArray()

    private fun requestPermissions(permissions: Array<String>){
            requestPermissionLauncher.launch(permissions)
    }
    private fun displayRationale(permissions: Array<String>) {
        AlertDialog.Builder(this)
            .setTitle("Need permission")
            .setMessage("this permission is needed to run the app")
            .setCancelable(true)
            .setPositiveButton("Ok") { _, _ ->
                requestPermissions(permissions)
            }
            .show()
    }
    private fun needAnyRationale(permission:String)=shouldShowRequestPermissionRationale(permission)
    private fun hasPermission(permission: String) = ContextCompat.checkSelfPermission(
        this,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}