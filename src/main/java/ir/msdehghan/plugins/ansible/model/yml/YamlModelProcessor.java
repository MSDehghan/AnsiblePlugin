package ir.msdehghan.plugins.ansible.model.yml;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import ir.msdehghan.plugins.ansible.AnsibleUtil;
import ir.msdehghan.plugins.ansible.model.yml.YamlField.Relation;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlMappingType;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlType;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.*;

import java.util.Optional;

public class YamlModelProcessor {
    private final YamlField rootField;

    public YamlModelProcessor(YamlField rootField) {
        this.rootField = rootField;
    }

    public FieldAndValueRelation locate(PsiElement element) {
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
            return FieldAndValueRelation.createOrNull(rootField, Relation.Mapping);
        } else if (parent instanceof YAMLSequenceItem) {
            return FieldAndValueRelation.createOrNull(locate(parent.getParent()).getField(), Relation.Sequence);
        } else if (parent instanceof YAMLKeyValue) {
            YAMLKeyValue keyValue = (YAMLKeyValue) parent;
            FieldAndValueRelation parentField = locate(keyValue.getParent());
            YamlMappingType parentType = ObjectUtils.tryCast(parentField.getFieldValueType(), YamlMappingType.class);

            if (parentType == null) return null;
            YamlField field = parentType.getFieldByName(keyValue.getKeyText()).orElse(null);

            Relation relation = Relation.Mapping;
            if (value instanceof YAMLSequence) relation = Relation.Sequence;
            else if (value instanceof YAMLScalar && isValue((YAMLScalar) value)) relation = Relation.Scalar;

            return FieldAndValueRelation.createOrNull(field, relation);
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

    public static class FieldAndValueRelation {
        private final YamlField field;
        private final Relation relation;

        public YamlField getField() {
            return field;
        }

        public Relation getRelation() {
            return relation;
        }

        public YamlType getFieldValueType() {
            return field.getType(relation);
        }

        public FieldAndValueRelation(YamlField field, Relation relation) {
            this.field = field;
            this.relation = relation;
        }

        public static FieldAndValueRelation createOrNull(YamlField field, Relation relation) {
            return field == null ? null : new FieldAndValueRelation(field, relation);
        }

        @Override
        public String toString() {
            return "FieldAndValueRelation{" +
                    "field=" + field.getName() +
                    ", relation=" + relation +
                    ", valueType=" + getFieldValueType() +
                    '}';
        }
    }
}
