package ir.msdehghan.plugins.ansible;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.util.ProgressIndicatorUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.CommonProcessors;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.navigation.YAMLKeysIndex;
import org.jetbrains.yaml.psi.YAMLQuotedText;
import org.jetbrains.yaml.psi.YAMLScalar;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AnsibleReferenceProvider extends PsiReferenceProvider {
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        if (element instanceof YAMLScalar) {
            return new PsiReference[]{new AnsibleRef(((YAMLScalar) element))};
        }
        return PsiReferenceBase.EMPTY_ARRAY;
    }

    static class AnsibleRef extends PsiReferenceBase.Poly<YAMLScalar> {
        private static final Pattern VARIABLE_START_PATTERN = Pattern.compile("^.*?\\.vars.(.+)");
        private static final Pattern VARIABLE_DEC_PATTERN = Pattern.compile("^\\s*\\{\\{\\s*(.*?)\\s*\\}\\}\\s*$");
        private final String value;
        private final Project project;

        public AnsibleRef(@NotNull YAMLScalar element) {
            super(element, true);
            project = element.getProject();
            value = ApplicationManager.getApplication().runReadAction((Computable<String>) element::getTextValue).trim();
        }

        @Override
        public @NotNull ResolveResult[] multiResolve(boolean incompleteCode) {
            Matcher matcher = VARIABLE_DEC_PATTERN.matcher(value);
            if (!matcher.matches()) return ResolveResult.EMPTY_ARRAY;
            String text = matcher.group(1);
            Collection<String> allKeys = FileBasedIndex.getInstance().getAllKeys(YAMLKeysIndex.KEY, project);
            Set<String> matchedStrings = allKeys.parallelStream()
                    .unordered()
                    .filter(s -> VARIABLE_START_PATTERN.matcher(s).matches())
                    .filter(s -> s.substring(s.indexOf("vars.") + "vars.".length()).equals(text))
                    .collect(Collectors.toSet());
            GlobalSearchScope filter = GlobalSearchScope.projectScope(project);
            List<PsiElement> elementList = new ArrayList<>(matchedStrings.size());
            for (String varName : matchedStrings) {
                ProgressManager.checkCanceled();
                CommonProcessors.CollectProcessor<VirtualFile> files = new CommonProcessors.CollectProcessor<>();
                FileBasedIndex.getInstance().getFilesWithKey(YAMLKeysIndex.KEY,
                        Collections.singleton(varName),
                        files,
                        filter);

                for (VirtualFile file : files.getResults()) {
                    ProgressManager.checkCanceled();
                    PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                    if (psiFile == null) {
                        continue;
                    }

                    List<Integer> positions = FileBasedIndex.getInstance().getValues(YAMLKeysIndex.KEY, varName,
                            GlobalSearchScope.fileScope(project, file));

                    for (Integer position : positions) {
                        elementList.add(psiFile.findElementAt(position));
                    }

                }
            }
            return PsiElementResolveResult.createResults(elementList);
        }

        @Override
        public @NotNull Object[] getVariants() {
            FileBasedIndex index = FileBasedIndex.getInstance();
            Collection<String> allKeys = index.getAllKeys(YAMLKeysIndex.KEY, project);
            String start;
            String end;
            if (myElement instanceof YAMLQuotedText) {
                start = "{{ ";
                end = " }}";
            } else {
                start = "\"{{ ";
                end = " }}\"";
            }
            return allKeys.stream()
                    .unordered()
                    .filter(s -> VARIABLE_START_PATTERN.matcher(s).matches())
                    .filter(s -> !index.getValues(YAMLKeysIndex.KEY, s, GlobalSearchScope.projectScope(project)).isEmpty())
                    .map(s -> start + s.substring(s.indexOf("vars.") + "vars.".length()) + end).toArray();
        }
    }
}
