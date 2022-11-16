package nextstep.subway.repository;

import nextstep.subway.domain.Line;
import nextstep.subway.domain.Section;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectionRepository extends JpaRepository<Section, Long> {

    void deleteByLine(Line line);
}