package com.kickstarter.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kickstarter.R
import com.kickstarter.databinding.ModalBottomSheetContentBinding
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.views.compose.projectpage.KSBottomSheetContent

class KSModalBottomSheet(
    val bodyText: String?,
    val onCtaClicked: () -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: ModalBottomSheetContentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = ModalBottomSheetContentBinding.inflate(inflater, container, false)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogStyle)

        val composeView = binding.composeView

        composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                KSTheme {
                    KSBottomSheetContent(
                        text = bodyText,
                        onLinkClicked = onCtaClicked,
                        onClose = { close() }
                    )
                }
            }
        }
        return binding.root
    }

    fun close() = this.dismiss()

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}