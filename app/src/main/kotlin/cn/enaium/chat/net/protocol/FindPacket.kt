package cn.enaium.chat.net.protocol

import cn.enaium.chat.utility.send
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import java.net.InetSocketAddress

/**
 * @author Enaium
 */
data class FindPacket(
    override val type: Int = TYPE,
    override var sender: InetSocketAddress = InetSocketAddress("0.0.0.0", 8888)
) : ProtocolPacket(type, sender) {
    companion object {
        const val TYPE = 0x0
    }

    @ChannelHandler.Sharable
    class Handler : SimpleChannelInboundHandler<FindPacket>() {
        override fun channelRead0(
            ctx: ChannelHandlerContext,
            msg: FindPacket
        ) {
            send(msg.sender, FindReplyPacket())
        }
    }
}
