package com.kickstarter.ui.fragments.projectpage.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme

@Preview(
    name = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun SimilarProjectPlaceholderCardPreview() {
    KSTheme {
        Column(Modifier.fillMaxSize()) {
            SimilarProjectPlaceholderCard {
                // Figma size
                Box(modifier = Modifier.size(320.dp, 270.dp))
            }
            Spacer(Modifier.height(8.dp))
            SimilarProjectPlaceholderCard {
                Box(modifier = Modifier.size(320.dp, 180.dp))
            }
            Spacer(Modifier.height(8.dp))
            SimilarProjectPlaceholderCard {
                Box(modifier = Modifier.size(256.dp, 256.dp))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun SimilarProjectPlaceholderCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier
    ) {
        Box(
            // Either this box has alpha 0...
            modifier = Modifier.alpha(0f)
        ) {
            content()
        }
        Box(
            // Or this box has an opaque background color
            modifier = Modifier.matchParentSize()
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(CornerSize(4)))
                        .background(colorResource(R.color.kds_legacy_grey_500))
                        .fillMaxWidth()
                        .weight(198f / 270f)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(72f / 270f)
                ) {
                    Spacer(
                        modifier = Modifier
                            .weight(12f / 72f)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(20f / 72f)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .fillMaxHeight()
                                .weight(227f)
                                .background(colorResource(R.color.kds_legacy_grey_500))
                        )
                        Spacer(
                            modifier = Modifier
                                .weight(85f)
                        )
                    }
                    Spacer(
                        modifier = Modifier
                            .weight(8f / 72f)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(20f / 72f)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .fillMaxHeight()
                                .weight(136f / 312f)
                                .background(colorResource(R.color.kds_legacy_grey_500))
                        )
                        Spacer(
                            modifier = Modifier
                                .weight(176f / 312f)
                        )
                    }
                    Spacer(
                        modifier = Modifier
                            .weight(12f / 72f)
                    )
                }
            }
        }
    }
}
