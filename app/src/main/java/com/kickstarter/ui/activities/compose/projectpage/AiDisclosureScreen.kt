package com.kickstarter.ui.activities.compose.projectpage

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.models.AiDisclosure
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.viewmodels.projectpage.ProjectAIViewModel

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun AiDisclosureScreenPreview() {
    KSTheme {
        val aiDisclosure = AiDisclosure.builder()
            .generatedByAiConsent("Some generated consent")
            .generatedByAiDetails("Some generated details")
            .otherAiDetails("Other details to include")
            .build()
        AiDisclosureScreen(
            state = ProjectAIViewModel.UiState(aiDisclosure = aiDisclosure),
            clickCallback = {}
        )
    }
}

@Composable
fun AiDisclosureScreen(
    state: ProjectAIViewModel.UiState,
    clickCallback: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                PaddingValues(
                    start = KSTheme.dimensions.paddingMediumLarge,
                    top = KSTheme.dimensions.paddingMedium,
                    end = KSTheme.dimensions.paddingMedium
                )
            ),
        verticalArrangement = Arrangement.spacedBy(KSTheme.dimensions.paddingMediumSmall)
    ) {
        Text(
            text = stringResource(id = R.string.Use_of_ai_fpo),
            style = KSTheme.typography.title1Bold,
            color = KSTheme.colors.kds_support_700
        )
        Text(
            text = stringResource(id = R.string.My_project_seeks_founding_fpo),
            style = KSTheme.typography.headline,
            color = KSTheme.colors.kds_support_700
        )
        Text(
            text = stringResource(id = R.string.For_the_database_orsource_fpo),
            style = KSTheme.typography.footnote,
            color = KSTheme.colors.kds_support_700
        )
        Text(
            text = stringResource(id = R.string.The_owners_of_fpo),
            style = KSTheme.typography.footnote,
            color = KSTheme.colors.kds_support_700
        )
        Text(
            text = stringResource(id = R.string.There_is_or_will_be_fpo),
            style = KSTheme.typography.footnote,
            color = KSTheme.colors.kds_support_700
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = KSTheme.colors.kds_support_300
                )
                .height(KSTheme.dimensions.dividerThickness)
        )
        Text(
            text = stringResource(id = R.string.I_plan_to_use_AI_fpo),
            style = KSTheme.typography.headline,
            color = KSTheme.colors.kds_support_700
        )
        Text(
            text = state.aiDisclosure?.generatedByAiDetails ?: "",
            style = KSTheme.typography.footnote,
            color = KSTheme.colors.kds_support_700
        )

        Text(
            text = state.aiDisclosure?.generatedByAiConsent ?: "",
            style = KSTheme.typography.footnote,
            color = KSTheme.colors.kds_support_700
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = KSTheme.colors.kds_support_300
                )
                .height(KSTheme.dimensions.dividerThickness)
        )
        Text(
            text = stringResource(id = R.string.I_am_incorporating_AI_fpo),
            style = KSTheme.typography.headline,
            color = KSTheme.colors.kds_support_700
        )

        Text(
            text = state.aiDisclosure?.otherAiDetails ?: "",
            style = KSTheme.typography.footnote,
            color = KSTheme.colors.kds_support_700
        )

        // - TODO: extract as reusable composable to DesignSystem
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = KSTheme.colors.kds_support_300
                )
                .height(KSTheme.dimensions.dividerThickness)
        )

        // - TODO: extract to reusable composable to DesignSysmtem, only requires the callback
        ClickableText(
            text = AnnotatedString(
                text = stringResource(id = R.string.Learn_about_AI_fpo),
                spanStyle = SpanStyle(
                    color = KSTheme.colors.kds_create_700,
                    textDecoration = TextDecoration.Underline,
                )
            ),
            onClick = {
                clickCallback(state.openExternalUrl)
            }
        )
    }
}
