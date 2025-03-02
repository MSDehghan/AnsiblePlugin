package ir.msdehghan.plugins.ansible;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.yaml.YAMLLanguage;

import java.net.URISyntaxException;
import java.nio.file.Paths;

public class JsonSchemaExcludeTest extends BasePlatformTestCase {

    private static final String TEST_PATH;

    static {
        try {
            TEST_PATH = Paths.get(TestCompletion.class.getResource("/testData/jsonSchemaExclude").toURI()).toAbsolutePath()
                    .toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getTestDataPath() {
        return TEST_PATH;
    }


    public void testIsExcluded() {
        final JsonSchemaExclude schemaExclude = new JsonSchemaExclude();
        final VirtualFile virtualFile = myFixture.copyDirectoryToProject("/", "/");

        final VirtualFile playbookFile = virtualFile.findChild("playbook.yml");
        assertTrue(schemaExclude.isExcluded(playbookFile)); // First time it's not cached.
        assertTrue(schemaExclude.isExcluded(playbookFile)); // This time it's cached

        final VirtualFile notAnsibleFile = virtualFile.findChild("notAnsibleFile.yml");
        assertFalse(schemaExclude.isExcluded(notAnsibleFile)); // First time it's not cached.
        assertFalse(schemaExclude.isExcluded(notAnsibleFile)); // This time it's cached
    }

    public void testIsExcludedLightVirtualFile() {
        final JsonSchemaExclude schemaExclude = new JsonSchemaExclude();
        final VirtualFile play = new LightVirtualFile("play.yml", YAMLLanguage.INSTANCE, "- hosts: a");

        assertFalse(schemaExclude.isExcluded(play)); // It's not cached any-where so it must be false.
    }
}