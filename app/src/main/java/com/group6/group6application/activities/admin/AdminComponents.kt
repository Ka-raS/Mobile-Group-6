package com.group6.group6application.activities.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import java.text.Normalizer
import java.util.Locale

data class AdminRecord(val id: Int, val title: String, val subtitle: String, val details: List<Pair<String, String>>)

/** Accent-insensitive search also works for Vietnamese names and the letter đ. */
internal fun searchKey(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFD)
    .replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT).replace('đ', 'd')

@Composable
fun AdminHeader(title: String, onBack: () -> Unit, enabled: Boolean = true) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = onBack, enabled = enabled) { Text("Back") }
        Text(title, style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
fun AdminError(message: String?) {
    message?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
}

@Composable
fun AdminListContent(
    title: String,
    records: List<AdminRecord>?,
    query: String,
    onQuery: (String) -> Unit,
    searchHint: String,
    busy: Boolean,
    error: String?,
    deleteWarning: String,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit,
) {
    var detailId by rememberSaveable { mutableStateOf<Int?>(null) }
    var deleteId by rememberSaveable { mutableStateOf<Int?>(null) }
    val key = searchKey(query.trim())
    val filtered = records?.filter { record ->
        searchKey(record.title + " " + record.subtitle + " " + record.details.joinToString(" ") { it.second }).contains(key)
    }
    BackHandler(enabled = busy) { /* Finish the database operation before leaving. */ }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AdminHeader(title, onBack, !busy)
        Button(onClick = onAdd, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("Add New") }
        OutlinedTextField(
            value = query, onValueChange = onQuery, label = { Text("Search") },
            placeholder = { Text(searchHint) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
        )
        AdminError(error)
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        when {
            records == null && error == null -> CircularProgressIndicator()
            records == null -> Unit
            filtered.isNullOrEmpty() -> Text(if (query.isBlank()) "No data yet. Tap Add New to get started." else "No matching results found.")
            else -> {
                Text("${filtered.size} ${if (filtered.size == 1) "result" else "results"}", style = MaterialTheme.typography.labelLarge)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filtered, key = { it.id }) { record ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(record.title, style = MaterialTheme.typography.titleMedium)
                                Text(record.subtitle, style = MaterialTheme.typography.bodyMedium)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    TextButton(onClick = { detailId = record.id }, enabled = !busy) { Text("Details") }
                                    TextButton(onClick = { onEdit(record.id) }, enabled = !busy) { Text("Edit") }
                                    TextButton(onClick = { deleteId = record.id }, enabled = !busy) {
                                        Text("Delete", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    records?.find { it.id == detailId }?.let { record ->
        AlertDialog(
            onDismissRequest = { detailId = null }, title = { Text(record.title) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    record.details.forEach { (label, value) ->
                        Column {
                            Text(label, style = MaterialTheme.typography.labelMedium)
                            Text(value.ifBlank { "None" })
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { detailId = null; onEdit(record.id) }) { Text("Edit") } },
            dismissButton = { TextButton(onClick = { detailId = null }) { Text("Close") } },
        )
    }
    records?.find { it.id == deleteId }?.let { record ->
        AlertDialog(
            onDismissRequest = { deleteId = null }, title = { Text("Delete ${record.title}?") },
            text = { Text(deleteWarning) },
            confirmButton = {
                TextButton(onClick = { deleteId = null; onDelete(record.id) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { deleteId = null }) { Text("Cancel") } },
        )
    }
    // Do not restore a stale dialog if its record is removed by another operation.
    LaunchedEffect(records) {
        if (records != null) {
            if (records.none { it.id == detailId }) detailId = null
            if (records.none { it.id == deleteId }) deleteId = null
        }
    }
}

@Composable
fun AdminFormContent(
    title: String,
    busy: Boolean,
    loaded: Boolean,
    error: String?,
    onBack: () -> Unit,
    onSave: () -> Unit,
    fields: @Composable ColumnScope.() -> Unit,
) {
    BackHandler(enabled = busy) { }
    Column(
        Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AdminHeader(title, onBack, !busy)
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (loaded) fields()
        AdminError(error)
        Button(onClick = onSave, enabled = loaded && !busy, modifier = Modifier.fillMaxWidth()) {
            Text(if (busy) "Saving…" else "Save")
        }
    }
}

@Composable
fun AdminField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    enabled: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    multiline: Boolean = false,
    password: Boolean = false,
) {
    var showPassword by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) }, enabled = enabled,
        singleLine = !multiline, minLines = if (multiline) 3 else 1,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (password && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (password) {
            { TextButton(onClick = { showPassword = !showPassword }) { Text(if (showPassword) "Hide" else "Show") } }
        } else null,
        modifier = Modifier.fillMaxWidth(),
    )
}

