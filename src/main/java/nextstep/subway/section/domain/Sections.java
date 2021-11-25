package nextstep.subway.section.domain;

import nextstep.subway.station.domain.Station;
import org.hibernate.annotations.BatchSize;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Embeddable
public class Sections {

    private static final String STATION_NOT_CONTAINS_MESSAGE = "상행과 하행 모두 포함되지 않습니다.";
    private static final String SECTION_NOT_CORRECT_MESSAGE = "동일한 구간을 추가할 수 없습니다.";
    private static final String DISTANCE_GREATER_OR_CORRECT_MESSAGE = "거리가 작아야합니다.";
    private static final String SECTION_NOT_EXIST_MESSAGE = "구간이 1개일 때 삭제할 수 없습니다.";
    private static final String SECTION_NOT_FOUND_MESSAGE = "찾는 구간이 존재하지 않습니다.";
    private static final int MIN_SECTION_COUNT = 1;

    @OneToMany(mappedBy = "line", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @BatchSize(size = 200)
    private List<Section> sections = new ArrayList<>();

    public void add(Section section) {

        if(validateEqualSection(section)) {
            throw new IllegalArgumentException(SECTION_NOT_CORRECT_MESSAGE);
        }

        if(validateUpStationOrDownStationNotContains(section)) {
            throw new IllegalArgumentException(STATION_NOT_CONTAINS_MESSAGE);
        }

        if(validateGreaterEqualDistance(section)) {
            throw new IllegalArgumentException(DISTANCE_GREATER_OR_CORRECT_MESSAGE);
        }

        if(!addUpSection(section) && !addDownSection(section)) {
            this.sections.add(section);
        }
    }

    public boolean remove(Station station) {

        if(validateNotContains(station)) {
            throw new IllegalArgumentException(STATION_NOT_CONTAINS_MESSAGE);
        }

        if(validateIfRemoveNotExist()) {
            throw new IllegalArgumentException(SECTION_NOT_EXIST_MESSAGE);
        }

        Section upSection = removeUpSection(station);
        Section downSection = removeDownSection(station);

        if(Objects.nonNull(upSection) && Objects.nonNull(downSection)) {
            Section section = upSection.sumBySection(downSection);
            this.sections.add(section);
        }

        return true;
    }

    private boolean addUpSection(Section section) {
        return addSection(section.getDownStation(), section.getDistance(), s -> s.matchUpStationFromUpStation(section));
    }

    private boolean addDownSection(Section section) {
        return addSection(section.getUpStation(), section.getDistance(), s -> s.matchDownStationFromDownStation(section));
    }

    private boolean addSection(Station station, Integer distance, Predicate<Section> express) {
        if(sectionStream().anyMatch(express)) {
            List<Section> divideBySections = this.sections.stream()
                    .filter(express)
                    .flatMap(s -> s.divideByStation(station, distance).stream())
                    .collect(toList());

            return this.sections.addAll(divideBySections);
        }
        return false;
    }

    private Section removeUpSection(Station station) {
        return removeSection(s -> s.matchDownStation(station));
    }

    private Section removeDownSection(Station station) {
        return removeSection(s -> s.matchUpStation(station));
    }

    private Section removeSection(Predicate<Section> express) {
        if(sectionStream().anyMatch(express)) {

            Section section = this.sections.stream()
                    .filter(express)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(SECTION_NOT_FOUND_MESSAGE));

            this.sections.remove(section);
            return section;
        }
        return null;
    }

    private Optional<Section> matchStation(Section section) {
        return this.sections.stream()
                .filter(s -> s.matchUpStationFromUpStation(section) || s.matchDownStationFromDownStation(section))
                .findFirst();
    }

    private boolean validateNotContains(Station station) {
        return this.sections.stream().noneMatch(s -> s.matchStation(station));
    }

    private boolean isGreaterEqualDistance(Section section, Section otherSection) {
        return otherSection.isGreaterOrEqualDistance(section);
    }

    private boolean isNotEmpty() {
        return !this.sections.isEmpty();
    }

    private boolean validateEqualSection(Section section) {
        return this.sections.contains(section);
    }

    private boolean validateUpStationOrDownStationNotContains(Section section) {
        return isNotEmpty() && this.sections.stream().noneMatch(s -> s.matchUpStation(section) || s.matchDownStation(section));
    }

    private boolean validateGreaterEqualDistance(Section section) {
        return matchStation(section)
                .map(s -> isNotEmpty() && isGreaterEqualDistance(s, section))
                .orElse(false);
    }

    private boolean validateIfRemoveNotExist() {
        return this.sections.size() == MIN_SECTION_COUNT;
    }

    private Stream<Section> sectionStream() {
        return this.sections.stream();
    }

    public List<Section> getSections() {
        return sections;
    }
}
