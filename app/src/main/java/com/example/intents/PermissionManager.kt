package com.example.intents

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity

class PermissionManager(val activity:AppCompatActivity,val context: Context) {
   private var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    init {
        requestPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts
                .RequestMultiplePermissions()
        ) { grantResults ->
            if(grantResults.all { hasPermission(it.key) }){
                Toast.makeText(context, "All Granted", Toast.LENGTH_SHORT).show()
            }else{
                if(grantResults.any{needAnyRationale(it.key)}){
                    displayRationale(context,deniedPermissions(grantResults
                        .keys.filter {
                            !hasPermission(it)
                        }.toTypedArray()))
                }else{
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri: Uri = Uri.fromParts("package", activity.packageName, null)
                    intent.data = uri
                    activity.startActivity(intent)
                }

            }
        }
    }
    fun  askPermission(permissions: Array<String>,granted: (Boolean)->Unit ={}){
        if(permissions.all { hasPermission(it) }){
           granted(true)
        }else{
            requestPermissions(deniedPermissions(permissions))
        }
    }
    private fun displayRationale(context: Context,permissions: Array<String>) {
        AlertDialog.Builder(context)
            .setTitle("Need permission")
            .setMessage("this permission is needed to run the app")
            .setCancelable(false)
            .setPositiveButton("Ok") { _, _ ->
                requestPermissions(permissions)
            }
            .show()
    }

    private fun requestPermissions(permissions: Array<String>){
        requestPermissionLauncher.launch(permissions)
    }
    private fun deniedPermissions(permissions: Array<String>): Array<String> =
        permissions.filter {
            !hasPermission(it)
        }.toTypedArray()

    private fun needAnyRationale(permission:String)=shouldShowRequestPermissionRationale(activity,
        permission)
    private fun hasPermission(permission: String) = ContextCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED

}