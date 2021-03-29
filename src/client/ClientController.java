package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
import javafx.scene.input.MouseEvent;

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
				send("추출완료");
				
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
						UploadbutAction(event);
					}
					
				});
				
				Downloadbut.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						DownloadbutAction(event);				
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
	
	
	//은혜 덧붙임
		@FXML
		public void upLoadClick(MouseEvent event) {		
			FileAddress.setText("");
		}
		
		@FXML
		public void downLoadClick(MouseEvent event) {
			TargetFileAddress.setText("");
		}
		
		public void Upload() {

			String fileAddr = FileAddress.getText();
			int slash = fileAddr.lastIndexOf("\\");
			String fileName =fileAddr.substring(slash); 
			File file = new File(fileAddr);
			String tmpAddr = "C:\\FXProject\\tmpmemory";
			File tmpmemory= new File(tmpAddr+fileName);
			
			System.out.println("fileAddr: "+ fileAddr);
			System.out.println("fileName :  " + fileName);
			System.out.println("tmpAddr: " + tmpAddr);
			System.out.println("tmpmemory: " + tmpmemory);
					try{
						if(! new File(tmpAddr).exists()) { new File(tmpAddr).mkdirs();
						}
						if(! tmpmemory.exists()) tmpmemory.createNewFile();
						FileOutputStream os = new FileOutputStream(tmpmemory);
						FileInputStream is = new FileInputStream(file);
						int len;
						byte[] bytes = new byte[5000];
						while((len = is.read(bytes)) != -1) {
							os.write(bytes, 0, len);
						}	
						os.flush();
						os.close();
						is.close();
						
					} catch(Exception e) {
						e.printStackTrace();
					}
				System.out.println("[tmp 복사완료]");
			
		}
		
		public void Download(){		
		
			String arriveFileAddr = TargetFileAddress.getText();
			File arriveFile = new File(arriveFileAddr); 
			int slash2 = arriveFileAddr.lastIndexOf("\\"); 
			String newAddr = arriveFileAddr.substring(0, slash2);
			String fileName = arriveFileAddr.substring(slash2);
			String tmpAddr = "C:\\FXProject\\tmpmemory";
			File tmpmemory= new File(tmpAddr+fileName);
			
					try {		
						//파일어드레스 적으면 그것따라서 아래에 경로와 파일을 생성한다.
						if(! new File(newAddr).exists()) { new File(newAddr).mkdirs();
						}
						
						if(! arriveFile.exists()) arriveFile.createNewFile(); //파일생성
						FileOutputStream os = new FileOutputStream(arriveFile);
						FileInputStream is = new FileInputStream(tmpmemory);
						int len;
						byte[] bytes = new byte[5000];
						while((len = is.read(bytes)) != -1) {
							os.write(bytes, 0, len);
						}
						os.flush();
						os.close();
						is.close();
						System.out.println("[파일전송완료]");
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
		
		public void UploadbutAction(ActionEvent event) {
			Upload();
		}
		
		public void DownloadbutAction(ActionEvent event) {
			Download();
		}
	
	
}
