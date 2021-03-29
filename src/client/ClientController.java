package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

public class ClientController implements Initializable{
	@FXML public Button sendButtonC;
	@FXML public Button connectButton;
	@FXML public TextField portC;
	@FXML public TextField nickName;
	@FXML public TextField ipAddress;
	@FXML public TextArea chatArea;
	@FXML public TextField message;
	@FXML public Button exportButton;
	
	//*은혜
	@FXML public Button Uploadbut;
	@FXML public Button Downloadbut;
	@FXML public TextField FileAddress;
	@FXML public TextField TargetFileAddress;
	
	@FXML public ListView<String> fileList;
	
	public static ObservableList<String> files = FXCollections.observableArrayList();
	
	static ArrayList<String> farr = new ArrayList<String>();
	static ArrayList<String> fco = new ArrayList<String>();
	
	
	//은혜 덧붙임
	@FXML
	public void upLoadClick(MouseEvent event) {	
		try {
		FileAddress.setText("");
		FileChooser fch = new FileChooser();
		FileAddress.setText(fch.showOpenDialog(null).getPath());
		} catch(Exception e) {
			
		}
		
	}
	
	@FXML
	public void downLoadClick(MouseEvent event) {
		try {
		TargetFileAddress.setText("");
		FileChooser fch = new FileChooser();
		TargetFileAddress.setText(fch.showSaveDialog(null).getPath());
		} catch(Exception e) {
			
		}
	}

	
	Socket socket;
	Socket fileSocket;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		fileList.setItems(files);
		
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
		
		message.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				sendButtonCAction(event);
			}
			
		});
		
		exportButton.setOnAction(new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent event) {
				File file = new File("C:/logTemp/Export.txt");
				
				try {
				FileWriter fw = new FileWriter(file, true);
				fw.write(chatArea.getText());
				send("추출완료 \n");
				
				fw.flush();
				fw.close();
				
				}catch(Exception e) {
					e.printStackTrace();
					send(e.getMessage());
				}
				
			}
			
		});
	
		
		//Upload~Download 은혜

				Uploadbut.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						UpLoad();
					}
					
				});
				
				Downloadbut.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						DownLoad();			
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
				byte[] buffer = new byte[1024];
				int length = in.read(buffer);
				if(length==-1) {
					throw new IOException();
				}
				
				// /로 시작하면 파일의 이름
				if(buffer[0]=='/') {
					String path = new String(buffer,1,length,"UTF-8");
					Platform.runLater(()->{
						farr.add(path);
						files.add(path);
						fileList.setItems(files);
					});
				}
				
				// $로 시작하면 파일의 내용
				else if(buffer[0]=='$') {
					String contents = new String(buffer,1,length,"UTF-8");
					fco.add(contents);
				}
				else if(buffer[0]=='|') {
					Platform.runLater(()->{
						files.remove(buffer[1]-48);
						farr.remove(buffer[1]-48);
						fco.remove(buffer[1]-48);
						fileList.setItems(files);
					});
				}
				else {
				String message = new String(buffer, 0, length, "UTF-8");
				Platform.runLater(()->{
					chatArea.appendText(message);
				});
				}
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
	
		
	public void UpLoad() {
		String path;
		String contents = "$";
		try {
			FileInputStream fis = new FileInputStream(FileAddress.getText());
			
			InputStreamReader is = new InputStreamReader(fis,"utf-8");
			
			path = "/" + FileAddress.getText();
			
			int cnt;
			
			while((cnt = is.read())!=-1) {
				contents = contents + (char)cnt;
			}
			
			send(path);
			
			wait(1000);
			
			
			
		} catch(InterruptedException e1) {
			System.out.println("소켓전송 대기중");
		} catch(Exception e) {
			System.out.println("파일경로가 올바르지 않습니다!");
		}
		send(contents);
		FileAddress.setText("");
	
	}
		
	public void DownLoad(){
		try {
			FileWriter fw = new FileWriter(TargetFileAddress.getText(),StandardCharsets.UTF_8);
			String contents = fco.get(fileList.getSelectionModel().getSelectedIndex());
			
			fw.write(contents);
			
			send(nickName.getText() + ": "+ TargetFileAddress.getText() + " 다운로드 완료 \n");
			
			Platform.runLater(()->{
				TargetFileAddress.setText("");
			});
			
			fw.close();
		} catch(Exception e) {
			System.out.println("경로가 올바르지 않습니다.");
		}
	}
	
}