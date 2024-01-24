/**
 * The main logic package for this game.
 * <p></p>
 * It is split up between the network aspect and the game's internal logic. The game's internal logic is programmed with immutable data structures,
 * which doesn't allow for the controller to refer to the game's most recent state.
 * This resulted in the networking aspect having to store and constantly update the game's model record.
 */
package Model;