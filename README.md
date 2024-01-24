# Wizard

![](Screenshots/Startscreen.png)

PokePong ist ein kurzweiliges *Pokemon* Spiel, bei dem zwei Spieler gegeneinander im klassischen Spiel Pong antreten.<p>
>Spieler 1 steuert Pikachu mit **W** und **S** <p>
>Spieler 2 steuert Evoli mit den Pfeiltasten **UP** und **Down**

## Starteinleitung

Zum **Starten** des Spieles müssen folgende Schritte befolgt werden:
>1. Öffnen der Datei `PokePong.Main.java`

>2. Starten der Funktion `main()`

## Verwendete Bibliotheken
Das Programm verwendet die folgenden Bibliotheken:
- [Processing](http://www.processing.org)
- <a href="https://junit.org"> Junit</a>

## Screenshots
Ein Screenshot des laufenden Spieles:
>![game](Screenshots/game.png)

## Jshell Anleitung
Ein Sinnvolles Spiel ist nur begrentzt mit der Jshell möglich.
>1. Starten einer Konsole
>2. Öffnen der Jshell über `jshell -class-path ./out/production/Wizard"` in einer Kommandozeile
>3. Importieren des Paketes Model über `import Model.*`

`var w = new WizardModel()` Erzeugt ein neues Wizard Spiel.<p>
Über `w = w.BEFEHL` wird der Befehl ausgeführt und das Model aktualisiert, ohne das "=" wird das neue Model nicht gespeichert. <p>
Wenn man mehrere Befehler hintereinander ausführen will, kann man nach jedem Befehl ein .BEFEHL dranhängen:`w = w.BEFEHL1.BEFEHL2`. Dies gilt nicht für die Abfragen, die mit is, get oder have beginnen <p>

### Befehle/Abfragen:

`addPlayer()` Fügt dem Spiel einen Spieler hinzu.<p>
`dealCards()` Teilt an alle Spiele Karten in Abhängigkeit von der derzeitigen Rundenzahl aus.<p>
`isLegalTrickCall(ANZAHL, SPIELER)` Überprüft, ob eine gewisse Stichansage möglich ist.<p>
`isLegalMove(KARTE)` Überprüft, ob eine gewisse Karte gespielt werden kann.<p>
`setTricksCalled(ANZAHL, SPIELER)` Setzt eine Stichanzahl für einen Spieler.
`playCard(KARTE)` Spielt eine Karte aus, der ausspielende Spieler ist der, der als nächstes an der Reihe ist. <p>
`isTrickOver()` Überprüft, ob der aktuelle Stich vorbei ist (wenn alle Spieler eine Karte gespielt haben).<p>
`isRoundOver()` Überprüft, ob die aktuelle Runde vorbei ist (wenn alle Spieler leere Hände haben und der Stich leer ist).<p>
`isGameOver()` Überprüft, ob das Spiel vorbei ist (nach einer bestimmten Anzahl von Runden).<p>
`endTrick()` Beendet den aktuellen Stich und geht zum nächsten über.<p>
`endRound()` Beendet die aktuelle Runde und geht zur nächsten über.<p>
`haveAllPlayersCalledTricks()` Überprüft, ob alle Spieler ihre Stichansagen für die aktuelle Runde gemacht haben.<p>
`getCurrentPlayerNum()` Gibt die aktuelle Spielernummer zurück, der an der Reihe ist, eine Karte zu spielen.<p>
`getCurrentTrickCaller()` Gibt die Spielernummer zurück,  der an der Reihe ist, einen Stich vorherzusagen.<p>
`getCurrentGameWinner()` Ermittelt die derzeitigen Gewinner des Spiels.

### Beispiel-Nutzung:
var w = new WizardModel()
w = w.addPlayer().addPlayer().addPlayer().dealCards()


