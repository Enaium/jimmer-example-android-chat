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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

/**
 * @author Enaium
 */
@Composable
fun AppNav(modifier: Modifier) {
    val nav = rememberNavController()

    Column(modifier.fillMaxSize()) {
        NavHost(navController = nav, startDestination = "home") {
            composable("home") {
                Home(nav)
            }
            composable("find_contact") {
                FindContact(nav)
            }
            composable(
                route = "message/{contactId}",
                arguments = listOf(
                    navArgument("contactId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
                Message(nav, contactId)
            }
            composable(
                route = "contact_info/{contactId}",
                arguments = listOf(
                    navArgument("contactId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
                ContactInfo(nav, contactId)
            }
        }
    }
}