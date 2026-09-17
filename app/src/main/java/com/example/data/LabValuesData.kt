package com.example.data

import com.example.model.LabValueItem

object LabValuesData {
    val LAB_VALUES: List<LabValueItem> = listOf(
        LabValueItem(
            testName = "Potassium (K+)",
            normalRange = "3.5 - 5.0",
            unit = "mEq/L",
            panicLow = "< 2.5 mEq/L (Ventricular dysrhythmias)",
            panicHigh = "> 6.5 mEq/L (Peaked T waves, Cardiac arrest)",
            clinicalSignificance = "Major intracellular cation essential for myocardial electrical conduction and neuromuscular excitability.",
            nursingAlert = "CRITICAL: Never administer IV Potassium push (fatal!). Always dilute and infuse ≤ 10-20 mEq/hr via pump with continuous ECG telemetry."
        ),
        LabValueItem(
            testName = "Sodium (Na+)",
            normalRange = "135 - 145",
            unit = "mEq/L",
            panicLow = "< 120 mEq/L (Seizure risk, cerebral edema)",
            panicHigh = "> 160 mEq/L (Severe cellular dehydration)",
            clinicalSignificance = "Dominant extracellular cation regulating intravascular volume, fluid shifts, and neurological status.",
            nursingAlert = "Implement seizure precautions for Na+ < 125 mEq/L. Correct slowly (max 8-10 mEq/L in 24h) to avoid Central Pontine Myelinolysis."
        ),
        LabValueItem(
            testName = "Ionized Calcium (Ca2+)",
            normalRange = "4.5 - 5.6",
            unit = "mg/dL (or total: 8.5 - 10.5 mg/dL)",
            panicLow = "< 6.5 mg/dL total (Tetany, laryngospasm)",
            panicHigh = "> 13.0 mg/dL total (Heart block, coma)",
            clinicalSignificance = "Vital for bone matrix, cardiac excitation-contraction coupling, and coagulation cascade.",
            nursingAlert = "Hypocalcemia checks: Positive Trousseau's (carpal spasm with BP cuff) and Chvostek's sign (facial twitch on cheek tap). Have Calcium Gluconate ready."
        ),
        LabValueItem(
            testName = "Magnesium (Mg2+)",
            normalRange = "1.5 - 2.5",
            unit = "mEq/L",
            panicLow = "< 1.0 mEq/L (Torsades de Pointes)",
            panicHigh = "> 4.0 mEq/L (Loss of deep tendon reflexes, respiratory arrest)",
            clinicalSignificance = "Cofactor for ATP production and muscle relaxation. Low Mg triggers refractory hypokalemia.",
            nursingAlert = "When infusing Magnesium Sulfate for pre-eclampsia: monitor hourly Patellar DTRs, RR (hold if <12), and urine output (hold if <30mL/hr). Antidote: Calcium Gluconate."
        ),
        LabValueItem(
            testName = "Blood Urea Nitrogen (BUN)",
            normalRange = "7 - 20",
            unit = "mg/dL",
            panicLow = null,
            panicHigh = "> 60 mg/dL (Azotemia, Uremia)",
            clinicalSignificance = "End product of protein metabolism cleared by kidneys. Disproportionate BUN:Cr ratio (>20:1) suggests dehydration or GI bleed.",
            nursingAlert = "Evaluate hydration status. Ensure adequate fluid intake unless patient has end-stage renal disease or congestive heart failure."
        ),
        LabValueItem(
            testName = "Serum Creatinine",
            normalRange = "0.6 - 1.2",
            unit = "mg/dL",
            panicLow = null,
            panicHigh = "> 4.0 mg/dL (Acute Kidney Injury / Renal Failure)",
            clinicalSignificance = "Direct indicator of glomerular filtration rate (GFR). Not influenced by dietary protein intake like BUN.",
            nursingAlert = "Hold nephrotoxic medications (NSAIDs, aminoglycosides, IV radiocontrast dye) and notify provider if Creatinine doubles baseline."
        ),
        LabValueItem(
            testName = "Hemoglobin (Hgb)",
            normalRange = "Male: 13.5 - 17.5 | Female: 12.0 - 15.5",
            unit = "g/dL",
            panicLow = "< 7.0 g/dL (Symptomatic anemia, transfusion threshold)",
            panicHigh = "> 20.0 g/dL (Polycythemia, hyperviscosity)",
            clinicalSignificance = "Oxygen-carrying capacity of red blood cells delivering O2 to end organs.",
            nursingAlert = "Transfusion standard: Verify 2 RN identifiers, prime with 0.9% Normal Saline only, infuse within 4 hours, obtain baseline vitals and recheck at 15 minutes."
        ),
        LabValueItem(
            testName = "White Blood Cell Count (WBC)",
            normalRange = "4,500 - 11,000",
            unit = "/mcL",
            panicLow = "< 2,000 /mcL (Neutropenic fever hazard)",
            panicHigh = "> 30,000 /mcL (Leukemoid reaction, severe sepsis)",
            clinicalSignificance = "Immune response to infection, inflammation, or hematologic malignancy.",
            nursingAlert = "Absolute Neutrophil Count (ANC) < 1,000 warrants strict Neutropenic Precautions: private room, positive pressure, no fresh plants, staff masks."
        ),
        LabValueItem(
            testName = "Platelets (Thrombocytes)",
            normalRange = "150,000 - 450,000",
            unit = "/mcL",
            panicLow = "< 20,000 /mcL (Spontaneous intracranial hemorrhage)",
            panicHigh = "> 1,000,000 /mcL (Thrombosis hazard)",
            clinicalSignificance = "Essential for primary hemostasis and platelet plug formation.",
            nursingAlert = "Thrombocytopenia precautions: soft toothbrush, electric razor only, no IM injections, avoid rectal temps/suppositories, fall risk protocol."
        ),
        LabValueItem(
            testName = "International Normalized Ratio (INR)",
            normalRange = "0.8 - 1.2 (Therapeutic on Warfarin: 2.0 - 3.0)",
            unit = "ratio",
            panicLow = null,
            panicHigh = "> 4.5 (High risk for fatal hemorrhage)",
            clinicalSignificance = "Standardized extrinsic coagulation pathway monitor primarily used for Warfarin (Coumadin) therapy.",
            nursingAlert = "INR > 3.5: hold Warfarin dose, assess for melena, hematuria, epistaxis, or petechiae. Antidote: Vitamin K (Phytonadione) and FFP / Kcentra."
        )
    )
}
