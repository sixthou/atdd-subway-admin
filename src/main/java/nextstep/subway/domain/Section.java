package nextstep.subway.domain;

import java.util.Objects;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import nextstep.subway.global.domain.BaseEntity;
import nextstep.subway.global.exception.CannotRegisterException;
import nextstep.subway.global.exception.ExceptionType;

@Entity
@Table(name = "section")
public class Section extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "up_station_id")
    private Station upStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "down_station_id")
    private Station downStation;

    @ManyToOne(fetch = FetchType.LAZY)
    private Line line;

    @Embedded
    private Distance distance;

    protected Section() {

    }

    private Section(Station upStation, Station downStation, Long distance) {
        this.upStation = upStation;
        this.downStation = downStation;
        this.distance = new Distance(distance);
    }

    public static Section of(Station upStation, Station downStation, Long distance) {
        return new Section(upStation, downStation, distance);
    }

    public void addLine(Line line) {
        line.addSection(this);
        this.line = line;
    }

    public Section addBetweenSection(Section newSection) {
        if (isEqualsUpStation(newSection)) {
            return rePositionUpStation(newSection);
        }

        if (isEqualsDownStation(newSection)) {
            return rePositionDownStation(newSection);
        }

        throw new CannotRegisterException(ExceptionType.IS_ADD_BETWEEN_STATION);
    }

    private Section rePositionUpStation(Section section) {
        this.upStation = section.getDownStation();
        this.distance.minus(section.getDistance());
        return section;
    }

    private Section rePositionDownStation(Section section) {
        this.downStation = section.getUpStation();
        this.distance.minus(section.getDistance());
        return section;
    }

    public void relocate(Section target) {
        this.downStation = target.downStation;
        this.distance.plus(target.getDistance());
    }

    public boolean isContains(Station station) {
        return isEqualsUpStation(station) || isEqualsDownStation(station);
    }

    public boolean isStationConnectable(Section newSection) {
        return isEqualsUpStation(newSection) || isEqualsDownStation(newSection);
    }

    public boolean isEqualsUpStation(Section section) {
        return upStation.equals(section.getUpStation());
    }

    public boolean isEqualsDownStation(Section section) {
        return downStation.equals(section.getDownStation());
    }

    public boolean isEqualsUpStation(Station station) {
        return upStation.equals(station);
    }

    public boolean isEqualsDownStation(Station station) {
        return downStation.equals(station);
    }

    public Long getId() {
        return id;
    }

    public Station getUpStation() {
        return upStation;
    }

    public Long getUpStationId() {
        return upStation.getId();
    }

    public Long getDownStationId() {
        return downStation.getId();
    }

    public Station getDownStation() {
        return downStation;
    }

    public Distance getDistance() {
        return distance;
    }

    public Long getDistanceValue() {
        return distance.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        Section section = (Section) o;
        return Objects.equals(getId(), section.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    public static Section empty() {
        return new Section();
    }

    public boolean isEmpty() {
        if (this.id != null) return false;
        if (this.upStation != null) return false;
        if (this.downStation != null) return false;
        if (this.line != null) return false;
        if (this.distance != null) return false;

        return true;
    }
}