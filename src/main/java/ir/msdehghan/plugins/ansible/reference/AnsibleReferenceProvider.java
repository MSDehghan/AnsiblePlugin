package ir.msdehghan.plugins.ansible.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import ir.msdehghan.plugins.ansible.model.yml.YamlModelProcessor.ElementSchemaInfo;
import ir.msdehghan.plugins.ansible.model.yml.type.api.ReferenceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLScalar;

import static ir.msdehghan.plugins.ansible.AnsibleModels.MODEL_PROCESSOR;

public class AnsibleReferenceProvider extends PsiReferenceProvider {
    @Override
    public @NotNull PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        if (!(element instanceof YAMLScalar)) throw new IllegalArgumentException("Element must be a YamlScalar");
        ElementSchemaInfo schemaInfo = MODEL_PROCESSOR.locate(element);
        if (schemaInfo == null || !(schemaInfo.getType() instanceof ReferenceProvider)) {
            throw new IllegalStateException("Schema of reference or it's type can't be null");
        }

        // TODO: clean-up
        //   TODO: add tasks documentation from site

        return ((ReferenceProvider) schemaInfo.getType()).getReferences(((YAMLScalar) element)).toArray(PsiReference.EMPTY_ARRAY);
    }
}
