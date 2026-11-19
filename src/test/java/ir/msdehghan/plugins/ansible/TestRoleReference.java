package ir.msdehghan.plugins.ansible;

import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPolyVariantReference;
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

    public void testRoleNameResolveWithJinjaAndRegexMetacharacters() {
        myFixture.copyDirectoryToProject("/", "/");

        String[] roleValues = {"{", "{{", "{{role_name}}", "[", ".", ".+", "+", "a(b", "a\\b", "$role", ""};
        for (String roleValue : roleValues) {
            myFixture.configureByText("jinja-play.yml", """
                    - hosts: localhost
                      roles:
                        - "<caret>%s"
                    """.formatted(roleValue.replace("\\", "\\\\")));

            PsiReference reference = myFixture.getReferenceAtCaretPositionWithAssertion();
            assertNull("Expected unresolved role for: " + roleValue, reference.resolve());
            if (reference instanceof PsiPolyVariantReference poly) {
                assertEquals("Expected empty multiResolve for: " + roleValue, 0, poly.multiResolve(false).length);
            }
            myFixture.doHighlighting();
        }
    }

    @Override
    protected boolean shouldContainTempFiles() {
        return false;
    }
}
