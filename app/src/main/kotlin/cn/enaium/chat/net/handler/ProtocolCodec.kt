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

package cn.enaium.chat.net.handler

import cn.enaium.chat.net.protocol.FindPacket
import cn.enaium.chat.net.protocol.FindReplyPacket
import cn.enaium.chat.net.protocol.ProtocolPacket
import cn.enaium.chat.net.protocol.TextMessagePacket
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.DatagramPacket
import io.netty.handler.codec.MessageToMessageCodec
import kotlin.math.min

/**
 * @author Enaium
 */
@ChannelHandler.Sharable
class ProtocolCodec : MessageToMessageCodec<DatagramPacket, ProtocolPacket>() {
    override fun encode(
        ctx: ChannelHandlerContext,
        msg: ProtocolPacket,
        out: MutableList<Any>
    ) {
        val buffer = ctx.alloc().buffer()
        buffer.writeInt(MAGIC)
        buffer.writeInt(msg.type)
        when (msg) {
            is FindPacket -> {
                // Nothing...
            }

            is FindReplyPacket -> {
                // Nothing...
            }

            is TextMessagePacket -> {
                buffer.writeByte(min(msg.content.length, 100))
                buffer.writeCharSequence(msg.content, Charsets.UTF_8)
            }
        }
        out.add(buffer)
    }

    override fun decode(
        ctx: ChannelHandlerContext,
        msg: DatagramPacket,
        out: List<Any>
    ) {
        val content = msg.content()

        if (content.readableBytes() < 8) {
            content.resetReaderIndex()
            ctx.fireChannelRead(msg.retain())
            return
        }


        val magic = content.readInt()

        if (magic != MAGIC) {
            content.resetReaderIndex()
            ctx.fireChannelRead(msg.retain())
            return
        }

        val type = content.readInt()
        val sender = msg.sender()
        when (type) {
            FindPacket.TYPE -> {
                ctx.fireChannelRead(FindPacket(type, sender))
            }

            FindReplyPacket.TYPE -> {
                ctx.fireChannelRead(FindReplyPacket(type, sender))
            }

            TextMessagePacket.TYPE -> {
                val length = content.readByte()
                ctx.fireChannelRead(
                    TextMessagePacket(
                        type,
                        msg.sender(),
                        content.readCharSequence(length.toInt(), Charsets.UTF_8).toString()
                    )
                )
            }
        }
    }
}

const val MAGIC = 0x250704E0