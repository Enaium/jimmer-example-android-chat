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
import cn.enaium.chat.model.entity.dto.ContactView
import cn.enaium.chat.model.entity.dto.MessageInput
import cn.enaium.chat.model.entity.dto.MessageView
import cn.enaium.chat.net.protocol.TextMessagePacket
import cn.enaium.chat.utility.DataService
import cn.enaium.chat.utility.send
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

/**
 * @author Enaium
 */
class MessageViewModel : ViewModel(), KoinComponent {

    private val dataService: DataService by inject()

    private val _messages = MutableStateFlow<List<MessageView>>(emptyList())
    val messages: StateFlow<List<MessageView>> = _messages.asStateFlow()

    private val _contact = MutableStateFlow<ContactView?>(null)
    val contact: StateFlow<ContactView?> = _contact.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    fun loadData(contactId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            try {
                val id = UUID.fromString(contactId)
                _contact.value = dataService.getContact(id)
                _messages.value = dataService.getMessages(id)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun startMessageRefresh(contactId: String) {
        viewModelScope.launch {
            while (true) {
                delay(1000) // Refresh every second
                try {
                    val id = UUID.fromString(contactId)
                    _messages.value = dataService.getMessages(id)
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to refresh messages: ${e.message}"
                }
            }
        }
    }

    fun updateMessageText(text: String) {
        _messageText.value = text
    }

    fun sendMessage(contactId: String, onSuccess: () -> Unit) {
        if (_messageText.value.isBlank()) return

        viewModelScope.launch {
            try {
                _contact.value?.let { contactView ->
                    dataService.saveMessage(
                        MessageInput(
                            _messageText.value,
                            true,
                            MessageInput.TargetOf_contact(contactView.address)
                        )
                    )
                    send(
                        contactView.address,
                        8888,
                        TextMessagePacket(content = _messageText.value)
                    )

                    // Refresh the message list after sending
                    val id = UUID.fromString(contactId)
                    _messages.value = dataService.getMessages(id)

                    _messageText.value = ""
                    _errorMessage.value = ""
                    onSuccess()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to send message: ${e.message}"
            }
        }
    }

    fun deleteMessage(messageId: UUID, contactId: String) {
        viewModelScope.launch {
            try {
                dataService.deleteMessage(messageId)
                // Refresh messages after deletion
                val id = UUID.fromString(contactId)
                _messages.value = dataService.getMessages(id)
                _errorMessage.value = ""
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete message: ${e.message}"
            }
        }
    }
} 