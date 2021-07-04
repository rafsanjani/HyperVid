package com.foreverrafs.hypervid.ui.videos

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.foreverrafs.hypervid.R.drawable
import com.foreverrafs.hypervid.analytics.Analytics
import com.foreverrafs.hypervid.analytics.events.ShareVideoEvent
import com.foreverrafs.hypervid.model.FBVideo
import com.foreverrafs.hypervid.ui.MainViewModel
import com.foreverrafs.hypervid.ui.TabLayoutCoordinator
import com.foreverrafs.hypervid.ui.states.VideoListState
import com.foreverrafs.hypervid.ui.style.HyperVidTheme
import com.foreverrafs.hypervid.util.getDurationString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class VideosFragment : Fragment() {
    @Inject
    lateinit var analytics: Analytics

    companion object {
        var tabLayoutCoordinator: TabLayoutCoordinator? = null

        fun newInstance(tabLayoutCoordinator: TabLayoutCoordinator): VideosFragment {
            this.tabLayoutCoordinator = tabLayoutCoordinator
            return VideosFragment()
        }
    }

    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {

            setContent {
                val mainViewModel: MainViewModel = viewModel()
                val state by mainViewModel.videosListState.collectAsState(initial = VideoListState.Loading)

                HyperVidTheme {
                    Surface {
                        when (state) {
                            is VideoListState.Error -> {
                                EmptyList()
                            }
                            VideoListState.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val alpha by rememberInfiniteTransition().animateFloat(
                                        initialValue = 0.2f,
                                        targetValue = 1f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(durationMillis = 500),
                                            repeatMode = RepeatMode.Reverse
                                        )
                                    )

                                    Text(
                                        modifier = Modifier.alpha(alpha),
                                        text = "Loading...",
                                        style = MaterialTheme.typography.h4
                                    )
                                }
                            }
                            is VideoListState.Videos -> {
                                VideoListPage(
                                    videoList = (state as VideoListState.Videos).videos,
                                    onPlay = { video ->
                                        playVideo(video)
                                    },
                                    onShare = { videos ->
                                        shareVideo(videos = videos.toTypedArray())
                                    },
                                    onDelete = { videos ->
                                        videos.forEach {
                                            mainViewModel.deleteVideo(it)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun shareVideo(vararg videos: FBVideo) {

        val context = this

        val uris = videos.map {
            FileProvider.getUriForFile(
                requireContext(),
                requireContext().packageName + ".provider",
                File(it.path)
            )
        }.toTypedArray()

        val videoShare = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris.toCollection(ArrayList()))
            type = "*/*"
        }

        try {
            context.startActivity(videoShare)
            videos.forEach {
                analytics.trackEvent(
                    ShareVideoEvent(
                        title = it.title,
                        url = it.originalUrl
                    )
                )
            }
        } catch (exception: ActivityNotFoundException) {
            Timber.e(exception, "No app to handle this request")
        }
    }

    private fun playVideo(video: FBVideo) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(video.path), "video/*")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                "Unable to play video. Locate and play it from your gallery",
                Toast.LENGTH_SHORT
            ).show()
            Timber.e(e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tabLayoutCoordinator = null
    }

    @Preview
    @Composable
    fun EmptyVideoListPreview() {
        HyperVidTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                EmptyList()
            }
        }
    }

    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    @Preview
    @Composable
    fun VideoListPreview() {
        HyperVidTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                VideoListPage(
                    videoList = listOf(
                        FBVideo(
                            title = "Hello World",
                            downloadUrl = "",
                            path = "",
                            originalUrl = ""
                        )
                    ),
                    onDelete = {
                    },
                    onShare = {
                    }
                )
            }
        }
    }

    @ExperimentalAnimationApi
    @Composable
    @Preview
    fun VideoCardPreview() {
        HyperVidTheme {
            Surface {
                Box(modifier = Modifier.padding(10.dp)) {
                    VideoCard(
                        video = FBVideo(
                            title = "Rafsanjani and the wailers",
                            path = "",
                            downloadUrl = "",
                            originalUrl = ""
                        ),
                        onSelectionChanged = {},
                        selected = false,
                        onPlay = {}) {
                    }
                }
            }
        }
    }

    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    @Composable
    fun VideoListPage(
        videoList: List<FBVideo> = emptyList(),
        onPlay: (video: FBVideo) -> Unit = {},
        onShare: (videos: List<FBVideo>) -> Unit,
        onDelete: (videos: List<FBVideo>) -> Unit,
    ) {
        val selectedVideos = remember { mutableStateListOf<FBVideo>() }
        var openDeleteDialog by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        val bottomSheetState = rememberBottomSheetState(
            initialValue = BottomSheetValue.Collapsed,
            animationSpec = tween(durationMillis = 500)
        )

        BackHandler(enabled = selectedVideos.isNotEmpty()) {
            selectedVideos.clear()
        }

        BottomSheetScaffold(
            sheetContent = {
                BottomSheetContent(onTabAction = { action ->
                    when (action) {
                        BottomSheetAction.PLAY -> {
                            onPlay(selectedVideos.first())
                        }
                        BottomSheetAction.SHARE -> {
                            onShare(selectedVideos.toList())
                        }
                        BottomSheetAction.DELETE -> {
                            openDeleteDialog = true
                        }
                    }
                })
            },
            sheetShape = MaterialTheme.shapes.small,
            sheetPeekHeight = 0.dp,
            scaffoldState = rememberBottomSheetScaffoldState(
                bottomSheetState = bottomSheetState
            ),
            sheetGesturesEnabled = false,
            content = {
                MainContent(
                    videoList = videoList,
                    onVideoSelected = {
                        scope.launch {
                            selectedVideos.add(it)
                            if (selectedVideos.isNotEmpty()) {
                                bottomSheetState.expand()
                            } else {
                                bottomSheetState.collapse()
                            }
                        }
                    },
                    onVideoUnselected = {
                        scope.launch {
                            selectedVideos.remove(it)
                            if (selectedVideos.isNotEmpty()) {
                                bottomSheetState.expand()
                            } else {
                                bottomSheetState.collapse()
                            }
                        }
                    },
                    selectionMode = selectedVideos.isNotEmpty(),
                    selectedVideos = selectedVideos,
                    onPlay = {
                        onPlay(it)
                    }
                )
                if (openDeleteDialog) {
                    val message = if (selectedVideos.size > 1) {
                        "Are you sure you want to delete these videos?"
                    } else {
                        "Are you sure you want to delete this video?"
                    }

                    DeleteDialog(
                        message = message,
                        onConfirm = {
                            onDelete(selectedVideos.toList())
                            selectedVideos.clear()

                            openDeleteDialog = false

                            scope.launch {
                                bottomSheetState.collapse()
                            }
                        },
                        onCancel = {
                            openDeleteDialog = false
                        }
                    )
                }
            }
        )
    }

    @Composable
    fun DeleteDialog(
        message: String,
        onConfirm: () -> Unit,
        onCancel: () -> Unit
    ) {
        AlertDialog(
            title = {
                Text(text = "Delete Video(s)")
            },
            text = {
                Text(text = message)
            },
            onDismissRequest = {
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onCancel()
                    }
                ) {
                    Text("Cancel")
                }
            },
            properties = DialogProperties(dismissOnClickOutside = false)
        )
    }

    @ExperimentalMaterialApi
    @ExperimentalAnimationApi
    @Composable
    fun MainContent(
        videoList: List<FBVideo> = emptyList(),
        selectedVideos: List<FBVideo>,
        onVideoSelected: (video: FBVideo) -> Unit,
        onVideoUnselected: (video: FBVideo) -> Unit,
        onPlay: (video: FBVideo) -> Unit,
        selectionMode: Boolean,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = rememberLazyListState()
        ) {
            items(items = videoList, key = { it.downloadUrl }) { video ->
                var selected by remember { mutableStateOf(selectedVideos.contains(video)) }

                VideoCard(
                    selectionMode = selectionMode,
                    video = video,
                    onSelectionChanged = { isSelected ->
                        selected = isSelected

                        if (selected) {
                            onVideoSelected(video)
                        } else {
                            onVideoUnselected(video)
                        }
                    },
                    selected = selected,
                    onPlay = {
                        onPlay(video)
                    },
                    onclick = {
                        if (selectedVideos.isNotEmpty()) {
                            selected = !selected
                        }
                    }
                )
            }
        }
    }

    enum class BottomSheetAction {
        PLAY {
            override fun toString() = "Play"
        },
        SHARE {
            override fun toString() = "Share"
        },
        DELETE {
            override fun toString() = "Delete"
        }
    }

    @Composable
    fun BottomSheetContent(
        onTabAction: (action: BottomSheetAction) -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val modifier = Modifier.weight(1f)
            BottomTab(
                modifier = modifier,
                image = Icons.Default.PlayArrow,
                title = "Play",
                onTabAction = { onTabAction(BottomSheetAction.PLAY) }
            )
            BottomTab(
                modifier = modifier,
                image = Icons.Default.Share,
                title = "Share",
                onTabAction = { onTabAction(BottomSheetAction.SHARE) }
            )
            BottomTab(
                modifier = modifier,
                image = Icons.Default.Delete,
                title = "Delete",
                onTabAction = { onTabAction(BottomSheetAction.DELETE) }
            )
        }
    }

    @Composable
    fun BottomTab(
        modifier: Modifier,
        image: ImageVector,
        title: String,
        onTabAction: (action: BottomSheetAction) -> Unit
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxHeight()
                .clickable {
                    onTabAction(
                        BottomSheetAction.valueOf(
                            title.uppercase(Locale.getDefault())
                        )
                    )
                }
        ) {
            Icon(imageVector = image, contentDescription = null)
            Text(text = title)
        }
    }

    @Composable
    fun EmptyList() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = drawable.ic_empty),
                contentDescription = "Empty list logo"
            )

            Text(
                text = "No Video",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                )
            )

            Spacer(
                modifier = Modifier
                    .height(14.dp)
            )

            Text(
                text = "Start by downloading some videos",
                style = TextStyle(fontSize = 18.sp),
            )
        }
    }

    @ExperimentalAnimationApi
    @Composable
    fun VideoCard(
        selectionMode: Boolean = false,
        video: FBVideo,
        onSelectionChanged: (selected: Boolean) -> Unit,
        selected: Boolean,
        onPlay: () -> Unit,
        onclick: () -> Unit,
    ) {
        val retriever = MediaMetadataRetriever()

        try {
            retriever.setDataSource(video.path)
        } catch (e: Exception) {
            Timber.e(e)
        }

        var image: ImageBitmap? by remember { mutableStateOf(null) }
        var duration: String by remember { mutableStateOf("00:00:00") }

        LaunchedEffect(Unit) {
            image = retriever.frameAtTime?.asImageBitmap()
            duration = getDurationString(
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                    ?: 0L
            )
            retriever.release()
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Box {
                // background image extracted from the media
                image?.let { it ->
                    Image(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        onSelectionChanged(!selected)
                                    },
                                    onTap = {
                                        onclick()
                                    }
                                )
                            },
                        bitmap = it,
                        contentDescription = video.title,
                        contentScale = ContentScale.FillBounds
                    )
                }

                // dark overlay on the picture
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = SolidColor(Color.Black), alpha = 0.40f),
                    contentAlignment = Alignment.TopEnd
                ) {
                    AnimatedVisibility(
                        visible = selectionMode,
                        enter = slideInHorizontally({ it / 2 }) + fadeIn(),
                        exit = slideOutHorizontally({ it / 2 }) + fadeOut()
                    ) {
                        Checkbox(
                            modifier = Modifier
                                .padding(10.dp),
                            checked = selected,
                            onCheckedChange = {
                                onSelectionChanged(!selected)
                            },
                            colors = CheckboxDefaults.colors(
                                checkmarkColor = Color.Black,
                                uncheckedColor = Color.White,
                                checkedColor = Color.White
                            )
                        )
                    }

                    // Play icon
                    Icon(
                        modifier = Modifier
                            .clickable {
                                onPlay()
                            }
                            .align(Alignment.Center)
                            .size(80.dp),
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Icon",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(5.dp)
                        .align(Alignment.TopEnd)
                        .size(width = 96.dp, height = 30.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(20.dp)
                        )
                ) {
                    Text(
                        text = duration,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.BottomStart),
                    text = video.title,
                    color = Color.White,
                    style = MaterialTheme.typography.subtitle2
                )
            }
        }
    }
}
