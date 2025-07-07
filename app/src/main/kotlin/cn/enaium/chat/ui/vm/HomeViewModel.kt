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
import cn.enaium.chat.utility.DataService
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
class HomeViewModel : ViewModel(), KoinComponent {

    private val dataService: DataService by inject()

    private val _chats = MutableStateFlow<List<ContactView>>(emptyList())
    val chats: StateFlow<List<ContactView>> = _chats.asStateFlow()

    private val _contacts = MutableStateFlow<List<ContactView>>(emptyList())
    val contacts: StateFlow<List<ContactView>> = _contacts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _chats.value = dataService.getChats()
                _contacts.value = dataService.getContacts()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        loadData()
    }

    fun refreshChats() {
        viewModelScope.launch {
            try {
                _chats.value = dataService.getChats()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteContact(contactId: UUID) {
        viewModelScope.launch {
            try {
                dataService.deleteContact(contactId)
                // Refresh the contacts list after deletion
                _contacts.value = dataService.getContacts()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun refreshContacts() {
        viewModelScope.launch {
            try {
                _contacts.value = dataService.getContacts()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun clearMessages(contactId: UUID) {
        viewModelScope.launch {
            try {
                dataService.clearMessages(contactId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}