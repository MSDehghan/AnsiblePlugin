import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.ui.DeferredIconImpl;
import com.intellij.util.IconUtil;
import ir.msdehghan.plugins.ansible.icon.Icons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class TestIcons extends BasePlatformTestCase {
    private static final String TEST_PATH;

    static {
        try {
            TEST_PATH = Paths.get(TestCompletion.class.getResource("/icons").toURI()).toAbsolutePath()
                    .toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getTestDataPath() {
        return TEST_PATH;
    }

    public void testPlaybookIcon() {
        PsiFile file = configureFileByTestName();
        Icon icon = IconUtil.getIcon(file.getVirtualFile(), Iconable.ICON_FLAG_READ_STATUS, file.getProject());
        if (icon instanceof DeferredIconImpl) {
            icon = ((DeferredIconImpl<?>) icon).retrieveIcon();
        }
        assertSame(Icons.ANSIBLE_FILE, icon);
    }

    private PsiFile configureFileByTestName() {
        return myFixture.configureByFile(getTestDataFile());
    }

    @NotNull
    private String getTestDataFile() {
        return getTestName(true) + ".yml";
    }
}
