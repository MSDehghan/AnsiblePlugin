package ir.msdehghan.plugins.ansible;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import ir.msdehghan.plugins.ansible.model.yml.YamlField;
import ir.msdehghan.plugins.ansible.model.yml.YamlModelProcessor;
import ir.msdehghan.plugins.ansible.model.yml.YamlModelProcessor.ElementSchemaInfo;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlMappingType;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlType;
import ir.msdehghan.plugins.ansible.model.yml.type.api.ValueProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLMapping;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static ir.msdehghan.plugins.ansible.model.yml.YamlField.Relation.Scalar;

public class AnsiblePlayCompletionProvider extends CompletionProvider<CompletionParameters> {
    private static final YamlModelProcessor model = new YamlModelProcessor(AnsibleModels.ROOT_PLAY_FIELD);

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        final PsiElement element = parameters.getPosition();

        final ElementSchemaInfo schemaPosition = model.locate(element);
        System.out.println("schemaPosition = " + schemaPosition);

        if (schemaPosition == null || schemaPosition.getType() == null) return;
        final YamlType type = schemaPosition.getType();

        if (type instanceof ValueProvider) {
            result.addAllElements(((ValueProvider) type).getValueLookups());
        }
        if (type instanceof YamlMappingType) {
            Set<String> siblings = getMappingSiblings(element);
            ((YamlMappingType) type).getFields().stream().unordered()
                    .filter(f -> !siblings.contains(f.getName()))
                    .map(YamlField::getLookupElement)
                    .forEach(result::addElement);
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
