package ir.msdehghan.plugins.ansible;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.jsonSchema.remote.JsonSchemaCatalogExclusion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLFileType;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLSequence;

import java.util.List;

public class JsonSchemaExclude implements JsonSchemaCatalogExclusion {
    @Override
    public boolean isExcluded(@NotNull VirtualFile file) {
        if (!(file.getFileType() instanceof YAMLFileType)) return false;
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects.length == 0) return false;
        Project project = projects[0];
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (!(psiFile instanceof YAMLFile)) return false;
        List<YAMLDocument> yamlDocuments = ((YAMLFile) psiFile).getDocuments();
        return !yamlDocuments.isEmpty() && yamlDocuments.get(0).getTopLevelValue() instanceof YAMLSequence;
    }
}
