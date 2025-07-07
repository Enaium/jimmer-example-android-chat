package cn.enaium.chat.net.protocol

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import java.net.InetSocketAddress

/**
 * @author Enaium
 */
data class FindReplyPacket(
    override val type: Int = TYPE,
    override var sender: InetSocketAddress = InetSocketAddress.createUnresolved("0.0.0.0", 0)
) : ProtocolPacket(type, sender) {
    companion object {
        const val TYPE = 0x1
    }

    @ChannelHandler.Sharable
    class Handler : SimpleChannelInboundHandler<FindReplyPacket>() {
        override fun channelRead0(
            ctx: ChannelHandlerContext,
            msg: FindReplyPacket
        ) {
            // Notify the discovery callback if available
            DiscoveryCallback.onContactFound?.invoke(msg.sender.address.hostAddress ?: "unknown")
        }
    }

    object DiscoveryCallback {
        var onContactFound: ((String) -> Unit)? = null
    }
}
