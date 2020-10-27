package ir.msdehghan.plugins.ansible.reference;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLFileType;
import org.jetbrains.yaml.psi.YAMLScalar;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RoleNameReference extends PsiReferenceBase.Poly<YAMLScalar> {
    private static final Pattern ROLE_PATH_PATTERN = Pattern.compile("/roles/([^/]+?)/tasks/main.ya?ml$");
    private static final String ROLE_PATH_STRING = "/roles/[name]/tasks/main.ya?ml$";

    public RoleNameReference(YAMLScalar element) {
        super(element, true);
    }

    @Override
    public @NotNull ResolveResult[] multiResolve(boolean incompleteCode) {
        final Pattern rolePattern = Pattern.compile(ROLE_PATH_STRING.replace("[name]", getValue()));

        Collection<VirtualFile> yamlFiles = FileTypeIndex.getFiles(YAMLFileType.YML,
                GlobalSearchScope.projectScope(myElement.getProject()));
        Set<PsiFile> roles = new HashSet<>();
        for (VirtualFile file : yamlFiles) {
            final String path = file.getPath();
            if (rolePattern.matcher(path).find()) {
                roles.add(PsiManager.getInstance(myElement.getProject()).findFile(file));
            }
        }
        return PsiElementResolveResult.createResults(roles);
    }

    @Override
    public @NotNull Object[] getVariants() {
        Collection<VirtualFile> yamlFiles = FileTypeIndex.getFiles(YAMLFileType.YML,
                GlobalSearchScope.projectScope(myElement.getProject()));
        Set<String> roles = new HashSet<>();
        for (VirtualFile file : yamlFiles) {
            final String path = file.getPath();
            Matcher matcher = ROLE_PATH_PATTERN.matcher(path);
            if (matcher.find() && !StringUtil.isEmptyOrSpaces(matcher.group(1))) {
                roles.add(matcher.group(1));
            }
        }
        return roles.stream().map(RoleNameReference::createLookupElement).toArray();
    }

    private static LookupElement createLookupElement(String name) {
        return LookupElementBuilder.create(name)
                .withIcon(AllIcons.Actions.GroupByModuleGroup)
                .withTypeText("Ansible Role");
    }
}
