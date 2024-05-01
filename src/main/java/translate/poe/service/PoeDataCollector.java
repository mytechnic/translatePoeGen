package translate.poe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
        saveStatic();
        saveItems();
        saveStats();
        saveFile();
    }

    private void saveFile() {
        List<PoeDataEntity> entities = poeDataRepository.findByOrderBySourceLengthDesc();

        Dictionary dictionary = new Dictionary();
        for (PoeDataEntity entity : entities) {
            if (entity.getPatternType() == PatternType.PATTERN) {
                dictionary.getP().put(entity.getSource(), entity.getText());
            } else {
                dictionary.getH().put(entity.getSource(), entity.getText());
            }
        }

        String data;
        try {
            data = JsonUtils.getObjectMapper().writeValueAsString(dictionary);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 에러: " + e.getMessage());
        }
        FileUtils.save("./data/translate/kr.json", data);
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
        singleStore(category, source, target, sourceOptions, targetOptions);
    }

    private void multiStore(String category, String source, String target,
                            List<StatOptionProperty> sourceOptions, List<StatOptionProperty> targetOptions) {
        for (int i = 0; i < sourceOptions.size(); i++) {
            String sourceOption = sourceOptions.get(i).getText();
            String targetOption = targetOptions.get(i).getText();

            String[] sourceTexts = split(source);
            String[] targetTexts = split(target);
            if (sourceTexts.length != targetTexts.length) {
                throw new RuntimeException(String.format("줄 수가 맞지 않음 - source: %s, target: %s", source, target));
            }

            for (int m = 0; m < sourceTexts.length; m++) {
                if (sourceTexts[m].contains(Pattern.quote("#"))) {
                    String sourceText = sourceTexts[m].replaceAll("#", sourceOption);
                    String targetText = targetTexts[m].replaceAll("#", targetOption);
                    save(category, PatternType.PATTERN, sourceText, targetText);
                } else {
                    save(category, PatternType.STRING, sourceTexts[m], targetTexts[m]);
                }
            }
        }
    }

    private void singleStore(String category, String source, String target,
                             List<StatOptionProperty> sourceOptions, List<StatOptionProperty> targetOptions) {
        for (int i = 0; i < sourceOptions.size(); i++) {
            String sourceText = source.replaceAll("#", sourceOptions.get(i).getText());
            String targetText = target.replaceAll("#", targetOptions.get(i).getText());
            save(category, PatternType.PATTERN, sourceText, targetText);
        }
    }

    private void store(String category, String source, String target) {
        singleStore(category, source, target);
    }

    private void multiStore(String category, String source, String target) {
        String[] sourceTexts = split(source);
        String[] targetTexts = split(target);

        if (sourceTexts.length != targetTexts.length) {
            throw new RuntimeException(String.format("줄 수가 맞지 않음 - source: %s, target: %s", source, target));
        }

        for (int m = 0; m < sourceTexts.length; m++) {
            if (sourceTexts[m].contains(Pattern.quote("#"))) {
                save(category, PatternType.PATTERN, source, target);
            } else {
                save(category, PatternType.STRING, sourceTexts[m], targetTexts[m]);
            }
        }
    }

    private void singleStore(String category, String source, String target) {
        if (source.contains(Pattern.quote("#"))) {
            save(category, PatternType.PATTERN, source, target);
        } else {
            save(category, PatternType.STRING, source, target);
        }
    }

    private void save(String category, PatternType patternType, String source, String text) {

        source = source.trim();
        text = text.trim();

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
