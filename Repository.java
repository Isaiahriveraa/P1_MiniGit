import java.util.*;
import java.text.SimpleDateFormat;

// Programmer: Yonie Isaiah Rivera Aguilar
// Date: 05/07/2025
// Course: CSE 123
// Professor Brunelle
// TA: Eeshani Shilamkar 
// This class Repository models a simple version control systems, supporting commit history 
// management similar to a basic version of Git. Each Repository has a name and a reference to the
// most recent commit the "head". Users can create new commits, view commit history, drop 
// commits, check for the existence of a commit, and synchronize with another repository.
public class Repository {
    private Commit head;
    private String name;

    /**
     * 
     * Behavior:
     *   - Constructs and Initializes a new repository with the specified name and no commits.
     * Parameters:
     *   - name: the name of the repository (must be non-null and non-empty)
     * Exceptions:
     *   - Throws IllegalArgumentException if name is null or empty.
     */
    public Repository(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.name = name;
        head = null;
    }

    /**
     * Behavior:
     *   - Returns the id of the current head commit in the repository.
     * Returns:
     *   - String: the id of the head commit, or null if there are no commits.
     */
    public String getRepoHead() {
        if (head == null) {
            return null;
        }
        return head.id;
    }

    /**
     * Behavior:
     *   - Returns the id of the current head commit in the repository.
     *   - Provides a summary of the repository, including its name and the current head commit.
     *   - If it is empty what it does is lets the user see the name of the repository
     *     know that it has no commits.
     * Returns:
     *   - String: a description of the repository and its current state.
     */
    public String toString() {
        if (head == null) {
            return name + " - No commits";
        } 
        return name + " - Current head: " + head.toString();
    }

    /**
     * Behavior:
     *   - Checks if a commit with the given id exists in the repository.
     *   - Searches the commit history for a commit with the specified id.
     * Parameters:
     *   - targetId: the id of the commit to search for (must be non-null)
     * Returns:
     *   - boolean: true if a commit with the given id exists in the repository, false otherwise.
     * Exceptions:
     *   - Throws IllegalArgumentException if targetId is null.
     */
    public boolean contains(String targetId) {
        if (targetId == null) {
            throw new IllegalArgumentException("Target ID is null.");
        }

        Commit currentCommit = head;
        while (currentCommit != null) {
            if (currentCommit.id.equals(targetId)) {
                return true;
            }
            currentCommit = currentCommit.past;
        }
        return false;
    }

    /**
     * Behavior:
     *   - Returns a string containing up to n most recent commits in the repository.
     * Parameters:
     *   - n: the maximum number of commits to include (must be positive or greater than 0).
     * Returns:
     *   - String: a string listing up to n most recent commits, or an empty string if there 
     * are none.
     * Exceptions:
     *   - Throws IllegalArgumentException if n less than or equal to 0.
     */
    public String getHistory(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException();
        } else if  (head == null) {
            return "";
        }
        
        String commitHistory = "";
        Commit currentCommit = head;
        int numOfCommits = 0;

        while (currentCommit != null && numOfCommits < n) {
            commitHistory += (currentCommit.toString() + "\n");
            currentCommit = currentCommit.past;
            numOfCommits++;
        }

