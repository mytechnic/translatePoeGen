package translate.poe.client.poe.model;

import lombok.Data;

import java.util.List;

@Data
public class Static {
    private String id;
    private String label;
    private List<StaticEntry> entries;
}
