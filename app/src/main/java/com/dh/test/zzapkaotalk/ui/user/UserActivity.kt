package com.dh.test.zzapkaotalk.ui.user

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.dh.test.zzapkaotalk.*
import com.dh.test.zzapkaotalk.databinding.ActivityUserBinding
import com.dh.test.zzapkaotalk.network.Repository
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.tedpark.tedpermission.rx2.TedRx2Permission
import java.io.File

class UserActivity : BaseActivity() {

    override lateinit var viewModel: UserViewModel
    private lateinit var binding: ActivityUserBinding

    private val REQUEST_CODE_PHORO_PICK = 10001
    private val REQUEST_CODE_PHORO_CROP = 10002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = UserViewModel(Repository)
        binding = DataBindingUtil
            .setContentView<ActivityUserBinding>(this, R.layout.activity_user)
            .apply {
                this.lifecycleOwner = this@UserActivity
                this.viewModel = this@UserActivity.viewModel
            }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "짭카오톡"

        binding.viewModel = viewModel

        initViewModel()
        viewModel.postUser(Preferences.deviceId)
    }

    private fun initViewModel() {
        viewModel.userState.observe(this) {
            Log.d("dhlog", it.toString())
            UserHolder.userModel = it
            initViews()
        }
    }

    private fun initViews() {
        binding.button.setOnClickListener {
            val user = UserHolder.userModel
            val displayName = binding.editText.text.toString()
            val profileImageUrl = user.profileImageUrl

            viewModel.putUser(user.id, displayName, profileImageUrl)
        }

        binding.imageView.setOnClickListener {
            TedRx2Permission.with(this)
                .setDeniedMessage("ㅡㅡ;")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .request()
                .subscribe({
                    if (it.isGranted) {
                        openPhotoChooser(REQUEST_CODE_PHORO_PICK)
                    } else {
                        Toast.makeText(this, "권한 거절", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Toast.makeText(this, "권한 오류", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun openPhotoChooser(requestCode: Int) {
        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }.also {
            startActivityForResult(it, requestCode)
        }
    }

    private fun requestCrop(uri: Uri) {
        var outputFileName = "${System.currentTimeMillis()}_profile"
        val fileExtension = getFileExtensionFromContentUri(uri)
        if (fileExtension.isNotEmpty()) outputFileName += ".$fileExtension"

        val outputFile = File(externalCacheDir, outputFileName)
        val outputFileUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", outputFile)

        Intent("com.android.camera.action.CROP").apply {
            setDataAndType(uri, "image/*")
            putExtra("aspectX", 180)
            putExtra("aspectY", 180)
            putExtra("crop", true)
            putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        }.also {
            val list = packageManager.queryIntentActivities(it, 0)
            if (list.count() > 0) {
                list.forEach { info ->
                    info?.activityInfo?.packageName?.let { name ->
                        grantUriPermission(name, outputFileUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    }
                }
                startActivityForResult(it, REQUEST_CODE_PHORO_CROP)
            } else {
                uploadPhoto(uri, outputFileName)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_PHORO_PICK && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            requestCrop(uri)
            Log.d("dhlog", "UserActivity PHOTO_PICK onActivityResult : $uri")
        } else if (requestCode == REQUEST_CODE_PHORO_CROP && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            uploadPhoto(uri, uri.lastPathSegment?:"")
            Log.d("dhlog", "UserActivity PHORO_CROP onActivityResult : $uri")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun uploadPhoto(uri: Uri, fileName: String) {
        Log.d("dhlog", "UserActivity uploadPhoto() : $uri, $fileName")
        val storageRef = Firebase.storage
        val photoRef = storageRef.getReference("zzapkaotalk/$fileName")
        photoRef.putFile(uri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    Log.d("dhlog", "UserActivity putFile() 실패")
                    task.exception?.let {
                        throw it
                    }
                }
                Log.d("dhlog", "UserActivity putFile() 성공")
                photoRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.d("dhlog", "UserActivity getDownloadUrl() 실패")
                    task.exception?.let {
                        throw it
                    }
                }
                Log.d("dhlog", "UserActivity getDownloadUrl() 성공 >> ${task.result}")
                val user = UserHolder.userModel
                val displayName = user.displayName
                val profileImageUrl = task.result.toString()
                viewModel.putUser(user.id, displayName, profileImageUrl)
            }
    }

    private fun getFileExtensionFromContentUri(uri: Uri): String {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(contentResolver.getType(uri)) ?: ""
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}