        return commitHistory;
    }

    /**
     * Behavior:
     *   - Creates a new commit with the given message and adds it to the repository if it 
     * is not null.
     * Parameters:
     *   - message: the commit message (must be non-null)
     * Returns:
     *   - String: the id of the new head commit.
     * Exceptions:
     *   - Throws IllegalArgumentException if message is null.
     */
    public String commit(String message) {
        if (message == null) {
            throw new IllegalArgumentException("Message is null!");
        }
        Commit newCommit = new Commit(message, head);
        head = newCommit;
        return head.id;
    }

    /**
     * Behavior:
     *   - Removes the commit with the given id, if it exists, from the commit history while 
     * keeping the other commits connected in order.
     * Parameters:
     *   - targetId: the id of the commit to remove (must be non-null)
     * Returns:
     *   - boolean: true if the commit was found and removed, false otherwise.
     * Exceptions:
     *   - Throws IllegalArgumentException if targetId is null.
     */
    public boolean drop(String targetId) {
        if (targetId == null) {
            throw new IllegalArgumentException();
        } 
        
        if (head == null)  {
            return false;
        } 
        
        if (head.id.equals(targetId)) {
            head = head.past;
            return true;
        }

        Commit currentCommit = head;

        while (currentCommit.past != null) {
            if (currentCommit.past.id.equals(targetId)) {
                currentCommit.past = currentCommit.past.past;
                return true;
            }
            currentCommit = currentCommit.past;
        }
        return false;
    }

    /**
     * Behavior:
     *   - Counts the total number of commits in the repository and returns that number, otherwise
     * returns 0 if the head is null.
     * Returns:
     *   - int: Returns the number of commits currently in the repository.
     */
    public int getRepoSize() {
        if (head == null) {
            return 0;
        }

        Commit currentCommit = head;
        int size = 0;
        while (currentCommit != null) { 
            size++;
            currentCommit = currentCommit.past;
        }
        return size;
    } 

    /**
     * Behavior:
     *   - Synchronizes this repository with another repository.
     *   - Merges the commit history from the other repository into this one, based on commit
     * timestamps but the other must be non-null. If this repository head is null then this head
     * will be connected to the other since the other is not null. After synchronization, the 
     * other repository will have no commits.
     * Parameters:
     *   - other: the repository to synchronize with the current repository.
     * Exceptions:
     *   - Throws IllegalArgumentException if other is null.
     */
    public void synchronize(Repository other) {
        if (other == null) {
            throw new IllegalArgumentException();
        }

        if (other.head != null) {
            if (head == null) {
                head = other.head;
            } else {
                Commit validHeadCommit = null;
                Commit currentCommit = null;

                while (head != null && other.head != null) {
                    Commit temp;
                    if (head.timeStamp >= other.head.timeStamp) {
                        temp = head;
                        head = head.past;
                    } else {
                        temp = other.head;
                        other.head = other.head.past;
                    }

                    if (validHeadCommit == null) {
                        validHeadCommit = temp;
                        currentCommit = temp;
                    } else {
                        currentCommit.past = temp;
                        currentCommit = temp;
                    }
                }

                if (head != null) {
                    currentCommit.past = head;
                } else {
                    currentCommit.past = other.head;
                }
                
                head = validHeadCommit;
            }
        }
        other.head = null;
    }

    /**
     * DO NOT MODIFY
     * A class that represents a single commit in the repository.
     * Commits are characterized by an identifier, a commit message,
     * and the time that the commit was made. A commit also stores
     * a reference to the immediately previous commit if it exists.
     *
     * Staff Note: You may notice that the comments in this 
     * class openly mention the fields of the class. This is fine 
     * because the fields of the Commit class are public. In general, 
     * be careful about revealing implementation details!
     */
    public static class Commit {

        private static int currentCommitID;

        /**
         * The time, in milliseconds, at which this commit was created.
         */
        public final long timeStamp;

        /**
         * A unique identifier for this commit.
         */
        public final String id;

        /**
         * A message describing the changes made in this commit.
         */
        public final String message;

        /**
         * A reference to the previous commit, if it exists. Otherwise, null.
         */
        public Commit past;

        /**
         * Constructs a commit object. The unique identifier and timestamp
         * are automatically generated.
         * @param message A message describing the changes made in this commit. Should be non-null.
         * @param past A reference to the commit made immediately before this
         *             commit.
         */
        public Commit(String message, Commit past) {
            this.id = "" + currentCommitID++;
            this.message = message;
            this.timeStamp = System.currentTimeMillis();
            this.past = past;
        }

        /**
         * Constructs a commit object with no previous commit. The unique
         * identifier and timestamp are automatically generated.
         * @param message A message describing the changes made in this commit. Should be non-null.
         */
        public Commit(String message) {
            this(message, null);
        }

        /**
         * Returns a string representation of this commit. The string
         * representation consists of this commit's unique identifier,
         * timestamp, and message, in the following form:
         *      "[identifier] at [timestamp]: [message]"
         * @return The string representation of this collection.
         */
        @Override
        public String toString() {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
            Date date = new Date(timeStamp);

            return id + " at " + formatter.format(date) + ": " + message;
        }

        /**
        * Resets the IDs of the commit nodes such that they reset to 0.
        * Primarily for testing purposes.
        */
        public static void resetIds() {
            Commit.currentCommitID = 0;
        }
    }
}
