package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterBar(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    selectedYear: String,
    onYearSelected: (String) -> Unit,
    selectedDomain: String,
    onDomainSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showYearMenu by remember { mutableStateOf(false) }
    var showDomainMenu by remember { mutableStateOf(false) }

    val years = listOf(
        "All Years",
        "Year 1 (Foundations)",
        "Year 2 (Adult Health & Patho)",
        "Year 3 (Specialties & Peds)",
        "Year 4 (Leadership & Intensive)"
    )

    val domains = listOf(
        "All Domains",
        "Fundamentals & Assessment",
        "Pharmacology",
        "Adult Health & Med-Surg",
        "Maternal & Neonatal",
        "Pediatric Nursing",
        "Mental Health & Psychiatric",
        "Critical Care & Emergency",
        "Community & Public Health",
        "Leadership, Ethics & Legal"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Dropdown filter chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Year filter dropdown chip
            Box {
                FilterChip(
                    selected = selectedYear != "All Years",
                    onClick = { showYearMenu = true },
                    label = {
                        Text(
                            text = if (selectedYear == "All Years") "Academic Year" else selectedYear.take(12) + "...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                DropdownMenu(
                    expanded = showYearMenu,
                    onDismissRequest = { showYearMenu = false }
                ) {
                    years.forEach { year ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = year,
                                    fontWeight = if (year == selectedYear) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onYearSelected(year)
                                showYearMenu = false
                            },
                            leadingIcon = {
                                if (year == selectedYear) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        )
                    }
                }
            }

            // Domain filter dropdown chip
            Box {
                FilterChip(
                    selected = selectedDomain != "All Domains",
                    onClick = { showDomainMenu = true },
                    label = {
                        Text(
                            text = if (selectedDomain == "All Domains") "Nursing Domain" else selectedDomain.take(14) + "...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )

                DropdownMenu(
                    expanded = showDomainMenu,
                    onDismissRequest = { showDomainMenu = false }
                ) {
                    domains.forEach { domain ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = domain,
                                    fontWeight = if (domain == selectedDomain) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onDomainSelected(domain)
                                showDomainMenu = false
                            },
                            leadingIcon = {
                                if (domain == selectedDomain) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        )
                    }
                }
            }

            // Reset filters button if active
            if (selectedYear != "All Years" || selectedDomain != "All Domains") {
                TextButton(
                    onClick = {
                        onYearSelected("All Years")
                        onDomainSelected("All Domains")
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Reset Filters",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
