package ir.msdehghan.plugins.ansible.model.yml.type.api;

import java.util.List;
import java.util.Optional;

public interface MappingType {
    List<YamlField> getFields();

    Optional<YamlField> getFieldByName(String name);
}
