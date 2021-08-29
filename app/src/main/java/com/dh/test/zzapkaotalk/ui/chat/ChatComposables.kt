package com.dh.test.zzapkaotalk.ui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.rememberImagePainter
import com.dh.test.zzapkaotalk.UserHolder
import com.dh.test.zzapkaotalk.model.ChatModel
import com.dh.test.zzapkaotalk.model.UserModel
import com.dh.test.zzapkaotalk.ui.compose.theme.ZzapkaotalkTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel,
    onImageButtonClick: () -> Unit,
    text: String,
    onEditText: (String) -> Unit,
    onSendButtonClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopBar()
        }
    ) {
        BodyContent(
            list = chatViewModel.chatList,
            onImageButtonClick = onImageButtonClick,
            text = text,
            onEditText = onEditText,
            onSendButtonClick = onSendButtonClick,
        )
    }
}

@Composable
fun BodyContent(
    list: List<ChatModel>,
    onImageButtonClick: () -> Unit,
    text: String,
    onEditText: (String) -> Unit,
    onSendButtonClick: () -> Unit,
) {
    ConstraintLayout {
        val (chatList, input) = createRefs()
        InputText(
            onImageButtonClick = onImageButtonClick,
            onSendButtonClick = onSendButtonClick,
            text = text,
            onEditText = onEditText,
            modifier = Modifier
                .constrainAs(input) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
        )
        ChatList(
            currentUser = UserHolder.userModel,
            list = list,
            modifier = Modifier
                .constrainAs(chatList) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(input.top)
                    height = Dimension.preferredValue(0.dp) //  value(0.dp와의 차이는...?)
                }
                .background(MaterialTheme.colors.secondary)
        )
    }
}

@Composable
fun TopBar(modifier: Modifier = Modifier) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text("짭카오톡")
        },
    )
}

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    ZzapkaotalkTheme {
        TopBar()
    }
}

