package com.kickstarter.ui.compose

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R

@Preview(showBackground = true, backgroundColor = 0xFFF0EAE2)
@Composable
fun ToolBarPreview() {
    MaterialTheme {
        TopToolBar(
            title = "KS generic toolbar",
            rightIcon = Icons.Filled.Search
        )
    }
}

@Composable
fun TopToolBar(
    title: String,
    leftIcon: ImageVector = Icons.Filled.ArrowBack,
    rightIcon: ImageVector? = null,
    leftOnClickAction: () -> Unit = {},
    rightOnClickAction: () -> Unit = {}
) {

    TopAppBar(
        title = {
            Text(
                title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = { leftOnClickAction() }) {
                Icon(
                    imageVector = leftIcon,
                    contentDescription = stringResource(id = R.string.back)
                )
            }
        },
        actions = {
            rightIcon?.let {
                IconButton(onClick = { rightOnClickAction() }) {
                    Icon(
                        imageVector = it,
                        contentDescription = null
                    )
                }
            }
        },
        backgroundColor = colorResource(id = R.color.kds_white)
    )
}
