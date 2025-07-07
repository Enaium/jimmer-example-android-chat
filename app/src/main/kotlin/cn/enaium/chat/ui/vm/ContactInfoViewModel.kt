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
class ContactInfoViewModel : ViewModel(), KoinComponent {

    private val dataService: DataService by inject()

    private val _contact = MutableStateFlow<ContactView?>(null)
    val contact: StateFlow<ContactView?> = _contact.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    private val _editAddress = MutableStateFlow("")
    val editAddress: StateFlow<String> = _editAddress.asStateFlow()

    private val _editRemark = MutableStateFlow("")
    val editRemark: StateFlow<String> = _editRemark.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    fun loadContact(contactId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            try {
                val id = UUID.fromString(contactId)
                _contact.value = dataService.getContact(id)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load contact: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun showDeleteDialog() {
        _showDeleteDialog.value = true
    }

    fun hideDeleteDialog() {
        _showDeleteDialog.value = false
    }

    fun showEditDialog() {
        _contact.value?.let { contact ->
            _editAddress.value = contact.address
            _editRemark.value = contact.remark
            _showEditDialog.value = true
        }
    }

    fun hideEditDialog() {
        _showEditDialog.value = false
    }

    fun updateEditAddress(address: String) {
        _editAddress.value = address
    }

    fun updateEditRemark(remark: String) {
        _editRemark.value = remark
    }

    fun deleteContact(contactId: String, onSuccess: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                val id = UUID.fromString(contactId)
                dataService.deleteContact(id)
                _errorMessage.value = ""
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete contact: ${e.message}"
            }
        }
    }

    fun editContact(contactId: String, onSuccess: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                val id = UUID.fromString(contactId)
                val contactInput = ContactInput(
                    id = id,
                    address = _editAddress.value,
                    remark = _editRemark.value
                )
                dataService.saveContact(contactInput)
                _errorMessage.value = ""
                _showEditDialog.value = false
                // Refresh contact data
                _contact.value = dataService.getContact(id)
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update contact: ${e.message}"
            }
        }
    }
} 