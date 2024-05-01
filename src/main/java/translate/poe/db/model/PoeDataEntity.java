package translate.poe.db.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("poe_data")
@Data
public class PoeDataEntity {
    @Id
    private Long no;
    private Season season;
    private String category;
    private PatternType patternType;
    private String source;
    private String text;
    private Integer sourceLength;
    private Integer textLength;
    private LocalDateTime updated;
    private LocalDateTime created;
}
