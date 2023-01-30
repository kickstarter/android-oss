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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.stringsFromHtmlTranslation
import type.FlaggingKind

@Preview(widthDp = 300, heightDp = 300)
@Composable
fun PreviewTextLinks() {
    MaterialTheme {
        TextWithClickableLink(html = stringResource(id = R.string.FPO_projects_may_not_offer_items))
    }
}

@Preview(widthDp = 300, heightDp = 300)
@Composable
fun ReportProjectScreenPreview() {
    MaterialTheme {
        ReportProjectCategoryScreen(PaddingValues())
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

@Composable
fun rulesMap(): Map<Triple<String, String, Boolean>, List<Triple<String, String, String>>> {

    val projectCat = Triple(
        stringResource(id = R.string.FPO_this_project_breaks_one_of_our_rules),
        stringResource(id = R.string.FPO_projects_may_not_offer_items),
        true // Has a link om the subtitle
    )
    val rulesListProject = listOf(
        Triple(stringResource(id = R.string.FPO_Prohibided_Items), stringResource(id = R.string.FPO_Prohibided_Items_desc), FlaggingKind.PROHIBITED_ITEMS.rawValue()),
        Triple(stringResource(id = R.string.FPO_Copying_reselling_or), stringResource(id = R.string.FPO_Copying_reselling_or_desc), FlaggingKind.RESALE.rawValue()),
        Triple(stringResource(id = R.string.FPO_prototype_misrepresentation), stringResource(id = R.string.FPO_prototype_misrepresentation_desc), FlaggingKind.PROTOTYPE_MISREPRESENTATION.rawValue()),
        Triple(stringResource(id = R.string.FPO_suspicious_creator), stringResource(id = R.string.FPO_suspicious_creator_desc), FlaggingKind.POST_FUNDING_SUSPICIOUS_THIRD_PARTY.rawValue()), // TODO check this one on web
        Triple(stringResource(id = R.string.FPO_Not_raising_for_creative), stringResource(id = R.string.FPO_Not_raising_for_creative_desc), FlaggingKind.NOT_PROJECT.rawValue()) // TODO check on web
    )

    val spamCat = Triple(
        stringResource(id = R.string.FPO_report_spam_or_abusive),
        stringResource(id = R.string.FPO_report_spam_or_abusive_subtitle),
        true // Has a link om the subtitle
    )
    val rulesListSpam = listOf(
        Triple(stringResource(id = R.string.FPO_Spam), stringResource(id = R.string.FPO_Spam_desc), FlaggingKind.SPAM.rawValue()),
        Triple(stringResource(id = R.string.FPO_Abuse), stringResource(id = R.string.FPO_Abuse_desc), FlaggingKind.ABUSE.rawValue()),
    )

    val intellectualCat = Triple(
        stringResource(id = R.string.FPO_Intellectual_property_violation),
        stringResource(id = R.string.FPO_Intellectual_property_violation_Subtitle),
        false // Has a link om the subtitle
    )

    val rulesListIntellectual = listOf(
        Triple(stringResource(id = R.string.FPO_Intellectual_property_violation), stringResource(id = R.string.FPO_Intellectual_property_violation_desc), FlaggingKind.NOT_PROJECT.rawValue()), // TODO check on web
    )

    return mapOf(
        Pair(projectCat, rulesListProject),
        Pair(spamCat, rulesListSpam),
        Pair(intellectualCat, rulesListIntellectual)
    )
}

@Composable
fun Rules(rule: Triple<String, String, String>, navigationAction: (String) -> Unit) {
    Row(
        modifier = Modifier
            .padding(vertical = dimensionResource(id = R.dimen.grid_1))
            .clickable {
                navigationAction(rule.third)
            }
    ) {
        Column(
            modifier = Modifier
                .padding(PaddingValues(start = dimensionResource(id = R.dimen.grid_4)))
                .weight(1F)
        ) {
            Text(
                text = rule.first,
                style = MaterialTheme.typography.subtitle2.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = rule.second,
                style = MaterialTheme.typography.body2
            )
        }
        IconButton(
            onClick = {
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
fun RulesList(
    rulesList: List<Triple<String, String, String>>,
    navigationAction: (String) -> Unit = {}
) {
    // - Do not use LazyColum, will conflict the scroll events with the parent LazyColumn
    Column {
        rulesList.map { rule ->
            Rules(rule = rule, navigationAction)
        }
    }
}

@Composable
fun CategoryRow(
    category: Triple<String, String, Boolean>,
    rulesList: List<Triple<String, String, String>>,
    navigationAction: (String) -> Unit = {}
) {
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
                if (!category.third) { // Subtitles without links
                    val text = ViewUtils.html(category.second).toString()
                    Text(
                        text = text,
                        style = MaterialTheme.typography.body1
                    )
                } else {
                    TextWithClickableLink(html = category.second)
                }
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
            RulesList(rulesList = rulesList, navigationAction)
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
fun ReportProjectCategoryScreen(
    padding: PaddingValues,
    navigationAction: (String) -> Unit = {}
) {
    Surface(
        modifier = Modifier.animateContentSize()
    ) {

        val categories = rulesMap().keys.toList()
        Spacer(modifier = Modifier.padding(padding))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(id = R.color.kds_white))
        ) {
            items(items = categories) { key ->
                rulesMap()[key]?.let { value ->
                    CategoryRow(category = key, rulesList = value, navigationAction)
                }
            }
        }
    }
}

@Composable
fun TextWithClickableLink(html: String) {

    val stringList = html.stringsFromHtmlTranslation()
    if (stringList.size == 3) {
        val annotatedText = buildAnnotatedString {
            append(stringList.first())
            pushStringAnnotation(
                tag = stringList[1],
                annotation = ""
            )
            withStyle(
                style = SpanStyle(
                    color = colorResource(id = R.color.kds_create_700),
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(stringList[1])
            }
            append(stringList.last())

            pop()
        }

        val context = LocalContext.current
        ClickableText(
            text = annotatedText,
            style = MaterialTheme.typography.body1,
            onClick = {
                annotatedText.getStringAnnotations(
                    tag = stringList[1], start = it,
                    end = it
                )
                    .firstOrNull()?.let { annotation ->
                        Toast
                            .makeText(
                                context,
                                "Will open a formulary screen on next Story",
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    }
            }
        )
    }
}
