package com.ntscorp.auto_client.appium;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import com.ntscorp.auto_client.data.AutomationTime;

public class AvailablePortFinder implements AutomationTime{

	private AvailablePortFinder() {
		// Do nothing
	}

	public static int getAvailablePort() {
		TreeSet<Integer> sortedSet = new TreeSet<Integer>(getAvailablePorts(MIN_PORT_NUMBER, MAX_PORT_NUMBER));
		
		return sortedSet.last();
	}

	public static int getNextAvailable() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(0);
			int port = serverSocket.getLocalPort();
			serverSocket.close();

			return port;
		} catch (IOException ioe) {
			throw new NoSuchElementException(ioe.getMessage());
		}
	}

	public static int getNextAvailable(int fromPort) {
		if (fromPort < MIN_PORT_NUMBER || fromPort > MAX_PORT_NUMBER) {
			throw new IllegalArgumentException("Invalid start port: " + fromPort);
		}
		for (int i = fromPort; i <= MAX_PORT_NUMBER; i++) {
			if (available(i)) {
				return i;
			}
		}
		throw new NoSuchElementException("Could not find an available port " + "above " + fromPort);
	}

	public static boolean available(int port) {
		if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
			throw new IllegalArgumentException("Invalid start port: " + port);
		}
		ServerSocket ss = null;
		DatagramSocket ds = null;

		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
			// Do nothing
		} finally {
			if (ds != null) {
				ds.close();
			}
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
				}
			}
		}
		return false;
	}

	public static Set<Integer> getAvailablePorts(int fromPort, int toPort) {
		if (fromPort < MIN_PORT_NUMBER || toPort > MAX_PORT_NUMBER || fromPort > toPort) {
			throw new IllegalArgumentException("Invalid port range: " + fromPort + " ~ " + toPort);
		}
		Set<Integer> result = new TreeSet<Integer>();

		for (int i = fromPort; i <= toPort; i++) {
			ServerSocket s = null;
			try {
				s = new ServerSocket(i);
				result.add(Integer.valueOf(i));
			} catch (IOException e) {
				// Do nothing
			} finally {
				if (s != null) {
					try {
						s.close();
					} catch (IOException e) {
						/* should not be thrown */
					}
				}
			}
		}
		return result;
	}
}
