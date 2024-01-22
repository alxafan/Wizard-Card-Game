package Model;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * This is a thread that can be either a client or a server.
 */
public class ClientServerThread extends Thread {
    private final String ip;
    private final int port;
    private final int playerCount = 3; // TODO: implement way to change this more easily
    private ServerSocket serversocket;
    private ArrayList<Socket> sockets = new ArrayList<>();
    private WizardModel model;
    private ObjectOutputStream oos;
    private ArrayList<ObjectOutputStream> serverOOS;
    private ArrayList<ObjectInputStream> serverOIS;
    private int assignedPlayerNumber;

    private ClientServerThread(String ip, int port) {
        this.ip = ip;
        this.port = port;
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

    void setModel(WizardModel model) {
        this.model = model;
    }
    WizardModel getModel() {
        return model;
    }

    /**
     * Creates a new connection either as a client or as a server.
     */
    private synchronized void reconnect() {
        System.out.println("Reconnect");
        // Close all previously active sockets and streams before reconnecting
        if(!sockets.isEmpty()) {
            try {
                for (Socket socket : sockets) {
                    socket.close();
                }
            } catch (IOException e) {}
            sockets.clear();
        }
        if(serversocket != null) {
            try {
                serversocket.close();
            } catch (IOException e) {}
            serversocket = null;
        }
        if(oos != null) {
            try {
                oos.close();
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

    public synchronized void send(Object obj) {
        try {
            if (oos != null) {
                oos.reset();
                oos.writeObject(obj);
            }
        } catch (IOException e) {
            // Nothing. If sending not possible, do not send anything.
        }
    }

    /**
     * Method to send all clients the same object, should only be accessed by a server
     * @param obj the object to be sent
     */
    public synchronized void groupSend(Object obj) {
        // assert to signalize the requirements
        assert isServer() && !serverOOS.isEmpty();
        serverOOS.forEach(oos -> {
            try {
                if (oos != null) {
                    oos.reset();
                    oos.writeObject(obj);
                }
            } catch (IOException ee) {
                // Nothing. If sending not possible, do not send anything.
            }
        });
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
            // If this is a server accept one client
            if (sockets.isEmpty()) {
                assignedPlayerNumber = 0;
                for (int i = 0; i < playerCount; i++) sockets.add(sockets.size(),serversocket.accept());
                for (int i = 0; i < sockets.size(); i++) {
                    try {
                        serverOOS.add(serverOOS.size(), new ObjectOutputStream(sockets.get(serverOOS.size()).getOutputStream()));
                        serverOOS.get(i).write(i+1);
                        serverOIS.add(i, new ObjectInputStream(sockets.get(i).getInputStream()));
                        //TODO: remove print
                        System.out.println("Connection established with " + sockets.get(i).getInetAddress());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                ObjectInputStream ois = new ObjectInputStream(sockets.get(0).getInputStream());
                while (ois.read() == 0);
                assignedPlayerNumber = ois.read();
                ois.close();
            }
            while (true) {
                if (isServer()) {
                    // read first, update, then send to all others
                    for(ObjectInputStream ois : serverOIS) {
                        WizardModel obj = (WizardModel) ois.readObject();
                        if (!obj.equals(model)) groupSend(obj);
                    }

                } else {
                    ObjectInputStream ois = new ObjectInputStream(sockets.get(0).getInputStream());
                    model = (WizardModel) ois.readObject();
                    send(model);
                }

            }
        } catch (IOException e) {
            // Connection lost end game prematurely
        } catch (ClassNotFoundException e) {
            // This should not happen, since all classes are known in both server and client.
            throw new RuntimeException(e);
        }
    }
}

