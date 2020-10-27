package ir.msdehghan.plugins.ansible.reference;

import com.intellij.patterns.PatternCondition;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import ir.msdehghan.plugins.ansible.AnsibleUtil;
import ir.msdehghan.plugins.ansible.model.yml.YamlModelProcessor.ElementSchemaInfo;
import ir.msdehghan.plugins.ansible.model.yml.type.api.ReferenceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLScalar;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static ir.msdehghan.plugins.ansible.AnsibleModels.MODEL_PROCESSOR;
import static ir.msdehghan.plugins.ansible.reference.AnsibleReferenceContributor.AnsibleFilePatternCondition.isInAnsibleFile;
import static ir.msdehghan.plugins.ansible.reference.AnsibleReferenceContributor.HasAnsibleReference.isReferenceProvider;

public class AnsibleReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(psiElement(YAMLScalar.class).with(isInAnsibleFile())
                        .with(isReferenceProvider()), new AnsibleReferenceProvider());
    }

    static class AnsibleFilePatternCondition extends PatternCondition<YAMLScalar> {
        public AnsibleFilePatternCondition() {
            super("AnsibleFileCheck");
        }

        public static AnsibleFilePatternCondition isInAnsibleFile() {
            return new AnsibleFilePatternCondition();
        }

        @Override
        public boolean accepts(@NotNull YAMLScalar yamlScalar, ProcessingContext context) {
            return AnsibleUtil.isInAnsibleFile(yamlScalar);
        }
    }

    static class HasAnsibleReference extends PatternCondition<YAMLScalar> {
        public HasAnsibleReference() {
            super("AnsibleReferenceCheck");
        }

        public static HasAnsibleReference isReferenceProvider() {
            return new HasAnsibleReference();
        }

        @Override
        public boolean accepts(@NotNull YAMLScalar yamlScalar, ProcessingContext context) {
            ElementSchemaInfo schemaInfo = MODEL_PROCESSOR.locate(yamlScalar);
            if (schemaInfo == null) return false;
            return schemaInfo.getType() instanceof ReferenceProvider;
        }
    }
}
