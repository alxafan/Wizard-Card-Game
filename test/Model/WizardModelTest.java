package Model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class WizardModelTest {
    @Test
    void newGameCreatesNewGame() {
        WizardModel w = new WizardModel().addPlayer().addPlayer().addPlayer().dealCards();
        assertNotEquals(w, w.newGame());
        assertNotEquals(w.players(), w.newGame().players());
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
        assertEquals(w.isLegalMove(p1Hand.get(0)), 0);
        assertEquals(w.playCard(p1Hand.get(0)).isLegalMove(p2Hand.get(0)), 0);
        // checking if played Card appears in trick
        assertEquals( w.playCard(p1Hand.get(0)).trick().get(0) & 0b11001111, p1Hand.get(0) & 0b11001111);
        // card of another player trying to be played
        assertEquals(w.playCard(p1Hand.get(0)).isLegalMove(p3Hand.get(0)), 2);


        // new Players
        Player p4 = new Player().addCard((byte) 12).addCard((byte) 71);
        Player p5 = new Player().addCard((byte) 66).addCard((byte) 134);
        w =  new WizardModel(List.of(p4,p5),List.of(),2,0,(byte) 135,0,-1);
        List<Byte> p4Hand = w.players().get(0).hand();
        List<Byte> p5Hand = w.players().get(1).hand();
        // normal play
        assertEquals(w.playCard(p4Hand.get(1)).isLegalMove(p5Hand.get(0)), 0);
        // trying to play a card after all players previously did so
        assertEquals(w.playCard(p4Hand.get(0)).playCard(p5Hand.get(0))
                        .isLegalMove((byte) 0),1);
        // checking for color forcing by playing card of wrong color, while having correct color
        assertEquals(w.playCard(p4Hand.get(1)).isLegalMove(p5Hand.get(1)), 3);
    }

    @Test
    void toStringMethodAndCardToStringWorks() {
        WizardModel w = new WizardModel();
        String expected = """
                Round: 1
                Trump card: 0 Red
                Cards in trick:\s

                Players hands:\s
                Player-data:\s
                Current players turn: 0""";
        assertEquals(w.toString(), expected);

        Player p1 = new Player().addCard((byte) 12).addCard((byte) 71);
        Player p2 = new Player().addCard((byte) 206).addCard((byte) 134);
        w =  new WizardModel(List.of(p1,p2),List.of(),2,1,(byte) 135,0,-1);
        expected = """
                Round: 1
                Trump card: 0 Red
                Cards in trick:\s

                Players hands:\s
                Player-data:\s
                Current players turn: 0""";
        assertEquals(w.toString(), expected);
    }
}
