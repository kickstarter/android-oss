package com.kickstarter.ui.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
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
                    contentDescription = "Localized description"
                )
            }
        },
        actions = {
            rightIcon?.let {
                IconButton(onClick = { rightOnClickAction() }) {
                    Icon(
                        imageVector = it,
                        contentDescription = "Localized description"
                    )
                }
            }
        }
    )
}

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
