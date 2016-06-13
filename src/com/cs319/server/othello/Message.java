package com.cs319.server.othello;

import com.cs319.server.packets.Packet;

public final class Message implements Packet {
	private String message;

	public Message(final byte[] bytes) {
		//Get String of characters from the byte array
		char[] messageBuffer = new char[bytes.length - 1];
		for (int i = 1; i < bytes.length; i++) {
			messageBuffer[i - 1] = (char) bytes[i];
		}
		message = String.valueOf(messageBuffer);
	}

	public Message(final String message) {
		this.message = message;
	}

	public byte getIdentifierByte() {
		return (byte) 1;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public byte[] toBytes() {
		byte[] bytes = new byte[message.length() + 1];
		int index = 0;
		//Put all the bytes in the array
		bytes[index++] = getIdentifierByte();
		for (char c : message.toCharArray()) {
			bytes[index++] = (byte) c;
		}
		return bytes;
	}

	@Override
	public String toString() {
		return "Message: " + message;
	}

}
