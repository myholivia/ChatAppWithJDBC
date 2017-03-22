package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;


public class Server extends Application {
	
	/*---------------------------for server----------------------------------*/
	private static ServerSocket server;
	private static Socket each_conn; 
	private static ObjectInputStream input;
	private static ObjectOutputStream output;
	static String lastMsg;
	private String userId2;
    final static TextFlow history = new TextFlow();
    private static final List<String> list = new ArrayList<String>();
    private static final ObservableList<String> messageList = FXCollections.observableList(list);

	private static void close()
	{
		try{
			output.close();
			input.close();
			each_conn.close();

		}catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	private static void waitRequests() throws IOException
	{
		history.getChildren().clear();
		server = new ServerSocket(17173, 100);
		System.out.println("Waiting for new clients.");
		each_conn = server.accept(); //keep waiting until it returns
		System.out.println("A new client: " + each_conn.getInetAddress().getHostName());
	}

	private static void setStreams() throws IOException
	{
		output = new ObjectOutputStream(each_conn.getOutputStream());
		input = new ObjectInputStream(each_conn.getInputStream());
		System.out.println("Streams are setup!");
	}

	private void sendMessage(String m)
	{
		try {
			output.writeObject(m);
			output.flush();
			System.out.println("Message sent to client is: "+m);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static class MessageContainer implements Runnable
	{
		public void run()
		{
			while(true)
			{
				try {					
					lastMsg = (String)input.readObject();
					System.out.println("Message received from client is: " + lastMsg);
					
	        		int namePosi = 1;
	        		int stylePosi = 0;
	        		boolean styleFlag = lastMsg.contains("*&^@");
	        		boolean nameFlag = lastMsg.contains("$#%");
	        		if(nameFlag)
	        		{
	        			namePosi = lastMsg.indexOf("$#%");
	        		}
	        		if(styleFlag)
	        		{
	        			stylePosi = lastMsg.indexOf("*&^@");  
	        		}
	        		Text time1 =  new Text(lastMsg.substring(0, 8) + "\n");
	        		Text m1 = new Text(lastMsg.substring(8, namePosi) + ": " + 
	        				  		   lastMsg.substring(namePosi + 3, stylePosi) + "\n");
	        		String color = lastMsg.substring(stylePosi + 4).split(";")[1];
	        		m1.setStyle(lastMsg.substring(stylePosi + 4).split(";")[0]);
		        	m1.setFill(Color.valueOf(color));
					
					Platform.runLater(new Runnable() { 
		                @Override 
		                public void run() {
		                	history.getChildren().addAll(time1, m1);
		                } 
					});
					
				} 
				catch (ClassNotFoundException | IOException e) {
					// TODO Auto-generated catch block
					close();
					e.printStackTrace();
				}
			}
		}
	}
	    
	   
	    private void InitServerStage(Stage primaryStage) throws SQLException
	    {
	    	Group root = new Group();
	        Scene scene = new Scene(root, 650, 670, Color.WHITE);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			GridPane grid = new GridPane();
			
			grid.setId("grid-init2");
			grid.setPrefSize(650, 670);
			grid.setAlignment(Pos.CENTER);
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(15, 15, 15, 15));
			
	        //Set up the chat history
			ScrollPane textContainer = new ScrollPane();
			textContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); //Disable the horizontal scroll bar
			//final TextFlow history = new TextFlow();
			history.setId("history");
			history.setPrefSize(480, 280);
			history.getChildren().addListener(
	                (ListChangeListener<Node>) ((change) -> {
	                	history.layout();
	                    textContainer.layout();
	                    textContainer.setVvalue(1.0f);
	                }));//Enable the vertical scroll bar
			textContainer.setContent(history);
			String sql="SELECT * FROM records ";
			ResultSet rs = getRecords(sql);

			while (rs.next()) {
			Text temp=new Text(rs.getString("time") + "\n"+rs.getString("user")+ ": "+ rs.getString("message")+ "\n\n" );
			
				
				history.getChildren().add(temp);
			}
			GridPane.setHalignment(textContainer, HPos.CENTER);
			grid.add(textContainer, 1, 0);
			
			//Set up drop down lists
			ObservableList<String> colors = 
				FXCollections.observableArrayList(
				        "black",
				        "blue",
				        "red",
				        "green",
				        "pink",
				        "purple",
				        "silver"
				    );
			ObservableList<String> sizes = 
					FXCollections.observableArrayList(
					        "12",
					        "14",
					        "16",
					        "18",
					        "20",
					        "22",
					        "24"
					    );

			final ComboBox<String> color2 = new ComboBox<String>(colors);
			
			final ComboBox<String> size2 = new ComboBox<String>(sizes);
			final ComboBox<String> theme2 = new ComboBox<String>(colors);
			
			
			Callback<ListView<String>, ListCell<String>> factory = new Callback<ListView<String>, ListCell<String>>() {
		        @Override
		        public ListCell<String> call(ListView<String> list) {
		            return new ColorRectCell();
		        }
		    };
		    
		    theme2.setCellFactory(factory);
		    theme2.setButtonCell(factory.call(null));
			
			color2.setPrefSize(110, 35);
			color2.setValue("black");
			color2.setId("combobox");
			size2.setPrefSize(100, 35);
			size2.setValue("12");
			size2.setId("combobox");
			theme2.setPrefSize(140, 37);
			theme2.setValue("silver");
			theme2.setId("combobox");
			HBox hbDrop = new HBox(10);
			hbDrop.setAlignment(Pos.BOTTOM_CENTER);
			hbDrop.getChildren().addAll(color2, size2, theme2);
	        grid.add(hbDrop, 1, 1);
			
			//Set up the message input field	        
			final TextArea input = new TextArea();
			input.setDisable(true);
			input.setPrefRowCount(265);
			input.setPrefColumnCount(460);
			input.setWrapText(true);
			input.setPrefSize(460, 265);
			input.setId("textarea-init");
			input.setPromptText("Please enter your message here");
			
			input.setMaxWidth(460);
			input.setMaxHeight(265);
		
			input.textProperty().addListener(new ChangeListener<String>() {
			    @Override
			    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			    	input.setPrefWidth(input.getText().length() * 7); 
			    }
			});

	        GridPane.setHalignment(input, HPos.CENTER);
	        grid.add(input, 1, 2);
	        
	        //Set up send/clear buttons
	        final Button send = new Button("Send");
	        final Button clear = new Button("Clear");
	        final Button search = new Button("search");
	        send.setDisable(true);
	        send.setId("chatbtn");
	        send.setText("Send");
	        send.setPrefSize(120, 40);
	        send.setOnAction(new EventHandler<ActionEvent>() {
	        	@Override
	            public void handle(ActionEvent e) {
	        		Message inputMsg = new Message(userId2);	        
	        		inputMsg.Messge = input.getText();
	        		Text Msg = new Text(inputMsg.LocalTime + inputMsg.User_id + "$#%" + inputMsg.Messge + "*&^@" + " -fx-font-size: " + size2.getValue() + 
	        				            ";" + color2.getValue());
	        		messageList.add(Msg.getText());
	        		sendMessage(Msg.getText());
	        		
	        		String sql="INSERT INTO records ( user, message, time)" +"VALUES('"+inputMsg.User_id+"','"+inputMsg.Messge+"','"+inputMsg.LocalTime+"')";
	        		insertRecords(sql);
	        		
	        		input.clear();
	        		input.requestFocus();
	            }
	        });
	        clear.setOnAction(new EventHandler<ActionEvent>() {
	        	@Override
	            public void handle(ActionEvent e) {
	                input.clear();
	                input.requestFocus();
	            }
	        });
	        clear.setDisable(true);
	        clear.setId("chatbtn");
	        clear.setPrefSize(120, 40);
	        
	        
	        search.setOnAction(new EventHandler<ActionEvent>(){

				@Override
				public void handle(ActionEvent event) {
					// TODO Auto-generated method stub
					
					Stage secondarystage= new Stage();
					Group root = new Group();
			       
			        Scene scene = new Scene(root,500, 500, Color.WHITE);
			        GridPane grid = new GridPane();
					
					grid.setId("grid-init2");
					grid.setPrefSize(500, 500);
					grid.setAlignment(Pos.CENTER);
					grid.setHgap(10);
					grid.setVgap(10);
					grid.setPadding(new Insets(15, 15, 15, 15));
					
			        final Label searchlb = new Label("Please enter: ");
			        final TextField searchText = new TextField();
					final Button search = new Button("Search");
					final TextFlow sresult = new TextFlow();
					ScrollPane textContainer = new ScrollPane();
					textContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); //Disable the horizontal scroll bar
					
					sresult.setId("sresult");
					sresult.setPrefSize(350, 350);
					sresult.getChildren().addListener(
			                (ListChangeListener<Node>) ((change) -> {
			                	sresult.layout();
			                    textContainer.layout();
			                    textContainer.setVvalue(1.0f);
			                }));//Enable the vertical scroll bar
					textContainer.setContent(sresult);
					GridPane.setHalignment(textContainer, HPos.CENTER);
					grid.add(textContainer, 1, 0);
					
					search.setDisable(false);
			        search.setId("searchbtn");
			        search.setPrefSize(120, 40);
			      
			        HBox searchhb = new HBox(10);
			        searchhb.setAlignment(Pos.TOP_LEFT);
			        searchhb.getChildren().addAll(searchlb, searchText, search);
			        grid.add(searchhb, 1, 3);
			        
			        search.setOnAction(new EventHandler<ActionEvent>(){

						@Override
						public void handle(ActionEvent event) {
							// TODO Auto-generated method stub
							sresult.getChildren().clear();

							
							String searchword = searchText.getText();
							String sql="SELECT count(*) as rowNum FROM records where message LIKE '%"+searchword+"%'";

							
							ResultSet r = getRecords(sql);
							

							try {
								r.next();
								int count=r.getInt("rowNum");
								if(count==0){
									Text ta=new Text("Data not found!");
									sresult.getChildren().addAll(ta);
								}
								else{
								sql="SELECT * FROM records where message LIKE '%"+searchword+"%'";
								ResultSet rs = getRecords(sql);
								while(rs.next()){

								        //Retrieve by column name
					
								        String user = rs.getString("user");

								        String message = rs.getString("message");

								        String time = rs.getString("time");
								        
								        Text t=new Text(time + "\n"+user+ ": "+ message+ "\n\n" );
								        
								        sresult.getChildren().addAll(t);
								        
								      }
								}
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							searchText.clear();
						}
			        	
			        });
			        
			        VBox hbBtn = new VBox(10);
			        hbBtn.setAlignment(Pos.TOP_RIGHT);
			        hbBtn.getChildren().addAll(search);
			        grid.add(hbBtn, 2, 1);
			        
					root.getChildren().add(grid);
			        
			        secondarystage.setTitle("search");
					secondarystage.setScene(scene);
					secondarystage.sizeToScene();
					secondarystage.setResizable(false);
					secondarystage.show();
					
					
				}
	        
	        	
	        });
	        
	        search.setDisable(true);
	        search.setId("chatbtn");
	        search.setPrefSize(120, 40);
	        VBox hbBtn = new VBox(10);
	        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
	        hbBtn.getChildren().addAll(send, clear, search);
	        grid.add(hbBtn, 3, 3);
	        
	        //Add user name input field
	        final Label userlb = new Label("Enter User Name: ");
	        final TextField userText = new TextField();
	        final Button apply = new Button("Apply");
	        final Button reenter = new Button("Re-enter");
	        apply.setId("userbtn");
	        apply.setPrefSize(90, 20);
	        reenter.setId("userbtn");
	        reenter.setPrefSize(90, 20);
	        reenter.setDisable(true);
	        
	        HBox userhb = new HBox(10);
	        userhb.setAlignment(Pos.BOTTOM_LEFT);
	        userhb.getChildren().addAll(userlb, userText, apply, reenter);
	        grid.add(userhb, 1, 3);
	        //apply action
	        apply.setOnAction(new EventHandler<ActionEvent>() {
	        	@Override
	            public void handle(ActionEvent e) { 
	        		if(!userText.getText().isEmpty())
	        		{
	        			userId2 = userText.getText();
		        		userText.clear();
		        		userText.setDisable(true);
		        		apply.setDisable(true);
		        		reenter.setDisable(false);
		        		send.setDisable(false);
		        		clear.setDisable(false);
		        		input.setDisable(false);
		        		search.setDisable(false);
		        		input.requestFocus();
	        		}
	        		else{
	        			userText.requestFocus();
	        		}
	            }
	        });
	        //re-enter action
	        reenter.setOnAction(new EventHandler<ActionEvent>() {
	        	@Override
	            public void handle(ActionEvent e) {
	        		userText.clear();
	        		userText.setDisable(false);
	        		input.setDisable(true);
	        		apply.setDisable(false);
	        		send.setDisable(true);
	        		clear.setDisable(true);
	        		userText.requestFocus();
	            }
	        });
	        
	        color2.setOnAction(new EventHandler<ActionEvent>() {
	        	@Override
	            public void handle(ActionEvent e) {               
	        		input.setId("textarea");
	        		input.setStyle("-fx-text-fill: " + color2.getValue() + "; -fx-font-size: " + size2.getValue());
	            }
	        });
	        size2.setOnAction(new EventHandler<ActionEvent>() {
	        	@Override
	            public void handle(ActionEvent e) {               
	        		input.setStyle("-fx-text-fill: " + color2.getValue() + "; -fx-font-size: " + size2.getValue());
	        		
	        	}
	        });
	        theme2.setOnAction(new EventHandler<ActionEvent>() {
	        	@Override
	            public void handle(ActionEvent e) {               
	        		grid.setStyle("-fx-background-color: " + theme2.getValue());
	        	}
	        });
	        //Listen to the change of the list 
	        messageList.addListener(new ListChangeListener<String>() {
	          @Override
	          public void onChanged(ListChangeListener.Change<? extends String> c) {
	        	  while(c.next()){
	        		//Logic to separate message/user/style
	        		  String lastMsg = messageList.get(messageList.size() - 1);
	        		  int namePosi = 1;
	        		  int stylePosi = 0;
	        		  boolean styleFlag = lastMsg.contains("*&^@");
	        		  boolean nameFlag = lastMsg.contains("$#%");
	        		  if(nameFlag)
	        		  {
	        			  namePosi = lastMsg.indexOf("$#%");
	        		  }
	        		  if(styleFlag)
	        		  {
	        			  stylePosi = lastMsg.indexOf("*&^@");  
	        		  }
	        		  Text time2 =  new Text(lastMsg.substring(0, 8) + "\n");
	        		  Text m2 = new Text(lastMsg.substring(8, namePosi) + ": " + 
	        				  			lastMsg.substring(namePosi + 3, stylePosi) + "\n");
	        		  String color = lastMsg.substring(stylePosi + 4).split(";")[1];
	        		  m2.setStyle(lastMsg.substring(stylePosi + 4).split(";")[0]);
		        	  m2.setFill(Color.valueOf(color));
	        		  history.getChildren().addAll(time2, m2);	
	        	  }
	          }
	        });
	        //Set up the image view by choosing a photo
	        ImageView imageview = new ImageView();
	        Image initImage = new Image("file:src/default.jpg");
	        
	        imageview.prefHeight(150);
	        imageview.prefWidth(150);
	        
	        imageview.setImage(initImage);
	        final FileChooser fileChooser = new FileChooser();
	        final Button openBtn = new Button("Pick a photo");
	        openBtn.setId("openbtn");
	        openBtn.setPrefSize(120, 30);
	        
	        openBtn.setOnAction(new EventHandler<ActionEvent>() {
	                @Override
	                public void handle(ActionEvent e) {
	                    configureFileChooser(fileChooser);
	                    File file = fileChooser.showOpenDialog(primaryStage);
	                    if (file != null) {
	                    	String filePath = file.toURI().toString();
	                    	Image image =  new Image(filePath);
	                    	//Image size constraint
	                    	if(image.getHeight() <= 128 && image.getWidth() <= 128)
	                    	{
	                    		imageview.setImage(image);
	                    		if(send.isDisable())
	                    		{
	                    			userText.requestFocus();
	                    		}
	                    		else{
	                    			input.requestFocus();
	                    		}
	                    	}
	                    	else{
	                    		Alert alert = new Alert(AlertType.ERROR);
	                    		alert.setTitle("Error Dialog");
	                    		alert.setHeaderText(null);
	                    		alert.setContentText("Photo can not exceed 128*128!");

	                    		alert.showAndWait();
	                    		if(send.isDisable())
	                    		{
	                    			userText.requestFocus();
	                    		}
	                    		else{
	                    			input.requestFocus();
	                    		}
	                    	}
	            	        
	                    }
	                }
	            });
	        
	        VBox vbox = new VBox(10);
	        vbox.setAlignment(Pos.TOP_CENTER); 
	        vbox.getChildren().addAll(imageview, openBtn);
	        grid.add(vbox, 3, 0);
			
			//Add gridPane to root
	        root.getChildren().add(grid);
	        
	        primaryStage.setTitle("Server");
			primaryStage.setScene(scene);
			userText.requestFocus();
			primaryStage.sizeToScene();
			primaryStage.setResizable(false);
			primaryStage.show();

	    }
		
