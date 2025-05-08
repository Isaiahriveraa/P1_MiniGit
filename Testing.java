import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
/*
 *   This class contains JUnit tests for the Repository class, which models a simple
 *   version control system. The tests cover commit history, dropping commits, and
 *   repository synchronization, as well as helper methods for committing and verifying
 *   commit history. 
 */
public class Testing {
    private Repository repo1;
    private Repository repo2;
    private Repository repo3;

    // Occurs before each of the individual test cases
    // (creates new repos and resets commit ids)
    @BeforeEach
    public void setUp() {
        repo1 = new Repository("repo1");
        repo2 = new Repository("repo2");
        repo3 = new Repository("repo3");
        Repository.Commit.resetIds();
    }

    @Test
    @Timeout(1)
    @DisplayName("getHistory")
    public void getHistory() throws InterruptedException {
        String[] commitMessages = new String[3];
        commitMessages[0] = ("Start Commit");
        commitMessages[1] = ("Second Commit");
        commitMessages[2] = ("Thrid Commit");
        commitAll(repo1, commitMessages);
        testHistory(repo1, 1, commitMessages);
    }

    @Test
    @Timeout(1)
    @DisplayName("drop() head case & commit after a drop()")
    public void testHeadDrop() throws InterruptedException {
        commitAll(repo1, new String[]{"Repo 1"}); //id is 0
        commitAll(repo2, new String[]{"Repo 2"}); //id is 1
        commitAll(repo3, new String[]{"Repo 3"}); //id is 2
        assertTrue(repo1.drop("0"));
        assertTrue(repo2.drop("1"));
        assertTrue(repo3.drop("2"));

        assertEquals(repo2.getRepoSize(), 0);
        assertEquals(repo3.getRepoHead(), null);
        
        //now the repo3 head is "3"
        repo3.commit("3");
        assertEquals(repo3.getRepoHead(), "3");
        assertEquals(repo3.getRepoSize(), 1);

    }

    @Test
    @Timeout(1)
    @DisplayName("drop() (empty case)")
    public void testDropEmpty() {
        assertFalse(repo1.drop("123"));
    }

    @Test
    @Timeout(1)
    @DisplayName("synchronize() (one: [1, 2, 3, 4], two: [5, 6, 7] -> two: [8]," +
        " three: [9, 10]) -> three: null")
    public void testSynchronizeOne() throws InterruptedException {
        // Initialize commit messages
        commitAll(repo1, new String[]{"One", "Two", "Three", "Four"});
        commitAll(repo2, new String[]{"Five", "Six", "Seven"});

        commitAll(repo3, new String[]{"Nine", "Ten"});

        assertEquals(4, repo1.getRepoSize());
        assertEquals(3, repo2.getRepoSize());
        repo1.synchronize(repo2);
        repo2.commit("Eight");
        
        //repo 2 is 1 because it was emptied out before the eight message commit
        assertEquals(1, repo2.getRepoSize());
        // Synchronize repo2 into repo1
        assertEquals(7, repo1.getRepoSize());
        repo1.synchronize(repo3);
        assertEquals(0, repo3.getRepoSize());
        assertEquals(9, repo1.getRepoSize());
        // Make sure the history of repo1 is correctly synchronized
        testHistory(repo1, 9, new String[]{"One", "Two", "Three", "Four", "Five", "Six", "Seven",
             "Nine", "Ten"});
    }

        /////////////////////////////////////////////////////////////////////////////////
    // PROVIDED HELPER METHODS (You don't have to use these if you don't want to!) //
    /////////////////////////////////////////////////////////////////////////////////

    // Commits all of the provided messages into the provided repo, making sure timestamps
    // are correctly sequential (no ties). If used, make sure to include
    //      'throws InterruptedException'
    // much like we do with 'throws FileNotFoundException'. 
    // repo and messages should be non-null.
    // Example useage:
    //
    // repo1:
    //      head -> null
    // To commit the messages "one", "two", "three", "four"
    //      commitAll(repo1, new String[]{"one", "two", "three", "four"})
    // This results in the following after picture
    // repo1:
    //      head -> "four" -> "three" -> "two" -> "one" -> null
    //
    // YOU DO NOT NEED TO UNDERSTAND HOW THIS METHOD WORKS TO USE IT! (this is why documentation
    // is important!)
    public void commitAll(Repository repo, String[] messages) throws InterruptedException {
        // Commit all of the provided messages
        for (String message : messages) {
            int size = repo.getRepoSize();
            repo.commit(message);
            
            // Make sure exactly one commit was added to the repo
            assertEquals(size + 1, repo.getRepoSize(),
                         String.format("Size not correctly updated after commiting message [%s]",
                                       message));

            // Sleep to guarantee that all commits have different time stamps
            Thread.sleep(2);
        }
    }

    // Makes sure the given repositories history is correct up to 'n' commits, checking against
    // all commits made in order. repo and allCommits should be non-null.
    // Example useage:
    //
    // repo1:
    //      head -> "four" -> "three" -> "two" -> "one" -> null
    //      (Commits made in the order ["one", "two", "three", "four"])
    // To test the getHistory() method up to n=3 commits this can be done with:
    //      testHistory(repo1, 3, new String[]{"one", "two", "three", "four"})
    // Similarly, to test getHistory() up to n=4 commits you'd use:
    //      testHistory(repo1, 4, new String[]{"one", "two", "three", "four"})
    //
    // YOU DO NOT NEED TO UNDERSTAND HOW THIS METHOD WORKS TO USE IT! (this is why documentation
    // is important!)
    public void testHistory(Repository repo, int n, String[] allCommits) {
        int totalCommits = repo.getRepoSize();
        assertTrue(n <= totalCommits,
                   String.format("Provided n [%d] too big. Only [%d] commits",
                                 n, totalCommits));
        
        String[] nCommits = repo.getHistory(n).split("\n");
        
        assertTrue(nCommits.length <= n,
                   String.format("getHistory(n) returned more than n [%d] commits", n));
        assertTrue(nCommits.length <= allCommits.length,
                   String.format("Not enough expected commits to check against. " +
                                 "Expected at least [%d]. Actual [%d]",
                                 n, allCommits.length));
        
        for (int i = 0; i < n; i++) {
            String commit = nCommits[i];

            // Old commit messages/ids are on the left and the more recent commit messages/ids are
            // on the right so need to traverse from right to left
            int backwardsIndex = totalCommits - 1 - i;
            String commitMessage = allCommits[backwardsIndex];

            assertTrue(commit.contains(commitMessage),
                       String.format("Commit [%s] doesn't contain expected message [%s]",
                                     commit, commitMessage));
            assertTrue(commit.contains("" + backwardsIndex),
                       String.format("Commit [%s] doesn't contain expected id [%d]",
                                     commit, backwardsIndex));
        }
    }
}
