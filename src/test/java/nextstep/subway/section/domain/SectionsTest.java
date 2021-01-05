package nextstep.subway.section.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import nextstep.subway.station.domain.Station;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SectionsTest {

	private Sections sections;
	private Map<String, Section> sectionMap;
	private Map<String, Station> stationMap;

	@BeforeEach
	void setUp() {
		stationMap = new HashMap<>();
		sectionMap = new HashMap<>();

		Station 강남역 = new Station("강남역");
		Station 역삼역 = new Station("역삼역");
		Station 교대역 = new Station("교대역");
		Station 선릉역 = new Station("선릉역");
		Station 잠실역 = new Station("잠실역");
		Station 강변역 = new Station("강변역");
		ReflectionTestUtils.setField(강남역, "id", 1L);
		ReflectionTestUtils.setField(역삼역, "id", 2L);
		ReflectionTestUtils.setField(교대역, "id", 3L);
		ReflectionTestUtils.setField(선릉역, "id", 4L);
		ReflectionTestUtils.setField(잠실역, "id", 5L);
		ReflectionTestUtils.setField(강변역, "id", 6L);
		stationMap.put("강남역", 강남역);
		stationMap.put("역삼역", 역삼역);
		stationMap.put("교대역", 교대역);
		stationMap.put("선릉역", 선릉역);
		stationMap.put("잠실역", 잠실역);
		stationMap.put("강변역", 강변역);

		Section section = new Section(강남역, 역삼역, 2);
		Section upSection = new Section(교대역, 강남역, 2);
		Section downSection = new Section(역삼역, 선릉역, 2);
		Section unLinkedSection = new Section(잠실역, 강변역, 2);
		ReflectionTestUtils.setField(section, "id", 1L);
		ReflectionTestUtils.setField(upSection, "id", 2L);
		ReflectionTestUtils.setField(downSection, "id", 3L);
		ReflectionTestUtils.setField(unLinkedSection, "id", 4L);

		sectionMap.put("section", section);
		sectionMap.put("upSection", upSection);
		sectionMap.put("downSection", downSection);
		sectionMap.put("unLinkedSection", unLinkedSection);

		List<Section> newSections = new ArrayList<>();
		newSections.add(sectionMap.get("section"));
		sections = new Sections(newSections);
	}

	@DisplayName("정렬된 역 정보 확인")
	@Test
	void stationsInOrder() {
		//given
		sections.add(sectionMap.get("upSection"));
		sections.add(sectionMap.get("downSection"));

		//when
		List<Station> stations = sections.stationsInOrder();

		//then
		Section upSection = sectionMap.get("upSection");
		Section section = sectionMap.get("section");
		Section downSection = sectionMap.get("downSection");
		assertThat(stations).containsExactly(upSection.getUpStation(), upSection.getDownStation(),
			  section.getDownStation(), downSection.getDownStation());
	}

	@DisplayName("등록된 구간 앞 또는 뒤에 구간을 추가한다.")
	@Test
	void addSection() {
		//when
		sections.add(sectionMap.get("upSection"));
		sections.add(sectionMap.get("downSection"));

		//then
		assertThat(sections.getSections()).hasSize(3);
	}

	@DisplayName("등록된 구간과 연결되지 않은 경우 구간을 추가할 수 없다.")
	@Test
	void addSectionNoLinked() {
		//when,then
		assertThatIllegalArgumentException()
			  .isThrownBy(() -> sections.add(sectionMap.get("unLinkedSection")))
			  .withMessage("연결가능한 구간정보가 없습니다.");
	}

	@DisplayName("동일한 구간을 추가할 경우 추가할 수 없다.")
	@Test
	void addSameSection() {
		//when,then
		assertThatIllegalArgumentException()
			  .isThrownBy(() -> sections.add(sectionMap.get("section")))
			  .withMessage("이미 등록된 구간과 중복되거나, 추가할 수 없는 비정상적인 구간입니다.");
	}

	@DisplayName("다른 구간에 등록된 역을 추가할 수 없다.")
	@Test
	void addWrongSection() {
		//given
		Section newSection = new Section(stationMap.get("역삼역"), stationMap.get("강남역"), 2);

		//when,then
		assertThatIllegalArgumentException()
			  .isThrownBy(() -> sections.add(newSection))
			  .withMessage("이미 등록된 구간과 중복되거나, 추가할 수 없는 비정상적인 구간입니다.");
	}

	@DisplayName("역 사이에 새로운 역을 등록한다.")
	@Test
	void addSectionInner() {
		//given
		Section section = new Section(stationMap.get("강남역"), stationMap.get("선릉역"), 5);
		Section innerSection = new Section(stationMap.get("강남역"), stationMap.get("역삼역"), 2);

		List<Section> sectionList = new ArrayList<>();
		sectionList.add(section);

		Sections sections = new Sections(sectionList);

		//when
		sections.add(innerSection);

		//then
		Section expected1 = new Section(stationMap.get("강남역"), stationMap.get("역삼역"), 2);
		Section expected2 = new Section(stationMap.get("역삼역"), stationMap.get("선릉역"), 3);

		assertThat(sections.getSections()).hasSize(2);
		assertThat(sections.getSections()).containsAll(Arrays.asList(expected1, expected2));
	}

	@DisplayName("역 사이에 새로운 역을 등록한다. - 상행역이 같은 경우")
	@Test
	void addSectionInner2() {
		//given
		sections.add(sectionMap.get("upSection"));
		sections.add(sectionMap.get("downSection"));

		//when
		Section section = new Section(stationMap.get("강남역"), stationMap.get("강변역"), 1);
		sections.add(section);

		//then
		Section expectedSection = new Section(stationMap.get("강변역"), stationMap.get("역삼역"), 1);
		assertThat(sections.getSections()).containsAll(Arrays.asList(section, expectedSection));
	}

	@DisplayName("역 사이에 새로운 역을 등록한다. - 하행역이 같은 경우")
	@Test
	void addSectionInner3() {
		//given
		sections.add(sectionMap.get("upSection"));
		sections.add(sectionMap.get("downSection"));

		//when
		Section section = new Section(stationMap.get("강변역"), stationMap.get("역삼역"), 1);
		sections.add(section);

		//then
		Section expected = new Section(stationMap.get("강남역"), stationMap.get("강변역"), 1);
		assertThat(sections.getSections()).containsAll(Arrays.asList(section, expected));
	}

	@DisplayName("역 사이에 새로운 역을 등록할 경우 거리가 같거나 길면 추가할 수 없다")
	@Test
	void addSectionInnerWithWrongDistance() {
		//given
		Section section = new Section(stationMap.get("강남역"), stationMap.get("선릉역"), 5);
		Section innerSection = new Section(stationMap.get("강남역"), stationMap.get("역삼역"), 5);

		List<Section> sectionList = new ArrayList<>();
		sectionList.add(section);

		Sections sections = new Sections(sectionList);

		//when, then
		assertThatIllegalArgumentException()
			  .isThrownBy(() -> sections.add(innerSection))
			  .withMessage("추가하려는 구간의 길이는 기존 구간의 길이보다 작아야 합니다.");
	}

	@DisplayName("다른 구간의 상행역과 다른 구간의 하행역을 새로운 구간으로 등록할 수 없다.")
	@Test
	void addSectionAlreadyRegisteredStations() {
		//given
		Section section1 = new Section(stationMap.get("강남역"), stationMap.get("역삼역"), 5);
		Section section2 = new Section(stationMap.get("역삼역"), stationMap.get("선릉역"), 5);
		Section wrongSection = new Section(stationMap.get("강남역"), stationMap.get("선릉역"), 10);

		List<Section> sectionList = new ArrayList<>();
		sectionList.add(section1);
		sectionList.add(section2);

		Sections sections = new Sections(sectionList);

		//when
		assertThatIllegalArgumentException()
			  .isThrownBy(() -> sections.add(wrongSection))
			  .withMessage("이미 등록된 구간과 중복되거나, 추가할 수 없는 비정상적인 구간입니다.");
	}

	@DisplayName("상행 종점 또는 하행종점역을 삭제한다.")
	@Test
	void deleteStationEndOfSection() {
		//given
		Section upSection = sectionMap.get("upSection");
		Section downSection = sectionMap.get("downSection");
		sections.add(upSection);
		sections.add(downSection);

		//when
		sections.deleteSection(upSection.getUpStation().getId());
		sections.deleteSection(downSection.getDownStation().getId());

		//then
		assertThat(sections.getSections()).containsExactly(sectionMap.get("section"));
	}

	@DisplayName("구간 중간에 있는 역 삭제하기")
	@Test
	void deleteStationInMiddleOfSection() {
		//given
		Section upSection = sectionMap.get("upSection");
		Section section = sectionMap.get("section");
		Section expected = new Section(upSection.getUpStation(), section.getDownStation(),
			  upSection.getDistance() + section.getDistance());
		sections.add(upSection);

		//when
		sections.deleteSection(upSection.getDownStation().getId());

		//then
		List<Section> actual = this.sections.getSections();
		assertThat(actual).hasSize(1);
		assertThat(actual.get(0).getUpStation()).isEqualTo(expected.getUpStation());
		assertThat(actual.get(0).getDownStation()).isEqualTo(expected.getDownStation());
		assertThat(actual.get(0).getDistance()).isEqualTo(expected.getDistance());
	}

	@DisplayName("구간에 등록되지 않은 역은 삭제할 수 없다.")
	@Test
	void deleteSectionWitWrongStation() {
		//given
		sections.add(sectionMap.get("downSection"));
		Station station = stationMap.get("강변역");

		//when
		assertThatThrownBy(() -> sections.deleteSection(station.getId()))
			  .isInstanceOf(NoSuchElementException.class)
			  .hasMessage("해당구간에 존재하지 않는 역입니다.");
	}

	@DisplayName("구간이 하나밖에 없는 경우 삭제할 수 없다.")
	@Test
	void deleteSectionWhenSize1() {
		//when
		Station station = stationMap.get("강남역");
		assertThatThrownBy(() -> sections.deleteSection(station.getId()))
			  .isInstanceOf(IllegalArgumentException.class)
			  .hasMessage("등록된 구간이 최소 2개 이상일때 삭제할 수 있습니다.");
	}
}
