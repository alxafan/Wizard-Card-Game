package Wizard.Model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class WizardModelTest {
    @Test
    void newGameCreatesNewGame() {
        WizardModel w = new WizardModel().addPlayer().addPlayer().addPlayer().dealCards();
        assertNotEquals(w.newGame(), w);
        assertNotEquals(w.newGame().players(), w.players());
        //...
    }
    @Test
    void checkingAndPlayingMovesWorks () {
        // Setting up Scenario
        WizardModel w = new WizardModel().addPlayer().addPlayer().addPlayer().dealCards();
        List<Byte> p1Hand = w.players().get(0).hand();
        List<Byte> p2Hand = w.players().get(1).hand();
        List<Byte> p3Hand = w.players().get(2).hand();


        // regular legal cards being played
        assertEquals(0, w.isLegalMove(p1Hand.get(0)));
        assertEquals(0, w.playCard(p1Hand.get(0)).isLegalMove(p2Hand.get(0)));
        // checking if played Card appears in trick
        assertEquals( p1Hand.get(0) & 0b11001111, w.playCard(p1Hand.get(0)).trick().get(0) & 0b11001111);
        // card of another player trying to be played
        assertEquals(2, w.playCard(p1Hand.get(0)).isLegalMove(p3Hand.get(0)));


        // new Players
        Player p4 = new Player().addCard((byte) 12).addCard((byte) 71);
        Player p5 = new Player().addCard((byte) 66).addCard((byte) 134);
        w =  new WizardModel(List.of(p4,p5),List.of(),2,0,(byte) 135,0,-1);
        List<Byte> p4Hand = w.players().get(0).hand();
        List<Byte> p5Hand = w.players().get(1).hand();
        // normal play
        assertEquals(0, w.playCard(p4Hand.get(1)).isLegalMove(p5Hand.get(0)));
        // trying to play a card after all players previously did so
        assertEquals(1, w.playCard(p4Hand.get(0)).playCard(p5Hand.get(0))
                                                        .isLegalMove((byte) 0));
        // checking for color forcing by playing card of wrong color, while having correct color
        assertEquals(3, w.playCard(p4Hand.get(1)).isLegalMove(p5Hand.get(1)));
    }

    @Test
    void checkingAndCallingTricksWorks() {
        WizardModel w = new WizardModel().addPlayer().addPlayer().addPlayer().dealCards();
        assertEquals(0, w.isLegalTrickCall(0,0));
        assertEquals(0, w.isLegalTrickCall(1,0));
        assertEquals(2, w.isLegalTrickCall(0,3));
        assertEquals(3, w.isLegalTrickCall(0,1));
        assertEquals(4, w.isLegalTrickCall(-1,0));
        assertEquals(4, w.isLegalTrickCall(2,0));


        w = w.callTricks(0,0).callTricks(1,1);

        assertEquals(w.players().get(0).tricksCalled(), 0);
        assertEquals(w.players().get(1).tricksCalled(), 1);

        assertEquals(5, w.isLegalTrickCall(0,2));
        assertEquals(1, w.callTricks(1,2).isLegalTrickCall(0,0));
    }

    @Test
    void endingTrickWorks() {
        Player p1 = new Player().addCard((byte) 12).addCard((byte) 71).setTricksCalled(0);
        Player p2 = new Player().addCard((byte) 206).addCard((byte) 134).addToScore(20).setTricksCalled(0);
        WizardModel w =  new WizardModel(List.of(p1,p2),List.of((byte) 3),2,1,(byte) 135,0,-1)
                .playCard((byte) 12)
                .endTrick();

        assertTrue(w.trick().isEmpty());
        assertEquals(0, w.trickWinner());
        assertEquals(1,w.players().get(0).tricksWon());
        assertEquals(0,w.players().get(1).tricksWon());
        assertEquals(w.trickWinner(),w.startingPlayer());
    }

    @Test
    void endingRoundWorks() {
        Player p1 = new Player().addCard((byte) 12).setTricksCalled(1);
        Player p2 = new Player().setTricksCalled(0).setTricksWon(1).addToScore(20);
        WizardModel w =  new WizardModel(List.of(p1,p2),List.of((byte) 3),2,1,(byte) 135,0,-1)
                .playCard((byte) 12)
                .endTrick().endRound();

        assertEquals(3, w.round());
        assertEquals(0, w.startingPlayer());
        assertEquals(0, w.totalTricksCalled());

        assertEquals(30, w.players().get(0).score());
        assertEquals(10, w.players().get(1).score());

        assertEquals(0, w.players().get(0).tricksCalled());
        assertEquals(0, w.players().get(1).tricksCalled());

    }

    @Test
    void gettersAndQueriesWork() {
        Player p1 = new Player().addCard((byte) 12).addCard((byte) 71);
        Player p2 = new Player().addCard((byte) 206).addCard((byte) 134).addToScore(20);
        WizardModel w =  new WizardModel(List.of(p1,p2),List.of((byte) 3),2,1,(byte) 135,0,-1);


        assertEquals(List.of(p1,p2), w.players());
        assertEquals(List.of((byte) 3), w.trick());
        assertEquals(2, w.round());
        assertEquals(1, w.startingPlayer());
        assertEquals((byte) 135, w.trump());
        assertEquals(0, w.totalTricksCalled());
        assertEquals(-1, w.trickWinner());
        assertEquals(0, w.getCurrentPlayerNum());
        assertEquals(1, w.getCurrentTrickCaller());
        assertEquals(List.of(1), w.getCurrentGameWinner());

        assertFalse(w.isGameOver());
        assertFalse(w.isTrickOver());
        assertFalse(w.isRoundOver());
        assertFalse(w.haveAllPlayersCalledTricks());
    }

    @Test
    void toStringMethodAndCardToStringWorks() {
        Player p1 = new Player().addCard((byte) 12).addCard((byte) 71);
        Player p2 = new Player().addCard((byte) 206).addCard((byte) 134);
        WizardModel w =  new WizardModel(List.of(p1,p2),List.of(),2,1,(byte) 135,0,-1);
        String expected = """
                Round: 2
                Trump card: 7 Blue
                Cards in trick:\s

                Players hands:\s
                12 Red
                7 Green
                14 Yellow
                6 Blue
                Player-data: Player[hand=[12, 71], tricksCalled=0, tricksWon=0, score=0, hasCalledTrick=false]Player[hand=[-50, -122], tricksCalled=0, tricksWon=0, score=0, hasCalledTrick=false]
                Current players turn: 1""";
        assertEquals(expected,w.toString());
        assertEquals("7 Green",w.cardToString((byte) 71));
    }

}
