package translate.poe.db;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import translate.poe.db.model.PoeCustomEntity;

import java.util.List;

@Repository
public interface PoeCustomRepository extends CrudRepository<PoeCustomEntity, Long> {

    List<PoeCustomEntity> findByOrderBySourceLengthDesc();
}
