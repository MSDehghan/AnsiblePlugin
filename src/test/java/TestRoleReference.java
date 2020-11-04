import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Objects;

public class TestRoleReference extends BasePlatformTestCase {
    private static final String TEST_PATH;

    static {
        try {
            TEST_PATH = Paths.get(TestRoleReference.class.getResource("/testData/roleReference").toURI()).toAbsolutePath()
                    .toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getTestDataPath() {
        return TEST_PATH;
    }

    public void testRoleNameCompletion() {
        VirtualFile root = myFixture.copyDirectoryToProject("/", "/");
        myFixture.configureFromExistingVirtualFile(Objects.requireNonNull(root.findFileByRelativePath("play.yml")));

        myFixture.completeBasic();
        assertNotNull("Lookup must be shown", LookupManager.getInstance(myFixture.getProject()).getActiveLookup());
        assertContainsElements(myFixture.getLookupElementStrings(), "apache", "hbase");

        myFixture.type("role: ");
        myFixture.completeBasic();
        assertNotNull("Lookup must be shown", LookupManager.getInstance(myFixture.getProject()).getActiveLookup());
        assertContainsElements(myFixture.getLookupElementStrings(), "apache", "hbase");
    }

    public void testRoleNameResolve() {
        VirtualFile root = myFixture.copyDirectoryToProject("/", "/");
        myFixture.configureFromExistingVirtualFile(Objects.requireNonNull(root.findFileByRelativePath("apache-play.yml")));

        PsiReference reference = myFixture.getReferenceAtCaretPositionWithAssertion();
        assertEquals("apache", reference.getCanonicalText());

        PsiDirectory roleDirectory = PsiManager.getInstance(myFixture.getProject())
                .findDirectory(root.findFileByRelativePath("roles/apache"));
        assertTrue(reference.isReferenceTo(roleDirectory));

        assertSame(reference.getElement(), reference.handleElementRename("rename"));
        assertSame(reference.getElement(), reference.bindToElement(roleDirectory.getParent()));
    }

    @Override
    protected boolean shouldContainTempFiles() {
        return false;
    }
}
