package ir.msdehghan.plugins.ansible;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;
import ir.msdehghan.plugins.ansible.model.yml.YamlModelProcessor;
import ir.msdehghan.plugins.ansible.model.yml.type.api.MappingType;
import ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.util.Optional;

import static ir.msdehghan.plugins.ansible.AnsibleModels.PLAY_MODEL_PROCESSOR;

public class AnsiblePlayDocProvider extends AbstractDocumentationProvider {
    @Override
    public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        if (element instanceof DocPsi) {
            return ((DocPsi) element).field.generateDoc();
        } else {
            if (originalElement != null && !originalElement.getNode().getElementType().equals(YAMLTokenTypes.SCALAR_KEY))
                return null;
            YamlModelProcessor.ElementSchemaInfo schemaInfo = PLAY_MODEL_PROCESSOR.locate(originalElement);
            if (schemaInfo == null || schemaInfo.getType() == null ||
                    !(schemaInfo.getType() instanceof MappingType) || !(element instanceof YAMLKeyValue)) {
                return null;
            }
            Optional<YamlField> field = ((MappingType) schemaInfo.getType()).getFieldByName(((YAMLKeyValue) element).getKeyText());
            return field.map(YamlField::generateDoc).orElse(null);
        }
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
