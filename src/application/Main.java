package application;

import javafx.application.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.*;
import javafx.scene.layout.*;

import java.io.*;
import java.net.*;
import java.util.Optional;
import java.util.Random;


public class Main extends Application {

	public GridPane playboard = null;
	Player user = null;
	ReversiBoard rboard = null;
	int pColor = 0; //player color
	Computer comp = null;
	int oColor = 0; //opponent color
	public int tColor = 0; //color of entity taking this turn

	Socket socket = null;
	DataInputStream in = null;
	DataOutputStream out = null;

	@Override
	public void start(Stage primaryStage) {
		try {
			//creates region in the main page
			VBox mainroot = setMainScreen(primaryStage);

			//create scene with root
			Scene mainscene = new Scene(mainroot,900,600);
			mainscene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			//puts scene on stage
			primaryStage.setScene(mainscene);
			primaryStage.setTitle("Reversi Launcher 3000");

			//display
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	//creates the Region for the main screen
	public VBox setMainScreen(Stage primaryStage) {

		//creates the actual vbox
		VBox main = new VBox();
		main.setSpacing(30);
		main.setId("mainvbox");

		//adds title
		Text title = new Text("REVERSI");
		title.setId("titletext");
		main.getChildren().add(title);

		//adds buttons
		Button singlePlayerButton = new Button("Singleplayer");
		singlePlayerButton.getStyleClass().add("mainbutton");
		Button multiPlayerButton = new Button("Multiplayer");
		multiPlayerButton.getStyleClass().add("mainbutton");
		Button quitButton = new Button("Quit Game");
		quitButton.getStyleClass().add("mainbutton");
		main.getChildren().add(singlePlayerButton);
		main.getChildren().add(multiPlayerButton);
		main.getChildren().add(quitButton);


		//when single player is clicked
		EventHandler<ActionEvent> SPClicked = new EventHandler<ActionEvent>() { 
			public void handle(ActionEvent e) 
			{ 

				rboard = new ReversiBoard();

				TextInputDialog dialog = new TextInputDialog("");
				dialog.setTitle("Name");
				dialog.setHeaderText("Name input");
				dialog.setContentText("Please enter your name:");
				Optional<String> result = dialog.showAndWait();
				String name = "";
				if (result.isPresent()){
					name = result.get();
					if (name.trim().length() == 0)
						return;
				}
				else {
					return;
				}

				user = new Player(name, rboard);
				pColor = user.getColor();
				comp = new Computer(rboard, pColor * -1);
				oColor = pColor * -1;

				BorderPane gameRegion = createGameRegion();
				Scene gameScene = new Scene(gameRegion, 900, 600);
				gameScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
				primaryStage.setScene(gameScene);
			} 
		}; 

		//when multi player is clicked
		EventHandler<ActionEvent> MPClicked = new EventHandler<ActionEvent>() { 
			public void handle(ActionEvent e) 
			{ 

				rboard = new ReversiBoard();

				Random rand = new Random();
				TextInputDialog dialog = new TextInputDialog("Player" + rand.nextInt(100));
				dialog.setTitle("Name");
				dialog.setHeaderText("Name input");
				dialog.setContentText("Please enter your name:");
				Optional<String> result = dialog.showAndWait();
				String name = "";
				if (result.isPresent()) {
					name = result.get();
					if (name.trim().length() == 0)
						return;
				}
				else {
					return;
				}
				user = new Player(name, rboard);

				pColor = user.getColor();

				Platform.runLater(new Runnable()
				{
					@Override
					public void run() {
						try {
							boolean connected = connect();
							if (connected) {
								BorderPane gameRegion = createMultiplayerGameRegion();
								Scene gameScene = new Scene(gameRegion, 900, 600);
								gameScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
								primaryStage.setScene(gameScene);
							}
						} catch (ClassNotFoundException | IOException e) {
							e.printStackTrace();
						}
					}
				});

			} 
		};

		//when quit is clicked
		EventHandler<ActionEvent> QuitClicked = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e)
			{
				Platform.exit();
			}
		};

		singlePlayerButton.setOnAction(SPClicked);
		multiPlayerButton.setOnAction(MPClicked);
		quitButton.setOnAction(QuitClicked);

		return main;

	}

	public boolean connect() throws IOException, ClassNotFoundException {

		InetAddress ip = InetAddress.getByName("localhost");
		socket = new Socket(ip, 5331);

		out = new DataOutputStream(socket.getOutputStream());
		in = new DataInputStream(socket.getInputStream());

		out.writeUTF(user.getName());
		out.flush();

		//Keep asking for name if name is a duplicate
		TextInputDialog namedialog = new TextInputDialog();
		namedialog.setTitle("Name");
		namedialog.setHeaderText("That name is already taken!");
		namedialog.setContentText("Enter name: ");
		String reply = in.readUTF();
		Optional<String> ans = null;
		while (reply.equals("taken")) {
			ans = namedialog.showAndWait();
			if (!ans.isPresent() || ans.get().trim().length() == 0) {
				out.writeUTF("EXIT");
				in.close();
				out.close();
				socket.close();
				return false;
			}

			out.writeUTF(ans.get());
			reply = in.readUTF();
			if (!reply.equals("taken"))
				user.setName(ans.get());
		}

		//opponent's name
		TextInputDialog dialog = new TextInputDialog();
		dialog.setContentText("Enter opponent's name:");
		reply = "n";
		while (reply.equals("n")) {

			ans = null;
			ans = dialog.showAndWait();
			if (!ans.isPresent()) {
				out.writeUTF("EXIT");
				in.close();
				out.close();
				socket.close();
				return false;
			}

			String oname = ans.get();

			out.writeUTF(oname);
			reply = in.readUTF();

			if (reply.equals("n")) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText("Opponent not found!");
				alert.setContentText("Check for misspellings.");
				alert.showAndWait();
			}

			out.flush();

		}

		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Ready");
		alert.setHeaderText("Successfully connected to opponent!");
		alert.setContentText("Click Enter to proceed.");
		alert.showAndWait();

