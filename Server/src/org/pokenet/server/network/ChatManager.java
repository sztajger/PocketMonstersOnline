package org.pokenet.server.network;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.mina.common.IoSession;
import org.pokenet.server.GameServer;
import org.pokenet.server.backend.ServerMap;

/**
 * Handles chat messages sent by players
 * @author shadowkanji
 *
 */
public class ChatManager implements Runnable {
	private Thread m_thread;
	@SuppressWarnings("unused")
	private boolean m_isRunning;
	/*
	 * Local chat queue
	 * [Message, x, y]
	 */
	private Queue<Object []> m_localQueue;
	/*
	 * Private chat queue
	 * [session, sender, message]
	 */
	private Queue<Object []> m_privateQueue;
	
	/**
	 * Default Constructor
	 */
	public ChatManager() {
		m_thread = new Thread(this);
		m_localQueue = new ConcurrentLinkedQueue<Object []>();
		m_privateQueue = new ConcurrentLinkedQueue<Object []>();
	}
	
	/**
	 * Returns how many messages are queued in this chat manager
	 * @return
	 */
	public int getProcessingLoad() {
		return m_localQueue.size() + m_privateQueue.size();
	}
	
	/**
	 * Queues a local chat message
	 * @param message
	 * @param mapX
	 * @param mapY
	 */
	public void queueLocalChatMessage(String message, int mapX, int mapY) {
		m_localQueue.add(new Object[]{message, String.valueOf(mapX), String.valueOf(mapY)});
	}
	
	/**
	 * Queues a private chat message
	 * @param message
	 * @param receiver
	 * @param sender
	 */
	public void queuePrivateMessage(String message, IoSession receiver, String sender) {
		m_privateQueue.add(new Object[]{message, receiver, sender});
	}
	
	/**
	 * Called by m_thread.start()
	 */
	public void run() {
		Object [] o;
		ServerMap m;
		IoSession s;
		while(true) {
			//Send next local chat message
			synchronized(m_localQueue) {
				if(m_localQueue.peek() != null) {
					o = m_localQueue.poll();
					m = GameServer.getServiceManager().getMovementService().
						getMapMatrix().getMapByGamePosition(Integer.parseInt((String) o[1]), Integer.parseInt((String) o[2]));
					if(m != null)
						m.sendToAll("Cl" + ((String)o[0]));
				}
			}
			//Send next private chat message
			synchronized(m_privateQueue) {
				if(m_privateQueue.peek() != null) {
					o = m_privateQueue.poll();
					s = (IoSession) o[0];
					if(s.isConnected() && !s.isClosing())
						s.write("Cp" + ((String) o[1]) + "," + ((String) o[2]));
				}
			}
			try {
				Thread.sleep(250);
			} catch (Exception e) {}
		}
	}
	
	/**
	 * Start this chat manager
	 */
	public void start() {
		m_isRunning = true;
		m_thread.start();
	}
	
	/**
	 * Stop this chat manager
	 */
	public void stop() {
		m_isRunning = false;
	}

}