package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LabValuesData
import com.example.model.OptimumCondition
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicalToolsSheet(
    optimumConditions: List<OptimumCondition>,
    isSyncingOptimum: Boolean,
    lastSyncTime: String,
    onSyncOptimum: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        "Dosage & IV Rates",
        "⚡ ABG Analyzer",
        "💚 Optimum Conditions",
        "🧪 Lab Values",
        "⚙️ Settings & Contact"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF071426),
        contentColor = Color.White,
        modifier = Modifier.testTag("sheet_clinical_tools")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0D9488)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Column {
                        Text(
                            text = "Clinical Nursing Calculators & Reference Tools",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Dosage math, ABG evaluator, online-synced optimum conditions & settings",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                }
            }

            // Scrollable Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF0B192C),
                contentColor = Color.White,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) Color(0xFF00E5FF) else Color(0xFF94A3B8)
                            )
                        }
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> DosageAndIvRatesTab()
                    1 -> AbgAnalyzerTab()
                    2 -> OptimumVitalsTab(optimumConditions, isSyncingOptimum, lastSyncTime, onSyncOptimum)
                    3 -> LabValuesTab()
                    4 -> SettingsAndContactTab()
                }
            }

            // Bottom Status and Done Button
            Surface(
                color = Color(0xFF071426),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Online Guidelines Status:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text("🟢 Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                    }
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                    ) {
                        Text("Done", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DosageAndIvRatesTab() {
    // 1. Desired Over Have
    var doseDesired by remember { mutableStateOf("50") }
    var doseOnHand by remember { mutableStateOf("25") }
    var vehicleQuantity by remember { mutableStateOf("1") }

    // 2. IV Flow Rate
    var ivTotalVolume by remember { mutableStateOf("1000") }
    var ivInfusionHours by remember { mutableStateOf("8") }
    var selectedDropFactorText by remember { mutableStateOf("15 gtt/mL (Standard Macro)") }
    var selectedDropFactorVal by remember { mutableStateOf(15.0) }
    var dropFactorDropdownExpanded by remember { mutableStateOf(false) }

    val dropFactorOptions = listOf(
        "15 gtt/mL (Standard Macro)" to 15.0,
        "10 gtt/mL (Blood / Viscous)" to 10.0,
        "20 gtt/mL (Standard Regular)" to 20.0,
        "60 gtt/mL (Microdrip / Peds)" to 60.0
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Desired Over Have Formula
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2033)),
                border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "1. Desired Over Have Formula\n(Tablets / Liquids)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF064E3B)) {
                            Text(
                                text = "(Desired ÷ Have) × Quantity",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34D399),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = doseDesired,
                        onValueChange = { doseDesired = it },
                        label = { Text("Dose Desired (Ordered)", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFF071426),
                            unfocusedContainerColor = Color(0xFF071426)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = doseOnHand,
                        onValueChange = { doseOnHand = it },
                        label = { Text("Dose on Hand (Available)", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFF071426),
                            unfocusedContainerColor = Color(0xFF071426)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = vehicleQuantity,
                        onValueChange = { vehicleQuantity = it },
                        label = { Text("Vehicle Quantity (Tablets / mL)", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFF071426),
                            unfocusedContainerColor = Color(0xFF071426)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    val d = doseDesired.toDoubleOrNull() ?: 0.0
                    val h = doseOnHand.toDoubleOrNull() ?: 0.0
                    val q = vehicleQuantity.toDoubleOrNull() ?: 1.0
                    val calculatedAmount = if (h > 0) (d / h) * q else 0.0

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF042F2E),
                        border = BorderStroke(1.dp, Color(0xFF0D9488)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Calculated Administer\nQuantity:", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(String.format("%.2f", calculatedAmount), fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF00E5FF))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("units / ml", fontSize = 12.sp, color = Color(0xFF38BDF8), modifier = Modifier.padding(bottom = 2.dp))
                            }
                        }
                    }
                }
            }
        }

        // Section 2: IV Flow Rate & Drip Rate
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2033)),
                border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "2. IV Flow Rate & Drip Rate Calculator",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = Color.White
                    )

                    OutlinedTextField(
                        value = ivTotalVolume,
                        onValueChange = { ivTotalVolume = it },
                        label = { Text("Total Volume (mL)", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFF071426),
                            unfocusedContainerColor = Color(0xFF071426)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = ivInfusionHours,
                        onValueChange = { ivInfusionHours = it },
                        label = { Text("Infusion Time (Hours)", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFF071426),
                            unfocusedContainerColor = Color(0xFF071426)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Tubing Drop Factor Dropdown
                    ExposedDropdownMenuBox(
                        expanded = dropFactorDropdownExpanded,
                        onExpandedChange = { dropFactorDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedDropFactorText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tubing Drop Factor (gtt/mL)", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropFactorDropdownExpanded) },
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF1E293B),
                                focusedContainerColor = Color(0xFF071426),
                                unfocusedContainerColor = Color(0xFF071426)
                            ),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = dropFactorDropdownExpanded,
                            onDismissRequest = { dropFactorDropdownExpanded = false },
                            modifier = Modifier.background(Color(0xFF0F172A))
                        ) {
                            dropFactorOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.first, color = Color.White, fontSize = 12.sp) },
                                    onClick = {
                                        selectedDropFactorText = option.first
                                        selectedDropFactorVal = option.second
                                        dropFactorDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    val vol = ivTotalVolume.toDoubleOrNull() ?: 0.0
                    val hrs = ivInfusionHours.toDoubleOrNull() ?: 0.0
                    val pumpRate = if (hrs > 0) vol / hrs else 0.0
                    val dripRate = if (hrs > 0) (vol * selectedDropFactorVal) / (hrs * 60.0) else 0.0

                    // Electronic IV Pump Rate Card
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0A192F),
                        border = BorderStroke(1.dp, Color(0xFF1E3A8A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Electronic IV Pump Rate:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(String.format("%.1f", pumpRate), fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("mL / hr", fontSize = 12.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(bottom = 2.dp))
                            }
                        }
                    }

                    // Manual Gravity Drip Rate Card
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0A192F),
                        border = BorderStroke(1.dp, Color(0xFF1E3A8A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Manual Gravity Drip Rate:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(String.format("%.0f", dripRate), fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF00E5FF))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("gtt / min", fontSize = 12.sp, color = Color(0xFF00E5FF), modifier = Modifier.padding(bottom = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsAndContactTab() {
    val context = LocalContext.current
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, Color(0xFF0D9488).copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                            Text("Contact Compiler (CHANDA FELIX)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        }

                        Button(
                            onClick = {
                                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:fchanda335@gmail.com")
                                }
                                try {
                                    context.startActivity(Intent.createChooser(emailIntent, "Contact Us"))
                                } catch (e: Exception) {}
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Mail, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Contact Us", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text("Have questions or wish to submit new nursing past papers? Reach out directly.", fontSize = 11.sp, color = Color(0xFF94A3B8))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF020617),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("fchanda335@gmail.com", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Author Email", "fchanda335@gmail.com"))
                                },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFCBD5E1))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", fontSize = 10.sp, color = Color(0xFFCBD5E1))
                            }
                        }
                    }

                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        placeholder = { Text("Subject (e.g. Module feedback, past paper update)", fontSize = 11.sp, color = Color(0xFF64748B)) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF0D9488),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF020617),
                            unfocusedContainerColor = Color(0xFF020617)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        placeholder = { Text("Type your message to Chanda Felix...", fontSize = 11.sp, color = Color(0xFF64748B)) },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF0D9488),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF020617),
                            unfocusedContainerColor = Color(0xFF020617)
                        ),
                        modifier = Modifier.fillMaxWidth().height(80.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AbgAnalyzerTab() {
    var ph by remember { mutableStateOf("7.30") }
    var paco2 by remember { mutableStateOf("50") }
    var hco3 by remember { mutableStateOf("24") }

    val phVal = ph.toDoubleOrNull() ?: 7.40
    val co2Val = paco2.toDoubleOrNull() ?: 40.0
    val hco3Val = hco3.toDoubleOrNull() ?: 24.0

    val isAcidosis = phVal < 7.35
    val isAlkalosis = phVal > 7.45
    val isCo2Acid = co2Val > 45.0
    val isCo2Alkaline = co2Val < 35.0
    val isHco3Acid = hco3Val < 22.0
    val isHco3Alkaline = hco3Val > 26.0

    val (diagnosis, compensation, cause, nursingPriority) = when {
        isAcidosis && isCo2Acid && !isHco3Acid && !isHco3Alkaline -> Quadruple(
            "Uncompensated Respiratory Acidosis",
            "No Renal Compensation",
            "Hypoventilation, COPD, opioid toxicity, chest wall injury",
            "Maintain airway patency, Naloxone if opioid-induced, upright positioning, assist ventilation"
        )
        isAcidosis && isCo2Acid && isHco3Alkaline -> Quadruple(
            "Partially Compensated Respiratory Acidosis",
            "Partial Renal Compensation (Kidneys retaining HCO3)",
            "Chronic respiratory failure, severe emphysema",
            "Maintain controlled oxygen (target SpO2 88-92%), pulmonary toilet, bronchodilators"
        )
        isAcidosis && !isCo2Acid && !isCo2Alkaline && isHco3Acid -> Quadruple(
            "Uncompensated Metabolic Acidosis",
            "No Respiratory Compensation",
            "Diabetic Ketoacidosis (DKA), lactic acidosis, renal failure, severe diarrhea",
            "Regular Insulin IV infusion for DKA, vigorous Normal Saline rehydration"
        )
        isAlkalosis && isCo2Alkaline && !isHco3Acid && !isHco3Alkaline -> Quadruple(
            "Uncompensated Respiratory Alkalosis",
            "No Renal Compensation",
            "Hyperventilation, anxiety panic attack, early hypoxia, pulmonary embolism, fever",
            "Encourage slow diaphragmatic rebreathing, treat fever/pain, evaluate for PE"
        )
        isAlkalosis && !isCo2Acid && !isCo2Alkaline && isHco3Alkaline -> Quadruple(
            "Uncompensated Metabolic Alkalosis",
            "No Respiratory Compensation",
            "Severe vomiting / NG suctioning (loss of acid), loop diuretic overuse",
            "Discontinue suctioning temporarily, administer IV Potassium Chloride & Normal Saline"
        )
        else -> Quadruple(
            "Normal / Mixed Arterial Blood Gas",
            "Homeostatic Balance",
            "Standard cardiopulmonary baseline",
            "Routine clinical nursing observation"
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "ROME Rule: Respiratory = Opposite (pH ↑ PaCO2 ↓) | Metabolic = Equal (pH ↑ HCO3 ↑)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF)
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = ph,
                    onValueChange = { ph = it },
                    label = { Text("pH (7.35-7.45)", fontSize = 11.sp, color = Color(0xFF94A3B8)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF1E293B),
                        focusedContainerColor = Color(0xFF071426),
                        unfocusedContainerColor = Color(0xFF071426)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = paco2,
                    onValueChange = { paco2 = it },
                    label = { Text("PaCO2 (35-45)", fontSize = 11.sp, color = Color(0xFF94A3B8)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF1E293B),
                        focusedContainerColor = Color(0xFF071426),
                        unfocusedContainerColor = Color(0xFF071426)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = hco3,
                    onValueChange = { hco3 = it },
                    label = { Text("HCO3 (22-26)", fontSize = 11.sp, color = Color(0xFF94A3B8)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF1E293B),
                        focusedContainerColor = Color(0xFF071426),
                        unfocusedContainerColor = Color(0xFF071426)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E33)),
                border = BorderStroke(1.dp, if (isAcidosis || isAlkalosis) Color(0xFFEF4444) else Color(0xFF0D9488))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("DIAGNOSTIC INTERPRETATION", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = if (isAcidosis || isAlkalosis) Color(0xFFEF4444) else Color(0xFF00E5FF))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isAcidosis || isAlkalosis) Color(0xFF7F1D1D) else Color(0xFF064E3B)
                        ) {
                            Text(compensation, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(diagnosis, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Common Etiologies: $cause", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Immediate Nursing Priorities: ⚡ $nursingPriority", fontSize = 11.sp, color = Color(0xFF34D399), fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun OptimumVitalsTab(
    conditions: List<OptimumCondition>,
    isSyncing: Boolean,
    lastSyncTime: String,
    onSync: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(conditions) { cond ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E33)),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(cond.parameter, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF00E5FF))
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF064E3B)) {
                            Text(
                                text = "${cond.optimumRange} ${cond.unit}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF34D399),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Significance: ${cond.clinicalSignificance}", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Action If Abnormal: ${cond.nursingInterventionIfAbnormal}", fontSize = 11.sp, color = Color(0xFFFBBF24))
                }
            }
        }
    }
}

@Composable
private fun LabValuesTab() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(LabValuesData.LAB_VALUES) { lab ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E33)),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(lab.testName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF0C4A6E)) {
                            Text(
                                text = "${lab.normalRange} ${lab.unit}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Significance: ${lab.clinicalSignificance}", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF451A03),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ ${lab.nursingAlert}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFDE68A),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
