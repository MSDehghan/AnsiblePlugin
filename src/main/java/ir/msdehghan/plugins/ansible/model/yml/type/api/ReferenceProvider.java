package ir.msdehghan.plugins.ansible.model.yml.type.api;

import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLScalar;

import java.util.Collection;

public interface ReferenceProvider {
    @NotNull
    Collection<PsiReference> getReferences(YAMLScalar scalar);
}
