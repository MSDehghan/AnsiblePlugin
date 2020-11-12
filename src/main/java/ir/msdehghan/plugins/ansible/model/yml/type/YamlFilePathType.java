package ir.msdehghan.plugins.ansible.model.yml.type;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import ir.msdehghan.plugins.ansible.model.yml.type.api.ReferenceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLScalar;

import java.util.Arrays;
import java.util.Collection;

import static java.util.Collections.emptyList;

public class YamlFilePathType extends YamlType implements ReferenceProvider {
    private final boolean endingSlashAllowed;
    private final FileType[] fileTypes;

    public YamlFilePathType(String name, boolean endingSlashAllowed, @Nullable FileType[] fileTypes) {
        super(name);
        this.endingSlashAllowed = endingSlashAllowed;
        this.fileTypes = fileTypes;
    }

    @Override
    public @NotNull Collection<PsiReference> getReferences(YAMLScalar scalar) {
        final TextRange range = ElementManipulators.getValueTextRange(scalar);
        int offset = range.getStartOffset();
        String text = range.substring(scalar.getText());
        if (text.startsWith("/")) return emptyList();
        final FileReferenceSet fileReferenceSet = new FileReferenceSet(text, scalar, offset, null, true,
                endingSlashAllowed, fileTypes);
        return Arrays.asList(fileReferenceSet.getAllReferences());
    }
}
