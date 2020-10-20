package ir.msdehghan.plugins.ansible.model.yml;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField.Relation;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlType;
import ir.msdehghan.plugins.ansible.model.yml.type.api.MappingType;
import ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.*;

public abstract class YamlModelProcessor {
    protected abstract YamlField getRootField(YAMLDocument document);

    @Nullable
    public ElementSchemaInfo locate(PsiElement element) {
        YAMLValue value = getNearestYamlValue(element);
        if (value == null) return null;

        PsiElement parent = PsiTreeUtil.getParentOfType(value, YAMLKeyValue.class, YAMLSequenceItem.class,
                YAMLDocument.class);
        if (parent instanceof YAMLDocument) {
            return ElementSchemaInfo.createOrNull(getRootField((YAMLDocument) parent), YamlField.Relation.MAPPING);
        } else if (parent instanceof YAMLSequenceItem) {
            ElementSchemaInfo seqParent = locate(parent.getParent());
            if (seqParent == null) return null;
            return ElementSchemaInfo.createOrNull(seqParent.getField(), YamlField.Relation.SEQUENCE);
        } else if (parent instanceof YAMLKeyValue) {
            YAMLKeyValue keyValue = (YAMLKeyValue) parent;
            ElementSchemaInfo parentField = locate(keyValue.getParent());
            if (parentField == null) return null;
            MappingType parentType = ObjectUtils.tryCast(parentField.getType(), MappingType.class);

            if (parentType == null) return null;
            YamlField field = parentType.getFieldByName(keyValue.getKeyText()).orElse(null);

            Relation relation = YamlField.Relation.MAPPING;
            if (value instanceof YAMLSequence) relation = YamlField.Relation.SEQUENCE;
            else if (value instanceof YAMLScalar && isValue((YAMLScalar) value)) relation = YamlField.Relation.SCALAR;

            return ElementSchemaInfo.createOrNull(field, relation);
        }
        return null;
    }

    @Nullable
    private YAMLValue getNearestYamlValue(PsiElement element) {
        if (element instanceof YAMLValue) {
            return (YAMLValue) element;
        } else if (element != null && element.getContainingFile() instanceof YAMLFile) {
            return PsiTreeUtil.getParentOfType(element, YAMLValue.class, false);
        } else {
            return null;
        }
    }

    private boolean isValue(YAMLScalar scalar) {
        if (!(scalar.getParent() instanceof YAMLKeyValue)) return false;
        PsiElement beforeSibling = PsiTreeUtil.skipWhitespacesBackward(scalar);
        if (beforeSibling == null || beforeSibling.getNode().getElementType() != YAMLTokenTypes.COLON) return false;
        beforeSibling = PsiTreeUtil.skipWhitespacesBackward(beforeSibling);
        return beforeSibling != null && beforeSibling.getNode().getElementType() == YAMLTokenTypes.SCALAR_KEY;
    }

    public static class ElementSchemaInfo {
        private final YamlField field;
        private final Relation relation;

        public YamlField getField() {
            return field;
        }

        public Relation getRelation() {
            return relation;
        }

        public YamlType getType() {
            return field.getType(relation);
        }

        public ElementSchemaInfo(YamlField field, Relation relation) {
            this.field = field;
            this.relation = relation;
        }

        public static ElementSchemaInfo createOrNull(YamlField field, Relation relation) {
            return field == null ? null : new ElementSchemaInfo(field, relation);
        }

        @Override
        public String toString() {
            return "FieldAndValueRelation{" +
                    "field=" + field.getName() +
                    ", relation=" + relation +
                    ", type=" + getType() +
                    '}';
        }
    }
}
