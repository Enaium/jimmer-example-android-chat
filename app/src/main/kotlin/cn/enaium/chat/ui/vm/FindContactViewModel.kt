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

package cn.enaium.chat.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.enaium.chat.model.entity.dto.ContactInput
import cn.enaium.chat.net.protocol.FindPacket
import cn.enaium.chat.net.protocol.FindReplyPacket
import cn.enaium.chat.utility.DataService
import cn.enaium.chat.utility.send
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.InetSocketAddress

/**
 * @author Enaium
 */
class FindContactViewModel : ViewModel(), KoinComponent {

    private val dataService: DataService by inject()

    private val _discoveredContacts = MutableStateFlow<List<String>>(emptyList())
    val discoveredContacts: StateFlow<List<String>> = _discoveredContacts.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    private val _isManualMode = MutableStateFlow(false)
    val isManualMode: StateFlow<Boolean> = _isManualMode.asStateFlow()

    private val _manualAddress = MutableStateFlow("")
    val manualAddress: StateFlow<String> = _manualAddress.asStateFlow()

    private val _manualRemark = MutableStateFlow("")
    val manualRemark: StateFlow<String> = _manualRemark.asStateFlow()

    init {
        setupDiscoveryCallback()
    }

    private fun setupDiscoveryCallback() {
        FindReplyPacket.DiscoveryCallback.onContactFound = { address ->
            viewModelScope.launch {
                if (!_discoveredContacts.value.contains(address)) {
                    _discoveredContacts.value = _discoveredContacts.value + address
                }
            }
        }
    }

    fun startDiscovery() {
        viewModelScope.launch {
            _isScanning.value = true
            _errorMessage.value = ""
            _discoveredContacts.value = emptyList()

            try {
                // Send broadcast FindPacket
                send(
                    InetSocketAddress("255.255.255.255", 8888),
                    FindPacket()
                )

                // Wait for responses
                delay(3000) // Wait 3 seconds for responses

                if (_discoveredContacts.value.isEmpty()) {
                    _errorMessage.value = "No contacts found on the network"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Discovery failed: ${e.message}"
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun setManualMode(enabled: Boolean) {
        _isManualMode.value = enabled
        if (!enabled) {
            startDiscovery()
        }
    }

    fun updateManualAddress(address: String) {
        _manualAddress.value = address
    }

    fun updateManualRemark(remark: String) {
        _manualRemark.value = remark
    }

    fun addManualContact(onSuccess: suspend () -> Unit) {
        if (_manualAddress.value.isBlank()) {
            _errorMessage.value = "Please enter an IP address"
            return
        }

        viewModelScope.launch {
            try {
                val contactInput = ContactInput(
                    address = _manualAddress.value,
                    remark = _manualRemark.value
                )
                dataService.saveContact(contactInput)
                _errorMessage.value = ""
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "Invalid IP address: ${e.message}"
            }
        }
    }

    fun addDiscoveredContact(address: String, onSuccess: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                val contactInput = ContactInput(
                    address = address,
                    remark = ""
                )
                dataService.saveContact(contactInput)
                _errorMessage.value = ""
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add contact: ${e.message}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up callback when view model is cleared
        FindReplyPacket.DiscoveryCallback.onContactFound = null
    }
} 