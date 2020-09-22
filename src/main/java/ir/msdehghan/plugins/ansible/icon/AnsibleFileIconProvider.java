package ir.msdehghan.plugins.ansible.icon;

import com.intellij.ide.FileIconProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import ir.msdehghan.plugins.ansible.AnsibleUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AnsibleFileIconProvider implements FileIconProvider {

    @Override
    public @Nullable Icon getIcon(@NotNull VirtualFile file, int flags, @Nullable Project project) {
        if (project == null) return null;
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        return AnsibleUtil.isInPlayBook(psiFile) ? Icons.ANSIBLE_FILE : null;
    }
}
