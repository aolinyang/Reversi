package application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Optional;
import java.util.Random;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

	private int windowWidth = 1350;
	private int windowHeight = 900;

	private GridPane playboard = null;
	private Player user = null;
	private Player opponent = null;
	private String pName = "";
	private String oName = "";
	private ReversiBoard rboard = null;
	private int pColor = 0; //player color
	private Computer comp = null;
	private int oColor = 0; //opponent color
	private int tColor = 0; //color of entity taking this turn

	//special modifications
	private int numBlocked = 15;
	private int length = 4;
	private int height = 10;
	private int lowerlength = 6;
	private int higherlength = 12;
	private int lowerheight = 6;
	private int higherheight = 12;

	private Socket socket = null;
	private DataInputStream in = null;
	private DataOutputStream out = null;

	private Scene mainscene = null;
	private Text title = null;
	private Button singlePlayerButton = null;
	private Button multiPlayerButton = null;
	private Button quitButton = null;
	private Stage pStage = null;
	private Text winAnnounce = new Text();
	private Button rematchButton = new Button();
	private Button connectButton = new Button();
	private VBox sideVBox = new VBox();

	private boolean listening; //for the master listener thread

	@Override
	public void start(Stage primaryStage) {
		try {
			//creates region in the main page
			VBox mainroot = setMainScreen(primaryStage);

			//create scene with root
			mainscene = new Scene(mainroot,windowWidth,windowHeight);
			mainscene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			//puts scene on stage
			primaryStage.setScene(mainscene);
			primaryStage.setTitle("Reversi Launcher 3000");

			pStage = primaryStage;

			//display
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void animateMain() {

		Node[] mainNodes = {title, singlePlayerButton, multiPlayerButton, quitButton};

		for (Node n : mainNodes) {
			FadeTransition fade = new FadeTransition(Duration.millis(2500), n);
			fade.setFromValue(0);
			fade.setToValue(1);
			fade.play();
		}

	}

	//creates the Region for the main screen
	public VBox setMainScreen(Stage primaryStage) {

		//creates the actual vbox
		VBox main = new VBox();
		main.setSpacing(30);
		main.setId("mainvbox");

		//adds title
		title = new Text("REVERSI");
		title.setId("titletext");
		main.getChildren().add(title);

		//adds buttons
		//There are 2 mainbutton CSS classes - this deals with a bug where the top button is
		//a little wider than the others
		singlePlayerButton = new Button("Play Computer");
		singlePlayerButton.getStyleClass().add("mainbutton2");
		multiPlayerButton = new Button("Play Online");
		multiPlayerButton.getStyleClass().add("mainbutton");
		quitButton = new Button("Quit Game");
		quitButton.getStyleClass().add("mainbutton");
		main.getChildren().add(singlePlayerButton);
		main.getChildren().add(multiPlayerButton);
		main.getChildren().add(quitButton);

		animateMain();

		//when single player is clicked
		EventHandler<ActionEvent> SPClicked = new EventHandler<ActionEvent>() { 
			public void handle(ActionEvent e) 
			{ 
				rboard = new ReversiBoard(length, height, numBlocked);

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

				Random rand = new Random();
				pColor = rand.nextInt(2) * 2 - 1;
				user = new Player(name, rboard, pColor);
				comp = new Computer(rboard, pColor * -1);
				oColor = pColor * -1;

				BorderPane gameRegion = createGameRegion();
				Scene gameScene = new Scene(gameRegion, windowWidth, windowHeight);
				gameScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
				pStage.setScene(gameScene);
			} 
		}; 

		//when multi player is clicked
		EventHandler<ActionEvent> MPClicked = new EventHandler<ActionEvent>() { 
			public void handle(ActionEvent e) 
			{ 

				rboard = new ReversiBoard(length, height, numBlocked);

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
				pName = name;

				String ip;
				TextInputDialog ipdialog = new TextInputDialog();
				ipdialog.setTitle("Connection");
				ipdialog.setHeaderText("Connect to Server");
				ipdialog.setContentText("Enter the server's ip address.");
				Optional<String> result2 = ipdialog.showAndWait();
				if (result2.isPresent()) {
					ip = result2.get();
					if (ip.trim().length() == 0)
						return;
				}
				else {
					return;
				}

				Platform.runLater(new Runnable()
				{
					@Override
					public void run() {
						try {
							boolean connected = connect(ip);
							if (connected) {
								BorderPane gameRegion = createMultiplayerGameRegion(false);
								Scene gameScene = new Scene(gameRegion, windowWidth, windowHeight);
								gameScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
								pStage.setScene(gameScene);
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

	//attempts to connect to the server with ip
	public boolean connect(String ip) throws IOException, ClassNotFoundException {

		socket = new Socket(ip, 5337);

		out = new DataOutputStream(socket.getOutputStream());
		in = new DataInputStream(socket.getInputStream());

		out.writeUTF("USERNAME#" + pName);
		out.flush();

		return true;

	}

	//creates the borderpane for the multiplayer game
	public BorderPane createMultiplayerGameRegion(boolean playedBefore) {

		tColor = -1;

		BorderPane gameRegion = new BorderPane();
		gameRegion.setId("playregion");

		HBox topHBox = new HBox();
		sideVBox = new VBox();

		//create top text
		Text heading = new Text("REVERSI");
		heading.setId("playregionhead");

		//create connect text
		Text connectText = new Text();

		//create connect and cancel buttons
		connectButton = new Button("Find opponent");
		Button cancelButton = new Button("Cancel");

		//create the board region
		playboard = new GridPane();
		playboard.setId("playboard");

		//MASTER LISTENER THREAD
		//listens to all possible messages
		class Listener extends Thread {

			public void run() {
				try {
					while (listening) {
						String[] received = in.readUTF().split("#");
						String keyword = received[0];
						if (keyword.equals("MOVE")) {
							int oppoX = Integer.parseInt(received[1]);
							int oppoY = Integer.parseInt(received[2]);
							int[][] oldboard = rboard.getBoard();
							rboard.updateLegal(oColor);
							opponent.makeMove(oppoX, oppoY);
							tColor = pColor;
							boolean canUserPlay = updateBoard(oldboard);
							if (!canUserPlay) {
								out.writeUTF("PASS");
								tColor = oColor;
							}

							Platform.runLater(new Runnable() {
								public void run() {
									endGameMP();
								}
							});

						}
						else if (keyword.equals("PASS")) { //opponent decided to pass
							tColor = pColor;
							int[][] oldboard = rboard.getBoard();
							boolean canUserPlay = updateBoard(oldboard);
							if (!canUserPlay) {
								out.writeUTF("GAMEEND");
								Platform.runLater(new Runnable() {
									public void run() {
										endGameMP();
									}
								});
							}
						}
						else if (keyword.equals("FOUNDOPPONENT")) {
							tColor = -1;

							Platform.runLater(new Runnable() {
								public void run() {
									sideVBox.getChildren().remove(cancelButton);
								}
							});

							//everything here is deciding who goes first
							oName = received[1];
							Random rand = new Random();

							//whoever has the larger number goes first
							double num = rand.nextDouble();
							out.writeUTF("CHALLENGERNUM#" + num);
							double opponum = Double.parseDouble(in.readUTF());
							if (num > opponum) {
								pColor = -1;
								oColor = 1;
								connectText.setText("Opponent found! You are black.");
								int[][] oldboard = rboard.getBoard();
								updateBoard(oldboard);
							}
							else {
								pColor = 1;
								oColor = -1;
								connectText.setText("Opponent found! You are white.");
								int[][] oldboard = rboard.getBoard();
								updateBoard(oldboard);
								disableAllButtons();
							}
							user = new Player(pName, rboard, pColor);
							opponent = new Player(oName, rboard, oColor);
						}

						else if (keyword.equals("OPPOEXIT")) {
							out.writeUTF("OPPOEXIT");
							connectText.setText("Opponent disconnected.");
							Platform.runLater(new Runnable() {
								public void run() {
									Alert stayornot = new Alert(AlertType.CONFIRMATION);
									stayornot.setTitle("Opponent disconnect");
									stayornot.setHeaderText("Opponent disconnected from game!");
									stayornot.setContentText("Stay in server or disconnect?");
									ButtonType stay = new ButtonType("Stay");
									ButtonType exit = new ButtonType("Exit");
									stayornot.getButtonTypes().setAll(stay, exit);
									Optional<ButtonType> choice = stayornot.showAndWait();
									if (choice.get() == stay) {
										connectButton.setDisable(false);
										rboard = new ReversiBoard(length, height, numBlocked);
										disableAllButtons();
									}
									else {
										listening = false;
										try {
											in.close();
											out.close();
											socket.close();
											out.writeUTF("EXIT");
										} catch (IOException e) {
											e.printStackTrace();
										}
										pStage.setScene(mainscene);
										animateMain();
									}
								}
							});
						}
						else if (keyword.equals("GAMEEND")) {
							Platform.runLater(new Runnable() {
								public void run() {
									endGameMP();
								}
							});
						}
						else if (keyword.equals("REMATCHREQUEST")) {
							Platform.runLater(new Runnable() {
								public void run() {
									try {
										Alert rematchask = new Alert(AlertType.CONFIRMATION);
										rematchask.setTitle("Rematch");
										rematchask.setHeaderText("Opponent is requesting a rematch.");
										rematchask.setContentText("");
										ButtonType accept = new ButtonType("Accept");
										ButtonType decline = new ButtonType("Decline");
										rematchask.getButtonTypes().setAll(accept, decline);
										Optional<ButtonType> ans = rematchask.showAndWait();
										if (ans.get() == accept) {
											Random rand = new Random();
											pColor = rand.nextInt(2) * 2 - 1;
											oColor = pColor * -1;
											out.writeUTF("REMATCHACCEPT#" + oColor);
											rboard = new ReversiBoard(length, height, numBlocked);
											user = new Player(pName, rboard, pColor);
											opponent = new Player(oName, rboard, oColor);
											BorderPane newregion = createMultiplayerGameRegion(true);
											Scene newscene = new Scene(newregion, windowWidth, windowHeight);
											newscene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
											pStage.setScene(newscene);
											int[][] oldboard = rboard.getBoard();
											updateBoard(oldboard);
											if (pColor == 1)
												disableAllButtons();
										}
										else {
											out.writeUTF("REMATCHDECLINE");
											connectButton.setDisable(false);
											rematchButton.setDisable(true);
											Alert msg = new Alert(AlertType.CONFIRMATION);
											msg.setTitle("Decline");
											msg.setHeaderText("You declined the rematch.");
											msg.showAndWait();
											rboard = new ReversiBoard(length, height, numBlocked);
										}
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							});
						}
						else if (keyword.equals("REMATCHACCEPT")) {
							pColor = Integer.parseInt(received[1]);
							oColor = pColor * -1;
							rboard = new ReversiBoard(length, height, numBlocked);
							user = new Player(pName, rboard, pColor);
							opponent = new Player(oName, rboard, oColor);
							Platform.runLater(new Runnable() {
								public void run() {
									BorderPane newregion = createMultiplayerGameRegion(true);
									Scene newscene = new Scene(newregion, windowWidth, windowHeight);
									newscene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
									pStage.setScene(newscene);
									int[][] oldboard = rboard.getBoard();
									updateBoard(oldboard);
									if (pColor == 1)
										disableAllButtons();
								}
							});
						}
						else if (keyword.equals("REMATCHDECLINE")) {
							Platform.runLater(new Runnable() {
								public void run() {
									Alert alert = new Alert(AlertType.CONFIRMATION);
									alert.setTitle("Declined");
									alert.setHeaderText("Opponent declined rematch!");
									alert.showAndWait();
									connectButton.setDisable(false);
									rematchButton.setDisable(true);
									rboard = new ReversiBoard(length, height, numBlocked);
									try {
										out.writeUTF("REMOVEOPPONENT");
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							});
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		listening = true;

		if (!playedBefore) {
			Listener listener = new Listener();
			listener.start();
		}

		//set action to cancel button
		EventHandler<ActionEvent> cancelClicked = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				try {
					out.writeUTF("CANCELFINDOPPONENT");
					sideVBox.getChildren().remove(cancelButton);
					connectButton.setDisable(false);
					connectText.setText("");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		};
		cancelButton.setOnAction(cancelClicked);

		//set action to connect button
		EventHandler<ActionEvent> connectClicked = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				try {
					connectText.setText("Looking for opponent...");
					out.writeUTF("FINDOPPONENT");
					sideVBox.getChildren().add(cancelButton);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				connectButton.setDisable(true);
			}
		};
		connectButton.setOnAction(connectClicked);
		if (playedBefore) {
			connectButton.setDisable(true);
		}

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
						user.makeMove(selCoords[0], selCoords[1]);
						try {
							out.writeUTF("MOVE#" + selCoords[0] + "#" + selCoords[1]);
						} catch (IOException e1) {
							e1.printStackTrace();
						}

						tColor = oColor;
						updateBoard(oldboard);
						disableAllButtons();
					}
				};

				button.setOnAction(boardClicked);
				button.setDisable(true);
				playboard.add(button, x, y);
			}
		}

		//create back button
		Button backButton = new Button("Exit");
		EventHandler<ActionEvent> exitClicked = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				try {
					Alert confirmation = new Alert(AlertType.CONFIRMATION);
					confirmation.setContentText("Are you sure you want to exit the game?");
					ButtonType yes = new ButtonType("Yes");
					ButtonType no = new ButtonType("No");
					confirmation.getButtonTypes().setAll(yes, no);
					Optional<ButtonType> result = confirmation.showAndWait();
					if (result.get() == yes) {
						out.writeUTF("EXIT");
						listening = false;
						in.close();
						out.close();
						socket.close();
						pStage.setScene(mainscene);
						animateMain();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		backButton.setOnAction(exitClicked);

		//create top hbox
		topHBox.getChildren().add(backButton);
		topHBox.getChildren().add(heading);
		topHBox.getChildren().add(winAnnounce);

		//create right vbox
		sideVBox.getChildren().add(connectButton);
		sideVBox.getChildren().add(connectText);

		gameRegion.setTop(topHBox);
		gameRegion.setCenter(playboard);
		gameRegion.setRight(sideVBox);
		return gameRegion;
	}

	//disables all buttons, called when waiting for opponent's turn
	public void disableAllButtons() {

		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				GridButton button = (GridButton)playboard.getChildren().get(8 * x + y);
				button.setDisable(true);
			}
		}

	}

	//creates the borderpane for the single player game
	public BorderPane createGameRegion() {

		tColor = -1;

		BorderPane gameRegion = new BorderPane();
		gameRegion.setId("playregion");

		//create top text
		Text heading = new Text("REVERSI");
		heading.setId("playregionhead");

		sideVBox = new VBox();

		//create back button
		Button backButton = new Button("Exit");
		EventHandler<ActionEvent> exitClicked = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				Alert confirmation = new Alert(AlertType.CONFIRMATION);
				confirmation.setContentText("Are you sure you want to exit the game?");
				ButtonType yes = new ButtonType("Yes");
				ButtonType no = new ButtonType("No");
				confirmation.getButtonTypes().setAll(yes, no);
				Optional<ButtonType> result = confirmation.showAndWait();
				if (result.get() == yes) {
					listening = false;
					pStage.setScene(mainscene);
					animateMain();
				}
			}
		};
		backButton.setOnAction(exitClicked);

		//create the board region
		playboard = new GridPane();
		playboard.setId("playboard");

		//sets sizes of row and column
		int size = (int)Math.min(640/length, 640/height);
		int maxDim = (int)Math.max(length, height);
		for (int i = 0; i < maxDim; i++) {
			ColumnConstraints cc = new ColumnConstraints(size);
			playboard.getColumnConstraints().add(cc);
			RowConstraints rc = new RowConstraints(size);
			playboard.getRowConstraints().add(rc);
		}

		//adds each button
		for (int x = 0; x < length; x++) {
			for (int y = 0; y < height; y++) {
				if (rboard.getSpace(x, y) != 2) {
					GridButton button = new GridButton(x, y);
					button.getStyleClass().add("gridsquare");
					//Image img = new Image(getClass().getResourceAsStream("emptysquare.png"));
					//button.setGraphic(new ImageView(img));
					button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

					EventHandler<ActionEvent> boardClicked = new EventHandler<ActionEvent>() {
						public void handle(ActionEvent e)
						{
							int[][] oldboard = rboard.getBoard();
							boolean canComputerPlay;

							int[] selCoords = button.getcoord();
							user.makeMove(selCoords[0], selCoords[1]);

							tColor = oColor;
							canComputerPlay = updateBoard(oldboard);

							if (canComputerPlay) {
								boolean canUserPlay;
								while (true) {
									oldboard = rboard.getBoard();
									comp.makeMove();
									tColor = pColor;
									canUserPlay = updateBoard(oldboard);
									if (canUserPlay)
										break;
									oldboard = rboard.getBoard();
									tColor = oColor;
									canComputerPlay = updateBoard(oldboard);
									if (!canComputerPlay) {
										endGameSP();
										break;
									}
								}
							}
							else {
								tColor = pColor;
								oldboard = rboard.getBoard();
								boolean canUserPlay = updateBoard(oldboard);
								if (!canUserPlay)
									endGameSP();
							}

						}
					};

					button.setOnAction(boardClicked);
					playboard.add(button, x, y);
				}
				else {
					Rectangle rect = new Rectangle();
					rect.setFill(Color.grayRgb(140));
					rect.setStroke(Color.BLACK);
					rect.setWidth(size);
					rect.setHeight(size);
					//rect.getStyleClass().add("smallborder");
					playboard.add(rect, x, y);
				}
			}
		}
		int grayscale = 140;
		if (length > height) {
			for (int x = 0; x < length; x++) {
				for (int y = height; y < length; y++) {
					Rectangle rect = new Rectangle();
					//rect.setFill(Color.grayRgb(grayscale));
					rect.setFill(Color.TRANSPARENT);
					rect.setWidth(size);
					rect.setHeight(size);
					playboard.add(rect, x, y);
				}
			}
		}
		else if (height > length) {
			for (int x = length; x < height; x++) {
				for (int y = 0; y < height; y++) {
					Rectangle rect = new Rectangle();
					//rect.setFill(Color.grayRgb(grayscale));
					rect.setFill(Color.TRANSPARENT);
					rect.setWidth(size);
					rect.setHeight(size);
					playboard.add(rect, x, y);
				}
			}
		}

		int[][] oldboard = rboard.getBoard();
		updateBoard(oldboard);
		if (user.getColor() == 1) {
			comp.makeMove();
			tColor *= -1;
			updateBoard(oldboard);
		}

		//create top hbox
		HBox topHBox = new HBox();
		topHBox.getChildren().add(backButton);
		topHBox.getChildren().add(heading);
		topHBox.getChildren().add(winAnnounce);

		gameRegion.setTop(topHBox);
		gameRegion.setCenter(playboard);
		gameRegion.setRight(sideVBox);
		return gameRegion;
	}

	//updates which buttons are disabled through legalboard, and which colors are which through the actual board
	public boolean updateBoard(int[][] oldboard) {

		boolean canPlay = !rboard.updateLegal(tColor);
		int[][] newboard = rboard.getBoard();
		int[][][] legalboard = rboard.getLegal();

		for (int x = 0; x < length; x++) {
			for (int y = 0; y < height; y++) {
				Node button = playboard.getChildren().get(height * x + y);
				if (button instanceof GridButton) {
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
					else if (rboard.getSpace(x, y) == 0) {
						button.getStyleClass().clear();
						button.setStyle("null");
						button.getStyleClass().add("gridsquare");
					}

					if (legalboard[x][y][0] == 0)
						button.setDisable(true);
					else
						button.setDisable(false);
				}
			}
		}

		return canPlay;

	}

	public void endGameSP() {

		int wColor = rboard.findWinner();
		if (wColor == pColor) {
			winAnnounce.setText("You win!");
		}
		else if (wColor == pColor * -1) {
			winAnnounce.setText("You lose!");
		}
		else {
			winAnnounce.setText("You tied with your opponent!");
		}

		Button playAgainButton = new Button("Play Again");
		EventHandler<ActionEvent> playAgainButtonclicked = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				sideVBox.getChildren().remove(playAgainButton);

				rboard = new ReversiBoard(length, height, numBlocked);

				Random rand = new Random();
				pColor = rand.nextInt(2) * 2 - 1;
				user = new Player(pName, rboard, pColor);
				comp = new Computer(rboard, pColor * -1);
				oColor = pColor * -1;
				tColor = -1;

				BorderPane gameRegion = createGameRegion();
				Scene gameScene = new Scene(gameRegion, windowWidth, windowHeight);
				gameScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
				pStage.setScene(gameScene);

			}
		};
		playAgainButton.setOnAction(playAgainButtonclicked);
		sideVBox.getChildren().add(playAgainButton);

	}

	public void endGameMP() {

		int wColor = rboard.findWinner();
		if (wColor == pColor) {
			winAnnounce.setText("You win!");
		}
		else if (wColor == pColor * -1) {
			winAnnounce.setText("You lose!");
		}
		else {
			winAnnounce.setText("You tied with your opponent!");
		}

		rematchButton = new Button("Rematch");
		EventHandler<ActionEvent> rematchClicked = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				try {
					connectButton.setDisable(true);
					rematchButton.setDisable(true);
					out.writeUTF("REMATCHREQUEST");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		};
		rematchButton.setOnAction(rematchClicked);
		sideVBox.getChildren().add(rematchButton);
		connectButton.setDisable(false);

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