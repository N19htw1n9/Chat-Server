package BaccaratGame;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;

public class BaccaratThread extends Thread {
    private Socket connection;
    private int id;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private BaccaratGame game;

    private Consumer<Serializable> callback;

    public BaccaratThread(Socket connection, Consumer<Serializable> callback, int id) {
        this.connection = connection;
        this.callback = callback;
        this.id = id;
    }

    @Override
    public void run() {
        callback.accept("Game created for client #" + this.id);

        try {
            this.out = new ObjectOutputStream(this.connection.getOutputStream());
            this.in = new ObjectInputStream(this.connection.getInputStream());
            this.connection.setTcpNoDelay(true);
        } catch (Exception e) {
            callback.accept("Game client #" + id);
            callback.accept("\tError: Could not open streams");
        }

        while (!this.connection.isClosed()) {
            try {
                BaccaratInfo req = (BaccaratInfo) in.readObject();

                callback.accept("Game client #" + this.id + " sent a request: ");
                callback.accept("\tBid: " + req.bid + "\tHand: " + req.hand + "\n");

                game = new BaccaratGame(req.bid, req.hand);
                BaccaratDealer dealer = game.getTheDealer();
                BaccaratInfo res = new BaccaratInfo(req.bid, req.hand);

                ArrayList<Card> bankerHand = game.getBankerHand();
                ArrayList<Card> playerHand = game.getPlayerHand();
                // Check if we need to draw an additional card...
                if (BaccaratGameLogic.evaluatePlayerDraw(playerHand)) {
                    Card playerDrawCard = dealer.drawOne();

                    if (BaccaratGameLogic.evaluateBankerDraw(bankerHand, playerDrawCard)) {
                        Card bankerDrawCard = dealer.drawOne();
                        bankerDrawCard.setValue(playerDrawCard.getValue());
                        bankerHand.add(bankerDrawCard);
                    }
                    playerHand.add(playerDrawCard);
                }

                // Convert card to map with image
                res.bankerHand = game.convertCardToString(game.getBankerHand());
                res.playerHand = game.convertCardToString(game.getPlayerHand());

                // Set winner
                res.winner = BaccaratGameLogic.whoWon(game.getBankerHand(), game.getPlayerHand());
                res.winnings = game.evaluateWinnings();
                out.writeObject(res);

                callback.accept("\tResponse sent!");
            } catch (Exception e) {
                callback.accept("Error: Could not fetch request from game client #" + this.id);
                try {
                    this.connection.close();
                } catch (IOException e1) {
                    callback.accept("Error: Could not close connection for game client #" + this.id);
                }
            }
        }
        callback.accept("Connection closed for game client #" + this.id);
    }
}
