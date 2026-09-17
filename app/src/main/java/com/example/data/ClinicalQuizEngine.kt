package com.example.data

import com.example.model.QuizQuestion
import com.example.model.ResourceItem

object ClinicalQuizEngine {

    fun generateQuiz(resource: ResourceItem, targetCount: Int = 5): List<QuizQuestion> {
        val list = mutableListOf<QuizQuestion>()
        var idx = 1

        // 1. Existing past paper questions
        resource.questions.forEach { q ->
            val options: List<String>
            val correctIndex: Int
            if (q.options.size >= 4) {
                options = q.options.take(4)
                correctIndex = (q.correctOptionIndex ?: 0).coerceIn(0, 3)
            } else {
                val correctText = q.markingScheme.ifBlank { "Immediate airway stabilization and upright Fowler positioning" }
                options = listOf(
                    correctText,
                    "Administer rapid IV push sedative and document status after 30 minutes",
                    "Place patient in steep Trendelenburg position and monitor vitals q4h",
                    "Awaiting family consent before initiating emergency oxygen therapy"
                )
                correctIndex = 0
            }

            list.add(
                QuizQuestion(
                    id = "quiz-${resource.id}-${idx++}",
                    question = q.questionText,
                    options = options,
                    correctAnswerIndex = correctIndex,
                    explanation = q.clinicalRationale.ifBlank { q.markingScheme.ifBlank { "Evidence-based nursing priority." } },
                    clinicalPearl = q.highYieldTip ?: "Priority Action: Immediate life-support interventions precede secondary assessments.",
                    category = if (q.type == "scenario_case") "Clinical Scenario" else "Priority Action",
                    difficulty = "NCLEX High-Yield"
                )
            )
        }

        // 2. Syllabus competencies
        resource.syllabus.forEach { unit ->
            val comp = unit.keyCompetencies.firstOrNull() ?: "clinical intervention"
            val topic = unit.topics.firstOrNull() ?: unit.title

            list.add(
                QuizQuestion(
                    id = "quiz-${resource.id}-${idx++}",
                    question = "A nursing student is caring for a patient requiring knowledge from Unit ${unit.unitNumber} (${unit.title}). Which action represents adherence to \"$comp\"?",
                    options = listOf(
                        "Execute standard clinical procedure for $topic adhering strictly to aseptic technique and baseline parameter verification.",
                        "Delegate the entire sterile procedure to an unlicensed assistive personnel (UAP) without direct supervision.",
                        "Administer interventions without checking patient identification or confirming allergy history.",
                        "Delay recording vital signs and clinical assessment until the conclusion of the 12-hour shift."
                    ),
                    correctAnswerIndex = 0,
                    explanation = "Unit ${unit.unitNumber} requires licensed registered nurses to execute or directly supervise complex clinical competencies, ensure 2-patient identification, and record vitals contemporaneously.",
                    clinicalPearl = "ADPIE Standard: Assessment always precedes intervention unless immediate cardiac or airway arrest is present.",
                    category = "Priority Action",
                    difficulty = "Standard"
                )
            )
        }

        // 3. High yield key points
        resource.highYieldKeyPoints.forEach { pt ->
            list.add(
                QuizQuestion(
                    id = "quiz-${resource.id}-${idx++}",
                    question = "In the clinical management of \"${resource.title}\", which nursing statement reflects the evidence-based standard of care?",
                    options = listOf(
                        pt,
                        "Withhold physician notification when vitals deteriorate until scheduled round time.",
                        "Rely exclusively on automated electronic monitors without manual palpation or auscultation.",
                        "Skip hand hygiene between bed spaces if gloves appear visually clean."
                    ),
                    correctAnswerIndex = 0,
                    explanation = "Clinical standard: $pt. This directly preserves patient safety and optimizes clinical outcomes in ${resource.domain}.",
                    clinicalPearl = "Patient Safety First: Early identification and escalation of deterioration prevents cardiopulmonary arrest.",
                    category = "Priority Action",
                    difficulty = "NCLEX High-Yield"
                )
            )
        }

        // 4. Clinical sections
        resource.sections.forEach { sec ->
            list.add(
                QuizQuestion(
                    id = "quiz-${resource.id}-${idx++}",
                    question = "Regarding \"${sec.title}\" in ${resource.title}, what is the critical nursing management priority?",
                    options = listOf(
                        "Maintain vigilant observation, monitor vital organ perfusion, and adhere to protocol guidelines.",
                        "Discharge patient without providing medication education or emergency red-flag warnings.",
                        "Administer high-alert medications without an independent two-nurse double-check.",
                        "Assume asymptomatic presentations do not require scheduled assessments or documentation."
                    ),
                    correctAnswerIndex = 0,
                    explanation = sec.content.take(200) + if (sec.content.length > 200) "..." else "",
                    clinicalPearl = sec.callout?.text ?: "Assess Airway, Breathing, Circulation, and Mental Status continuously.",
                    category = "Clinical Scenario",
                    difficulty = "Standard"
                )
            )
        }

        // Fallback question if resource had minimal content
        if (list.isEmpty()) {
            list.add(
                QuizQuestion(
                    id = "quiz-${resource.id}-def",
                    question = "What is the primary clinical objective for the study of \"${resource.title}\"?",
                    options = listOf(
                        "Develop clinical reasoning, critical thinking, and patient safety mastery across ${resource.domain}.",
                        "Memorize definitions without understanding bedside clinical application.",
                        "Focus exclusively on administrative paperwork rather than patient assessment.",
                        "Limit evaluations to laboratory results without performing physical assessment."
                    ),
                    correctAnswerIndex = 0,
                    explanation = "The foundation of nursing education is synthesizing theoretical physiology with bedside clinical competencies and ethical standards.",
                    clinicalPearl = "Remember the five rights of medication administration and 2 patient identifiers.",
                    category = "Core Concepts",
                    difficulty = "Standard"
                )
            )
        }

        return list.take(maxOf(targetCount, 5))
    }
}
