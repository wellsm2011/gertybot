package werewolf.net;

import java.io.IOException;
import java.io.Serializable;

import java.util.logging.Logger;
import java.util.regex.Pattern;


/**
 * The command class represents a single interaction from the user to the bot. Commands are parsed by the ForumThread
 * class and the raw text is fed into this class. Commands may be hidden to render them invisible to human users, or
 * invalidated to prevent the bot from using them and to show human users that they are not being used.
 */
public class Command implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(Command.class.getName());

    @SuppressWarnings("compatibility:-3253140145062707166")
    private static final long serialVersionUID = -8803103421504120775L;

    private String    fullCommand;
    private String    command;
    private String    params;
    private boolean   hidden;
    private boolean   markedHidden;
    private ForumUser user;
    private boolean   invalidated;
    private boolean   checking = false; //True if the bot is currently checking this invalid command for valitity.
    private ForumPost post = null;

    private int postCounter = 0;


    /**
     * Creates a new command object with an attached post. This will allow invalidate() and hide() to work.
     *
     * @param command The raw text of the command. The command class will parse this into a name and param(s).
     * @param user The forum user that made the command.
     * @param invalidated True if the command is already invalid.
     * @param hidden True if the command is already hidden.
     * @param post The post associated with this command.
     */
    public Command(String command, ForumUser user, boolean invalidated, boolean hidden, ForumPost post) {
        fullCommand = command;
        command = command.replaceAll("^\\[+|\\]+$", "");
        this.command = command.replaceFirst(" .*$", "");
        this.params = command.replaceFirst("^[^ ]+ ", "");
        this.user = user;
        this.invalidated = invalidated;
        this.hidden = hidden;
        markedHidden = hidden;
        if (command.matches("^\\[\\[.*\\]\\]$"))
            markedHidden = true;
        this.post = post;
    }

    /**
     * @return The name of the command used.
     */
    public String getCommand() {
        return command;
    }

    /**
     * @return The param(s) of the command.
     */
    public String getParams() {
        return params;
    }

    /**
     * @return The forum user who made the command.
     */
    public ForumUser getUser() {
        return user;
    }

    /**
     * @return True if the command has been marked as invalid.
     */
    public boolean isInvalidated() {
        return invalidated;
    }

    /**
     * Marks the command as invalid and edits the post (if editing is allowed) to reflect the invalid state.
     *
     * @param reason The reason for invalidation. Used in the edit as an explination.
     */
    public void invalidate(String reason) {
        checking = false;
        if (invalidated)
            return;
        String infoString =
            "\nCommand: " + command + "\nLocation: " + post.getUrl() + "\nReason: " + reason + "\nContext: " +
            post.getContext().toString();
        try {
            if (postCounter == 0)
                LOGGER.info("Invalid command." + infoString);

            ForumPostEditor editor = post.getEditor();
            String          replaceCommand = "[color=#FF0000][b]" + fullCommand + "[/b] (" + reason + ")[/color]";
            //TODO: This doesn't work properly if the user has multiples of the same invalid command. Fix.
            editor.setText(editor.getText().replaceFirst("\\[b\\]" + Pattern.quote(fullCommand) + "\\[\\/b\\]",
                                                         replaceCommand));
            editor.submit();
            invalidated = true;
        } catch (IOException e) {
            if (postCounter++ < 5) {
                LOGGER.warning("IOException while trying to invalidate post. Context: " + post.getContext().toString());
                invalidate(reason);
            } else {
                LOGGER.warning("IOException while trying to invalidate post. Unable to invalidate. Context: " +
                               post.getContext().toString());
            }
        } catch (IllegalStateException e) {
            LOGGER.warning("IllegalStateException while trying to invalidate post. Context: " +
                           post.getContext().toString());
        }

        postCounter = 0;
    }


    /**
     * Marks a previously invalid command as valid and edits the post (if editing is allowed) to reflect the valid
     * state.
     */
    public void validate() {
        if (!invalidated)
            return;
        checking = false;
        //TODO: Implement validation logic.
    }


    /**
     * @return True if invalidate() and validate() have not been called since this command entered checking state.
     */
    public boolean isChecking() {
        return checking;
    }

    /**
     * Marks this command as being checked for validity. Does nothing if the command has not previously been marked as
     * invalid.
     */
    public void startCheck() {
        checking = invalidated;
    }


    /**
     * Hides this command, rendering it invisible to normal users but still visible to the bot. This is only allowed in
     * posts made by the host.
     */
    public void hide() {
        if (hidden)
            return;
        try {
            ForumPostEditor editor = post.getEditor();
            //TODO: Export the color and size tags to the ForumContext so they can be changed from forum to forum.
            editor.setText(editor.getText().replaceFirst("\\[b\\]" + Pattern.quote(fullCommand) + "\\[\\/b\\]",
                                                         "[color=#2e2e2f][size=1]" + fullCommand + "[/size][/color]"));
            editor.submit();
            hidden = true;
        } catch (IOException e) {
            if (postCounter++ < 5)
                hide();
            else
                System.err.println("Unable to hide command.");
        } catch (IllegalArgumentException e) {
            System.err.println("Unable to hide command.");
        }

        postCounter = 0;
    }


    /**
     * @return True if this command is marked as a hidden command.
     */
    public boolean isMarkedHidden() {
        return markedHidden;
    }


    /**
     * @return Returns the post associated with this command, or null if there is no post.
     */
    public ForumPost getPost() {
        return post;
    }


    /**
     * @return Returns the post ID associated with this command, or -1 if there is no post.
     */
    public int getPostId() {
        if (post == null)
            return -1;
        return post.getPostId();
    }

    /**
     * @param o The object to compare this command to.
     * @return Returns true if the given command is the same as this command.
     */
    public boolean equals(Object o) {
        if (o instanceof Command) {
            Command other = (Command)o;
            if (!user.equals(other.user))
                return false;
            if (!params.equals(other.params))
                return false;
            return command.equals(other.command);
        }
        return false;
    }

    /**
     * @return A string representation of this command.
     */
    public String toString() {
        return getCommand() + ":" + getParams() + " - " + getUser();
    }
}
