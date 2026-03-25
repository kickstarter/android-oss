package com.kickstarter.features.projectstory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.features.projectstory.data.RichTextItem
import com.kickstarter.features.projectstory.ui.RichTextItemPhotoComponent
import com.kickstarter.features.projectstory.ui.RichTextItemTextComponent
import com.kickstarter.features.projectstory.ui.WebViewComponent
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.isDarkModeEnabled
import com.kickstarter.ui.compose.ProjectImageFromURl
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class ProjectStoryActivity : ComponentActivity() {

    private lateinit var projectStoryViewModelFactory: ProjectStoryViewModel.Factory
    private val projectStoryViewModel: ProjectStoryViewModel by viewModels { projectStoryViewModelFactory }
    private val compositeDisposable = CompositeDisposable()
    private var ksString: KSString? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.ksString = requireNotNull(getEnvironment()?.ksString())

        this.getEnvironment()?.let { env ->
            projectStoryViewModelFactory = ProjectStoryViewModel.Factory(env)

            setContent {
                val context = LocalContext.current

                val uriHandler = object : UriHandler {
                    override fun openUri(uri: String) {
                        ApplicationUtils.openUrlExternally(context, uri)
                    }
                }

                KickstarterApp(
                    useDarkTheme = isDarkModeEnabled(env)
                ) {
                    val uiState = projectStoryViewModel.projectStoryUiState.collectAsState()
                    val txtState = projectStoryViewModel.txt
                    CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                        CampaignScreen(uiState, txtState)
//                        CaptionedImageScreen()
                    }
                }
            }
        }
    }

    @Preview
    @Composable
    private fun CampaignScreenPreview() {
        val projectStoryUiState = ProjectStoryUiState()
        val uiState = remember { mutableStateOf(projectStoryUiState) }
        val txtState = remember { mutableStateOf("kiwamiyatei/grill-x-carbon-graphite-grill-for-perfect-charcoal-flavor") }
        KSTheme {
            CampaignScreen(uiState, txtState)
        }
    }

    @Composable
    private fun CampaignScreen(
        uiState: State<ProjectStoryUiState>,
        txtState: State<String>,
    ) {
        val context = LocalContext.current

        val storiedProject = uiState.value.storiedProject
        val project = storiedProject?.project
        val story = storiedProject?.story

        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current

        Scaffold(
            modifier = Modifier.systemBarsPadding(),
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
            ) {
                ProjectImageFromURl(
                    imageUrl = project?.photo()?.full(),
                    modifier = Modifier
                        .background(Color.Red)
                        .aspectRatio(1.77f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = txtState.value,
                    onValueChange = {
                        projectStoryViewModel.updateTxt(it)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
//                    label = { Text("Label") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    modifier = Modifier.height(40.dp),
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        projectStoryViewModel.fetchProject()
                    },
                ) {
                    if (uiState.value.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(1f),
                            color = Color.White
                        )
                    } else {
                        Text("Load content")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Text("${project?.name()}")
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
//                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(story?.items ?: listOf<RichTextItem>(), contentType = { it::class.simpleName }) { item ->
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            when (item) {
                                is RichTextItem.Text -> {
                                    when (item) {
                                        is RichTextItem.Text.Paragraph -> {
                                            val childPhoto = item.children?.firstOrNull { it is RichTextItem.Photo } as? RichTextItem.Photo
                                            when {
                                                childPhoto != null -> {
                                                    val link = item.link
                                                    RichTextItemPhotoComponent(childPhoto, link)
                                                }
                                                else -> RichTextItemTextComponent(item)
                                            }
                                        }
                                        else -> {
                                            RichTextItemTextComponent(item)
                                        }
                                    }
                                }
                                is RichTextItem.Photo -> {
                                    RichTextItemPhotoComponent(item)
                                }
                                is RichTextItem.Oembed -> {
                                    Timber.d("RichTextItem.Oembed item: $item")
                                    if (item.iframeUrl.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().height(224.dp) // .defaultMinSize(minHeight = 200.dp)
                                        ) {
                                            WebViewComponent(item.iframeUrl)
                                        }
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}
