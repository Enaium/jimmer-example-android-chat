/*
 * Copyright (c) 2025 Enaium
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package cn.enaium.chat.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cn.enaium.chat.model.entity.dto.ContactView
import cn.enaium.chat.ui.vm.ContactInfoViewModel
import cn.enaium.chat.utility.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter

/**
 * @author Enaium
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactInfo(nav: NavController, contactId: String) {
    val viewModel: ContactInfoViewModel = koinViewModel()
    val context = LocalContext.current

    val contact by viewModel.contact.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()
    val editAddress by viewModel.editAddress.collectAsState()
    val editRemark by viewModel.editRemark.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(contactId) {
        viewModel.loadContact(contactId)
    }

    // Show error messages as toasts
    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
            context.toast(errorMessage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contact Info") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Edit button
                    IconButton(
                        onClick = { viewModel.showEditDialog() },
                        enabled = contact != null
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Contact")
                    }
                    // Message button
                    IconButton(
                        onClick = { nav.navigate("message/$contactId") },
                        enabled = contact != null
                    ) {
                        Icon(Icons.Default.Message, contentDescription = "Send Message")
                    }
                    // Delete button
                    IconButton(
                        onClick = { viewModel.showDeleteDialog() },
                        enabled = contact != null
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Contact")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (contact == null) {
                Text(
                    text = "Contact not found",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                ContactInfoContent(contact = contact!!)
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteDialog() },
            title = { Text("Delete Contact") },
            text = {
                Text(
                    "Are you sure you want to delete this contact? " +
                            "This action cannot be undone and will also delete all associated messages."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteContact(contactId) {
                            context.toast("Contact deleted successfully")
                            withContext(Dispatchers.Main) {
                                viewModel.hideDeleteDialog()
                            }
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit contact dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideEditDialog() },
            title = { Text("Edit Contact") },
            text = {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    TextField(
                        value = editAddress,
                        onValueChange = { viewModel.updateEditAddress(it) },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = editRemark,
                        onValueChange = { viewModel.updateEditRemark(it) },
                        label = { Text("Remark (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.editContact(contactId) {
                            context.toast("Contact updated successfully")
                        }
                    },
                    enabled = editAddress.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideEditDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ContactInfoContent(contact: ContactView) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Contact Details",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                ContactInfoRow("Name", contact.remark.ifEmpty { "No name" })
                ContactInfoRow("Address", contact.address)
                ContactInfoRow(
                    "Created",
                    contact.createdTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                )
                ContactInfoRow(
                    "Modified",
                    contact.modifiedTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                )
            }
        }
    }
}

@Composable
fun ContactInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
} 