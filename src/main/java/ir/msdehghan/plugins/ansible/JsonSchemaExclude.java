package ir.msdehghan.plugins.ansible;

import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.testFramework.LightVirtualFile;
import com.jetbrains.jsonSchema.remote.JsonSchemaCatalogExclusion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLFileType;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLSequence;

import java.util.List;

@SuppressWarnings("MissingRecentApi")
public class JsonSchemaExclude implements JsonSchemaCatalogExclusion {
    @Override
    public boolean isExcluded(@NotNull VirtualFile file) {
        if (!file.isValid()) {
            return false;
        }

        if (!(file.getFileType() instanceof YAMLFileType)) return false;
        PsiFile psiFile = findCachedPsiFile(file);
        if (psiFile == null) {
            // These files must be loaded into memory.
            if (file instanceof LightVirtualFile || file instanceof VirtualFileWindow) {
                return false;
            }
            psiFile = findPsiFile(file);
            if (psiFile == null) return false;
        }
        if (!(psiFile instanceof YAMLFile)) return false;
        List<YAMLDocument> yamlDocuments = ((YAMLFile) psiFile).getDocuments();
        return !yamlDocuments.isEmpty() && yamlDocuments.getFirst().getTopLevelValue() instanceof YAMLSequence;
    }

    @Nullable
    private PsiFile findPsiFile(@NotNull VirtualFile file) {
        PsiFile psiFile;
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects.length == 0) return null;
        Project project = projects[0];
        psiFile = PsiManager.getInstance(project).findFile(file);
        return psiFile;
    }

    @Nullable
    private static PsiFile findCachedPsiFile(VirtualFile file) {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : openProjects) {
            final PsiManagerEx psiManager = PsiManagerEx.getInstanceEx(project);
            FileViewProvider viewProvider = psiManager.getFileManager().findCachedViewProvider(file);
            if (viewProvider != null) {
                return viewProvider.getPsi(viewProvider.getBaseLanguage());
            }
        }

        return null;
    }
}
