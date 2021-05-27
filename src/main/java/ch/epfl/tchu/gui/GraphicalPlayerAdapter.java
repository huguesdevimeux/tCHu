package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static javafx.application.Platform.runLater;

/**
 * Adapter for Player.
 *
 * @author Hugues Devimeux (327282)
 * @author Luca Mouchel (324748)
 */
public final class GraphicalPlayerAdapter implements Player {

    private final BlockingQueue<SortedBag<Ticket>> ticketsRetrieverQueue =
            new ArrayBlockingQueue<>(1);
    private final BlockingQueue<Integer> drawSlotRetrieverQueue = new ArrayBlockingQueue<>(1);
    private final BlockingQueue<Route> claimedRouteRetrieverQueue = new ArrayBlockingQueue<>(1);
    private final BlockingQueue<SortedBag<Card>> initialClaimCardsRetrieverQueue =
            new ArrayBlockingQueue<>(1);
    private GraphicalPlayer graphicalPlayer;

    /**
     * Constructor for {@link GraphicalPlayerAdapter}.
     */
    public GraphicalPlayerAdapter() {
    }

    @Override
    public void initPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        BlockingQueue<GraphicalPlayer> queue = new ArrayBlockingQueue<>(1);
        runLater(() -> queue.add(new GraphicalPlayer(ownId, playerNames)));
        this.graphicalPlayer = retrieveFromQueue(queue);
    }

    @Override
    public void receiveInfo(String info) {
        runLater(() -> graphicalPlayer.receiveInfo(info));
    }

    @Override
    public void updateState(PublicGameState newState, PlayerState ownState) {
        runLater(() -> graphicalPlayer.setState(newState, ownState));
    }

    @Override
    public void setInitialTicketChoice(SortedBag<Ticket> tickets) {
        runLater(
                () ->
                        graphicalPlayer.chooseTickets(
                                tickets,
                                chosenTickets -> putInQueue(ticketsRetrieverQueue, chosenTickets)));
    }

    @Override
    public SortedBag<Ticket> chooseInitialTickets() {
        return retrieveFromQueue(ticketsRetrieverQueue);
    }

    @Override
    public TurnKind nextTurn() {
        BlockingQueue<TurnKind> turnKindRetrieverQueue = new ArrayBlockingQueue<>(1);
        ActionHandlers.DrawTicketsHandler drawTicketsHandler =
                () -> putInQueue(turnKindRetrieverQueue, TurnKind.DRAW_TICKETS);
        ActionHandlers.DrawCardHandler drawCardHandler =
                indexOfChosenCard -> {
                    putInQueue(turnKindRetrieverQueue, TurnKind.DRAW_CARDS);
                    putInQueue(drawSlotRetrieverQueue, indexOfChosenCard);
                };
        ActionHandlers.ClaimRouteHandler claimRouteHandler =
                (claimedRoute, initialClaimCards) -> {
                    putInQueue(turnKindRetrieverQueue, TurnKind.CLAIM_ROUTE);
                    putInQueue(claimedRouteRetrieverQueue, claimedRoute);
                    putInQueue(initialClaimCardsRetrieverQueue, initialClaimCards);
                };
        runLater(() -> graphicalPlayer.startTurn(
                                drawTicketsHandler, drawCardHandler, claimRouteHandler));
        return retrieveFromQueue(turnKindRetrieverQueue);
    }

    @Override
    public SortedBag<Ticket> chooseTickets(SortedBag<Ticket> options) {
        setInitialTicketChoice(options);
        return chooseInitialTickets();
    }

    @Override
    public int drawSlot() {
        // NOTE this method is not supposed to block.
        if (drawSlotRetrieverQueue.peek() != null) return drawSlotRetrieverQueue.poll();

        ActionHandlers.DrawCardHandler drawCardHandler =
                indexOfChosenCard -> putInQueue(drawSlotRetrieverQueue, indexOfChosenCard);
        runLater(() -> graphicalPlayer.drawCards(drawCardHandler));
        return retrieveFromQueue(drawSlotRetrieverQueue);
    }

    @Override
    public Route claimedRoute() {
        return retrieveFromQueue(claimedRouteRetrieverQueue);
    }

    @Override
    public SortedBag<Card> initialClaimCards() {
        return retrieveFromQueue(initialClaimCardsRetrieverQueue);
    }

    @Override
    public SortedBag<Card> chooseAdditionalCards(List<SortedBag<Card>> options) {
        BlockingQueue<SortedBag<Card>> queue = new ArrayBlockingQueue<>(1);
        ActionHandlers.ChooseCardsHandler handler =
                // usedCardsToClaimRoute is null when nothing as been chosen.
                usedCardsToClaimRoute ->
                        putInQueue(
                                queue,
                                Objects.requireNonNullElseGet(
                                        usedCardsToClaimRoute, SortedBag::of));
        runLater(() -> graphicalPlayer.chooseAdditionalCards(options, handler));
        return retrieveFromQueue(queue);
    }

    /**
     * Given a supplier and a queue, puts the supplier result in the queue in JavaFX's thread.
     *
     * @param value The supplier that provide the desired value.
     * @param queue The blocking queue.
     * @param <T>   Type of the value.
     * @throws Error If there is an error during the supplier execution.
     */
    private <T> void putInQueue(BlockingQueue<T> queue, T value) {
        try {
            queue.put(value);
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

    /**
     * Retrieve an element from a blocking queue.
     *
     * @param queue The queue.
     * @param <T>   Type of the element.
     * @return The element.
     */
    private <T> T retrieveFromQueue(BlockingQueue<T> queue) {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }
}
