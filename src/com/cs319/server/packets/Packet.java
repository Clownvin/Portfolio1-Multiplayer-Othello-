package com.cs319.server.packets;

public interface Packet {
	public byte getIdentifierByte();

	public byte[] toBytes();
}
