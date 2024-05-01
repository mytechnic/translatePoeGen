package translate.poe.service.model;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class Dictionary {
    private Map<String, String> h = new LinkedHashMap<>();
    private Map<String, String> p = new LinkedHashMap<>();
}
