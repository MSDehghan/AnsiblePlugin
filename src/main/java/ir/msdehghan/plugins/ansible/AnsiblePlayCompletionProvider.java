package ir.msdehghan.plugins.ansible;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import ir.msdehghan.plugins.ansible.model.yml.YamlModelProcessor.ElementSchemaInfo;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlType;
import ir.msdehghan.plugins.ansible.model.yml.type.api.MappingType;
import ir.msdehghan.plugins.ansible.model.yml.type.api.ValueProvider;
import ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLMapping;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static ir.msdehghan.plugins.ansible.AnsibleModels.MODEL_PROCESSOR;

public class AnsiblePlayCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        final PsiElement element = parameters.getPosition();

        final ElementSchemaInfo schemaPosition = MODEL_PROCESSOR.locate(element);

        if (schemaPosition == null || schemaPosition.getType() == null) return;
        final YamlType type = schemaPosition.getType();

        if (type instanceof ValueProvider) {
            result.addAllElements(((ValueProvider) type).getValueLookups());
        }
        if (type instanceof MappingType) {
            Set<String> siblings = getMappingSiblings(element);
            ((MappingType) type).getFields().stream().unordered()
                    .filter(f -> !siblings.contains(f.getName()))
                    .map(YamlField::getLookupElement)
                    .forEach(result::addElement);
        }
    }

    private Set<String> getMappingSiblings(PsiElement element) {
        PsiElement parent = element.getParent();
        if (parent.getParent() instanceof YAMLMapping mapping) {
            return mapping.getKeyValues().stream().map(k -> k.getKeyText().trim()).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

}
