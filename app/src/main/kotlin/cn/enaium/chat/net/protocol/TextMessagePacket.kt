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

package cn.enaium.chat.net.protocol

import cn.enaium.chat.model.entity.dto.MessageInput
import cn.enaium.chat.utility.DataService
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.InetSocketAddress

/**
 * @author Enaium
 */
data class TextMessagePacket(
    override val type: Int = TYPE,
    override var sender: InetSocketAddress = InetSocketAddress.createUnresolved("0.0.0.0", 0),
    val content: String
) : ProtocolPacket(type, sender) {
    companion object {
        const val TYPE = 0x3
    }

    @ChannelHandler.Sharable
    class Handler : SimpleChannelInboundHandler<TextMessagePacket>(), KoinComponent {
        val dataService: DataService by inject()

        override fun channelRead0(
            ctx: ChannelHandlerContext,
            msg: TextMessagePacket
        ) {
            // Process incoming message in background
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val senderAddress = msg.sender.address.hostAddress ?: "unknown"

                    // Save the incoming message
                    val messageInput = MessageInput(
                        content = msg.content,
                        me = false, // This is an incoming message, not from me
                        contact = MessageInput.TargetOf_contact(senderAddress)
                    )
                    dataService.saveMessage(messageInput)

                    // Notify UI to refresh (this will be handled by the ViewModel)
                    // The UI will automatically refresh when it becomes active
                } catch (e: Exception) {
                    // Handle error - could log or show notification
                    e.printStackTrace()
                }
            }
        }
    }
}