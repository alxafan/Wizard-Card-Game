package Model;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This is a thread that can be either a client or a server.
 */
public class ClientServerThread extends Thread implements IWizardModel{
    private final String ip;
    private final int port;
    private final int playerCount; // TODO: implement way to change this more easily
    private ServerSocket serversocket;
    private ArrayList<Socket> sockets = new ArrayList<>();
    private WizardModel model;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private ArrayList<ObjectOutputStream> serverOOS = new ArrayList<>();
    private ArrayList<ObjectInputStream> serverOIS = new ArrayList<>();
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
     * Creates a new client if a server exists or becomes a server.
     * @param ip    IP of the partner
     * @param port  Port to establish the connection
     * @return A new ClientServerThread-object.
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
    //TODO: Decide if to keep
    /**
     * Checks if this thread is a server. Is used in the model to decide what it should do.
     * @return true if the game is a server, false otherwise.
     */
    public boolean isServer() {return serversocket != null;}
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
                } catch (IOException e) {endConnection();}
            }
            ois = new ObjectInputStream(sockets.get(0).getInputStream());
            assignedPlayerNumber = ois.read();
            while (true) {
                model = (WizardModel) ois.readObject();
            }
        } catch (IOException e) { endConnection();
        } catch (ClassNotFoundException e) {throw new RuntimeException(e);} // This should not happen, since all classes are known in both server and client.
    }
    //TODO: decide on private or not
    public void newGame() {
        if (isServer()) {
            model = model.newGame();
            for (int i = 0; i <= sockets.size(); i++) model = model.addPlayer();
            serverOOS.forEach(oos -> send(oos, model));
        }
    }
    public void dealCards() {
        if (isServer()) {
            model = model.dealCards();
            serverOOS.forEach(oos -> send(oos, model));
        }
    }
    public void setTricksCalled(int tricksCalled, int playerNum) {
        if (isServer()) {
            model = model.setTricksCalled(tricksCalled,playerNum);
            serverOOS.forEach(oos -> send(oos, model));
        }
        else send(oos, model.setTricksCalled(tricksCalled, playerNum));
    }
    public void playCard(byte card) {
        if (isServer()) {
            model = model.playCard(card);
            serverOOS.forEach(oos -> send(oos, model));
        }
        else send(oos, model.playCard(card));
    }
    public void endTrick() {
        if (isServer()) {
            model = model.endTrick();
            serverOOS.forEach(oos -> send(oos, model));
        }
    }
    public void endRound() {
        if (isServer()) {
            model = model.endRound().dealCards();
            serverOOS.forEach(oos -> send(oos, model));
        }
    }
    public int isLegalTrickCall(int tricksCalled, int playerNum) {return model.isLegalTrickCall(tricksCalled, playerNum);}
    public int isLegalMove(byte card) {return model.isLegalMove(card);}
    public boolean isGameOver() {return model.isGameOver();}
    public boolean isTrickOver() {return model.isTrickOver();}
    public boolean isRoundOver() {return model.isRoundOver();}
    public boolean allPlayersCalledTricks() {return model.haveAllPlayersCalledTricks();}
    public List<Player> players() {return model.players();}
    public List<Byte> trick() {return model.trick();}
    public byte trump() {return model.trump();}
    public int round() {return model.round();}
    public int winner() {return model.trickWinner();}
    public int getCurrentPlayerNum() {return model.getCurrentPlayerNum();}
    public int getCurrentTrickCaller(){return model.getCurrentTrickCaller();}
    public List<Integer> getCurrentGameWinner() {return model.getCurrentGameWinner();}
    public int getAssignedPlayerNum() {return assignedPlayerNumber;}
    public boolean hasGameEnded() {return gameEnded;}
    void endConnection() {
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

