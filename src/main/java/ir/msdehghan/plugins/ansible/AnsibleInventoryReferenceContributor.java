package ir.msdehghan.plugins.ansible;

import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import com.intellij.xml.util.XmlEnumeratedValueReferenceProvider;
import ir.msdehghan.plugins.ansible.model.yml.YamlModelProcessor;
import ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLScalar;

import static ir.msdehghan.plugins.ansible.AnsibleModels.PLAY_MODEL_PROCESSOR;

public class AnsibleInventoryReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(YAMLScalar.class).with(new PatternCondition<YAMLScalar>("Ansible Playbook Detect") {
            @Override
            public boolean accepts(@NotNull YAMLScalar yamlScalar, ProcessingContext context) {
                if (!AnsibleUtil.isInPlayBook(yamlScalar)) return false;
                YamlModelProcessor.ElementSchemaInfo schemaInfo = PLAY_MODEL_PROCESSOR.locate(yamlScalar);
                return schemaInfo != null && schemaInfo.getRelation() == YamlField.Relation.Scalar
                        && schemaInfo.getType() != null;
            }
        }), new AnsibleReferenceProvider());
    }
}
