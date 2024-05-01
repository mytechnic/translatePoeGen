package translate.poe.client.poe.model;

import lombok.Data;

@Data
public class ItemEntry {
    private String type;
    private String text;
    private String name;
    private ItemFlag flags;
}
