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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import cn.enaium.chat.ui.vm.FindContactViewModel
import cn.enaium.chat.ui.vm.HomeViewModel
import cn.enaium.chat.utility.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

/**
 * @author Enaium
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindContact(nav: NavController) {
    val findContactViewModel: FindContactViewModel = koinViewModel()
    val homeViewModel: HomeViewModel = koinViewModel()
    val context = LocalContext.current

    val discoveredContacts by findContactViewModel.discoveredContacts.collectAsState()
    val isScanning by findContactViewModel.isScanning.collectAsState()
    val errorMessage by findContactViewModel.errorMessage.collectAsState()
    val isManualMode by findContactViewModel.isManualMode.collectAsState()
    val manualAddress by findContactViewModel.manualAddress.collectAsState()
    val manualRemark by findContactViewModel.manualRemark.collectAsState()

    // Start discovery when screen is first loaded
    LaunchedEffect(Unit) {
        if (!isManualMode) {
            findContactViewModel.startDiscovery()
        }
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
                title = { Text("Find Contact") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isManualMode) {
                        IconButton(
                            onClick = { findContactViewModel.startDiscovery() },
                            enabled = !isScanning
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Mode toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isManualMode) "Manual Input" else "Auto Discovery",
                    style = MaterialTheme.typography.titleMedium
                )
                Switch(
                    checked = isManualMode,
                    onCheckedChange = {
                        findContactViewModel.setManualMode(it)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isManualMode) {
                // Manual input mode
                OutlinedTextField(
                    value = manualAddress,
                    onValueChange = { findContactViewModel.updateManualAddress(it) },
                    label = { Text("IP Address") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., 192.168.1.100") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = manualRemark,
                    onValueChange = { findContactViewModel.updateManualRemark(it) },
                    label = { Text("Remark (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., John's Phone") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = {
                        findContactViewModel.addManualContact {
                            homeViewModel.refreshContacts()
                            context.toast("Contact added successfully")
                            withContext(Dispatchers.Main) {
                                nav.popBackStack()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Contact")
                }
            } else {
                // Auto discovery mode
                if (isScanning) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Scanning for contacts...")
                        }
                    }
                } else {
                    if (discoveredContacts.isNotEmpty()) {
                        Text(
                            text = "Discovered Contacts",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn {
                            items(discoveredContacts) { contact ->
                                DiscoveredContactItem(
                                    contact = contact,
                                    onAddContact = { address ->
                                        findContactViewModel.addDiscoveredContact(address) {
                                            homeViewModel.refreshContacts()
                                            context.toast("Contact added successfully")
                                            withContext(Dispatchers.Main) {
                                                nav.popBackStack()
                                            }
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No contacts found",
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                if (errorMessage.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = errorMessage,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(onClick = { findContactViewModel.startDiscovery() }) {
                                    Text("Scan Again")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiscoveredContactItem(
    contact: String,
    onAddContact: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAddContact(contact) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = contact,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { onAddContact(contact) }
            ) {
                Text("Add")
            }
        }
    }
}