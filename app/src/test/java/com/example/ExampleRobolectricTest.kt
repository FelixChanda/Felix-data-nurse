package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ClinicalQuizEngine
import com.example.data.DataLoader
import com.example.data.FlashcardEngine
import com.example.data.LabValuesData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `verify app name resource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("DATANURSE", appName)
    }

    @Test
    fun `verify all curated nursing resources loaded from assets`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val resources = DataLoader.loadInitialResources(context)
        assertEquals(21, resources.size)

        // Verify categories
        val modules = resources.filter { it.category == "modules" }
        val pastPapers = resources.filter { it.category == "past_papers" }
        val textbooks = resources.filter { it.category == "textbooks" }
        val notes = resources.filter { it.category == "notes" }
        val documents = resources.filter { it.category == "documents" }

        assertEquals(7, modules.size)
        assertEquals(3, pastPapers.size)
        assertEquals(3, textbooks.size)
        assertEquals(4, notes.size)
        assertEquals(4, documents.size)
    }

    @Test
    fun `verify optimum conditions and vitals targets loaded`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val conditions = DataLoader.loadOptimumConditions(context)
        assertEquals(10, conditions.size)
        assertTrue(conditions.any { it.parameter.contains("Blood Pressure", ignoreCase = true) })
        assertTrue(conditions.any { it.parameter.contains("Oxygen Saturation", ignoreCase = true) })
    }

    @Test
    fun `verify nursing topics and OSCE stations loaded`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val topics = DataLoader.loadNursingTopics(context)
        val osceStations = DataLoader.loadOsceVideos(context)

        assertEquals(6, topics.size)
        assertEquals(12, osceStations.size)
        assertTrue(topics.any { it.region.contains("Zambia", ignoreCase = true) })
        assertTrue(osceStations.any { it.institutionBadge.contains("UNZA", ignoreCase = true) })
    }

    @Test
    fun `verify flashcard engine generates valid cards`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val resources = DataLoader.loadInitialResources(context)
        val pastPaper = resources.first { it.category == "past_papers" }
        val cards = FlashcardEngine.generateCards(pastPaper)

        assertTrue(cards.isNotEmpty())
        assertTrue(cards.first().question.isNotBlank())
        assertTrue(cards.first().answer.isNotBlank())
    }

    @Test
    fun `verify clinical quiz engine generates 4-choice questions`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val resources = DataLoader.loadInitialResources(context)
        val module = resources.first { it.category == "modules" }
        val quiz = ClinicalQuizEngine.generateQuiz(module)

        assertTrue(quiz.isNotEmpty())
        val q = quiz.first()
        assertEquals(4, q.options.size)
        assertTrue(q.correctAnswerIndex in 0..3)
        assertTrue(q.explanation.isNotBlank())
    }

    @Test
    fun `verify lab values reference contains panic alerts`() {
        val labs = LabValuesData.LAB_VALUES
        assertTrue(labs.isNotEmpty())
        val potassium = labs.first { it.testName.contains("Potassium") }
        assertTrue(potassium.panicHigh?.isNotBlank() == true)
        assertTrue(potassium.nursingAlert.contains("Never administer IV Potassium push"))
    }
}
