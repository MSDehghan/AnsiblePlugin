package ir.msdehghan.plugins.ansible;

import com.intellij.codeInsight.completion.*;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLElementTypes;
import org.jetbrains.yaml.YAMLTokenTypes;

public class AnsibleCompletionContributor extends CompletionContributor {
    public AnsibleCompletionContributor() {
        extend(CompletionType.BASIC, completionPlace(),  new AnsiblePlayCompletionProvider());
    }

    private PsiElementPattern.Capture<PsiElement> completionPlace() {
        PsiElementPattern.Capture<PsiElement> updatedKey = PlatformPatterns.psiElement(YAMLTokenTypes.SCALAR_KEY);
        PsiElementPattern.Capture<PsiElement> inserted = PlatformPatterns.psiElement().withParent(
                PlatformPatterns.psiElement().withElementType(
                        TokenSet.create(YAMLElementTypes.SCALAR_PLAIN_VALUE, YAMLElementTypes.SCALAR_QUOTED_STRING)
                )
        );
        return PlatformPatterns.psiElement().andOr(inserted, updatedKey).with(new PatternCondition<>("In Ansible") {
            @Override
            public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext context) {
                return AnsibleUtil.isInAnsibleFile(psiElement);
            }
        });
    }
}
