package translate.poe.client.poe.model;

import lombok.Data;

import java.util.List;

@Data
public class Item {
    private String id;
    private String label;
    private List<ItemEntry> entries;
}
