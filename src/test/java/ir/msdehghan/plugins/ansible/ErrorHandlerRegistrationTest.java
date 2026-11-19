package ir.msdehghan.plugins.ansible;

import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.List;

public class ErrorHandlerRegistrationTest extends BasePlatformTestCase {

    private static final String PLUGIN_ID = "ir.msdehghan.plugins.ansible";
    private static final String MARKETPLACE_SUBMITTER = "com.intellij.diagnostic.JetBrainsMarketplaceErrorReportSubmitter";
    private static final ExtensionPointName<ErrorReportSubmitter> ERROR_HANDLER_EP =
            ExtensionPointName.create("com.intellij.errorHandler");

    public void testRegistersMarketplaceErrorReportSubmitter() {
        List<ErrorReportSubmitter> pluginHandlers = ERROR_HANDLER_EP.getExtensionList().stream()
                .filter(handler -> {
                    PluginDescriptor descriptor = handler.getPluginDescriptor();
                    return descriptor != null && PLUGIN_ID.equals(descriptor.getPluginId().getIdString());
                })
                .toList();

        assertEquals(1, pluginHandlers.size());
        assertEquals(MARKETPLACE_SUBMITTER, pluginHandlers.getFirst().getClass().getName());
    }
}
