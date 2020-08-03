package ir.msdehghan.plugins.ansible;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import ir.msdehghan.plugins.ansible.model.yml.YamlModelProcessor;
import ir.msdehghan.plugins.ansible.model.yml.YamlModelProcessor.ElementSchemaInfo;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlType;
import ir.msdehghan.plugins.ansible.model.yml.type.api.ValueProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLMapping;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class AnsiblePlayCompletionProvider extends CompletionProvider<CompletionParameters> {
    private static final YamlModelProcessor model = new YamlModelProcessor(AnsibleModels.ROOT_PLAY_FIELD);

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        PsiElement element = parameters.getPosition();

        final ElementSchemaInfo schemaPosition = model.locate(element);
        if (schemaPosition == null || schemaPosition.getType() == null) return;
        final YamlType type = schemaPosition.getType();

        switch (schemaPosition.getRelation()) {
            case Scalar:
                if (type instanceof ValueProvider) {
                    result.addAllElements(((ValueProvider) type).getValueLookups());
                }
                break;
            case Mapping:
            case Sequence:
        }


    }

    private Set<String> getMappingSiblings(PsiElement element) {
        PsiElement parent = element.getParent();
        if (parent.getParent() instanceof YAMLMapping) {
            YAMLMapping mapping = (YAMLMapping) parent.getParent();
            return mapping.getKeyValues().stream().map(k -> k.getKeyText().trim()).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

}
