package com.cs319.server.packets;

public class Request implements Packet {
	//Requests
	public static final byte REQUEST_GAME_SEARCH = 1;
	public static final byte REQUEST_LEAVE_GAME = 2;
	private final byte request;

	public Request(final byte request) {
		this.request = request;
	}

	public Request(final byte[] bytes) {
		this.request = bytes[1];
	}

	public byte getIdentifierByte() {
		return 5;
	}

	public byte getRequest() {
		return request;
	}

	public byte[] toBytes() {
		return new byte[] { getIdentifierByte(), request };
	}

	@Override
	public String toString() {
		return "Request: " + request;
	}

}
