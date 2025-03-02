package ir.msdehghan.plugins.ansible.model.ansible.role;

import com.intellij.psi.PsiReference;
import ir.msdehghan.plugins.ansible.model.yml.YamlTypes;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlMappingType;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlType;
import ir.msdehghan.plugins.ansible.model.yml.type.api.ReferenceProvider;
import ir.msdehghan.plugins.ansible.reference.RoleNameReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLScalar;

import java.util.Collection;

import static ir.msdehghan.plugins.ansible.model.ansible.AnsibleFields.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;

public class Role extends YamlMappingType implements ReferenceProvider {
    public static final Role TYPE = new Role();
    private static final int FIELDS_SIZE = 1;

    protected Role() {
        super("Sequence[Role/String]", FIELDS_SIZE + getBaseGroup().size() + getConditionalGroup().size()
                + getTaggableGroup().size() + getConditionalGroup().size() + getDelegateGroup().size());

        getBaseGroup().forEach(this::addField);
        getConditionalGroup().forEach(this::addField);
        getTaggableGroup().forEach(this::addField);
        getDelegateGroup().forEach(this::addField);
        getCollectionsGroup().forEach(this::addField);

        addField("role")
                .setType(RoleName.TYPE)
                .setDescription("The name of the role to include.")
                .setRequired();
    }

    @Override
    public @NotNull Collection<PsiReference> getReferences(YAMLScalar scalar) {
        // We should only return reference on single scalar sequence item.
        if (scalar.getParent() instanceof YAMLMapping) {
            return emptyList();
        }
        return singleton(new RoleNameReference(scalar));
    }

    static class RoleName extends YamlType implements ReferenceProvider {
        public static final RoleName TYPE = new RoleName();

        private RoleName() {
            super(YamlTypes.STRING.getName());
        }

        @Override
        public @NotNull Collection<PsiReference> getReferences(YAMLScalar scalar) {
            return singleton(new RoleNameReference(scalar));
        }
    }
}
