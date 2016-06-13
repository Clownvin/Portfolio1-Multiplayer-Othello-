package com.cs319.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.cs319.server.othello.Message;
import com.cs319.server.packets.Packet;
import com.cs319.server.packets.PacketFactory;
import com.cs319.server.util.ClosedIDSystem;
import com.cs319.server.util.ClosedIDSystem.IDTag;
import com.cs319.server.util.CorruptDataException;
import com.cs319.server.util.StreamUtils;

public final class Connection {
	//States
	public static final byte IDLE = 0;
	public static final byte SEARCHING = 1;
	public static final byte IN_GAME = 2;
	//Current state
	private volatile byte status = 0;
	//Socket related objects
	private final Socket socket;
	private final OutputStream out;
	private final InputStream in;
	//Is disconnected (or want to disconnect)
	private volatile boolean disconnected = false;
	//Unique IDTag
	private final IDTag tag;
	//Object to wait on
	private final Object waitObject;
	//Packet lists
	private final List<Packet> outgoingPackets = new ArrayList<>();
	private volatile List<Packet> incommingPackets = new ArrayList<>(10);
	//Thread that reads packets from input stream
	private final Thread inputThread = new Thread(new Runnable() {

		@Override
		public void run() {
			while (!disconnected) {
				synchronized (waitObject) {
					try {
						waitObject.wait();
					} catch (InterruptedException e) {
					}
				}
				incommingPackets.clear();
				try {
					do {
						try {
							incommingPackets.add(PacketFactory.buildPacket(StreamUtils.readBlockFromStream(in)));
						} catch (CorruptDataException e) {
							continue;
						} catch (IOException e) {
							disconnect();
							break;
						} catch (RuntimeException e) {
							e.printStackTrace();
						}
						if (incommingPackets.size() == 11) {
							sendPacket(new Message("You've been disconnect for sending packets to quickly."));
							Thread.sleep(100); // To make sure outputThread has time to send packet.
							disconnect();
						}
					} while (in.available() > 0);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}

	});
	//Sends packets
	private final Thread outputThread = new Thread(new Runnable() {

		@Override
		public void run() {
			while (!disconnected) {
				try {
					synchronized (outgoingPackets) {
						while (outgoingPackets.size() > 0) {
							System.out.println("Sending " + outgoingPackets.get(0) + " to " + Connection.this);
							StreamUtils.writeBlockToStream(out, outgoingPackets.remove(0).toBytes());
						}
						outgoingPackets.wait();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	});
	
	public Connection(final Socket socket, final Object waitObject) throws IOException {
		this.socket = socket;
		this.out = socket.getOutputStream();
		this.in = socket.getInputStream();
		this.waitObject = waitObject;
		this.tag = ClosedIDSystem.getTag(); // Get a unique tag to distinguish this connection from system
		inputThread.start();
		outputThread.start();
	}

	public void disconnect() {
		tag.returnTag(); // Make sure tag goes back into system.
		try {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
			if (socket != null)
				socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		disconnected = true;
		synchronized (outgoingPackets) {
			outgoingPackets.notifyAll(); // Make sure outgoing thread exits wait block.
		}
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Connection && ((Connection) other).tag.equals(tag);
	}

	public Packet getPacket() {
		synchronized (incommingPackets) {
			return incommingPackets.remove(0);
		}
	}

	public byte getStatus() {
		return status;
	}

	public boolean hasPacket() {
		synchronized (incommingPackets) {
			return incommingPackets.size() > 0;
		}
	}

	public boolean isDisconnected() {
		return disconnected;
	}

	public void sendPacket(final Packet packet) {
		synchronized (outgoingPackets) {
			outgoingPackets.add(packet);
			outgoingPackets.notifyAll(); // notify of new packet (will wake thread)
		}
	}

	public void setStatus(final byte status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return socket.getInetAddress().getHostAddress() + ":" + tag.getID();
	}
}
