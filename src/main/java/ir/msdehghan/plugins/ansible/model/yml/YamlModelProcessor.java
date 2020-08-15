package ir.msdehghan.plugins.ansible.model.yml;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField.Relation;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlType;
import ir.msdehghan.plugins.ansible.model.yml.type.api.MappingType;
import ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.*;

public class YamlModelProcessor {
    private final YamlField rootField;

    public YamlModelProcessor(YamlField rootField) {
        this.rootField = rootField;
    }

    public ElementSchemaInfo locate(PsiElement element) {
        YAMLValue value;
        if (element instanceof YAMLValue) {
            value = (YAMLValue) element;
        } else if (element.getContainingFile() instanceof YAMLFile) {
            value = PsiTreeUtil.getParentOfType(element, YAMLValue.class, false);
        } else {
            return null;
        }

        PsiElement parent = PsiTreeUtil.getParentOfType(value, YAMLKeyValue.class, YAMLSequenceItem.class, YAMLDocument.class);
        if (parent instanceof YAMLDocument) {
            return ElementSchemaInfo.createOrNull(rootField, YamlField.Relation.Mapping);
        } else if (parent instanceof YAMLSequenceItem) {
            return ElementSchemaInfo.createOrNull(locate(parent.getParent()).getField(), YamlField.Relation.Sequence);
        } else if (parent instanceof YAMLKeyValue) {
            YAMLKeyValue keyValue = (YAMLKeyValue) parent;
            ElementSchemaInfo parentField = locate(keyValue.getParent());
            if (parentField == null) return null;
            MappingType parentType = ObjectUtils.tryCast(parentField.getType(), MappingType.class);

            if (parentType == null) return null;
            YamlField field = parentType.getFieldByName(keyValue.getKeyText()).orElse(null);

            Relation relation = YamlField.Relation.Mapping;
            if (value instanceof YAMLSequence) relation = YamlField.Relation.Sequence;
            else if (value instanceof YAMLScalar && isValue((YAMLScalar) value)) relation = YamlField.Relation.Scalar;

            return ElementSchemaInfo.createOrNull(field, relation);
        }
        return null;
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
