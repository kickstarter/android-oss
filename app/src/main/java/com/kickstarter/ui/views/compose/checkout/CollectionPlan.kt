import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showBackground = false, name = "Eligible - Pledge Over Time Selected")
@Composable
fun PreviewPledgeOverTimeSelected() {
    CollectionPlan(isEligible = true, initialSelectedOption = "Pledge Over Time")
}

@Preview(showBackground = false, name = "Eligible - Pledge in Full Selected")
@Composable
fun PreviewPledgeInFullSelected() {
    CollectionPlan(isEligible = true, initialSelectedOption = "Pledge in full")
}

@Preview(showBackground = false, name = "Not Eligible")
@Composable
fun PreviewNotEligibleComponent() {
    CollectionPlan(isEligible = false, initialSelectedOption = "Pledge in full")
}

@Composable
fun CollectionPlan(isEligible: Boolean, initialSelectedOption: String = "Pledge in full") {
    var selectedOption by remember { mutableStateOf(initialSelectedOption) }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        PledgeOption(
            optionText = "Pledge in full",
            selected = selectedOption == "Pledge in full",
            onSelect = { selectedOption = "Pledge in full" }
        )
        Spacer(modifier = Modifier.height(8.dp))

        PledgeOption(
            optionText = "Pledge Over Time",
            selected = selectedOption == "Pledge Over Time",
            description = if (!isEligible) "Available for pledges over $150" else "You will be charged for your pledge over four payments, at no extra cost.",
            onSelect = {
                if (isEligible) selectedOption = "Pledge Over Time" // Only set if eligible
            },
            isExpanded = selectedOption == "Pledge Over Time" && isEligible,
            isSelectable = isEligible // Control if it should appear grayed out or not
        )
    }
}

@Composable
fun PledgeOption(
    optionText: String,
    selected: Boolean,
    description: String? = null,
    onSelect: () -> Unit,
    isExpanded: Boolean = false,
    isSelectable: Boolean = true // New parameter to control selection availability
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelectable) Color.White else Color(0xFFF0F0F0)) // Light gray if not selectable
            .clickable(enabled = isSelectable, onClick = onSelect) // Disable click if not selectable
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selected,
                onClick = onSelect.takeIf { isSelectable }, // Only clickable if selectable
                colors = RadioButtonDefaults.colors(
                    selectedColor = if (isSelectable) Color(0xFF008000) else Color.Gray,
                    unselectedColor = Color.Gray
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = optionText,
                fontWeight = FontWeight.Bold,
                color = if (isSelectable) Color.Black else Color.Gray // Gray text if not selectable
            )
        }

        // Description and additional information, if applicable
        if (description != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.body2,
                color = Color.Gray
            )

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "The first charge will be 24 hours after the project ends successfully, then every 2 weeks until fully paid. When this option is selected no further edits can be made to your pledge.",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray
                )
                Text(
                    text = "See our Terms of Use",
                    style = MaterialTheme.typography.body2,
                    color = Color(0xFF008000)
                )
                Spacer(modifier = Modifier.height(8.dp))
                ChargeSchedule()
            }
        }
    }
}

@Composable
fun ChargeSchedule() {
    Column(modifier = Modifier.fillMaxWidth()) {
        ChargeItem("Charge 1", "Aug 11, 2024", "$250")
        ChargeItem("Charge 2", "Aug 15, 2024", "$250")
        ChargeItem("Charge 3", "Aug 29, 2024", "$250")
        ChargeItem("Charge 4", "Sep 12, 2024", "$250")
    }
}

@Composable
fun ChargeItem(title: String, date: String, amount: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontWeight = FontWeight.Bold)
        Text(text = date, color = Color.Gray)
        Text(text = amount, color = Color.Gray)
    }
}
