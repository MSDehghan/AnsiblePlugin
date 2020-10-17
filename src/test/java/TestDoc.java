import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;
import java.nio.file.Paths;

public class TestDoc extends BasePlatformTestCase {
    private static final String TEST_PATH;

    static {
        try {
            TEST_PATH = Paths.get(TestPlayCompletion.class.getResource("/doc").toURI()).toAbsolutePath()
                    .toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getTestDataPath() {
        return TEST_PATH;
    }

    public void testModuleGenerateDoc() {
        PsiFile file = configureFileByTestName();
        String doc = getDocInCaret(file);
        assertTrue(doc.contains("Module digital_ocean"));
        assertTrue(doc.contains("Deprecated"));
        assertTrue(doc.contains("Removed in"));
        assertTrue(doc.contains("Added in"));
    }

    public void testModuleLookupDoc() {
        PsiFile file = configureFileByTestName();
        myFixture.completeBasic();
        String doc = getDocInCaret(file);
        assertTrue(doc.contains("Module copy"));
        assertTrue(doc.contains("Notes"));
        assertTrue(doc.contains("The 'copy' module copies a file from the local or remote machine"));
        assertTrue(doc.contains("Added in"));
    }

    public void testModuleOptionGenerateDoc() {
        PsiFile file = configureFileByTestName();
        String doc = getDocInCaret(file);
        assertTrue(doc.contains("Required"));
        assertTrue(doc.contains("Remote absolute path where the file should be copied to"));
    }

    public void testPlayFieldGenerateDoc() {
        PsiFile file = configureFileByTestName();
        String doc = getDocInCaret(file);
        assertTrue(doc.contains("This field is required"));
        assertTrue(doc.contains("A list of groups, hosts or host pattern that translates into a list of hosts that are" +
                " the play's target."));
    }

    public void testWrongPlace() {
        PsiFile file = configureFileByTestName();
        String doc = getDocInCaret(file);
        assertNull(doc);
    }

    private String getDocInCaret(PsiFile file) {
        DocumentationManager documentationManager = DocumentationManager.getInstance(file.getProject());
        PsiElement targetElement = documentationManager.findTargetElement(myFixture.getEditor(), file);
        PsiElement originalElement = myFixture.getElementAtCaret();
        return documentationManager.generateDocumentation(targetElement, originalElement, false);
    }

    private PsiFile configureFileByTestName() {
        return myFixture.configureByFile(getTestDataFile());
    }

    @NotNull
    private String getTestDataFile() {
        return getTestName(true) + ".yml";
    }
}
