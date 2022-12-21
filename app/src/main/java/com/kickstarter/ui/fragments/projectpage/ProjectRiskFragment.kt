package com.kickstarter.ui.fragments.projectpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kickstarter.R
import com.kickstarter.libs.Configure
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.projectpage.ProjectRiskViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class ProjectRiskFragment :
    Fragment(),
    Configure {

    private lateinit var viewModelFactory: ProjectRiskViewModel.Factory
    private val viewModel: ProjectRiskViewModel.ProjectRiskViewModel by viewModels { viewModelFactory }

    private var disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        this.context?.getEnvironment()?.let { env ->
            viewModelFactory = ProjectRiskViewModel.Factory(env)
        }

        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            // Compose world
            setContent {
                MaterialTheme {
                    RisksScreen(
                        riskDescState = viewModel.projectRisks().subscribeAsState(initial = "")
                    )
                }
            }
        }
    }

    @Composable
    fun RisksScreen(
        riskDescState: State<String>
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(id = R.string.Risks_and_challenges),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                modifier = Modifier
                    .paddingFromBaseline(
                        top = dimensionResource(id = R.dimen.grid_8),
                        bottom = dimensionResource(id = R.dimen.grid_4)
                    )
                    .padding(horizontal = dimensionResource(id = R.dimen.grid_3))
            )
            Text(
                text = riskDescState.value,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .paddingFromBaseline(
                        top = dimensionResource(id = R.dimen.grid_3)
                    )
                    .padding(horizontal = dimensionResource(id = R.dimen.grid_3))
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensionResource(id = R.dimen.grid_3),
                        vertical = dimensionResource(id = R.dimen.grid_3)
                    )
                    .background(
                        color = colorResource(id = R.color.kds_support_300)
                    )
                    .height(1.dp)
            )
            ClickableText(
                text = AnnotatedString(
                    text = stringResource(id = R.string.Learn_about_accountability_on_Kickstarter),
                    spanStyle = SpanStyle(
                        color = colorResource(id = R.color.kds_create_700),
                        textDecoration = TextDecoration.Underline,
                    )
                ),
                onClick = {
                    viewModel.onLearnAboutAccountabilityOnKickstarterClicked()
                },
                modifier = Modifier
                    .paddingFromBaseline(
                        top = dimensionResource(id = R.dimen.grid_3),
                        bottom = dimensionResource(id = R.dimen.grid_5)
                    )
                    .padding(horizontal = dimensionResource(id = R.dimen.grid_3))
            )
        }
    }

    @Preview(showBackground = true, backgroundColor = 0xFFF0EAE2)
    @Composable
    fun ProjectRisksPreview() {
        MaterialTheme {
            val desc = stringResource(id = R.string.risk_description)
            val riskDesc = remember { mutableStateOf(desc) }
            RisksScreen(riskDescState = riskDesc)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        disposables.add(
            this.viewModel.outputs.openLearnAboutAccountabilityOnKickstarter()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    context?.let { context ->
                        ApplicationUtils.openUrlExternally(context, it)
                    }
                }
        )
    }

    override fun configureWith(projectData: ProjectData) {
        this.viewModel.inputs.configureWith(projectData)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
    }

    companion object {
        @JvmStatic
        fun newInstance(position: Int): ProjectRiskFragment {
            val fragment = ProjectRiskFragment()
            val bundle = Bundle()
            bundle.putInt(ArgumentsKey.PROJECT_PAGER_POSITION, position)
            fragment.arguments = bundle
            return fragment
        }
    }
}
