package com.example.shopitemsdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

private val Navy = Color(0xFF111827)
private val Slate = Color(0xFF475569)
private val Muted = Color(0xFF94A3B8)
private val Border = Color(0xFFDCE4EE)
private val Canvas = Color(0xFFF8FAFC)
private val Teal = Color(0xFF079889)
private val Mint = Color(0xFFC6F4EA)
private val Danger = Color(0xFFFF4D59)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ShopItemsTheme { ShoppingListScreen() } }
    }
}

@Composable
fun ShoppingListScreen(viewModel: ShoppingViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var itemBeingEdited by remember { mutableStateOf<ShoppingItem?>(null) }

    Scaffold(
        containerColor = Canvas,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { insets ->
        Column(
            Modifier.fillMaxSize().padding(insets).padding(horizontal = 24.dp),
        ) {
            Header()
            Spacer(Modifier.height(28.dp))
            SummaryCards(state = state, onFilter = viewModel::setFilter)
            HorizontalDivider(Modifier.padding(vertical = 30.dp), color = Border)
            SectionTitle("ADD ITEM")
            Spacer(Modifier.height(10.dp))
            AddItemInput(viewModel::addItem)
            Spacer(Modifier.height(28.dp))
            SectionTitle("YOUR LIST")
            Spacer(Modifier.height(14.dp))

            if (state.visibleItems.isEmpty()) {
                EmptyState(state.filter, Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.visibleItems, key = ShoppingItem::id) { item ->
                        ShoppingItemRow(
                            item = item,
                            onToggle = { viewModel.toggleItem(item.id) },
                            onEdit = { itemBeingEdited = item },
                            onDelete = {
                                viewModel.deleteItem(item.id)?.let { deleted ->
                                    scope.launch {
                                        val result = snackbar.showSnackbar("Removed ${deleted.name}", "Undo")
                                        if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                            viewModel.restoreItem(deleted)
                                        }
                                    }
                                }
                            },
                        )
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 28.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Info, null, Modifier.size(18.dp), tint = Muted)
                Text("  Items auto-sort by checked status", color = Muted, fontSize = 13.sp)
            }
        }
    }

    itemBeingEdited?.let { item ->
        EditItemDialog(
            item = item,
            onDismiss = { itemBeingEdited = null },
            onSave = { name ->
                if (viewModel.editItem(item.id, name)) itemBeingEdited = null
            },
        )
    }
}

