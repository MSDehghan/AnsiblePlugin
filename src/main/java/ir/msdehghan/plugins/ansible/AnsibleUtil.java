package ir.msdehghan.plugins.ansible;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.*;

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
}
