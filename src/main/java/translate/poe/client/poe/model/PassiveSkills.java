package translate.poe.client.poe.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PassiveSkills {
    private List<ClassEntry> classes;
    private List<ClassAscendancy> alternateAscendancies;
    private Map<String, PassiveSkillNode> nodes;
}
