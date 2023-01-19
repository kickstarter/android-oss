package com.kickstarter.ui.activities.compose

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.ui.compose.TopToolBar

@Composable
fun rulesMap(): Map<Pair<String, String>, List<Pair<String, String>>> {

    val projectCat = Pair(
        stringResource(id = R.string.FPO_this_project_breaks_one_of_our_rules),
        stringResource(id = R.string.FPO_projects_may_not_offer_items)
    )
    val rulesListProject = listOf(
        Pair(stringResource(id = R.string.FPO_Prohibided_Items), stringResource(id = R.string.FPO_Prohibided_Items_desc)),
        Pair(stringResource(id = R.string.FPO_Copying_reselling_or), stringResource(id = R.string.FPO_Copying_reselling_or_desc)),
        Pair(stringResource(id = R.string.FPO_prototype_misrepresentation), stringResource(id = R.string.FPO_prototype_misrepresentation_desc)),
        Pair(stringResource(id = R.string.FPO_suspicious_creator), stringResource(id = R.string.FPO_suspicious_creator_desc)),
        Pair(stringResource(id = R.string.FPO_Not_raising_for_creative), stringResource(id = R.string.FPO_Not_raising_for_creative_desc))
    )

    val spamCat = Pair(
        stringResource(id = R.string.FPO_report_spam_or_abusive),
        stringResource(id = R.string.FPO_report_spam_or_abusive_subtitle)
    )
    val rulesListSpam = listOf(
        Pair(stringResource(id = R.string.FPO_Spam), stringResource(id = R.string.FPO_Spam_desc)),
        Pair(stringResource(id = R.string.FPO_Abuse), stringResource(id = R.string.FPO_Abuse_desc)),
    )

    val intellectualCat = Pair(
        stringResource(id = R.string.FPO_Intellectual_property_violation),
        stringResource(id = R.string.FPO_Intellectual_property_violation_Subtitle)
    )

    val rulesListIntellectual = listOf(
        Pair(stringResource(id =  R.string.FPO_Intellectual_property_violation), stringResource(id = R.string.FPO_Intellectual_property_violation_desc)),
    )

    return mapOf(
        Pair(projectCat, rulesListProject),
        Pair(spamCat, rulesListSpam),
        Pair(intellectualCat, rulesListIntellectual)
    )
}

@Composable
fun Rules(rulePair: Pair<String, String>) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .padding(vertical = dimensionResource(id = R.dimen.grid_1))
            .clickable {
                Toast
                    .makeText(
                        context,
                        "Will open a formulary screen on next Story",
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
    ) {
        Column(
            modifier = Modifier
                .padding(PaddingValues(start = dimensionResource(id = R.dimen.grid_4)))
                .weight(1F)
        ) {
            Text(
                text = rulePair.first,
                style = MaterialTheme.typography.subtitle2.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = rulePair.second,
                style = MaterialTheme.typography.body2
            )
        }
        IconButton(
            onClick = {
                Toast
                    .makeText(
                        context,
                        "Will open a formulary screen on next Story",
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null
            )
        }
    }
}

@Composable
fun RulesList(rulesList: List<Pair<String, String>>) {
    // - Do not use LazyColum, will conflict the scroll events with the parent LazyColumn
    Column {
        rulesList.map { rule ->
            Rules(rulePair = rule)
        }
    }
}

@Composable
fun CategoryRow(category: Pair<String, String>, rulesList: List<Pair<String, String>>) {
    val expanded = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .padding(
                    PaddingValues(
                        start = dimensionResource(id = R.dimen.grid_3),
                        top = dimensionResource(id = R.dimen.grid_2)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = category.first,
                    style = MaterialTheme.typography.h6.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = category.second,
                    style = MaterialTheme.typography.body1
                )
            }
            IconButton(
                onClick = {
                    expanded.value = !expanded.value
                }
            ) {
                Icon(
                    painter = painterResource(
                        id =
                        if (expanded.value) R.drawable.ic_arrow_up
                        else R.drawable.ic_arrow_down
                    ),
                    contentDescription = null
                )
            }
        }
        if (expanded.value) {
            RulesList(rulesList = rulesList)
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensionResource(id = R.dimen.grid_2),
                    vertical = dimensionResource(id = R.dimen.grid_2)
                )
                .background(
                    color = colorResource(id = R.color.kds_support_300)
                )
                .height(1.dp)
        )
    }
}

@Composable
fun ReportProjectCategoryScreen() {
    Scaffold(
        topBar = {
            TopToolBar(
                title = stringResource(id = R.string.FPO_report_project_title)
            )
        },
        content = {
            val categories = rulesMap().keys.toList()
            Spacer(modifier = Modifier.padding(it))
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(items = categories) { key ->
                    rulesMap()[key]?.let { value ->
                        CategoryRow(category = key, rulesList = value)
                    }
                }
            }
        }
    )
}

@Preview(widthDp = 300, heightDp = 300)
@Composable
fun ReportProjectScreenPreview() {
    MaterialTheme {
        ReportProjectCategoryScreen()
    }
}

@Preview(widthDp = 300, heightDp = 300)
@Composable
fun CategoryRowPreview() {
    MaterialTheme {
        val list = rulesMap().values.first()
        val list2 = rulesMap().keys.first()
        CategoryRow(category = list2, rulesList = list)
    }
}

@Preview(widthDp = 300, heightDp = 300)
@Composable
fun RulesListPreview() {
    MaterialTheme {
        val list = rulesMap().values.first()
        RulesList(rulesList = list)
    }
}
