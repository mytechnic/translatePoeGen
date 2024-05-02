package translate.poe.client.poe.model;

import lombok.Data;

import java.util.List;

@Data
public class PassiveSkillNode {
    private String name;
    private List<String> stats;
}