	    @Override
		public void start(Stage primaryStage) 
		{
			try
			{
				InitServerStage(primaryStage);
				
			} 
			catch(Exception e)
			{	
				e.printStackTrace();
			}
		}
		
		
		//Configure the file chooser
		protected void configureFileChooser(final FileChooser fileChooser) {
			fileChooser.setTitle("Pick A Picture");
	        fileChooser.setInitialDirectory(
	            new File(System.getProperty("user.home"))
	        ); 
	        fileChooser.getExtensionFilters().clear();
	        fileChooser.getExtensionFilters().addAll(
	             
	                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
	                new FileChooser.ExtensionFilter("PNG", "*.png"),
	                new FileChooser.ExtensionFilter("GIF", "*.gif")
	            );
		}

		public static void createTable() throws Exception{

			try{


			Connection conn = getConnection();

			Statement stmt = conn.createStatement();


			String sql = "CREATE TABLE IF NOT EXISTS `ChatHistory`.`records`" + 
					  "(`id` INT NOT NULL AUTO_INCREMENT,"+
					  "`user` VARCHAR(45) NULL,"+
					  "`message` VARCHAR(45) NULL,"+
					 "`time` VARCHAR(45) NULL,"+
					 " PRIMARY KEY (`id`))";



			      stmt.executeUpdate(sql);

			}catch(Exception e)

			{

			System.out.println(e);

			}finally{

			System.out.println("Table is created successfully!");

			}

			}
		
