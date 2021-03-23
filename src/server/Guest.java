package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Guest {
	Socket socket;
	
	public Guest(Socket socket) {
		this.socket = socket;
		receive();
	}
	
	public void send(String message) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				} catch(Exception e) {
					try {
						System.out.println("[메시지 송신 오류]"
								+socket.getRemoteSocketAddress()
								+": " + Thread.currentThread().getName());
						
						
						ServerController.guests.remove(Guest.this);
						socket.close();
					}catch(Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		ServerController.threadPool.submit(thread);
	}
	
	public void receive() {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try{
					while(true) {
						InputStream in = socket.getInputStream();
						byte[] buffer = new byte[512];
						int length = in.read(buffer);
						while(length==-1) {
							throw new IOException();
						}
						System.out.println("[메시지 수신 성공]"
						+socket.getRemoteSocketAddress()
						+ ": " + Thread.currentThread().getName());
						
						
						String message = new String(buffer, 0, length, "UTF-8");
						
						for(Guest guest : ServerController.guests) {
							guest.send(message);
						}
					}
				}catch(Exception e) {
					try {
						System.out.println("[메시지 수신 오류] "
							+socket.getRemoteSocketAddress()
								+": "+Thread.currentThread().getName());
						
						
					}catch(Exception e2) {
						e2.printStackTrace();
					}
				}
			}
			
		};
		ServerController.threadPool.submit(thread);
	}
}