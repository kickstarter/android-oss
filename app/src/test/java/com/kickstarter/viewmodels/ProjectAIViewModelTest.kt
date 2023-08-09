package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.AiDisclosure
import com.kickstarter.viewmodels.projectpage.ProjectAIViewModel
import org.junit.Test

class ProjectAIViewModelTest : KSRobolectricTestCase() {

    private val vm = ProjectAIViewModel.Factory().create(ProjectAIViewModel::class.java)

    @Test
    fun testEmptyState() {
        assertTrue(vm.state.openExternalUrl == ProjectAIViewModel.AIPOLICY)
        assertNull(vm.state.aiDisclosure)
    }

    @Test
    fun testUpdateState() {
        val aiDisc = AiDisclosure
            .builder()
            .id(23L)
            .build()
        val project = ProjectFactory.project()
            .toBuilder()
            .aiDisclosure(aiDisc)
            .build()

        // - Empty state
        assertTrue(vm.state.openExternalUrl == ProjectAIViewModel.AIPOLICY)
        assertNull(vm.state.aiDisclosure)

        val projectData = ProjectDataFactory.project(project = project)
        vm.eventUpdate(ProjectAIViewModel.Event(projectData = projectData))

        // - Check state has been updated after sending event with new data to the VM.
        assertEquals(vm.state.aiDisclosure, aiDisc)
        assertEquals(vm.state.openExternalUrl, ProjectAIViewModel.AIPOLICY)
    }
}
