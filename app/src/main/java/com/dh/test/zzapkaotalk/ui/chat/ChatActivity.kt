package com.dh.test.zzapkaotalk.ui.chat

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.core.content.FileProvider
import com.dh.test.zzapkaotalk.BaseActivity
import com.dh.test.zzapkaotalk.Const
import com.dh.test.zzapkaotalk.UserHolder
import com.dh.test.zzapkaotalk.model.ChatModel
import com.dh.test.zzapkaotalk.network.Repository
import com.dh.test.zzapkaotalk.ui.compose.theme.ZzapkaotalkTheme
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.tedpark.tedpermission.rx2.TedRx2Permission
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.EngineIOException
import org.json.JSONObject
import java.io.File

class ChatActivity : BaseActivity() {

    private val REQUEST_CODE_PHOTO_PICK = 10001
    private val REQUEST_CODE_PHORO_CROP = 10002

    override lateinit var viewModel: ChatViewModel

    private lateinit var socket: Socket
    private var roomNo: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ChatViewModel(Repository)
        title = intent.getStringExtra("roomName") ?: "짭카오톡"
        roomNo = intent.getIntExtra("roomNo", 0)

        setContent {
            ZzapkaotalkTheme {
                ChatScreen(
                    viewModel,
                    onImageButtonClick = ::requestFilePermission,
                    text = viewModel.editingText.value,
                    onEditText = viewModel::onEditText,
                    onSendButtonClick = ::sendTextMessage
                )
            }
        }

        initSocket()
        viewModel.getChats(roomNo)
    }

    private fun initSocket() {
        try {
            socket = IO.socket("${Const.BASE_URL}/chat?room_no=$roomNo&user=${UserHolder.userModel.id}")

            socket.on(Socket.EVENT_CONNECT) {
                Log.d("dhlog", "연결 성공")
            }

            socket.on(Socket.EVENT_CONNECT_ERROR) {
                Log.d("dhlog", "연결실패")
                val e = it[0] as EngineIOException
                e.printStackTrace()
            }

            socket.on(Socket.EVENT_DISCONNECT) {
                Log.d("dhlog", "연결 끊김")
            }

            socket.on("chat") {
                Log.d("dhlog", "CHAT 메시지 수신")
                val chat = Gson().fromJson(it[0].toString(), ChatModel::class.java)
                runOnUiThread {
                    viewModel.chatReceived(chat)
                }
                Log.d("dhlog", chat.toString())
            }
            socket.connect()
        } catch (e: Exception) {
            Log.d("dhlog", "${e.message}")
            e.printStackTrace()
        }
    }

    private fun requestFilePermission() {
        TedRx2Permission.with(this)
            .setDeniedMessage("ㅡㅡ;")
            .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .request()
            .subscribe({
                if (it.isGranted) {
                    openPhotoChooser(REQUEST_CODE_PHOTO_PICK)
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
        if (requestCode == REQUEST_CODE_PHOTO_PICK && resultCode == Activity.RESULT_OK) {
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
                task.result?.let {
                    sendImageMessage(it.toString())
                }
            }
    }

    private fun getFileExtensionFromContentUri(uri: Uri): String {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(contentResolver.getType(uri)) ?: ""
    }

    private fun sendTextMessage() {
        val j = JSONObject()
        j.put("user_id", UserHolder.userModel.id)
        j.put("type", "text")
        j.put("message", viewModel.editingText.value)
        j.put("room_no", roomNo)
        val userObj = JSONObject(Gson().toJson(UserHolder.userModel))
        j.putOpt("user", userObj)

        socket.emit("chat", j)
        Log.d("dhlog", "CHAT 메시지 발신")
        Log.d("dhlog", j.toString())

        viewModel.onEditText("")
    }

    private fun sendImageMessage(uri: String) {
        val j = JSONObject()
        j.put("user_id", UserHolder.userModel.id)
        j.put("type", "image")
        j.put("message", uri)
        j.put("room_no", roomNo)
        val userObj = JSONObject(Gson().toJson(UserHolder.userModel))
        j.putOpt("user", userObj)

        socket.emit("chat", j)
        Log.d("dhlog", "CHAT 메시지 발신")
        Log.d("dhlog", j.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::socket.isInitialized && socket.connected()) {
            Log.d("dhlog", "소켓 연결 해제")
            try {
                socket.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

