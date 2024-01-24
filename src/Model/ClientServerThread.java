package Model;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This is a thread that can be either a client or a server. Handles the networking for the game
 * <p></p>
 * stores the most up-to-date model for the controller to access,
 * passes most methods through to model (kind of like an interface).
 */
public class ClientServerThread extends Thread implements IWizardModel{
    private final String ip;
    private final int port;
    private final int playerCount; // TODO: implement way to change this more easily
    private ServerSocket serversocket;
    private final ArrayList<Socket> sockets = new ArrayList<>();
    private WizardModel model;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private final ArrayList<ObjectOutputStream> serverOOS = new ArrayList<>();
    private final ArrayList<ObjectInputStream> serverOIS = new ArrayList<>();
    private int assignedPlayerNumber = -1;
    private int inputCheckCounter;
    private boolean gameEnded;
    private ClientServerThread(String ip, int port, int playerCount) {
        this.ip = ip;
        this.port = port;
        this.playerCount = playerCount-1;
        model = new WizardModel();
    }
    /**
     * Creates a new client-thread and connects it, if a server exists, otherwise it creates a server-thread.
     * @param ip    IP of the server if there is one
     * @param port  Port to establish the connection
     * @return either a new server-object or a new client-object.
     */
    public static ClientServerThread newAny(String ip, int port, int playerCount) {
        var cst = new ClientServerThread(ip, port, playerCount);
        cst.connect();
        return cst;
    }
    /**
     * Creates a new connection either as a client or as a server.
     */
    private synchronized void connect() {
        // Initialize a new connection
        try {
            // Try to connect as a client first
            sockets.add(sockets.size(), new Socket(ip, port));
            oos = new ObjectOutputStream(sockets.get(sockets.size()-1).getOutputStream());
        } catch (IOException e) {
            // Create a server if connection is not possible
            try {
                serversocket = new ServerSocket(port);
                sockets.clear();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }
    private synchronized void send(ObjectOutputStream oos, Object obj) {
        try {
            if (oos != null) {
                oos.reset();
                oos.writeObject(obj);
            }
        } catch (IOException e) {}// if something went wrong, send nothing
    }
    /**
     * Determines whether this thread is a server or not.
     * @return true if this thread is a server, false otherwise.
     */
    public boolean isServer() {return serversocket != null;}

    /**
     * Handles the assigning of player numbers to the clients/server and the models being sent using ObjectInput-/OutputStreams.
     * Whenever a model is sent by a client the server updates it for himself and sends it to all other clients.
     */
    @Override
    public void run() {
        try {
            // If this is a server accept a number of clients
            if (isServer()) {
                assignedPlayerNumber = 0;
                for (int i = 0; i < playerCount; i++) sockets.add(sockets.size(),serversocket.accept());
                try {
                    for (int i = 0; i < sockets.size(); i++) {
                        serverOOS.add(serverOOS.size(), new ObjectOutputStream(sockets.get(serverOOS.size()).getOutputStream()));
                        serverOOS.get(i).write(i+1);
                        serverOIS.add(i, new ObjectInputStream(sockets.get(i).getInputStream()));
                    }
                    newGame();
                    while (true) {
                        try {
                            sockets.get(inputCheckCounter).setSoTimeout(50);
                            model = (WizardModel) serverOIS.get(inputCheckCounter).readObject();
                            serverOOS.forEach(ou -> send(ou, model));
                        } catch (SocketTimeoutException e) { inputCheckCounter = (inputCheckCounter+1)%playerCount;}
                    }
                } catch (IOException e) {
                    endGame();}
            }
            ois = new ObjectInputStream(sockets.get(0).getInputStream());
            assignedPlayerNumber = ois.read();
            while (true) {
                model = (WizardModel) ois.readObject();
            }
        } catch (IOException e) { endGame();
        } catch (ClassNotFoundException e) {throw new RuntimeException(e);} // This should not happen, since all classes are known in both server and client.
    }

    /**
     * Creates a new game/resets it and sends the new model to the clients. Only usable by the server.
     */
    public void newGame() {
        if (isServer()) {
            model = model.newGame();
            for (int i = 0; i <= sockets.size(); i++) model = model.addPlayer();
            serverOOS.forEach(oos -> send(oos, model));
        }
    }

    /**
     * Deals out the cards, determines a trump and sends the new model to the clients. Only usable by the server.
     */
    public void dealCards() {
        if (isServer()) {
            model = model.dealCards();
            serverOOS.forEach(oos -> send(oos, model));
        }
    }

    /**
     * As server: executes method in model and sends updated model to all clients
     * <p></p>
     * As client: executes method in model and sends updated model to server.
     * <p></p>
     * See <a href="src.Model.WizardModel">Model</a> for more information about this method.
     */
    public void setTricksCalled(int tricksCalled, int playerNum) {
        if (isServer()) {
            model = model.setTricksCalled(tricksCalled,playerNum);
            serverOOS.forEach(oos -> send(oos, model));
        }
        else send(oos, model.setTricksCalled(tricksCalled, playerNum));
    }
    /**
     * As server: executes method in model and sends updated model to all clients
     * <p></p>
     * As client: executes method in model and sends updated model to server.
     * <p></p>
     * See <a href="src.Model.WizardModel">Model</a> for more information about this method.
     */
    public void playCard(byte card) {
        if (isServer()) {
            model = model.playCard(card);
            serverOOS.forEach(oos -> send(oos, model));
        }
        else send(oos, model.playCard(card));
    }
    /**
     * Executes method in model and sends updated model to all clients. Only usable by the server.
     * See <a href="src.Model.WizardModel">Model</a> for more information about this method.
     */
    public void endTrick() {
        if (isServer()) {
            model = model.endTrick();
            serverOOS.forEach(oos -> send(oos, model));
        }
    }
    /**
     * Executes method in model and sends updated model to all clients. Only usable by the server.
     * See <a href="src.Model.WizardModel">Model</a> for more information about this method.
     */
    public void endRound() {
        if (isServer()) {
            model = model.endRound().dealCards();
            serverOOS.forEach(oos -> send(oos, model));
        }
    }

    /**
     * see <a href="src.Model.WizardModel">Model</a> for more information about this method
     */
    public int isLegalTrickCall(int tricksCalled, int playerNum) {return model.isLegalTrickCall(tricksCalled, playerNum);}
    /**
     * see <a href="src.Model.WizardModel">Model</a> for more information about this method
     */
    public int isLegalMove(byte card) {return model.isLegalMove(card);}
    /**
     * see <a href="src.Model.WizardModel">Model</a> for more information about this method
     */
    public boolean isGameOver() {return model.isGameOver();}
    /**
     * see <a href="src.Model.WizardModel">Model</a> for more information about this method
     */
    public boolean isTrickOver() {return model.isTrickOver();}
    /**
     * see <a href="src.Model.WizardModel">Model</a> for more information about this method
     */
    public boolean isRoundOver() {return model.isRoundOver();}
    /**
     * see <a href="src.Model.WizardModel">Model</a> for more information about this method
     */
    public boolean allPlayersCalledTricks() {return model.haveAllPlayersCalledTricks();}
    /**
     * see <a href="src.Model.WizardModel">Model</a> for more information about this method
     */
    public List<Player> players() {return model.players();}
    /**
     * see <a href="src.Model.WizardModel">Model</a> for more information about this method
     */
    public List<Byte> trick() {return model.trick();}
    /**
     * see <a href="src.Model.WizardModel">Model</a> for more information about this method
     */
    public byte trump() {return model.trump();}
    /**
     * see <a href="src.Model.WizardModel">Model</a> for more information about this method
     */
    public int round() {return model.round();}
    /**
     * see <a href="src.Model.WizardModel">Model</a> for more information about this method
     */
    public int winner() {return model.trickWinner();}
    /**
     * see <a href="src.Model.WizardModel">Model</a> for more information about this method
     */
    public int getCurrentPlayerNum() {return model.getCurrentPlayerNum();}
    /**
     * see <a href="src.Model.WizardModel">Model</a> for more information about this method
     */
    public int getCurrentTrickCaller(){return model.getCurrentTrickCaller();}
    /**
     * see <a href="src.Model.WizardModel">Model</a> for more information about this method
     */
    public List<Integer> getCurrentGameWinner() {return model.getCurrentGameWinner();}
    /**
     * see <a href="src.Model.WizardModel">Model</a> for more information about this method
     */
    public int getAssignedPlayerNum() {return assignedPlayerNumber;}
    /**
     * see <a href="src.Model.WizardModel">Model</a> for more information about this method
     */
    public boolean hasGameEnded() {return gameEnded;}
    /**
     * Ends the connection with server/clients
     */
    public void endGame() {
        try {
            gameEnded = true;
            if (isServer()) {
                for(ObjectOutputStream s : serverOOS) s.close();
                for(ObjectInputStream s : serverOIS) s.close();
                for(Socket s : sockets) s.close();
                serversocket.close();
            } else {
                oos.close();
                ois.close();
                for(Socket s : sockets) s.close();
            }
        } catch (IOException e) {}
    }
}

