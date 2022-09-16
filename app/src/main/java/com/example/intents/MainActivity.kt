package com.example.intents

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.intents.databinding.ActivityMainBinding
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var phonePermissionLauncher: ActivityResultLauncher<String>
    private lateinit var takeImagelauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImages: ActivityResultLauncher<String>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var requestedPermission: Array<String>
    lateinit var uri: Uri
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPermissionsLauncher()

        requestedPermission = arrayOf(
            Manifest.permission.SEND_SMS, Manifest.permission
                .RECEIVE_SMS, Manifest.permission.CAMERA
        )

        if (requestedPermission.all { hasPermission(it) }) {
            Toast.makeText(this, "All Granted", Toast.LENGTH_SHORT).show()
        } else {
            requestPermissions(deniedPermissions(requestedPermission))
        }
        binding.testView.setOnClickListener {
            sendSms()
        }
        binding.sendMailBtn.setOnClickListener {
            sendEmail()
        }
        pickImages = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri.let {
                binding.imageView.setImageURI(it)
            }
        }
        takeImagelauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) {
                binding.imageView.setImageURI(uri)
            }
        }
        binding.chooseImgBtn.setOnClickListener {
            chooseImage()
        }
        binding.takeImgBtn.setOnClickListener {
            takeImage()
        }
        phonePermissionLauncher = registerForActivityResult(
            ActivityResultContracts
                .RequestPermission()
        ) {
            if (it == true) {
                call()
            }
        }
        binding.callBtn.setOnClickListener {
            if (hasPermission(Manifest.permission.CALL_PHONE)) {
                Toast.makeText(this, "granted", Toast.LENGTH_SHORT).show()
                call()
            } else {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.CALL_PHONE))
            }
        }
    }

    private fun call() {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:+918639888917"))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun chooseImage() {
        pickImages.launch("image/*")
    }

    private fun takeImage() {
        val file = File(
            Environment.getExternalStoragePublicDirectory(
                Environment
                    .DIRECTORY_PICTURES
            ), "image_" + System.currentTimeMillis() + ".jpg"
        )
        uri = FileProvider.getUriForFile(this, packageName + ".provider", file)
        takeImagelauncher.launch(uri)

    }

    private fun sendEmail() {

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, "venkatmbts43@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, "Sample Subject")
        }
        startActivity(Intent.createChooser(intent, "Choose email"))
    }

    private fun setupPermissionsLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts
                .RequestMultiplePermissions()
        ) { grantResults ->
            if (grantResults.all { hasPermission(it.key) }) {
                Toast.makeText(this, "All Granted", Toast.LENGTH_SHORT).show()
            } else {
                if (requestedPermission.any { needAnyRationale(it) }) {
                    displayRationale(deniedPermissions(requestedPermission))
                } else {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri: Uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }

            }
        }
    }

    private fun sendSms() {
        val smsManager = getSystemService(SmsManager::class.java) as SmsManager
        smsManager.sendTextMessage("+9179956612656", null, "Testing Sms", null, null)
        Toast.makeText(this, "sent", Toast.LENGTH_SHORT).show()
    }

    private fun deniedPermissions(permissions: Array<String>): Array<String> =
        permissions.filter {
            !hasPermission(it)
        }.toTypedArray()

    private fun requestPermissions(permissions: Array<String>) {
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

    private fun needAnyRationale(permission: String) =
        shouldShowRequestPermissionRationale(permission)

    private fun hasPermission(permission: String) = ContextCompat.checkSelfPermission(
        this,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}