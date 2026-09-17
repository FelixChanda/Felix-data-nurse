package com.example.data

import com.example.model.Flashcard
import com.example.model.ResourceItem

object FlashcardEngine {

    fun generateCards(resource: ResourceItem, targetCount: Int = 8): List<Flashcard> {
        val list = mutableListOf<Flashcard>()
        var idx = 1

        // 1. From Past Paper questions
        resource.questions.forEach { q ->
            var ans = q.markingScheme
            if (q.options.isNotEmpty() && q.correctOptionIndex != null && q.correctOptionIndex in q.options.indices) {
                val letter = ('A' + q.correctOptionIndex)
                ans = "${q.options[q.correctOptionIndex]} (Option $letter)\n\n$ans"
            }
            list.add(
                Flashcard(
                    id = "fc-${resource.id}-${idx++}",
                    question = q.questionText,
                    answer = ans.ifBlank { q.clinicalRationale },
                    category = if (q.type == "scenario_case") "NCLEX Case" else "Clinical Rationale",
                    explanation = q.clinicalRationale.ifBlank { q.markingScheme },
                    keyPearl = q.highYieldTip ?: "Priority Rule: Physiological stabilization always supersedes documentation.",
                    difficulty = "NCLEX High-Yield"
                )
            )
        }

        // 2. From Syllabus Units
        resource.syllabus.forEach { unit ->
            unit.keyCompetencies.forEach { comp ->
                list.add(
                    Flashcard(
                        id = "fc-${resource.id}-${idx++}",
                        question = "In \"${resource.title}\", Unit ${unit.unitNumber} (${unit.title}):\nHow does the nurse demonstrate \"$comp\"?",
                        answer = "Apply strict evidence-based clinical protocols covering: ${unit.topics.take(3).joinToString(", ")}.",
                        category = "Priority Action",
                        explanation = "Unit ${unit.unitNumber} clinical competencies mandate mastery of ${unit.title}.",
                        keyPearl = "Safety Check: Always verify patient identity with 2 distinct identifiers prior to interventions.",
                        difficulty = "Standard"
                    )
                )
            }
        }

        // 3. From Textbook Chapters
        resource.tableOfContents.forEach { ch ->
            ch.keyPearls.forEach { pearl ->
                list.add(
                    Flashcard(
                        id = "fc-${resource.id}-${idx++}",
                        question = "Chapter ${ch.chapterNumber} (\"${ch.title}\") Clinical Pearl:\nWhat is the essential evidence-based practice standard?",
                        answer = pearl,
                        category = "Clinical Rationale",
                        explanation = ch.summary,
                        keyPearl = "Core clinical pearl from ${resource.title}.",
                        difficulty = "NCLEX High-Yield"
                    )
                )
            }
        }

        // 4. From High Yield Points
        resource.highYieldKeyPoints.forEach { pt ->
            list.add(
                Flashcard(
                    id = "fc-${resource.id}-${idx++}",
                    question = "Clinical Alert for ${resource.title}:\nWhat priority safety principle must the nurse enforce?",
                    answer = pt,
                    category = "Priority Action",
                    explanation = "Essential safety protocol established for ${resource.domain}.",
                    keyPearl = "High-Alert Rule: Independent double-check is mandatory for insulin, heparin, and chemotherapeutics.",
                    difficulty = "NCLEX High-Yield"
                )
            )
        }

        // 5. From Clinical Sections
        resource.sections.forEach { sec ->
            list.add(
                Flashcard(
                    id = "fc-${resource.id}-${idx++}",
                    question = "Regarding \"${sec.title}\" in ${resource.title}:\nWhat is the clinical management guideline?",
                    answer = sec.content.take(240) + if (sec.content.length > 240) "..." else "",
                    category = "Diagnostic Sign",
                    explanation = sec.callout?.text ?: "Standard clinical workflow guideline.",
                    keyPearl = sec.callout?.text ?: "Monitor vital signs and patient response continuously.",
                    difficulty = "Standard"
                )
            )
        }

        // Fallback default cards if resource has no structured arrays
        if (list.isEmpty()) {
            list.add(
                Flashcard(
                    id = "fc-${resource.id}-def-1",
                    question = "What are the core educational outcomes of \"${resource.title}\"?",
                    answer = resource.description,
                    category = "Core Recall",
                    explanation = "Primary focus: ${resource.domain} (${resource.yearLevel}).",
                    keyPearl = "Remember the ADPIE nursing process framework: Assessment, Diagnosis, Planning, Implementation, Evaluation.",
                    difficulty = "Standard"
                )
            )
            list.add(
                Flashcard(
                    id = "fc-${resource.id}-def-2",
                    question = "What is the primary ABC triage rule applicable in ${resource.domain}?",
                    answer = "Airway, Breathing, Circulation take precedence over all non-emergent interventions and documentation.",
                    category = "Priority Action",
                    explanation = "Patient physiological stability is the foundation of clinical nursing practice.",
                    keyPearl = "Assessment before intervention: Never treat an unverified deterioration blindly.",
                    difficulty = "NCLEX High-Yield"
                )
            )
        }

        return list.take(maxOf(targetCount, 6))
    }
}
