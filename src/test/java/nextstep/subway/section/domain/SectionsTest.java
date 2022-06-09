package nextstep.subway.section.domain;

import nextstep.subway.domain.Distance;
import nextstep.subway.domain.Section;
import nextstep.subway.domain.Sections;
import nextstep.subway.domain.Station;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class SectionsTest {

    private static final Station 강남역 = new Station("강남역");
    private static final Station 광교역 = new Station("광교역");
    private static final Station 양재역 = new Station("양재역");

    @ParameterizedTest
    @MethodSource("getSectionsTestParameter")
    void 역_리스트_정렬(Station A, Station B, Station C) {
        // given
        final Sections sections = new Sections();
        Section section1 = new Section(A, C, 10);
        Section section2 = new Section(A, B, 4);
        sections.add(section1);
        sections.add(section2);

        // when
        List<Station> stations = sections.getStationsInOrder();

        // then
        assertAll(
                () -> assertThat(stations).hasSize(3),
                () -> assertThat(section1.getDistance()).isEqualTo(new Distance(6)),
                () -> assertThat(section1.getUpStation()).isEqualTo(B),
                () -> assertThat(toStationNames(stations)).containsExactly(A.getName(), B.getName(), C.getName())
        );
    }

    private static Stream<Arguments> getSectionsTestParameter() {
        return Stream.of(
                arguments(강남역, 광교역, 양재역),
                arguments(양재역, 광교역, 강남역),
                arguments(광교역, 양재역, 강남역)
        );
    }

    @Test
    void 새로운_역을_상행_종점으로_등록할_경우() {
        // given
        final Sections sections = new Sections();
        Section section1 = new Section(강남역, 광교역, 7);
        Section section2 = new Section(양재역, 강남역, 4);
        sections.add(section1);

        // when
        sections.add(section2);
        List<Station> stations = sections.getStationsInOrder();

        // then
        assertAll(
                () -> assertThat(toStationNames(stations)).containsExactly(양재역.getName(), 강남역.getName(), 광교역.getName()),
                () -> assertThat(section1.getDistance()).isEqualTo(new Distance(7)),
                () -> assertThat(section2.getDistance()).isEqualTo(new Distance(4))
        );

    }

    @DisplayName("역 사이에 새로운 역을 등록할 경우 기존 역 사이 길이보다 크거나 같으면 등록을 할 수 없음")
    @Test
    void 구간_등록_시_예외_케이스() {
        // given
        final Sections sections = new Sections();
        Section section1 = new Section(강남역, 광교역, 10);
        Section section2 = new Section(강남역, 양재역, 10);
        sections.add(section1);

        // when & then
        assertThatThrownBy(() -> sections.add(section2)).isInstanceOf(IllegalArgumentException.class);
    }

    private List<String> toStationNames(List<Station> stations) {
        return stations
                .stream()
                .map(Station::getName)
                .collect(toList());
    }

}