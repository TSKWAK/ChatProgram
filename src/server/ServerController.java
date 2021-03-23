package server;

import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ServerController implements Initializable{
	ServerSocket serverSocket;
	
	//여러개의 스레드를 효율적으로 관리하기 위한 대표적인 스레드풀 (스레드 갯수 제한)
	public static ExecutorService threadPool;
		
	//연결되는 클라이언트들을 따로 저장하는 배열의 한 방식인 Vector 컬렉션 프레임워크를 이용
	public static Vector<Guest> guests = new Vector<Guest>();
	
	@FXML public Button sendButtonS;
	@FXML public Button startButton;
	@FXML public TextField ipAddress;
	@FXML public TextField portS;
	@FXML public TextArea chatArea;
	@FXML public TextField message;
	
	@Override
	public void initialize(URL location, ResourceBundle resources){
		sendButtonS.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				sendButtonSAction(event);
			}
		});
		
		startButton.setOnAction(new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				startButtonAction(event);
			}
		});

		
	}
	
	//서버의 접속버튼을 눌렀을때 실행되는 메소드
	protected void startButtonAction(ActionEvent event){
		
		startButton.setDisable(true);
		
		
		String ip = ipAddress.getText();
		int port = Integer.parseInt(portS.getText());
		startServer(ip,port);
		Platform.runLater(() -> {
			String message = String.format("[서버 시작]\n",ip,port);
			if(!serverSocket.isClosed()) {
			chatArea.appendText(message);
			}
		});
	}
	
	protected void startServer(String ip, int port) {
		//서버소켓을 생성하는 생성부
		try {
			serverSocket = new ServerSocket();		//serverSocket을 생성함으로써 클라이언트들이 연결될 수 있도록함
			serverSocket.bind(new InetSocketAddress(ip, port));	//serverSocket에 연결하기 위한 값들을 바인딩함(IP, 포트)
			
		} catch(BindException e1) {
			chatArea.appendText("이미 사용중인 바인드입니다. \n");
			startButton.setDisable(false);
			if(!serverSocket.isClosed()) {			//오류가 발생했을때 혹시라도 서버소켓이 닫혀있지 않을시 제대로 닫아줌
				stopServer();
			}
			return;
		}
		catch(Exception e) {
			e.printStackTrace();
			if(!serverSocket.isClosed()) {			//오류가 발생했을때 혹시라도 서버소켓이 닫혀있지 않을시 제대로 닫아줌
				stopServer();
			}
			return;
		}
		
		//클라이언트가 접속할 때 까지 기다리는 쓰레드
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(true) {
					Socket socket;
					try {
						socket = serverSocket.accept();		//accept 메소드는 연결이 되기 전까진 차단되기때문에 연결이 되어야만 이 대입이 이루어진다.
						guests.add(new Guest(socket));
						System.out.println("[클라이언트 접속] "
								+socket.getRemoteSocketAddress()
								+": " + Thread.currentThread().getName());
						chatArea.appendText("[클라이언트 접속] "
								+socket.getRemoteSocketAddress()
								+": " + Thread.currentThread().getName()+"\n");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						if(!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}
					
				}
			}
			
		};
		threadPool = Executors.newCachedThreadPool();
		threadPool.submit(thread);
	}
	
	//서버의 연결을 끊는 메소드
	protected void stopServer() {
		try {
			//현재 작동 중인 모든 소켓 닫기
			Iterator<Guest> iterator = guests.iterator();
			while(iterator.hasNext()) {
				Guest guest = iterator.next();
				guest.socket.close();
				iterator.remove();
			}
			//서버 소켓 닫기
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			//스레드 풀 종료하기
			if(threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdown();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	//클라이언트의 전송버튼을 눌렀을때 실행되는 메소드
	protected void sendButtonSAction(ActionEvent event) {
		Platform.runLater(()->{
			for(Guest guest : guests) {
				guest.send("Root:"+message.getText()+"\n");
			}
			chatArea.appendText("Root:"+message.getText()+"\n");
			message.setText("");
		});
	}
	
	
	
}
