package ir.msdehghan.plugins.ansible;

import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;
import ir.msdehghan.plugins.ansible.model.yml.YamlField;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.psi.YAMLKeyValue;

public class AnsiblePlayDocProvider implements DocumentationProvider {
    @Override
    public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        if (element instanceof DocPsi) {
            return ((DocPsi) element).field.generateDoc();
        } else if(element instanceof YAMLKeyValue) {
            if (!AnsibleUtil.isPlaybookField(originalElement)) return null;
            for (YamlField field : AnsibleModels.PLAY.getFields()) {
                if (field.getName().equals(((YAMLKeyValue) element).getKeyText().trim())) {
                    return field.generateDoc();
                }
            }
        }
        return null;
    }

    @Override
    public @Nullable PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element) {
        if (object instanceof YamlField) {
            return new DocPsi(psiManager, (YamlField) object);
        }
        return null;
    }

    private static class DocPsi extends LightElement {
        final YamlField field;

        protected DocPsi(PsiManager manager, YamlField field) {
            super(manager, YAMLLanguage.INSTANCE);
            this.field = field;
        }

        @Override
        public String toString() {
            return null;
        }
    }
}
