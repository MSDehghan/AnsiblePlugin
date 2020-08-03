package ir.msdehghan.plugins.ansible;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import ir.msdehghan.plugins.ansible.model.ansible.AnsiblePlay;
import ir.msdehghan.plugins.ansible.model.yml.YamlField;
import ir.msdehghan.plugins.ansible.model.yml.YamlModelProcessor;
import ir.msdehghan.plugins.ansible.model.yml.type.api.ValueProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AnsiblePlayCompletionProvider extends CompletionProvider<CompletionParameters> {
    private static final YamlModelProcessor model = new YamlModelProcessor(AnsibleModels.ROOT_PLAY_FIELD);

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        PsiElement element = parameters.getPosition();

        System.out.println(model.locate(element));

        if (!AnsibleUtil.isPlaybookField(element)) return;
        List<YamlField> fields = AnsibleModels.PLAY.getFields();

        if (AnsibleUtil.isKey(element)) {
            Set<String> siblings = getMappingSiblings(element);

            for (YamlField field : fields) {
                if (siblings.contains(field.getName())) continue;
                result.addElement(field.getLookupElement());
            }

        } else if (AnsibleUtil.isScalarValue(element)) {
            String key = ((YAMLKeyValue) element.getParent().getParent()).getKeyText();

            for (YamlField f : fields) {
                if (f.getName().equals(key) && f.getDefaultType() instanceof ValueProvider) {
                    result.addAllElements(((ValueProvider) f.getDefaultType()).getValueLookups());
                    return;
                }
            }

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
