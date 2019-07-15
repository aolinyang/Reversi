# REVERSI: oblique

This is a Desktop application based on the popular game Reversi (or Othello). This game is similar to the original Reversi, but allows you to customize the board. It can be played with the built-in AI (which utilizes the Minimax Alpha-Beta algorithm), or in a LAN network with your friends.

## Downloading the files
There are two ways to download the client and server JAR files.

Method one: Directly from Github
1. In the same directory as this README, you should see the files "Reversi.jar" and "Server.zip." Download these.

Method two: From my website
1. Go to allenyang.info
2. Scroll to Projects and find "Reversi: Oblique"
3. Click on "Application JAR" and "Server Launcher." This will download Reversi.jar and Server.zip.

Now that you have both files downloaded, you can play Reversi by simply opening the JAR file. You can either play singleplayer or multiplayer mode.
## Singleplayer mode
REVERSI: oblique is unique in that instead of a simple 8x8 board, you can make each dimension anywhere from 4 square to 20 squares. The board does not necessarily have to be a square.
You can also choose how many blocked squares there are. A blocked square is a square that you cannot place a piece on.

If you click on "Settings," under "Customize single player board" you can adjust the length, height, and number of pieces blocked. Note that the max number of pieces allowed to be blocked depends on the board size.

Note that the computer may take a while to compute moves as it goes through each layer in the Minimax algorithm.

## Multiplayer mode
Only one person is required to start the Server for everyone on the LAN network to connect to each other.

To start the server, extract the contents of Server.zip and open "Server Launcher".jar. Alternatively, you can run Server.jar on the command line. WARNING: do not open Server.jar by double-clicking. That will require you to force close it through Task Manager.

The server will give you its IP address. Entering multiplayer mode will require you to input an IP address, so make sure every player knows this address.

The server randomly matches players with anyone else who pressed the "Find Opponent" button. The board dimensions and squares blocked are randomly determined from a set that satisfies both players' conditions, which can be altered in Settings under Multiplayer Settings.

## Questions
I genuinely hope you enjoy this game. If you have any questions, feel free to send me an email at allenyang2813@gmail.com.