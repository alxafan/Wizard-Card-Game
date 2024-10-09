package Wizard.Model;

import Wizard.Model.ClientServerThread;
import Wizard.Model.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class ClientServerThreadTest {
    ClientServerThread server;
    ClientServerThread client1;
    ClientServerThread client2;
    @BeforeEach
    void setup() throws InterruptedException {
        server = ClientServerThread.newAny("localhost", 5555, 3);
        server.start();
        Thread.sleep(200);
        client1 = ClientServerThread.newAny("localhost", 5555, 3);
        client1.start();
        Thread.sleep(200);
        client2 = ClientServerThread.newAny("localhost", 5555, 3);
        client2.start();
        Thread.sleep(200);
    }
    @AfterEach
    void tearDown() {
        server.endGame();
        client1.endGame();
        client2.endGame();
    }

    @Test
    void serverIsInitializedAndClientsConnectedSuccessfully() {
        assertEquals(server.players(),client1.players());
        assertEquals(server.players(),client2.players());
    }

    @Test
    void dealCardsWorks() throws InterruptedException {
        server.dealCards();
        Thread.sleep(100);
        assertEquals(server.players(),client1.players());
        assertEquals(server.players(),client2.players());
        List<Player> tmp = client1.players();
        client1.dealCards();
        Thread.sleep(200);
        assertEquals(tmp, client1.players());
    }

    @Test
    void setTricksCalledWorks() throws InterruptedException{
        List<Player> tmp = client1.players();
        try {
            client1.setTricksCalled(0,1);
        } catch(AssertionError e) {
            Thread.sleep(200);
            assertEquals(tmp, client1.players());
        }

        tmp = server.players();
        server.setTricksCalled(1,0);
        Thread.sleep(200);
        // server setting tricks successfully and clients receiving
        assertNotEquals(tmp, server.players());
        assertNotEquals(tmp, client1.players());

        tmp = client1.players();
        client1.setTricksCalled(0,1);
        Thread.sleep(200);
        assertNotEquals(tmp, client1.players());
        assertEquals(server.players(), client1.players());
    }

    @Test
    void playCardWorks() throws InterruptedException{
        server.dealCards();
        Thread.sleep(200);

        List<Player> tmp = client1.players();
        try {
            client1.playCard(client1.players().get(0).hand().get(0));
        } catch(AssertionError e) {
            Thread.sleep(200);
            assertEquals(tmp, client1.players());
        }

        tmp = server.players();
        server.playCard(server.players().get(0).hand().get(0));
        Thread.sleep(200);
        // server setting tricks successfully and clients receiving
        assertNotEquals(tmp, server.players());
        assertNotEquals(tmp, client1.players());
        // Checking if played card landed in trick
        assertEquals(server.trick().get(0) & 0b11001111, tmp.get(0).hand().get(0) & 0b11001111);
        assertEquals( client1.trick().get(0) & 0b11001111, tmp.get(0).hand().get(0) & 0b11001111);


        tmp = client1.players();
        client1.playCard(client1.players().get(1).hand().get(0));
        Thread.sleep(200);
        assertNotEquals(tmp, client1.players());
        assertEquals(server.players(), client1.players());

        assertEquals(server.trick().get(1) & 0b11001111, tmp.get(1).hand().get(0) & 0b11001111);
        assertEquals(client1.trick().get(1) & 0b11001111, tmp.get(1).hand().get(0) & 0b11001111);
    }
    
    @Test
    void endTrickWorks() throws InterruptedException {
        server.dealCards();
        Thread.sleep(10);
        server.playCard(server.players().get(0).hand().get(0));
        Thread.sleep(10);
        server.playCard(server.players().get(1).hand().get(0));
        Thread.sleep(10);
        server.playCard(server.players().get(2).hand().get(0));

        client1.endTrick();
        Thread.sleep(200);
        assertFalse(client1.trick().isEmpty());
        assertFalse(server.trick().isEmpty());

        server.endTrick();
        Thread.sleep(200);
        assertTrue(server.trick().isEmpty());
        assertTrue(client2.trick().isEmpty());
    }
    @Test
    void endRoundWorks() throws InterruptedException {
        server.dealCards();
        Thread.sleep(10);
        server.playCard(server.players().get(0).hand().get(0));
        Thread.sleep(10);
        server.playCard(server.players().get(1).hand().get(0));
        Thread.sleep(10);
        server.playCard(server.players().get(2).hand().get(0));
        server.endTrick();

        client1.endRound();
        Thread.sleep(200);
        assertNotEquals(2, client1.round());
        assertNotEquals(2, server.round());

        server.endRound();
        Thread.sleep(200);
        assertEquals(2, client1.round());
        assertEquals(2, server.round());
    }

    @Test
    void modelMethodsGetPassedTroughCorrectly() throws InterruptedException {
        server.dealCards();
        Thread.sleep(200);
        assertEquals(0, server.isLegalTrickCall(0,0));
        assertEquals(0, server.isLegalMove(client1.players().get(0).hand().get(0)));
        assertFalse(client2.isGameOver());
        assertFalse(server.isTrickOver());
        assertFalse(client1.isRoundOver());
        assertFalse(client2.allPlayersCalledTricks());
        assertFalse(server.hasGameEnded());
        assertEquals(server.trump(), client1.trump());
        assertEquals(server.winner(), client1.winner());
        assertEquals(server.getCurrentPlayerNum(), client1.getCurrentPlayerNum());
        assertEquals(server.getCurrentTrickCaller(), client1.getCurrentTrickCaller());
        assertEquals(server.getCurrentGameWinner(), client1.getCurrentGameWinner());

        assertEquals(0, server.getAssignedPlayerNum());
        assertEquals(1, client1.getAssignedPlayerNum());
        assertEquals(2, client2.getAssignedPlayerNum());
    }
    @Test
    void isServerWorks() {
        assertTrue(server.isServer());
        assertFalse(client1.isServer());
        assertFalse(client2.isServer());
    }
}
