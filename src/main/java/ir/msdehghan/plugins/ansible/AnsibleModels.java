package ir.msdehghan.plugins.ansible;

import ir.msdehghan.plugins.ansible.model.ansible.AnsiblePlay;
import ir.msdehghan.plugins.ansible.model.yml.YamlField;

import static ir.msdehghan.plugins.ansible.model.yml.YamlField.Relation.Sequence;

public class AnsibleModels {
    public static final AnsiblePlay PLAY = new AnsiblePlay();
    public static final YamlField ROOT_PLAY_FIELD = YamlField.create("<Play Root>").setType(Sequence, PLAY, true);
}
