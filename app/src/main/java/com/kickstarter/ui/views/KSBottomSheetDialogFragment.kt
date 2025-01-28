package com.kickstarter.ui.views

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kickstarter.R
import com.kickstarter.databinding.ModalBottomSheetContentBinding
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.shapes
import com.kickstarter.ui.views.compose.projectpage.KSBottomSheetContent

class KSBottomSheetDialogFragment(
    val bodyText: String?,
    val onCtaClicked: () -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: ModalBottomSheetContentBinding

    override fun getTheme(): Int = R.style.BottomSheetDialogStyle

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = BottomSheetDialog(requireContext(), theme)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = ModalBottomSheetContentBinding.inflate(inflater, container, false)

        val composeView = binding.composeView

        composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                KSTheme {
                    Column(modifier = Modifier.padding(dimensions.paddingLarge)) {
                        Row(
                            modifier = Modifier
                                .background(
                                    color = colors.backgroundAccentGrayBold,
                                    shape = shapes.small
                                )
                                .width(dimensions.paddingXXLarge)
                                .height(dimensions.paddingXSmall)
                                .align(Alignment.CenterHorizontally)

                        ) { }

                        KSBottomSheetContent(
                            title = "Kickstarter has restricted this creator",
                            body = bodyText,
                            onLinkClicked = onCtaClicked,
                            onClose = { close() }
                        )
                    }


                }
            }
        }
        return binding.root
    }

    fun close() = this.dismiss()

    companion object {
        const val TAG = "BottomSheetDialogFragment"
    }
}