@Composable
private fun Header() {
    Row(
        Modifier.fillMaxWidth().padding(top = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("Weekly Grocery", color = Navy, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
            Text("Sunday Restock List", color = Slate, fontSize = 16.sp)
        }
        Box(
            Modifier.size(46.dp).clip(CircleShape).background(Mint),
            contentAlignment = Alignment.Center,
        ) {
            Text("JD", color = Teal, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }
    }
}

@Composable
private fun SummaryCards(state: ShoppingUiState, onFilter: (ItemFilter) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        SummaryCard(state.items.size, "All items", state.filter == ItemFilter.ALL, Modifier.weight(1f)) {
            onFilter(ItemFilter.ALL)
        }
        SummaryCard(state.remainingCount, "To buy", state.filter == ItemFilter.TO_BUY, Modifier.weight(1f)) {
            onFilter(ItemFilter.TO_BUY)
        }
        SummaryCard(state.purchasedCount, "Done", state.filter == ItemFilter.DONE, Modifier.weight(1f)) {
            onFilter(ItemFilter.DONE)
        }
    }
}

@Composable
private fun SummaryCard(count: Int, label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(86.dp),
        onClick = onClick,
        shape = RoundedCornerShape(15.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) Teal else Border),
        colors = CardDefaults.cardColors(containerColor = if (selected) Mint else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 19.dp), verticalArrangement = Arrangement.Center) {
            Text("$count", color = if (selected) Teal else Navy, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text(label, color = if (selected) Teal else Slate, fontSize = 14.sp)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = Slate, fontSize = 15.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun AddItemInput(onAdd: (String) -> Boolean) {
    var text by rememberSaveable { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current
    fun submit() {
        if (onAdd(text)) {
            text = ""
            keyboard?.hide()
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth().height(62.dp),
        shape = RoundedCornerShape(15.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        shadowElevation = 1.dp,
    ) {
        Row(Modifier.padding(start = 18.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.AddCircleOutline, null, Modifier.size(23.dp), tint = Muted)
            androidx.compose.foundation.text.BasicTextField(
                value = text,
                onValueChange = { if (it.length <= 80) text = it },
                modifier = Modifier.weight(1f).padding(horizontal = 14.dp),
                textStyle = TextStyle(color = Navy, fontSize = 16.sp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                decorationBox = { input ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (text.isEmpty()) Text("e.g. Greek Yogurt 500g...", color = Muted, fontSize = 16.sp)
                        input()
                    }
                },
            )
            Button(
                onClick = ::submit,
                enabled = text.isNotBlank(),
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal, disabledContainerColor = Teal.copy(alpha = .45f)),
                contentPadding = PaddingValues(horizontal = 20.dp),
            ) { Text("Add", fontSize = 15.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun ShoppingItemRow(item: ShoppingItem, onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(66.dp),
        shape = RoundedCornerShape(15.dp),
        color = if (item.purchased) Color(0xFFFBFCFD) else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        shadowElevation = if (item.purchased) 0.dp else 1.dp,
    ) {
        Row(Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (item.purchased) Color(0xFF6EC5BA) else Color.Transparent)
                    .border(2.dp, if (item.purchased) Color.Transparent else Muted, RoundedCornerShape(7.dp))
                    .clickable(role = Role.Checkbox, onClick = onToggle)
                    .semantics { contentDescription = if (item.purchased) "Mark ${item.name} not purchased" else "Mark ${item.name} purchased" },
                contentAlignment = Alignment.Center,
            ) {
                if (item.purchased) Icon(Icons.Outlined.Check, null, Modifier.size(20.dp), tint = Color.White)
            }
            Text(
                item.name,
                Modifier.weight(1f).padding(start = 16.dp),
                color = if (item.purchased) Color(0xFFBCC8D8) else Navy,
                fontSize = 17.sp,
                textDecoration = if (item.purchased) TextDecoration.LineThrough else null,
            )
            if (!item.purchased) {
                IconButton(onClick = onEdit, Modifier.semantics { contentDescription = "Edit ${item.name}" }) {
                    Icon(Icons.Outlined.Build, null, Modifier.size(23.dp), tint = Slate)
                }
            }
            IconButton(onClick = onDelete, Modifier.semantics { contentDescription = "Delete ${item.name}" }) {
                Icon(Icons.Outlined.DeleteOutline, null, Modifier.size(23.dp), tint = if (item.purchased) Danger.copy(alpha = .6f) else Danger)
            }
        }
    }
}

@Composable
private fun EmptyState(filter: ItemFilter, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            when (filter) {
                ItemFilter.ALL -> "Add your first grocery item above."
                ItemFilter.TO_BUY -> "Everything is checked off!"
                ItemFilter.DONE -> "No purchased items yet."
            },
            color = Muted,
            fontSize = 15.sp,
        )
    }
}

@Composable
private fun EditItemDialog(item: ShoppingItem, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by rememberSaveable(item.id) { mutableStateOf(item.name) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit item", color = Navy, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 80) name = it },
                label = { Text("Item name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (name.isNotBlank()) onSave(name) }),
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(name) }, enabled = name.isNotBlank()) { Text("Save", color = Teal) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Slate) } },
        containerColor = Color.White,
    )
}

@Composable
private fun ShopItemsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = androidx.compose.material3.lightColorScheme(primary = Teal, background = Canvas, surface = Color.White),
        content = content,
    )
}
