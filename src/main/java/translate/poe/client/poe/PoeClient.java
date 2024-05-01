package translate.poe.client.poe;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import translate.poe.client.poe.model.*;
import translate.poe.utils.FileUtils;
import translate.poe.utils.JsonUtils;

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
        Stats stats = restClient.build()
                .get()
                .uri(country.getBaseUrl() + "/api/trade/data/stats")
                .retrieve()
                .body(Stats.class);

        if (stats == null) {
            return null;
        }

        if (country == Country.KR) {
            for (int i = 0; i < stats.getResult().size(); i++) {
                for (int j = 0; j < stats.getResult().get(i).getEntries().size(); j++) {
                    if ("점유되지 않은 생명력이 차서 흡수가 제거되면 #%의 확률로 2초 동안 아드레날린 획득"
                            .equals(stats.getResult().get(i).getEntries().get(j).getText())) {
                        stats.getResult().get(i).getEntries().get(j).setText("점유되지 않은 생명력이 차서 흡수가 제거되면\n#%의 확률로 2초 동안 아드레날린 획득");
                    }
                }
            }
        }

        return save(country, "stats", stats);
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

    public PassiveSkills getPassiveSkill(Country country) {
        // TODO
        return null;
    }
}
