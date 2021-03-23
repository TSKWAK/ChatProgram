package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ClientController implements Initializable{
	@FXML public Button sendButtonC;
	@FXML public Button connectButton;
	@FXML public TextField portC;
	@FXML public TextField nickName;
	@FXML public TextField ipAddress;
	@FXML public TextArea chatArea;
	@FXML public TextField message;
	
	Socket socket;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		sendButtonC.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				sendButtonCAction(event);
			}
		});
		
		connectButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				connectButtonCAction(event);
			}
		});
		
	}
	
	//클라이언트의 접속버튼을 눌렀을때 실행되는 메소드
	protected void connectButtonCAction(ActionEvent event) {
		String ip = ipAddress.getText();
		int port = Integer.parseInt(portC.getText());
		
		startClient(ip, port);
		
		connectButton.setDisable(true);
	}
	
	
	//클라이언트 접속을 시작하는 메소드
	public void startClient(String ip, int port) {
		Thread thread = new Thread() {
			public void run() {
				try {
					socket = new Socket(ip, port);
					receive();
				}catch(Exception e) {
					if(!socket.isClosed()) {
						stopClient();
						System.out.println("[서버 접속 실패]");
						Platform.exit();
					}
				}
			}
		};
		thread.start();
	}
	
	
	
	//클라이언트 프로그램 종료 메소드
	public void stopClient() {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//서버로부터 메시지를 전달받는 메소드
	public void receive() {
		while(true) {
			try {
				InputStream in = socket.getInputStream();
				byte[] buffer = new byte[512];
				int length = in.read(buffer);
				if(length==-1) {
					throw new IOException();
				}
				String message = new String(buffer, 0, length, "UTF-8");
				Platform.runLater(()->{
					chatArea.appendText(message);
				});
			}catch(Exception e) {
				stopClient();
				break;
			}
		}
	}
	
	public void send(String message) {
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				}catch(Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}
	
	//클라이언트의 전송버튼을 눌렀을때 실행되는 메소드
	protected void sendButtonCAction(ActionEvent event) {
		send(nickName.getText() + ": " + message.getText()+ "\n");
		message.setText("");
	}
	
}