@Composable
fun InputText(
    modifier: Modifier = Modifier,
    onImageButtonClick: () -> Unit,
    text: String,
    onEditText: (String) -> Unit,
    onSendButtonClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(
            onClick = onImageButtonClick,
        ) {
            Icon(imageVector = Icons.Filled.Image, contentDescription = "")
        }
        TextField(
            value = text,
            onValueChange = onEditText,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onSendButtonClick,
        ) {
            Icon(imageVector = Icons.Filled.Send, contentDescription = "")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InputTextPreview() {
    ZzapkaotalkTheme {
        InputText(
            onImageButtonClick = {

            },
            text = "테스트",
            onSendButtonClick = {

            },
            onEditText = {

            }
        )
    }
}

@Composable
fun ChatList(modifier: Modifier = Modifier, list: List<ChatModel> = emptyList(), currentUser: UserModel) {
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = scrollState,
    ) {
        items(list.size) {
            val chat = list[it]
            when(chat.type) {
                "system" -> {
                    SystemChatHolder(chatModel = chat)
                }
                "text" -> {
                    if (chat.user.deviceId == currentUser.deviceId) {
                        MyTextChatHolder(chatModel = chat)
                    } else {
                        OthersTextChatHolder(chatModel = chat)
                    }
                }
                "image" -> {
                    if (chat.user.deviceId == currentUser.deviceId) {
                        MyImageChatHolder(chatModel = chat)
                    } else {
                        OthersImageChatHolder(chatModel = chat)
                    }
                }
            }
        }
        if (list.isNotEmpty()) {
            coroutineScope.launch {
                scrollState.scrollToItem(list.size - 1)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFBB86FC)
@Composable
fun ChatListPreview() {
    ChatList(list = sampleList, currentUser = UserHolder.userModel)
}

@Composable
fun MyTextChatHolder(modifier: Modifier = Modifier, chatModel: ChatModel) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            modifier = Modifier
                .weight(0.2f)
                .padding(end = 4.dp),
            textAlign = TextAlign.End,
            text = chatModel.dateTime.timeText()
        )
        Text(
            modifier = Modifier
                .weight(0.8f, false)
                .wrapContentWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(color = Color(0xFFFFDD77))
                .padding(4.dp),
            text = chatModel.message,
            fontSize = 22.sp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFBB86FC)
@Composable
fun MyTextChatHolderPreviewShort() {
    MyTextChatHolder(chatModel = ChatModel(
        id = 1,
        type = "text",
        message = "안녕하세요",
        dateTime = Date(),
        roomNo = 1,
        user = UserModel(
            id = 1,
            deviceId = "1",
            displayName = "안녕?",
            profileImageUrl = "https://firebasestorage.googleapis.com/v0/b/fir-test-29880.appspot.com/o/zzapkaotalk%2F1627455337817_profile.jpg?alt=media&token=8e7dfb72-3a00-49af-8885-515eea605d02"
        )
    ))
}

@Preview(showBackground = true, backgroundColor = 0xFFBB86FC)
@Composable
fun MyTextChatHolderPreviewLong() {
    MyTextChatHolder(chatModel = ChatModel(
        id = 1,
        type = "text",
        message = "안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 ",
        dateTime = Date(),
        roomNo = 1,
        user = UserModel(
            id = 1,
            deviceId = "1",
            displayName = "안녕?",
            profileImageUrl = "https://firebasestorage.googleapis.com/v0/b/fir-test-29880.appspot.com/o/zzapkaotalk%2F1627455337817_profile.jpg?alt=media&token=8e7dfb72-3a00-49af-8885-515eea605d02"
        )
    ))
}

@Composable
fun MyImageChatHolder(modifier: Modifier = Modifier, chatModel: ChatModel) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {

        Text(
            modifier = Modifier
                .padding(end = 4.dp),
            textAlign = TextAlign.End,
            text = chatModel.dateTime.timeText()
        )
        Image(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color = Color(0xFFFFDD77))
                .padding(4.dp),
            painter = rememberImagePainter(data = chatModel.message),
            contentDescription = "",
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFBB86FC)
@Composable
fun MyImageChatHolderPreview() {
    MyImageChatHolder(chatModel = ChatModel(
        id = 1,
        type = "image",
        message = "https://firebasestorage.googleapis.com/v0/b/fir-test-29880.appspot.com/o/zzapkaotalk%2F1627455337817_profile.jpg?alt=media&token=8e7dfb72-3a00-49af-8885-515eea605d02",
        dateTime = Date(),
        roomNo = 1, user =
        UserModel(
            id = 1,
            deviceId = "1",
            displayName = "안녕?",
            profileImageUrl = "https://firebasestorage.googleapis.com/v0/b/fir-test-29880.appspot.com/o/zzapkaotalk%2F1627455337817_profile.jpg?alt=media&token=8e7dfb72-3a00-49af-8885-515eea605d02"
        )
    ))
}

@Composable
fun OthersTextChatHolder(modifier: Modifier = Modifier, chatModel: ChatModel) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Row(
            modifier = Modifier
                .weight(0.8f, false)
        ) {
            Surface(
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colors.onSurface.copy(0.2f)
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentDescription = "",
                    painter = rememberImagePainter(data = chatModel.user.profileImageUrl)
                )
            }
            Column {
                Text(
                    modifier = Modifier
                        .padding(end = 4.dp),
                    text = chatModel.user.displayName,
                    style = MaterialTheme.typography.h6,
                    fontSize = 18.sp
                )
                Text(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(color = Color(0xFFFFFFFF))
                        .padding(4.dp),
                    text = chatModel.message,
                    fontSize = 22.sp,
                )
            }
        }
        Text(
            modifier = Modifier
                .weight(0.2f)
                .padding(start = 4.dp),
            textAlign = TextAlign.Start,
            text = chatModel.dateTime.timeText()
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFBB86FC)
@Composable
fun OthersTextChatHolderPreviewShort() {
    OthersTextChatHolder(chatModel = ChatModel(
        id = 1,
        type = "text",
        message = "안녕하세요",
        dateTime = Date(),
        roomNo = 1,
        user = UserModel(
            id = 1,
            deviceId = "1",
            displayName = "안녕?",
            profileImageUrl = "https://firebasestorage.googleapis.com/v0/b/fir-test-29880.appspot.com/o/zzapkaotalk%2F1627455337817_profile.jpg?alt=media&token=8e7dfb72-3a00-49af-8885-515eea605d02"
        )
    ))
}

@Preview(showBackground = true, backgroundColor = 0xFFBB86FC)
@Composable
fun OthersTextChatHolderPreviewLong() {
    OthersTextChatHolder(chatModel = ChatModel(
        id = 1,
        type = "text",
        message = "안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 안녕하세요 ",
        dateTime = Date(),
        roomNo = 1,
        user = UserModel(
            id = 1,
            deviceId = "1",
            displayName = "안녕?",
            profileImageUrl = "https://firebasestorage.googleapis.com/v0/b/fir-test-29880.appspot.com/o/zzapkaotalk%2F1627455337817_profile.jpg?alt=media&token=8e7dfb72-3a00-49af-8885-515eea605d02"
        )
    ))
}

@Composable
fun OthersImageChatHolder(modifier: Modifier = Modifier, chatModel: ChatModel) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Row(
            modifier = Modifier
                .weight(0.8f, false)
        ) {
            Surface(
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colors.onSurface.copy(0.2f)
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentDescription = "",
                    painter = rememberImagePainter(data = chatModel.user.profileImageUrl)
                )
            }
            Column {
                Text(
                    modifier = Modifier
                        .padding(end = 4.dp),
                    text = chatModel.user.displayName,
                    style = MaterialTheme.typography.h6,
                    fontSize = 18.sp
                )
                Image(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color = Color(0xFFFFFFFF))
                        .padding(4.dp),
                    painter = rememberImagePainter(data = chatModel.message),
                    contentDescription = "",
                )
            }
        }
        Text(
            modifier = Modifier
                .weight(0.2f)
                .padding(start = 4.dp),
            textAlign = TextAlign.Start,
            text = chatModel.dateTime.timeText()
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFBB86FC)
@Composable
fun OthersImageChatHolderPreview() {
    OthersImageChatHolder(chatModel = ChatModel(
        id = 1,
        type = "image",
        message = "https://firebasestorage.googleapis.com/v0/b/fir-test-29880.appspot.com/o/zzapkaotalk%2F1627455337817_profile.jpg?alt=media&token=8e7dfb72-3a00-49af-8885-515eea605d02",
        dateTime = Date(),
        roomNo = 1, user =
        UserModel(
            id = 1,
            deviceId = "1",
            displayName = "안녕?",
            profileImageUrl = "https://firebasestorage.googleapis.com/v0/b/fir-test-29880.appspot.com/o/zzapkaotalk%2F1627455337817_profile.jpg?alt=media&token=8e7dfb72-3a00-49af-8885-515eea605d02"
        )
    ))
}

