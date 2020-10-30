package ir.msdehghan.plugins.ansible;

import ir.msdehghan.plugins.ansible.model.ansible.play.AnsiblePlay;
import ir.msdehghan.plugins.ansible.model.ansible.task.AnsibleTask;
import ir.msdehghan.plugins.ansible.model.yml.DefaultYamlField;
import ir.msdehghan.plugins.ansible.model.yml.YamlModelProcessor;
import ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField;
import org.jetbrains.yaml.psi.YAMLDocument;

import static ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField.Relation.SEQUENCE;

public class AnsibleModels {
    public static final AnsiblePlay PLAY = new AnsiblePlay();
    public static final YamlField ROOT_PLAY_FIELD = DefaultYamlField.create("<Play Root>").setType(SEQUENCE, PLAY);
    public static final YamlField ROOT_ROLE_FIELD = DefaultYamlField.create("<Role Root>").setType(SEQUENCE, AnsibleTask.TYPE);
    public static final YamlModelProcessor MODEL_PROCESSOR = new YamlModelProcessor() {

        @Override
        protected YamlField getRootField(YAMLDocument document) {
            return AnsibleUtil.isRoleDocument(document) ? ROOT_ROLE_FIELD : ROOT_PLAY_FIELD;
        }
    };

    private AnsibleModels() {
        // Must not be instantiated.
    }
}
