package translate.poe.client.poe.model;

import lombok.Data;

@Data
public class StatEntry {
    private String id;
    private String text;
    private String type;
    private StatOption option;
}
