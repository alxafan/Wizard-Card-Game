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
    private final int playerCount = 2; // TODO: implement way to change this more easily
    private ServerSocket serversocket;
    private ArrayList<Socket> sockets = new ArrayList<>();
    private WizardModel model;
    private ObjectOutputStream oos;
    private ArrayList<ObjectOutputStream> serverOOS = new ArrayList<>();
    private ArrayList<ObjectInputStream> serverOIS = new ArrayList<>();
    private int assignedPlayerNumber;

    private ClientServerThread(String ip, int port) {
        this.ip = ip;
        this.port = port;
        model = new WizardModel();
    }
    /**
     * Creates a new client if a server exists or becomes a server.
     * @param ip    IP of the partner
     * @param port  Port to establish the connection
     * @return A new ClientServerThread-object.
     */
    public static ClientServerThread newAny(String ip, int port) {
        var cst = new ClientServerThread(ip, port);
        cst.reconnect();
        return cst;
    }

    /**
     * Creates a new connection either as a client or as a server.
     */
    private synchronized void reconnect() {
        System.out.println("Reconnect");
        // Close all previously active sockets and streams before reconnecting
        if(!sockets.isEmpty()) {
            try { for (Socket socket : sockets) {socket.close();}
            } catch (IOException e) {}
            sockets.clear();
        }
        if(serversocket != null) {
            try { serversocket.close();
            } catch (IOException e) {}
            serversocket = null;
        }
        if(oos != null) {
            try { oos.close();
            } catch (IOException e) {}
            oos = null;
        }

        // Initialize a new connection
        try {
            // Try to connect as a client first
            sockets.add(sockets.size(), new Socket(ip, port));
            oos = new ObjectOutputStream(sockets.get(sockets.size()-1).getOutputStream());
            System.out.println("This is a client");
        } catch (IOException e) {
            // Create a server if connection is not possible
            try {
                serversocket = new ServerSocket(port);
                sockets.clear();
                System.out.println("This is a server");
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    public synchronized void send(ObjectOutputStream oos, Object obj) {
        try {
            if (oos != null) {
                oos.reset();
                oos.writeObject(obj);
                System.out.println("Object sent");
            }
        } catch (IOException e) { System.out.println("something went wrong");
        }
    }

    /**
     * Checks if this thread is a server. Is used in the model to decide what it should do.
     * @return true if the game is a server, false otherwise.
     */
    public boolean isServer() {
        return serversocket != null;
    }

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

                        //TODO: remove print
                        System.out.println("Connection established with " + sockets.get(i).getInetAddress());
                    }

                    newGame();

                    while (true) {
                        for(ObjectInputStream oi : serverOIS) {
                            Object o = oi.readObject();
                            if (o instanceof WizardModel) {
                                System.out.println("Server: new Model received");
                                model = (WizardModel) o;
                                serverOOS.forEach(ou -> send(ou, model));
                            }
                        }
                    }
                } catch (IOException e) {throw new RuntimeException(e);}
            }
            ObjectInputStream ois = new ObjectInputStream(sockets.get(0).getInputStream());
            assignedPlayerNumber = ois.read();
            System.out.println("assigned PlayerNumber = " + assignedPlayerNumber);

            while (true) {
                WizardModel modelTest = (WizardModel) ois.readObject();
                if(!modelTest.equals(model)) System.out.println("new Model received");
                model = modelTest;
            }
        } catch (IOException e) {
            // Connection lost end game prematurely
        } catch (ClassNotFoundException e) {throw new RuntimeException(e);} // This should not happen, since all classes are known in both server and client.
    }

    public void newGame() {
        if (isServer()) {
            model = model.newGame();
            for (int i = 0; i <= sockets.size(); i++) model = model.addPlayer();
            System.out.println("test");
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
        model = model.setTricksCalled(tricksCalled,playerNum);
        System.out.println(tricksCalled);
        if (isServer()) serverOOS.forEach(oos -> send(oos, model));
        else send(oos, model);
    }
    public void playCard(byte card) {
        model = model.playCard(card);
        if (isServer()) serverOOS.forEach(oos -> send(oos, model));
        else send(oos, model);
    }
    public void endTrick() {
        if (isServer()) {
            model = model.endTrick();
            serverOOS.forEach(oos -> send(oos, model));
        }
    }
    public void endRound() {
        if (isServer()) {
            model = model.endRound();
            serverOOS.forEach(oos -> send(oos, model));
        }
    }
    public int isLegalTrickCall(int tricksCalled, int playerNum) {return model.isLegalTrickCall(tricksCalled, playerNum);}
    public int isLegalMove(byte card) {return model.isLegalMove(card);}
    public boolean isGameOver() {return model.isGameOver();}
    public boolean isTrickOver() {return model.isTrickOver();}
    public boolean isRoundOver() {return model.isRoundOver();}
    public boolean allPlayersCalledTricks() {return model.allPlayersCalledTricks();}
    public List<Player> players() {return model.players();}
    public List<Byte> trick() {return model.trick();}
    public byte trump() {return model.trump();}
    public int round() {return model.round();}
    public int winner() {return model.winner();}
    public int getCurrentPlayerNum() {return model.getCurrentPlayerNum();}
    public List<Integer> getCurrentGameWinner() {return model.getCurrentGameWinner();}
    public int getAssignedPlayerNum() {return assignedPlayerNumber;}
}

