package com.kickstarter.ui.activities.compose

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
import com.kickstarter.libs.utils.extensions.hrefUrlFromTranslation
import com.kickstarter.libs.utils.extensions.stringsFromHtmlTranslation
import com.kickstarter.viewmodels.ReportProjectViewModel
import type.FlaggingKind

@Preview(widthDp = 300, heightDp = 300)
@Composable
fun PreviewTextLinks() {
    MaterialTheme {
        TextWithClickableLink(html = stringResource(id = R.string.Projects_may_not_offer))
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
        stringResource(id = R.string.This_project_breaks),
        stringResource(id = R.string.Projects_may_not_offer),
        true // Has a link om the subtitle
    )
    val rulesListProject = listOf(
        Triple(stringResource(id = R.string.Prohibited_items), stringResource(id = R.string.Projects_may_not_offer), FlaggingKind.PROHIBITED_ITEMS.rawValue()),
        Triple(stringResource(id = R.string.Copying_reselling), stringResource(id = R.string.Projects_cannot_plagiarize), FlaggingKind.RESALE.rawValue()),
        Triple(stringResource(id = R.string.Prototype_misrepresentation), stringResource(id = R.string.Creators_must_be_transparent), FlaggingKind.PROTOTYPE_MISREPRESENTATION.rawValue()),
        Triple(stringResource(id = R.string.Suspicious_creator_behavior), stringResource(id = R.string.Project_creators_and_their), FlaggingKind.POST_FUNDING_SUSPICIOUS_THIRD_PARTY.rawValue()), // TODO check this one on web
        Triple(stringResource(id = R.string.Not_raising_funds), stringResource(id = R.string.Projects_on), FlaggingKind.NOT_PROJECT.rawValue()) // TODO check on web
    )

    val spamCat = Triple(
        stringResource(id = R.string.Report_spam),
        stringResource(id = R.string.Our),
        true // Has a link om the subtitle
    )
    val rulesListSpam = listOf(
        Triple(stringResource(id = R.string.Spam), stringResource(id = R.string.Ex_using), FlaggingKind.SPAM.rawValue()),
        Triple(stringResource(id = R.string.Abuse), stringResource(id = R.string.Ex_posting), FlaggingKind.ABUSE.rawValue()),
    )

    val intellectualCat = Triple(
        stringResource(id = R.string.Intellectual_property_violation),
        stringResource(id = R.string.A_project_is_infringing),
        false // Has a link om the subtitle
    )

    val rulesListIntellectual = listOf(
        Triple(stringResource(id = R.string.Intellectual_property_violation), stringResource(id = R.string.Kickstarter_takes_claims), FlaggingKind.NOT_PROJECT.rawValue()), // TODO check on web
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
    navigationAction: (String) -> Unit = {},
    inputs: ReportProjectViewModel.Inputs? = null,
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
                    TextWithClickableLink(
                        html = category.second,
                        onClickCallback = { tag ->
                            tag?.let { inputs?.openExternalBrowser(tag) }
                        }
                    )
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
    navigationAction: (String) -> Unit = {},
    inputs: ReportProjectViewModel.Inputs? = null
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
                    CategoryRow(category = key, rulesList = value, navigationAction, inputs)
                }
            }
        }
    }
}

@Composable
fun TextWithClickableLink(
    html: String,
    onClickCallback: (String?) -> Unit = {}
) {

    val stringList = html.stringsFromHtmlTranslation()
    val annotation = html.hrefUrlFromTranslation()
    if (stringList.size == 3) {
        val annotatedText = buildAnnotatedString {
            append(stringList.first())
            pushStringAnnotation(
                tag = annotation,
                annotation = stringList[1]
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

        ClickableText(
            text = annotatedText,
            style = MaterialTheme.typography.body1,
            onClick = {
                annotatedText.getStringAnnotations(
                    tag = annotation, start = it,
                    end = it
                )
                    .firstOrNull()?.let { annotation ->
                        onClickCallback(annotation.tag)
                    } ?: onClickCallback(null)
            }
        )
    }
}