package translate.poe.client.poe;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import translate.poe.client.poe.model.*;
import translate.poe.utils.FileUtils;
import translate.poe.utils.JsonUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class PoeClient {
    private final RestClient.Builder restClient;

    public Items getItems(Country country) {
        Items items = restClient.build()
                .get()
                .uri(country.getBaseUrl() + "/api/trade/data/items")
                .retrieve()
                .body(Items.class);

        if (items == null) {
            return null;
        }

        if (country == Country.KR) {
            ItemEntry itemEntry = new ItemEntry();
            itemEntry.setText("무거운 화살통");
            itemEntry.setType("무거운 화살통");
            items.getResult().get(1).getEntries().add(432, itemEntry);
        }

        return save(country, "items", items);
    }

    public Stats getStats(Country country) {
        return restClient.build()
                .get()
                .uri(country.getBaseUrl() + "/api/trade/data/stats")
                .retrieve()
                .body(Stats.class);
    }

    public Statics getStatic(Country country) {
        return save(country, "static", restClient.build()
                .get()
                .uri(country.getBaseUrl() + "/api/trade/data/static")
                .retrieve()
                .body(Statics.class));
    }

    public <T> T save(Country country, String name, T data) {

        String buffer;
        try {
            buffer = JsonUtils.getObjectMapper().writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 에러: " + e.getMessage());
        }

        String path = "./data/" + country.name().toLowerCase();
        String file = path + "/" + name + ".json";
        FileUtils.save(file, buffer);

        return data;
    }

    // https://poe.game.daum.net/passive-skill-tree
    public PassiveSkills getPassiveSkill(Country country) {

        String path = "./data/" + country.name().toLowerCase();
        String file = path + "/passive-skill-tree.json";
        String buffer = FileUtils.read(file);
        try {
            return JsonUtils.getObjectMapper().readValue(buffer, PassiveSkills.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 에러: " + e.getMessage());
        }
    }

    public CustomDictionary getCustom() {

        String file = "./data/translate/custom.txt";
        List<String> sourceList = FileUtils.readList(file);

        Map<String, String> data = new LinkedHashMap<>();
        for (String str : sourceList) {
            String[] z = str.split(Pattern.quote("|"));
            log.debug("{}", str);

            if (z.length != 2) {
                continue;
            }
            String source = z[0].trim();
            String text = z[1].trim();

            log.debug("{}: {}", source, text);
            if (source.isEmpty() || text.isEmpty()) {
                continue;
            }

            data.put(z[0].trim(), z[1].trim());
        }

        CustomDictionary dictionary = new CustomDictionary();
        dictionary.setData(data);
        return dictionary;
    }
}
