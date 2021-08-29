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
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.dh.test.zzapkaotalk.BaseActivity
import com.dh.test.zzapkaotalk.Preferences
import com.dh.test.zzapkaotalk.R
import com.dh.test.zzapkaotalk.UserHolder
import com.dh.test.zzapkaotalk.model.UserModel
import com.dh.test.zzapkaotalk.network.Repository
import com.dh.test.zzapkaotalk.ui.compose.theme.ZzapkaotalkTheme
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.tedpark.tedpermission.rx2.TedRx2Permission
import java.io.File

class UserActivity : BaseActivity() {

    override lateinit var viewModel: UserViewModel

    private val REQUEST_CODE_PHORO_PICK = 10001
    private val REQUEST_CODE_PHORO_CROP = 10002

    @ExperimentalComposeUiApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = UserViewModel(Repository)
        setContent {
            ZzapkaotalkTheme {
                ProfileScreen(
                    displayName = viewModel.userDisplayName.value,
                    profileImageUrl = viewModel.userProfileImageUrl.value,
                    onImageClick = ::requestStoragePermission,
                    onTextChanged = viewModel::onDisplayNameChanged,
                    onImeAction = viewModel::putUserDisplayName,
                )
            }
        }
        viewModel.postUser(Preferences.deviceId)
    }

    private fun requestStoragePermission() {
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
                val profileImageUrl = task.result.toString()
                viewModel.putUserProfileImageUrl(profileImageUrl)
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

@ExperimentalComposeUiApi
@Composable
private fun ProfileScreen(displayName: String,
                          profileImageUrl: String,
                          onImageClick: () -> Unit,
                          onTextChanged: (String) -> Unit,
                          onImeAction: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = MaterialTheme.shapes.medium,
            elevation = 1.dp,
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IconButton(
                    modifier = Modifier
                        .padding(24.dp)
                        .padding(top = 12.dp)
                        .size(240.dp)
                    ,
                    onClick = onImageClick
                ) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        painter = rememberImagePainter(
                            data = profileImageUrl,
                            builder = {
                                crossfade(true)
                                placeholder(R.drawable.ic_baseline_account_circle_24)
                                error(R.drawable.ic_baseline_account_circle_24)
                                transformations(CircleCropTransformation())
                            }
                        ),
                        contentDescription = "",
                    )
                }


                val keyboardController = LocalSoftwareKeyboardController.current
                TextField(
                    enabled = true,
                    modifier = Modifier
                        .padding(24.dp)
                        .padding(bottom = 12.dp)
                        .width(240.dp),
                    value = displayName,
                    onValueChange = onTextChanged,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        onImeAction()
                        keyboardController?.hide()
                    })
                )
            }
        }
    }
}

@ExperimentalComposeUiApi
@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    ZzapkaotalkTheme {
        ProfileScreen(
            displayName = "뚝딱",
            profileImageUrl = "https://firebasestorage.googleapis.com/v0/b/fir-test-29880.appspot.com/o/zzapkaotalk%2F1630213840726_profile.jpg?alt=media&token=f96b4247-5efa-4c85-9d4d-232a062627fc",
            {}, {}, {}
        )
    }
}