		public static void insertRecords(String command){

			try{

			Connection conn = getConnection();

			Statement stmt = conn.createStatement();

			stmt.executeUpdate(command);

			}catch(Exception e){

			System.out.println(e);

			}finally{

			System.out.println("Records are inserted successfully!");

			}

			}
		
		public static Connection getConnection()

		{

		try{

		String driver = "com.mysql.jdbc.Driver";

		String url = "jdbc:mysql://localhost:3306/ChatHistory";

		String username = "root";

		String password = "060304BLUE^ya";//Use your MySql password

		Class.forName(driver);


		Connection conn = DriverManager.getConnection(url, username, password);

		System.out.println("Connection is established.");

		return conn;

		}

		catch(Exception e)

		{

		System.out.println(e);

		}


		return null;


		}
		public static ResultSet getRecords(String command){

			try{

			Connection conn = getConnection();

			Statement stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(command);


			return rs;

			}catch(Exception e){

			System.out.println(e);

			}finally{

			}


			return null;

			}




		public static void main(String[] args) throws IOException, SQLException {
			
			Server serv = new Server();
			serv.waitRequests();
			serv.setStreams();
			MessageContainer msgContainer2 = new MessageContainer();

			Thread tt = new Thread(msgContainer2);
			tt.start();
			try {
				createTable();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			launch(args);
		}
	}
	//Color factory
	 class ColorRectCell extends ListCell<String> {

	    @Override
	    public void updateItem(String item, boolean empty) {
	        super.updateItem(item, empty);
	        Rectangle rect = new Rectangle(100, 20);
	        if (item != null) {
	            rect.setFill(Color.web(item));
	            setGraphic(rect);
	        }
	    }

	}

	//Message class 
	class Message extends Text{
		
		String Messge;
		String User_id;
		String LocalTime;
		
		public Message(String userId){
			LocalTime = GetDateTime();
			User_id = userId;
		}
		
		String GetDateTime()
		{
			Date LocalTime = new Date();
			DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
			
			return dateFormat.format(LocalTime);
		}
		
	}