		out.flush();

		return true;

	}
	
	//creates the borderpane for the multiplayer game
		public BorderPane createMultiplayerGameRegion() {

			//TODO: Determine whoever goes first, disable all buttons for the second guy, then maybe make an opponent class?
			
			BorderPane region = new BorderPane();
			region.setId("playregion");

			//create top text
			Text heading = new Text("REVERSI");
			heading.setId("playregionhead");

			//create the board region
			playboard = new GridPane();
			playboard.setId("playboard");

			//sets sizes of row and column
			for (int i = 0; i < 8; i++) {
				int size = 50;
				RowConstraints rc = new RowConstraints(size);
				playboard.getRowConstraints().add(rc);
				ColumnConstraints cc = new ColumnConstraints(size);
				playboard.getColumnConstraints().add(cc);
			}

			//adds each button
			for (int x = 0; x < 8; x++) {
				for (int y = 0; y < 8; y++) {
					GridButton button = new GridButton(x, y);
					button.getStyleClass().add("gridsquare");
					button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

					EventHandler<ActionEvent> boardClicked = new EventHandler<ActionEvent>() {
						public void handle(ActionEvent e)
						{

							int[][] oldboard = rboard.getBoard();

							int[] selCoords = button.getcoord();
							user.makeMove(user.getColor(), selCoords[0], selCoords[1]);

							tColor *= -1;
							updateBoard(oldboard);

							oldboard = rboard.getBoard();
							comp.makeMove();
							tColor *= -1;
							updateBoard(oldboard);
						}
					};

					button.setOnAction(boardClicked);
					playboard.add(button, x, y);
				}
			}

			int[][] oldboard = rboard.getBoard();
			updateBoard(oldboard);
			if (user.getColor() == 1) {
				comp.makeMove();
				tColor *= -1;
				updateBoard(oldboard);
			}

			region.setTop(heading);
			region.setCenter(playboard);
			return region;
		}

	//creates the borderpane for the actual game
	public BorderPane createGameRegion() {

		tColor = -1;

		BorderPane region = new BorderPane();
		region.setId("playregion");

		//create top text
		Text heading = new Text("REVERSI");
		heading.setId("playregionhead");

		//create the board region
		playboard = new GridPane();
		playboard.setId("playboard");

		//sets sizes of row and column
		for (int i = 0; i < 8; i++) {
			int size = 50;
			RowConstraints rc = new RowConstraints(size);
			playboard.getRowConstraints().add(rc);
			ColumnConstraints cc = new ColumnConstraints(size);
			playboard.getColumnConstraints().add(cc);
		}

		//adds each button
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				GridButton button = new GridButton(x, y);
				button.getStyleClass().add("gridsquare");
				//Image img = new Image(getClass().getResourceAsStream("emptysquare.png"));
				//button.setGraphic(new ImageView(img));
				button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

				EventHandler<ActionEvent> boardClicked = new EventHandler<ActionEvent>() {
					public void handle(ActionEvent e)
					{

						int[][] oldboard = rboard.getBoard();

						int[] selCoords = button.getcoord();
						user.makeMove(user.getColor(), selCoords[0], selCoords[1]);

						tColor *= -1;
						updateBoard(oldboard);

						oldboard = rboard.getBoard();
						comp.makeMove();
						tColor *= -1;
						updateBoard(oldboard);
					}
				};

				button.setOnAction(boardClicked);
				playboard.add(button, x, y);
			}
		}

		int[][] oldboard = rboard.getBoard();
		updateBoard(oldboard);
		if (user.getColor() == 1) {
			comp.makeMove();
			tColor *= -1;
			updateBoard(oldboard);
		}

		region.setTop(heading);
		region.setCenter(playboard);
		return region;
	}

	//updates which buttons are disabled through legalboard, and which colors are which through the actual board
	public void updateBoard(int[][] oldboard) {

		rboard.updateLegal(tColor);
		int[][] newboard = rboard.getBoard();
		int[][][] legalboard = rboard.getLegal();

		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				GridButton button = (GridButton)playboard.getChildren().get(8 * x + y);
				if (rboard.getSpace(x, y) == 1) {
					button.getStyleClass().clear();
					button.setStyle("null");
					if (newboard[x][y] != oldboard[x][y]) {
						if (pColor == 1)
							button.getStyleClass().add("wsrecentself");
						else
							button.getStyleClass().add("wsrecentoppo");
					}
					else {
						button.getStyleClass().add("whitesquare");
					}
				}
				else if (rboard.getSpace(x, y) == -1) {
					button.getStyleClass().clear();
					button.setStyle("null");
					if (newboard[x][y] != oldboard[x][y]) {
						if (pColor == -1)
							button.getStyleClass().add("bsrecentself");
						else
							button.getStyleClass().add("bsrecentoppo");
					}
					else {
						button.getStyleClass().add("blacksquare");
					}
				}

				if (legalboard[x][y][0] == 0)
					button.setDisable(true);
				else
					button.setDisable(false);
			}
		}

	}

	public void init() {
		System.out.println("Initiating Reversi Launcher 3000...");
	}

	public void stop() throws IOException {
		if (socket != null) {
			out.writeUTF("EXIT");
			in.close();
			out.close();
			socket.close();
		}
		System.out.println("Shutting down.");
	}

	public static void main(String[] args) {
		launch(args);
	}
}