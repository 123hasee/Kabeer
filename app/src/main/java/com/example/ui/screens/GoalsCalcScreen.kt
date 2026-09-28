package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun GoalsCalcScreen(
  modifier: Modifier = Modifier
) {
  var rentInput by remember { mutableStateOf("1200") }
  var utilitiesInput by remember { mutableStateOf("250") }
  var foodInput by remember { mutableStateOf("600") }
  var transportInput by remember { mutableStateOf("350") }
  var insuranceInput by remember { mutableStateOf("200") }
  var currentJobIncomeInput by remember { mutableStateOf("3500") }
  var expectedMonthlyReturnPercent by remember { mutableStateOf(5.0) } // Conservative 5%

  val rent = rentInput.toDoubleOrNull() ?: 0.0
  val utilities = utilitiesInput.toDoubleOrNull() ?: 0.0
  val food = foodInput.toDoubleOrNull() ?: 0.0
  val transport = transportInput.toDoubleOrNull() ?: 0.0
  val insurance = insuranceInput.toDoubleOrNull() ?: 0.0
  val jobIncome = currentJobIncomeInput.toDoubleOrNull() ?: 0.0

  val monthlyLE = rent + utilities + food + transport + insurance
  val annualLE = monthlyLE * 12.0
  val returnRate = (expectedMonthlyReturnPercent / 100.0).coerceAtLeast(0.01)

  val requiredAccountToCoverLE = monthlyLE / returnRate
  val requiredAccountToReplaceJob = jobIncome / returnRate

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(Slate950)
      .padding(horizontal = 14.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item { Spacer(modifier = Modifier.height(4.dp)) }

    // Title
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.Calculate, contentDescription = null, tint = Emerald500, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(text = "Monetary Goals & LE Calculator", color = Slate100, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Text(text = "Book 1: Want vs Need & Account Size Math", color = Slate400, fontSize = 11.sp)
        }
      }
    }

    // Results Summary Card
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = androidx.compose.ui.graphics.SolidColor(Emerald500.copy(alpha = 0.5f))
        ),
        modifier = Modifier.fillMaxWidth().testTag("goals_result_card")
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "TARGET ACCOUNT CAPITAL REQUIRED (${expectedMonthlyReturnPercent.toInt()}% Avg Monthly Return)",
            color = Slate400,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(text = "To Cover Living Expenses", color = Slate400, fontSize = 11.sp)
              Text(
                text = "$${String.format("%,.0f", requiredAccountToCoverLE)}",
                color = Emerald400,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
              )
              Text(
                text = "$${String.format("%,.0f", monthlyLE)}/mo ($${String.format("%,.0f", annualLE)}/yr)",
                color = Slate400,
                fontSize = 10.sp
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(text = "To Replace 9-5 Job", color = Slate400, fontSize = 11.sp)
              Text(
                text = "$${String.format("%,.0f", requiredAccountToReplaceJob)}",
                color = Blue500,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
              )
              Text(
                text = "$${String.format("%,.0f", jobIncome)}/mo salary",
                color = Slate400,
                fontSize = 10.sp
              )
            }
          }
        }
      }
    }

    // Return Rate Selector
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(text = "Target Monthly Return Rate:", color = Slate300, fontSize = 12.sp)
            Text(
              text = "${expectedMonthlyReturnPercent.toInt()}% / month",
              color = Emerald400,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }

          Slider(
            value = expectedMonthlyReturnPercent.toFloat(),
            onValueChange = { expectedMonthlyReturnPercent = it.toDouble() },
            valueRange = 2f..15f,
            steps = 12,
            colors = SliderDefaults.colors(
              thumbColor = Emerald500,
              activeTrackColor = Emerald500,
              inactiveTrackColor = Slate800
            ),
            modifier = Modifier.testTag("monthly_return_slider")
          )

          Text(
            text = "Book recommendation: 5% is conservative & reliable for scaling; 8-10% is standard for passing funding evaluations.",
            color = Slate400,
            fontSize = 10.sp
          )
        }
      }
    }

    // Monthly Living Expenses (LE) Form
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(text = "5 ESSENTIAL LIVING EXPENSES (LE):", color = Slate100, fontWeight = FontWeight.Bold, fontSize = 12.sp)

          ExpenseInputField("Rent / Mortgage ($)", rentInput) { rentInput = it }
          ExpenseInputField("Home Utilities ($)", utilitiesInput) { utilitiesInput = it }
          ExpenseInputField("Food & Groceries ($)", foodInput) { foodInput = it }
          ExpenseInputField("Transportation ($)", transportInput) { transportInput = it }
          ExpenseInputField("Insurance ($)", insuranceInput) { insuranceInput = it }
          ExpenseInputField("Current Job Monthly Income ($)", currentJobIncomeInput) { currentJobIncomeInput = it }
        }
      }
    }

    // Prop Firm & Account Scaling Roadmap
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(text = "BOOK 1 SCALING ROADMAP (FUNDING & LIVE ACCOUNTS)", color = Slate100, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Start with a 5k - 25k funding account. Generate 5% consistently. Scale into 50k, 100k, and 200k allocations. Never rush capital before building consistency!",
            color = Slate400,
            fontSize = 11.sp,
            lineHeight = 16.sp
          )
        }
      }
    }

    item { Spacer(modifier = Modifier.height(16.dp)) }
  }
}

@Composable
private fun ExpenseInputField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier.fillMaxWidth()
  ) {
    Text(text = label, color = Slate400, fontSize = 11.sp, modifier = Modifier.weight(1f))
    OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      singleLine = true,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Blue500,
        unfocusedBorderColor = Slate700,
        focusedTextColor = Slate100,
        unfocusedTextColor = Slate100
      ),
      modifier = Modifier
        .width(110.dp)
        .height(48.dp)
    )
  }
}
