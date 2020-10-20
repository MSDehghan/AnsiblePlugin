package ir.msdehghan.plugins.ansible;

import com.intellij.icons.AllIcons;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import ir.msdehghan.plugins.ansible.model.yml.YamlTypes;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlType;
import ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.*;

import javax.swing.*;

public class AnsibleUtil {
    private AnsibleUtil() {
        // This class is Utility class.
    }

    public static boolean isInAnsibleFile(PsiElement element) {
        if (element == null || !(element.getContainingFile() instanceof YAMLFile)) return false;
        YAMLValue topLevelValue = ((YAMLFile) element.getContainingFile()).getDocuments().get(0).getTopLevelValue();
        return topLevelValue instanceof YAMLSequence;
    }

    public static boolean isRoleDocument(YAMLDocument document) {
        PsiDirectory directory = document.getContainingFile().getOriginalFile().getContainingDirectory();
        if (directory == null) return false;
        String name = directory.getName();
        return name.equals("tasks") || name.equals("handlers");
    }

    public static void appendSection(@NotNull String title, @Nullable String doc, @NotNull StringBuilder sb) {
        if (doc == null || doc.isEmpty()) return;
        sb.append(DocumentationMarkup.SECTION_HEADER_START)
                .append(title)
                .append(DocumentationMarkup.SECTION_SEPARATOR)
                .append(doc)
                .append(DocumentationMarkup.SECTION_END);
    }

    public static Icon getIcon(@NotNull YamlField.Relation relation) {
        return relation == YamlField.Relation.SEQUENCE ? AllIcons.Json.Array : AllIcons.Json.Object;
    }

    public static String getTypeText(@NotNull YamlField.Relation relation, @NotNull YamlType type) {
        if (relation == YamlField.Relation.SCALAR) {
            return type.getName();
        } else {
            return type == YamlTypes.ANY ? relation.toString() : relation.toString() + '[' + type.getName() + ']';
        }
    }
}
