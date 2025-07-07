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

package cn.enaium.chat.net

import cn.enaium.chat.net.handler.ExceptionHandler
import cn.enaium.chat.net.handler.ProtocolCodec
import cn.enaium.chat.net.protocol.FindPacket
import cn.enaium.chat.net.protocol.FindReplyPacket
import cn.enaium.chat.net.protocol.TextMessagePacket
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.MultiThreadIoEventLoopGroup
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.nio.NioDatagramChannel
import org.koin.core.component.KoinComponent

/**
 * @author Enaium
 */
class ChatServer : KoinComponent {
    val codec = ProtocolCodec()
    val group = MultiThreadIoEventLoopGroup(NioIoHandler.newFactory())

    fun start() {
        Thread {
            try {
                val bootstrap = Bootstrap().group(group)
                    .channel(NioDatagramChannel::class.java)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(object : ChannelInitializer<Channel>() {
                        override fun initChannel(ch: Channel) {
                            ch.pipeline().addLast(codec)
                            ch.pipeline().addLast(FindPacket.Handler())
                            ch.pipeline().addLast(FindReplyPacket.Handler())
                            ch.pipeline().addLast(TextMessagePacket.Handler())
                            ch.pipeline().addLast(ExceptionHandler())
                        }
                    })
                val sync = bootstrap.bind(8888).sync()
                sync.channel().closeFuture().sync()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                group.shutdownGracefully()
            }
        }.start()
    }
}