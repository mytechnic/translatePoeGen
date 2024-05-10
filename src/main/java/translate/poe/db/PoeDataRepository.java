package translate.poe.db;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import translate.poe.db.model.PoeDataEntity;

import java.util.List;

@Repository
public interface PoeDataRepository extends CrudRepository<PoeDataEntity, Long> {

    int countBySource(String source);

    List<PoeDataEntity> findByOrderBySourceLengthDescSourceAsc();
}
