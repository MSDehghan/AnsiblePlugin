package ir.msdehghan.plugins.ansible;

import ir.msdehghan.plugins.ansible.model.ansible.play.AnsiblePlay;
import ir.msdehghan.plugins.ansible.model.yml.DefaultYamlField;
import ir.msdehghan.plugins.ansible.model.yml.YamlModelProcessor;
import ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField;

import static ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField.Relation.Sequence;

public class AnsibleModels {
    public static final AnsiblePlay PLAY = new AnsiblePlay();
    public static final YamlField ROOT_PLAY_FIELD = DefaultYamlField.create("<Play Root>").setType(Sequence, PLAY, true);
    public static final YamlModelProcessor PLAY_MODEL_PROCESSOR = new YamlModelProcessor(AnsibleModels.ROOT_PLAY_FIELD);
}
