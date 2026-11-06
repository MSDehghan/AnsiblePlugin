package ir.msdehghan.plugins.ansible;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

public class TestCompletion extends BasePlatformTestCase {
    private static final String TEST_PATH;

    static {
        try {
            TEST_PATH = Paths.get(TestCompletion.class.getResource("/testData/completion").toURI()).toAbsolutePath()
                    .toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getTestDataPath() {
        return TEST_PATH;
    }

    public void testNotAnsibleFile() {
        configureFileByTestName();
        assertEquals(0, myFixture.completeBasic().length);
    }

    public void testRequiredHostsField() {
        configureFileByTestName();
        LookupElement[] elements = myFixture.completeBasic();
        myFixture.assertPreferredCompletionItems(0, "hosts");
    }

    public void testPlayNotDuplicateKeySuggestion() {
        List<String> completionVariants = myFixture.getCompletionVariants(getTestDataFile());
        assertNotEmpty(completionVariants);
        assertDoesntContain(completionVariants, "hosts", "name");
    }

    public void testPlayTaskSuggestion() {
        List<String> completionVariants = myFixture.getCompletionVariants(getTestDataFile());
        assertNotEmpty(completionVariants);
        assertContainsElements(completionVariants, "copy", "composer", "command");
        // Task keywords must exist too. Only prefix matches are guaranteed a place in a full module list.
        assertContainsElements(completionVariants, "collections", "connection");
    }

    public void testShortModuleNamesRankBeforeQualifiedNames() {
        List<String> completionVariants = myFixture.getCompletionVariants("playTaskSuggestion.yml");
        assertContainsElements(completionVariants, "composer", "community.general.composer");
        assertTrue(completionVariants.indexOf("composer") < completionVariants.indexOf("community.general.composer"));
    }

    public void testQualifiedModuleOptionSuggestion() {
        List<String> completionVariants = myFixture.getCompletionVariants(getTestDataFile());
        assertContainsElements(completionVariants, "dest", "backup");
    }

    public void testQualifiedFreeFormModule() {
        assertCompletesFilePaths("qualifiedFreeFormModule.yml");
    }

    public void testQualifiedImportPlaybook() {
        assertCompletesFilePaths("qualifiedImportPlaybook.yml");
    }

    private void assertCompletesFilePaths(String testDataFile) {
        List<String> completionVariants = myFixture.getCompletionVariants(testDataFile,
                "role/tasks/main.yml", "notAnsibleFile.yml");
        assertContainsElements(completionVariants, "role", "notAnsibleFile.yml");
    }

    public void testTaskOptionSuggestion() {
        List<String> completionVariants = myFixture.getCompletionVariants(getTestDataFile());
        assertNotEmpty(completionVariants);
        assertContainsElements(completionVariants, "dest", "backup");
    }

    public void testBooleanValueSuggestion() {
        List<String> completionVariants = myFixture.getCompletionVariants(getTestDataFile());
        assertNotEmpty(completionVariants);
        assertSameElements(completionVariants, "true", "false");
    }

    public void testEnumValueSuggestion() {
        List<String> completionVariants = myFixture.getCompletionVariants(getTestDataFile());
        assertNotEmpty(completionVariants);
        assertSameElements(completionVariants, "reloaded", "restarted", "started", "stopped");
    }

    public void testFilePathModuleField() {
        List<String> completionVariants = myFixture.getCompletionVariants(getTestDataFile(),
                "role/tasks/main.yml", "notAnsibleFile.yml");
        assertNotNull("Lookup must be shown", LookupManager.getInstance(myFixture.getProject())
                .getActiveLookup());
        assertNotEmpty(completionVariants);
        assertContainsElements(completionVariants, "role", "notAnsibleFile.yml");

        myFixture.type("/");
        myFixture.completeBasic();
        assertNull("Lookup must not be shown", LookupManager.getInstance(myFixture.getProject())
                .getActiveLookup());
    }

    public void testFilePathCompletionFreeFormModule() {
        List<String> completionVariants = myFixture.getCompletionVariants(getTestDataFile(),
                "role/tasks/main.yml", "notAnsibleFile.yml");
        assertNotNull("Lookup must be shown", LookupManager.getInstance(myFixture.getProject()).getActiveLookup());
        assertNotEmpty(completionVariants);
        assertContainsElements(completionVariants, "role", "notAnsibleFile.yml");
    }

    public void testEmptyFile() {
        myFixture.configureByText("empty.yml", "<caret>");
        assertEquals(0, myFixture.completeBasic().length);
    }

    public void testWrongPlace() {
        configureFileByTestName();
        assertEquals(0, myFixture.completeBasic().length);
    }

    public void testCompletionInRole() {
        List<String> completionVariants = myFixture.getCompletionVariants("role/tasks/main.yml");
        assertNotEmpty(completionVariants);
        assertContainsElements(completionVariants, "name", "state");
    }

    private void configureFileByTestName() {
        myFixture.configureByFile(getTestDataFile());
    }

    @NotNull
    private String getTestDataFile() {
        return getTestName(true) + ".yml";
    }

    @Override
    protected boolean shouldContainTempFiles() {
        return false;
    }
}
