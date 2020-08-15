package ir.msdehghan.plugins.ansible;

import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import com.intellij.xml.util.XmlEnumeratedValueReferenceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLScalar;

public class AnsibleInventoryReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(YAMLScalar.class).with(new PatternCondition<YAMLScalar>("Ansible Playbook Detect") {
            @Override
            public boolean accepts(@NotNull YAMLScalar yamlScalar, ProcessingContext context) {
                return AnsibleUtil.isInPlayBook(yamlScalar);
            }
        }), new AnsibleReferenceProvider());
    }
}
