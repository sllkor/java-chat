package chat.server.handler;

import java.io.IOException;
import java.util.List;

import chat.common.handler.ChannelState;
import chat.common.handler.ProtocolDirection;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class ChatServerPacketDecoder extends ByteToMessageDecoder {
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		// TODO Auto-generated method stub
		int pid = in.readInt();
		ChannelState cs = ctx.channel().attr(AttributeSaver.state).get();
		Packet<?> p = cs.m.get(ProtocolDirection.SERVERBOUND).get(pid).getDeclaredConstructor().newInstance();
		if (p == null) {
			throw new IOException("Bad packet id " + pid);
		}
		p.decode(in);
		System.out.println("[Decoder] Server: Processing packet " + p.getClass().getSimpleName() + " (0x"
				+ Integer.toHexString(pid) + ") " + Utils.serialize(p));
		out.add(p);
	}
}
