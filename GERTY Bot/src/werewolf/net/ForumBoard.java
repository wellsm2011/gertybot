package werewolf.net;

import java.io.IOException;
import java.io.Serializable;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public abstract class ForumBoard implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(ForumBoard.class.getName());

    @SuppressWarnings("compatibility:8987008569289710696")
    private static final long serialVersionUID = -6322669366595710741L;

    public final int REFRESH_MINUTES;

    protected LinkedList<ForumThread> threads = new LinkedList<ForumThread>();
    protected LinkedList<ForumBoard>  subboards = new LinkedList<ForumBoard>();
    protected long                    lastRefresh = 0;
    protected String                  boardId;


    /**
     * Creates a new forum board that will wait 30 minutes before refreshing.
     *
     * @param boardId The unique identification for this board. Used by the realized class to access the board's data.
     */
    public ForumBoard(String boardId) {
        this(boardId, 30);
    }

    /**
     * Creates a new forum board with a given number of minutes before refreshing.
     *
     * @param boardId The unique identification for this board. Used by the realized class to access the board's data.
     * @param refreshMinutes The number of minutes before a refresh of the underlying data is requested.
     */
    public ForumBoard(String boardId, int refreshMinutes) {
        this.boardId = boardId;
        REFRESH_MINUTES = refreshMinutes;
    }

    /**
     * Refreshes the current information about this board, loading any new threads and updating the status of old
     * threads.
     * @throws IOException
     */
    private void refresh() throws IOException {
        if (lastRefresh != 0 && lastRefresh > System.currentTimeMillis() + REFRESH_MINUTES * 60 * 1000)
            return;

        if (lastRefresh == 0)
            LOGGER.info("Loading new board (context=" + getContext() + "). id=" + boardId);
        else
            LOGGER.info("Refreshing board (context=" + getContext() + "). id=" + boardId);

        lastRefresh = System.currentTimeMillis();

        threads = loadBoard();

        String output = "Board parsed (context=" + getContext() + "). " + threads.size() + " threads found:";
        for (ForumThread thread : threads)
            output += "\n\t" + thread;
        LOGGER.info(output);
    }

    /**
     * Implemented by realized classes. Parses a given board and returns a list of the threads contained in that board.
     *
     * @return A list of the threads contained in this forum board.
     * @throws IOException
     */
    protected abstract LinkedList<ForumThread> loadBoard() throws IOException;


    /**
     * @return The current list of threads in this forum board.
     * @throws IOException
     */
    public List<ForumThread> getThreads() throws IOException {
        refresh();
        return threads;
    }

    /**
     * @return A list of child boards ascociated with this board.
     * @throws IOException
     */
    public List<ForumBoard> getSubboards() throws IOException {
        refresh();
        return subboards;
    }

    /**
     * @return The forum context of this board.
     */
    public abstract ForumContext getContext();

    /**
     * @param o The forum board to compare this board to.
     * @return True if and only if the given forum board is from the same context and url as this board.
     */
    public boolean equals(ForumBoard o) {
        return o.getContext().equals(getContext()) && boardId.equals(o.boardId);
    }
}
