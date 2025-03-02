package ir.msdehghan.plugins.ansible.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import ir.msdehghan.plugins.ansible.model.yml.YamlModelProcessor.ElementSchemaInfo;
import ir.msdehghan.plugins.ansible.model.yml.type.api.ReferenceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLScalar;

import java.util.Objects;

import static ir.msdehghan.plugins.ansible.AnsibleModels.MODEL_PROCESSOR;

public class AnsibleReferenceProvider extends PsiReferenceProvider {
    @Override
    public @NotNull PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        validateIsInstanceOf(YAMLScalar.class, element);
        ElementSchemaInfo schemaInfo = MODEL_PROCESSOR.locate(element);
        Objects.requireNonNull(schemaInfo);
        validateIsInstanceOf(ReferenceProvider.class, schemaInfo.getType());

        return ((ReferenceProvider) schemaInfo.getType()).getReferences(((YAMLScalar) element)).toArray(PsiReference.EMPTY_ARRAY);
    }

    private static void validateIsInstanceOf(Class<?> type, Object obj) {
        if (!type.isInstance(obj)) {
            throw new IllegalArgumentException(String.format("Expected type: %s, actual: %s",
                    type.getName(), obj == null ? "null" : obj.getClass().getName()));
        }
    }
}
