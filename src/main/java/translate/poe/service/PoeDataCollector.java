package translate.poe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import translate.poe.client.poe.PoeClient;
import translate.poe.client.poe.model.*;
import translate.poe.db.PoeDataRepository;
import translate.poe.db.model.PatternType;
import translate.poe.db.model.PoeDataEntity;
import translate.poe.db.model.Season;
import translate.poe.service.model.Dictionary;
import translate.poe.utils.FileUtils;
import translate.poe.utils.JsonUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PoeDataCollector {
    private final PoeClient poeClient;
    private final PoeDataRepository poeDataRepository;

    public void save() {
//        saveStatic();
//        saveItems();
//        saveStats();
//        savePassiveSkill();
        saveCustom();
        build();
    }

    private void saveCustom() {
        CustomDictionary dictionary = poeClient.getCustom();
        for (String source : dictionary.getData().keySet()) {
            store("Custom", source, dictionary.getData().get(source));
        }
    }

    private void savePassiveSkill() {
        PassiveSkills us = poeClient.getPassiveSkill(Country.US);
        PassiveSkills kr = poeClient.getPassiveSkill(Country.KR);

        for (int i = 0; i < us.getClasses().size(); i++) {
            if (ObjectUtils.isEmpty(us.getClasses().get(i).getName())) {
                continue;
            }
            store("Class",
                    us.getClasses().get(i).getName(),
                    kr.getClasses().get(i).getName());
        }

        if (us.getAlternateAscendancies() != null) {
            for (int i = 0; i < us.getAlternateAscendancies().size(); i++) {
                if (ObjectUtils.isEmpty(us.getAlternateAscendancies().get(i).getName())) {
                    continue;
                }
                store("Alternate Ascendancy",
                        us.getAlternateAscendancies().get(i).getName(),
                        kr.getAlternateAscendancies().get(i).getName());
            }
        }

        for (String skill : us.getNodes().keySet()) {
            if (ObjectUtils.isEmpty(us.getNodes().get(skill).getName())) {
                continue;
            }
            store("Passive Node",
                    us.getNodes().get(skill).getName(),
                    kr.getNodes().get(skill).getName());
            for (int i = 0; i < us.getNodes().get(skill).getStats().size(); i++) {
                if (ObjectUtils.isEmpty(us.getNodes().get(skill).getStats().get(i))) {
                    continue;
                }
                store("Stats",
                        us.getNodes().get(skill).getStats().get(i),
                        kr.getNodes().get(skill).getStats().get(i));
            }

        }
    }

    private void build() {
        Dictionary dictionary = new Dictionary();

        List<PoeDataEntity> poeDataEntities = poeDataRepository.findByOrderBySourceLengthDesc();
        for (PoeDataEntity entity : poeDataEntities) {
            addDictionary(entity.getPatternType(), dictionary, entity.getSource(), entity.getText());
        }

        String data;
        try {
            data = JsonUtils.getObjectMapper().writeValueAsString(dictionary);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 에러: " + e.getMessage());
        }
        FileUtils.save("./data/translate/kr.json", data);
    }

    private static void addDictionary(PatternType entity, Dictionary dictionary, String source, String text) {
        if (entity == PatternType.PATTERN) {
            if (dictionary.getP().containsKey(source.toLowerCase())) {
                return;
            }
            dictionary.getP().put(source.toLowerCase(), text);
        } else {
            if (dictionary.getH().containsKey(source.toLowerCase())) {
                return;
            }
            dictionary.getH().put(source.toLowerCase(), text);
        }
    }

    private void saveStatic() {
        Statics us = poeClient.getStatic(Country.US);
        Statics kr = poeClient.getStatic(Country.KR);

        // 사전 데이터 점검
        for (int i = 0; i < us.getResult().size(); i++) {
            Static sourceItem = us.getResult().get(i);
            Static targetItem = kr.getResult().get(i);

            if (!sourceItem.getId().equals(targetItem.getId())) {
                log.warn("id not matched- index: {}, id: {}", i, targetItem.getId());
                return;
            }

            if (sourceItem.getEntries().size() != targetItem.getEntries().size()) {
                log.warn("entries not matched- index: {}, id: {}, label: {}, size: {}/{}",
                        i, targetItem.getId(), targetItem.getLabel(), sourceItem.getEntries().size(), targetItem.getEntries().size());
                return;
            }
        }

        for (int i = 0; i < us.getResult().size(); i++) {
            Static sourceItem = us.getResult().get(i);
            Static targetItem = kr.getResult().get(i);

            if (sourceItem.getLabel() == null) {
                continue;
            }

            store("Category", sourceItem.getLabel(), targetItem.getLabel());

            for (int j = 0; j < sourceItem.getEntries().size(); j++) {
                StaticEntry sourceItemEntry = sourceItem.getEntries().get(j);
                StaticEntry targetItemEntry = targetItem.getEntries().get(j);

                if (sourceItemEntry.getText() != null
                        && !Objects.equals(sourceItemEntry.getText(), targetItemEntry.getText())) {
                    store(sourceItem.getLabel(), sourceItemEntry.getText(), targetItemEntry.getText());
                }

                if (sourceItemEntry.getDescription() != null
                        && !Objects.equals(sourceItemEntry.getDescription(), targetItemEntry.getDescription())
                        && !Objects.equals(sourceItemEntry.getText(), sourceItemEntry.getDescription())) {
                    store(sourceItem.getLabel(), sourceItemEntry.getDescription(), targetItemEntry.getDescription());
                }
            }
        }
    }

    private void saveItems() {
        Items us = poeClient.getItems(Country.US);
        Items kr = poeClient.getItems(Country.KR);

        // 사전 데이터 점검
        for (int i = 0; i < us.getResult().size(); i++) {
            Item sourceItem = us.getResult().get(i);
            Item targetItem = kr.getResult().get(i);

            if (!sourceItem.getId().equals(targetItem.getId())) {
                log.warn("id not matched- index: {}, id: {}", i, targetItem.getId());
                return;
            }

            if (sourceItem.getEntries().size() != targetItem.getEntries().size()) {
                log.warn("entries not matched- index: {}, id: {}, label: {}, size: {}/{}",
                        i, targetItem.getId(), targetItem.getLabel(), sourceItem.getEntries().size(), targetItem.getEntries().size());
                return;
            }
        }

        for (int i = 0; i < us.getResult().size(); i++) {
            Item sourceItem = us.getResult().get(i);
            Item targetItem = kr.getResult().get(i);
            store("Category", sourceItem.getLabel(), targetItem.getLabel());

            for (int j = 0; j < sourceItem.getEntries().size(); j++) {
                ItemEntry sourceItemEntry = sourceItem.getEntries().get(j);
                ItemEntry targetItemEntry = kr.getResult().get(i).getEntries().get(j);

                if (sourceItemEntry.getType() != null
                        && !Objects.equals(sourceItemEntry.getType(), targetItemEntry.getType())) {
                    store(sourceItem.getLabel(), sourceItemEntry.getType(), targetItemEntry.getType());
                }

                if (sourceItemEntry.getText() != null
                        && !Objects.equals(sourceItemEntry.getText(), targetItemEntry.getText())
                        && !Objects.equals(sourceItemEntry.getType(), sourceItemEntry.getText())) {
                    store(sourceItem.getLabel(), sourceItemEntry.getText(), targetItemEntry.getText());
                }

                if (sourceItemEntry.getName() != null
                        && !Objects.equals(sourceItemEntry.getName(), targetItemEntry.getName())
                        && !Objects.equals(sourceItemEntry.getType(), sourceItemEntry.getName())
                        && !Objects.equals(sourceItemEntry.getText(), sourceItemEntry.getName())) {
                    store(sourceItem.getLabel(), sourceItemEntry.getName(), targetItemEntry.getName());
                }
            }
        }
    }

    private void saveStats() {
        Stats us = poeClient.getStats(Country.US);
        Stats kr = poeClient.getStats(Country.KR);

        // 사전 데이터 점검
        for (int i = 0; i < us.getResult().size(); i++) {
            Stat sourceItem = us.getResult().get(i);
            Stat targetItem = kr.getResult().get(i);

            if (!sourceItem.getId().equals(targetItem.getId())) {
                log.warn("id not matched- index: {}, id: {}", i, targetItem.getId());
                return;
            }

            if (sourceItem.getEntries().size() != targetItem.getEntries().size()) {
                log.warn("entries not matched- index: {}, id: {}, label: {}, size: {}/{}",
                        i, targetItem.getId(), targetItem.getLabel(), sourceItem.getEntries().size(), targetItem.getEntries().size());
                return;
            }

            for (int j = 0; j < sourceItem.getEntries().size(); j++) {
                StatEntry sourceItemEntry = sourceItem.getEntries().get(j);
                StatEntry targetItemEntry = kr.getResult().get(i).getEntries().get(j);

                if (!sourceItemEntry.getId().equals(targetItemEntry.getId())) {
                    log.warn("id not matched- index: ({},{}), source: {}, target: {}", i, j, sourceItemEntry.getId(), targetItemEntry.getId());
                    return;
                }
            }
        }

        for (int i = 0; i < us.getResult().size(); i++) {
            Stat sourceItem = us.getResult().get(i);
            Stat targetItem = kr.getResult().get(i);

            if ("pseudo".equals(sourceItem.getId())) {
                continue;
            }

            store("Category", sourceItem.getLabel(), targetItem.getLabel());

            for (int j = 0; j < sourceItem.getEntries().size(); j++) {
                StatEntry sourceItemEntry = sourceItem.getEntries().get(j);
                StatEntry targetItemEntry = kr.getResult().get(i).getEntries().get(j);

                if (sourceItemEntry.getText() == null || targetItemEntry.getText() == null) {
                    continue;
                }

                if (Objects.equals(sourceItemEntry.getText(), targetItemEntry.getText())) {
                    continue;
                }

                if (sourceItemEntry.getOption() != null) {
                    store(sourceItem.getLabel(), sourceItemEntry.getText(), targetItemEntry.getText(),
                            sourceItemEntry.getOption().getOptions(), targetItemEntry.getOption().getOptions());
                } else {
                    store(sourceItem.getLabel(), sourceItemEntry.getText(), targetItemEntry.getText());
                }
            }
        }

        for (int i = 0; i < us.getResult().size(); i++) {
            Stat sourceItem = us.getResult().get(i);
            Stat targetItem = kr.getResult().get(i);

            if (!"pseudo".equals(sourceItem.getId())) {
                continue;
            }

            store("Category", sourceItem.getLabel(), targetItem.getLabel());

            for (int j = 0; j < sourceItem.getEntries().size(); j++) {
                StatEntry sourceItemEntry = sourceItem.getEntries().get(j);
                StatEntry targetItemEntry = kr.getResult().get(i).getEntries().get(j);

                if (sourceItemEntry.getText() == null || targetItemEntry.getText() == null) {
                    continue;
                }

                if (Objects.equals(sourceItemEntry.getText(), targetItemEntry.getText())) {
                    continue;
                }

                if (sourceItemEntry.getOption() != null) {
                    store(sourceItem.getLabel(), sourceItemEntry.getText(), targetItemEntry.getText(),
                            sourceItemEntry.getOption().getOptions(), targetItemEntry.getOption().getOptions());
                } else {
                    store(sourceItem.getLabel(), sourceItemEntry.getText(), targetItemEntry.getText());
                }
            }
        }
    }

    private void store(String category, String source, String target,
                       List<StatOptionProperty> sourceOptions, List<StatOptionProperty> targetOptions) {
        for (int i = 0; i < sourceOptions.size(); i++) {
            String sourceText = source.replaceAll("#", sourceOptions.get(i).getText());
            String targetText = target.replaceAll("#", targetOptions.get(i).getText());
            save(category, PatternType.STRING, sourceText, targetText);
        }
    }

    private void store(String category, String source, String target) {
        if (source.contains("#")) {
            save(category, PatternType.PATTERN, source, target);
        } else {
            save(category, PatternType.STRING, source, target);
        }
    }

    private void save(String category, PatternType patternType, String source, String text) {

        source = source.trim();
        text = text.trim();

        if (source.equals(text)) {
            return;
        }

        int count = poeDataRepository.countBySource(source);
        if (count > 0) {
            log.info("exists record: {}, {}, {} -> {}", category, patternType, source, text);
            return;
        }

        PoeDataEntity entity = new PoeDataEntity();
        entity.setSeason(Season.Necropolis);
        entity.setCategory(category);
        entity.setPatternType(patternType);
        entity.setSource(source);
        entity.setText(text);
        entity.setSourceLength(source.length());
        entity.setTextLength(text.length());
        entity.setCreated(LocalDateTime.now());
        poeDataRepository.save(entity);
    }

    private String[] split(String source) {
        return source.split(Pattern.quote("\n"));
    }
}