@Composable
fun SystemChatHolder(modifier: Modifier = Modifier, chatModel: ChatModel) {
    Box(modifier = modifier
        .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()
                .background(Color(0x23000000)),
            text = chatModel.message,
            textAlign = TextAlign.Center
        )
    }

}

@Preview(showBackground = true, backgroundColor = 0xFFBB86FC)
@Composable
fun SystemChatHolderPreview() {
    SystemChatHolder(chatModel = ChatModel(
        id = 1,
        type = "system",
        message = "ㅋㅋㅋ님이 입장하셨습니다.",
        dateTime = Date(),
        roomNo = 1,
        user = UserModel()
    ))
}

fun Date.timeText(): String {
    return SimpleDateFormat("aa h:mm", Locale.getDefault()).format(this)
}

val sampleList = listOf(
    ChatModel(
        id = 1,
        type = "system",
        message = "짱절미님이 들어오셨습니다",
        dateTime = Date(),
        roomNo = 1,
        user = UserModel()
    ),
    ChatModel(
        id = 1,
        type = "text",
        message = "안녕하세요",
        dateTime = Date(),
        roomNo = 1,
        user = UserModel(
            id = 1,
            deviceId = UserHolder.userModel.deviceId,
            displayName = "나",
            profileImageUrl = "https://firebasestorage.googleapis.com/v0/b/fir-test-29880.appspot.com/o/zzapkaotalk%2F1627455337817_profile.jpg?alt=media&token=8e7dfb72-3a00-49af-8885-515eea605d02"
        )
    ),
    ChatModel(
        id = 1,
        type = "text",
        message = "안녕하세요",
        dateTime = Date(),
        roomNo = 1,
        user = UserModel(
            id = 1,
            deviceId = "1",
            displayName = "짱절미",
            profileImageUrl = "https://firebasestorage.googleapis.com/v0/b/fir-test-29880.appspot.com/o/zzapkaotalk%2F1627455337817_profile.jpg?alt=media&token=8e7dfb72-3a00-49af-8885-515eea605d02"
        )
    ),
    ChatModel(
        id = 1,
        type = "text",
        message = "안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요",
        dateTime = Date(),
        roomNo = 1,
        user = UserModel(
            id = 1,
            deviceId = UserHolder.userModel.deviceId,
            displayName = "나",
            profileImageUrl = "https://firebasestorage.googleapis.com/v0/b/fir-test-29880.appspot.com/o/zzapkaotalk%2F1627455337817_profile.jpg?alt=media&token=8e7dfb72-3a00-49af-8885-515eea605d02"
        )
    ),
    ChatModel(
        id = 1,
        type = "text",
        message = "안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요",
        dateTime = Date(),
        roomNo = 1,
        user = UserModel(
            id = 1,
            deviceId = "1",
            displayName = "짱절미",
            profileImageUrl = "https://firebasestorage.googleapis.com/v0/b/fir-test-29880.appspot.com/o/zzapkaotalk%2F1627455337817_profile.jpg?alt=media&token=8e7dfb72-3a00-49af-8885-515eea605d02"
        )
    ),
    ChatModel(
        id = 1,
        type = "image",
        message = "https://firebasestorage.googleapis.com/v0/b/fir-test-29880.appspot.com/o/zzapkaotalk%2F1627455337817_profile.jpg?alt=media&token=8e7dfb72-3a00-49af-8885-515eea605d02",
        dateTime = Date(),
        roomNo = 1,
        user = UserModel(
            id = 1,
            deviceId = UserHolder.userModel.deviceId,
            displayName = "나",
            profileImageUrl = "https://firebasestorage.googleapis.com/v0/b/fir-test-29880.appspot.com/o/zzapkaotalk%2F1627455337817_profile.jpg?alt=media&token=8e7dfb72-3a00-49af-8885-515eea605d02"
        )
    ),
    ChatModel(
        id = 1,
        type = "image",
        message = "https://firebasestorage.googleapis.com/v0/b/fir-test-29880.appspot.com/o/zzapkaotalk%2F1627455337817_profile.jpg?alt=media&token=8e7dfb72-3a00-49af-8885-515eea605d02",
        dateTime = Date(),
        roomNo = 1,
        user = UserModel(
            id = 1,
            deviceId = "1",
            displayName = "짱절미",
            profileImageUrl = "https://firebasestorage.googleapis.com/v0/b/fir-test-29880.appspot.com/o/zzapkaotalk%2F1627455337817_profile.jpg?alt=media&token=8e7dfb72-3a00-49af-8885-515eea605d02"
        )
    ),
)