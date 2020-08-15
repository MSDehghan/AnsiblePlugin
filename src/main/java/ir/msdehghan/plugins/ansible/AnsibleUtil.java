package ir.msdehghan.plugins.ansible;

import com.intellij.icons.AllIcons;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import ir.msdehghan.plugins.ansible.model.yml.YamlTypes;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlType;
import ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.*;

import javax.swing.*;

public class AnsibleUtil {
    private AnsibleUtil() {
        // This class is Utility class.
    }

    public static boolean isPlaybookField(PsiElement element) {
        if (element == null || !(element.getContainingFile() instanceof YAMLFile)) return false;
        YAMLValue topLevelValue = ((YAMLFile) element.getContainingFile()).getDocuments().get(0).getTopLevelValue();
        if (!(topLevelValue instanceof YAMLSequence)) return false;

        // This should be YAMLPlainText(inserted key) or YAMLKeyValue (updated key)
        PsiElement parent = element.getParent();
        if (!(parent instanceof YAMLScalar) && !(parent instanceof YAMLKeyValue)) return false;

        YAMLCompoundValue compoundParent = PsiTreeUtil.getParentOfType(element, YAMLCompoundValue.class);

        if (compoundParent instanceof YAMLSequence) {
            return compoundParent == topLevelValue;
        } else if (compoundParent instanceof YAMLMapping) {
            return PsiTreeUtil.getParentOfType(compoundParent, YAMLCompoundValue.class) == topLevelValue;
        }
        return false;
    }

    public static boolean isKey(PsiElement element) {
        return element.getNode().getElementType() == YAMLTokenTypes.SCALAR_KEY ||
                (element.getParent().getParent() instanceof YAMLMapping);
    }

    public static boolean isScalarValue(PsiElement element) {
        PsiElement parent = element.getParent();
        if (!(parent instanceof YAMLScalar) || !(parent.getParent() instanceof YAMLKeyValue)) return false;
        PsiElement beforeSibling = PsiTreeUtil.skipWhitespacesBackward(parent);
        if (beforeSibling == null || beforeSibling.getNode().getElementType() != YAMLTokenTypes.COLON) return false;
        beforeSibling = PsiTreeUtil.skipWhitespacesBackward(beforeSibling);
        return beforeSibling != null && beforeSibling.getNode().getElementType() == YAMLTokenTypes.SCALAR_KEY;
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
        return relation == YamlField.Relation.Sequence ? AllIcons.Json.Array : AllIcons.Json.Object;
    }

    public static String getTypeText(@NotNull YamlField.Relation relation, @NotNull YamlType type) {
        if (relation == YamlField.Relation.Scalar) {
            return type.getName();
        } else {
            return type == YamlTypes.ANY ? relation.name() : relation.name() + '[' + type.getName() + ']';
        }
    }
}
