import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

public class TestPlayCompletion extends BasePlatformTestCase {
    private static final String TEST_PATH;

    static {
        try {
            TEST_PATH = Paths.get(TestPlayCompletion.class.getResource("/completion").toURI()).toAbsolutePath()
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
        assertContainsElements(completionVariants, "copy", "composer");
        // Task other fields must exists too
        assertContainsElements(completionVariants, "loop_control", "command");
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

    public void testWrongPlace() {
        configureFileByTestName();
        assertEquals(0, myFixture.completeBasic().length);
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
