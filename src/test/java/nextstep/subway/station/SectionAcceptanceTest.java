package nextstep.subway.station;

import static nextstep.subway.utils.AttdLineHelper.지하철_노선_등록하기;
import static nextstep.subway.utils.AttdLineHelper.지하철_노선_조회하기;
import static nextstep.subway.utils.AttdSectionHelper.지하철_구간_등록하기;
import static nextstep.subway.utils.AttdSectionHelper.지하철_구간_조회하기;
import static nextstep.subway.utils.AttdSectionHelper.지하철_역_삭제하기;
import static nextstep.subway.utils.AttdStationHelper.지하철역_만들기;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.utils.DatabaseCleanup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

@DisplayName("지하철 구간등록")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SectionAcceptanceTest {

    @LocalServerPort
    int port;

    private final DatabaseCleanup databaseCleanup;
    private String 정자역_ID = null;
    private String 미금역_ID = null;
    private String ID_신분당선 = null;

    @Autowired
    public SectionAcceptanceTest(DatabaseCleanup databaseCleanup) {
        this.databaseCleanup = databaseCleanup;
    }

    @BeforeEach
    public void init() {
        RestAssured.port = port;
        databaseCleanup.execute();

        //given
        정자역_ID = 지하철역_만들기("정자역").jsonPath().get("id").toString();
        미금역_ID = 지하철역_만들기("미금역").jsonPath().get("id").toString();
        ID_신분당선 = 지하철_노선_등록하기(
            "신분당선", "bg-red-600", 정자역_ID, 미금역_ID, "10"
        ).jsonPath().get("id").toString();
    }

    /**
     * given 지하철역 2개와, 그를 포함하는 line이 주어지고(init) 미금-정자 given 지하철역 1개를 추가하고 when 해당 지하철역을 line 상향종착지를
     * 변경했을때 then 지하철 구간이 정상적으로 확인되고, 지하철 노선의 상향종착지가 변경된다.
     */
    @Test
    @DisplayName("구간 등록 정상 테스트")
    public void 구간등록_상향종착지변경_테스트() {
        //given
        String 판교역_ID = 지하철역_만들기("판교역").jsonPath().get("id").toString();
        ExtractableResponse<Response> 지하철_구간_등록하기_response = 지하철_구간_등록하기(정자역_ID, 판교역_ID, "5",
            ID_신분당선);

        //when
        ExtractableResponse<Response> 지하철_구간_조회하기_response = 지하철_구간_조회하기(ID_신분당선);
        ExtractableResponse<Response> 지하철_노선_조회하기_response = 지하철_노선_조회하기(ID_신분당선);

        //then
        assertAll(
            () -> assertThat(지하철_구간_등록하기_response.statusCode()).isEqualTo(HttpStatus.OK.value()),
            () -> assertThat(지하철_노선_조회하기_response.jsonPath().getList("stations.name"))
                .contains("판교역", "미금역"),
            () -> assertThat(지하철_구간_조회하기_response.jsonPath().getList("downStationInfo.name"))
                .contains("미금역", "정자역"),
            () -> assertThat(지하철_구간_조회하기_response.jsonPath().getList("upStationInfo.name"))
                .contains("정자역", "판교역"),
            () -> assertThat(지하철_구간_조회하기_response.jsonPath().getList("distance"))
                .contains(10, 5)
        );

    }

    /**
     * given 지하철역 2개와, 그를 포함하는 line이 주어지고(init) 미금-정자 given 지하철역 1개를 추가하고 when 해당 지하철역을 line 하향종착지를
     * 변경했을때 then 지하철 구간이 정상적으로 확인되고, 지하철 노선의 하향종착지가 변경된다.
     */
    @Test
    public void 구간등록_하향종착지변경_테스트() {
        //given
        String 동천역_ID = 지하철역_만들기("동천역").jsonPath().get("id").toString();

        //when
        ExtractableResponse<Response> 지하철_구간_등록하기_response = 지하철_구간_등록하기(동천역_ID, 미금역_ID, "20",
            ID_신분당선);
        ExtractableResponse<Response> 지하철_구간_조회하기_response = 지하철_구간_조회하기(ID_신분당선);
        ExtractableResponse<Response> 지하철_노선_조회하기_response = 지하철_노선_조회하기(ID_신분당선);

        //then
        assertAll(
            () -> assertThat(지하철_구간_등록하기_response.statusCode()).isEqualTo(HttpStatus.OK.value()),
            () -> assertThat(지하철_노선_조회하기_response.jsonPath().getList("stations.name"))
                .contains("동천역", "정자역"),
            () -> assertThat(지하철_구간_조회하기_response.jsonPath().getList("downStationInfo.name"))
                .contains("동천역", "미금역"), //동천 - 미금 - 정자
            () -> assertThat(지하철_구간_조회하기_response.jsonPath().getList("upStationInfo.name"))
                .contains("미금역", "정자역"),
            () -> assertThat(지하철_구간_조회하기_response.jsonPath().getList("distance"))
                .contains(20, 10)
        );
    }

    /**
     * given 지하철역 2개와, 그를 포함하는 line이 주어지고(init) 미금-정자 given 지하철역 2개를 추가 및 구간등록을 한 뒤 when 신규 지하철역을 사이
     * 구간에 삽입시 then 지하철 구간이 정상적으로 확인된다.
     */
    @Test
    public void 구간등록_구간_앞에_추가_테스트() {
        //given /* 성복 - 수지구청 - 미금 - 정자*/
        String 수지구청_ID = 지하철역_만들기("수지구청역").jsonPath().get("id").toString();
        String 성복_ID = 지하철역_만들기("성복역").jsonPath().get("id").toString();
        지하철_구간_등록하기(수지구청_ID, 미금역_ID, "10", ID_신분당선);
        지하철_구간_등록하기(성복_ID, 수지구청_ID, "10", ID_신분당선);
        ExtractableResponse<Response> 지하철_노선_조회하기_response1 = 지하철_노선_조회하기(ID_신분당선);

        //when /* 성복 - 수지구청 - 동천 - 미금 - 정자*/
        String 동천_ID = 지하철역_만들기("동천역").jsonPath().get("id").toString();
        지하철_구간_등록하기(수지구청_ID, 동천_ID, "5", ID_신분당선);
        ExtractableResponse<Response> 지하철_구간_조회하기_response = 지하철_구간_조회하기(ID_신분당선);
        ExtractableResponse<Response> 지하철_노선_조회하기_response = 지하철_노선_조회하기(ID_신분당선);

        //then
        assertAll(
            () -> assertThat(지하철_노선_조회하기_response.jsonPath().getList("stations.name"))
                .contains("성복역", "정자역"),
            () -> assertThat(지하철_구간_조회하기_response.jsonPath().getList("downStationInfo.name"))
                .contains("성복역", "수지구청역", "동천역", "미금역"), //동천 - 미금 - 정자
            () -> assertThat(지하철_구간_조회하기_response.jsonPath().getList("upStationInfo.name"))
                .contains("수지구청역", "동천역", "미금역", "정자역"),
            () -> assertThat(지하철_구간_조회하기_response.jsonPath().getList("distance"))
                .contains(10, 10, 5, 5)
        );
    }


    /**
     * given 지하철역 2개와, 그를 포함하는 line이 주어지고(init) 미금-정자 given 지하철역 2개를 추가 및 구간등록을 한 뒤 when 신규 지하철역을 사이
     * 구간에 삽입시 then 지하철 구간이 정상적으로 확인된다.
     */
    @Test
    public void 구간등록_구간_뒤에_추가_테스트() {
        //given /* 성복 - 수지구청 - 미금 - 정자*/
        String 수지구청_ID = 지하철역_만들기("수지구청역").jsonPath().get("id").toString();
        String 성복_ID = 지하철역_만들기("성복역").jsonPath().get("id").toString();
        지하철_구간_등록하기(수지구청_ID, 미금역_ID, "10", ID_신분당선);
        지하철_구간_등록하기(성복_ID, 수지구청_ID, "10", ID_신분당선);
        ExtractableResponse<Response> 지하철_노선_조회하기_response1 = 지하철_노선_조회하기(ID_신분당선);

        //when /* 성복 - 수지구청 - 동천 - 미금 - 정자*/
        String 동천_ID = 지하철역_만들기("동천역").jsonPath().get("id").toString();
        지하철_구간_등록하기(동천_ID, 미금역_ID, "5", ID_신분당선);
        ExtractableResponse<Response> 지하철_구간_조회하기_response = 지하철_구간_조회하기(ID_신분당선);
        ExtractableResponse<Response> 지하철_노선_조회하기_response = 지하철_노선_조회하기(ID_신분당선);

        //then
        assertAll(
            () -> assertThat(지하철_노선_조회하기_response.jsonPath().getList("stations.name"))
                .contains("성복역", "정자역"),
            () -> assertThat(지하철_구간_조회하기_response.jsonPath().getList("downStationInfo.name"))
                .contains("성복역", "수지구청역", "동천역", "미금역"), //동천 - 미금 - 정자
            () -> assertThat(지하철_구간_조회하기_response.jsonPath().getList("upStationInfo.name"))
                .contains("수지구청역", "동천역", "미금역", "정자역"),
            () -> assertThat(지하철_구간_조회하기_response.jsonPath().getList("distance"))
                .contains(10, 10, 5, 5)
        );
    }

    /**
     * given 지하철역 2개와, 그를 포함하는 line이 주어지고(init) 미금-정자 when 동일한 구간에 대해 재저장을 하면 then 400 에러가 발생한다.
     */
    @Test
    public void 중복저장_에러발생_테스트() {
        //when
        ExtractableResponse<Response> 지하철_구간_등록하기_response = 지하철_구간_등록하기(미금역_ID, 정자역_ID, "5",
            ID_신분당선);

        //then
        assertThat(지하철_구간_등록하기_response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * given distance가 10인 지하철역 2개와, 그를 포함하는 line이 주어지고(init) 미금-정자 when 미금역과 정자역 사이에 거리 11인 역을 추가하면
     * then 400 에러가 발생한다.
     */
    @Test
    public void 거리초과_에러발생_구간뒤에추가_테스트() {
        //when
        String 미금정자사이_ID = 지하철역_만들기("미금정자사이역").jsonPath().get("id").toString();
        ExtractableResponse<Response> 지하철_구간_등록하기_response = 지하철_구간_등록하기(미금정자사이_ID, 정자역_ID, "11",
            ID_신분당선);

        //then
        assertThat(지하철_구간_등록하기_response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * given distance가 10인 지하철역 2개와, 그를 포함하는 line이 주어지고(init) 미금-정자 when 미금역과 정자역 사이에 거리 11인 역을 추가하면
     * then 400 에러가 발생한다.
     */
    @Test
    public void 거리초과_에러발생_구간앞에추가_테스트() {
        //when
        String 미금정자사이_ID = 지하철역_만들기("미금정자사이역").jsonPath().get("id").toString();
        ExtractableResponse<Response> 지하철_구간_등록하기_response = 지하철_구간_등록하기(미금역_ID, 미금정자사이_ID, "11",
            ID_신분당선);

        //then
        assertThat(지하철_구간_등록하기_response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * given distance가 10인 지하철역 2개와, 그를 포함하는 line이 주어지고(init) 미금-정자
     * given 지하철역 2개를 추가 및 구간등록을 한 뒤
     * when 한개의 지하철 역을 삭제하면
     * then 연관된 구간이 연결된다.
     */
    @Test
    public void 노선_구간_삭제_테스트() {
        //given /* 성복 - 수지구청 - 미금 - 정자*/
        String 수지구청_ID = 지하철역_만들기("수지구청역").jsonPath().get("id").toString();
        String 성복_ID = 지하철역_만들기("성복역").jsonPath().get("id").toString();
        지하철_구간_등록하기(수지구청_ID, 미금역_ID, "10", ID_신분당선);
        지하철_구간_등록하기(성복_ID, 수지구청_ID, "10", ID_신분당선);
        ExtractableResponse<Response> 지하철_노선_조회하기_response1 = 지하철_노선_조회하기(ID_신분당선);

        //when
        지하철_역_삭제하기(ID_신분당선, 수지구청_ID);

        //then
        ExtractableResponse<Response> 지하철_구간_조회하기_response = 지하철_구간_조회하기(ID_신분당선);
        ExtractableResponse<Response> 지하철_노선_조회하기_response = 지하철_노선_조회하기(ID_신분당선);

        assertAll(
            () -> assertThat(지하철_노선_조회하기_response.jsonPath().getList("stations.name"))
                .contains("성복역", "정자역"),
            () -> assertThat(지하철_구간_조회하기_response.jsonPath().getList("downStationInfo.name"))
                .contains("성복역", "미금역"),
            () -> assertThat(지하철_구간_조회하기_response.jsonPath().getList("upStationInfo.name"))
                .contains("미금역", "정자역"),
            () -> assertThat(지하철_구간_조회하기_response.jsonPath().getList("distance"))
                .contains(20,10)
        );
    }

    /**
     * given distance가 10인 지하철역 2개와, 그를 포함하는 line이 주어지고(init) 미금-정자
     * given 지하철역 2개를 추가 및 구간등록을 한 뒤
     * when 가장 앞의 역을 삭제하면
     * then line의 시작지점과 distance, 구간들이 변경된다.
     */
    @Test
    public void 노선_구간_삭제_출발지변경_테스트() {
        //given /* 성복 - 수지구청 - 미금 - 정자*/
        String 수지구청_ID = 지하철역_만들기("수지구청역").jsonPath().get("id").toString();
        String 성복_ID = 지하철역_만들기("성복역").jsonPath().get("id").toString();
        지하철_구간_등록하기(수지구청_ID, 미금역_ID, "10", ID_신분당선);
        지하철_구간_등록하기(성복_ID, 수지구청_ID, "10", ID_신분당선);
        ExtractableResponse<Response> 지하철_노선_조회하기_response1 = 지하철_노선_조회하기(ID_신분당선);

        //when
        지하철_역_삭제하기(ID_신분당선, 성복_ID);

        //then
        ExtractableResponse<Response> 지하철_구간_조회하기_response = 지하철_구간_조회하기(ID_신분당선);
        ExtractableResponse<Response> 지하철_노선_조회하기_response = 지하철_노선_조회하기(ID_신분당선);

        assertAll(
            () -> assertThat(지하철_노선_조회하기_response.jsonPath().getList("stations.name"))
                .contains("수지구청역", "정자역"),
            () -> assertThat(지하철_구간_조회하기_response.jsonPath().getList("downStationInfo.name"))
                .contains("수지구청역", "미금역"),
            () -> assertThat(지하철_구간_조회하기_response.jsonPath().getList("upStationInfo.name"))
                .contains("미금역", "정자역"),
            () -> assertThat(지하철_구간_조회하기_response.jsonPath().getList("distance"))
                .contains(10,10)
        );
    }

    /**
     * given distance가 10인 지하철역 2개와, 그를 포함하는 line이 주어지고(init) 미금-정자
     * given 지하철역 2개를 추가 및 구간등록을 한 뒤
     * when 가장 뒤의 역을 삭제하면
     * then line의 종착지점과 distance, 구간들이 변경된다.
     */
    @Test
    public void 노선_구간_삭제_종착지변경_테스트() {
        //given /* 성복 - 수지구청 - 미금 - 정자*/
        String 수지구청_ID = 지하철역_만들기("수지구청역").jsonPath().get("id").toString();
        String 성복_ID = 지하철역_만들기("성복역").jsonPath().get("id").toString();
        지하철_구간_등록하기(수지구청_ID, 미금역_ID, "10", ID_신분당선);
        지하철_구간_등록하기(성복_ID, 수지구청_ID, "10", ID_신분당선);
        ExtractableResponse<Response> 지하철_노선_조회하기_response1 = 지하철_노선_조회하기(ID_신분당선);

        //when
        지하철_역_삭제하기(ID_신분당선, 정자역_ID);

        //then
        ExtractableResponse<Response> 지하철_구간_조회하기_response = 지하철_구간_조회하기(ID_신분당선);
        ExtractableResponse<Response> 지하철_노선_조회하기_response = 지하철_노선_조회하기(ID_신분당선);

        assertAll(
            () -> assertThat(지하철_노선_조회하기_response.jsonPath().getList("stations.name"))
                .contains("성복역", "미금역"),
            () -> assertThat(지하철_구간_조회하기_response.jsonPath().getList("downStationInfo.name"))
                .contains("수지구청역", "성복역"),
            () -> assertThat(지하철_구간_조회하기_response.jsonPath().getList("upStationInfo.name"))
                .contains("미금역", "수지구청역"),
            () -> assertThat(지하철_구간_조회하기_response.jsonPath().getList("distance"))
                .contains(10,10)
        );
    }

    /**
     * given distance가 10인 지하철역 2개와, 그를 포함하는 line이 주어지고(init) 미금-정자
     * when 미금역을 삭제할시
     * then 에러가 반환된다.
     */
    @Test
    public void 노선_구간_삭제_구간이_1개일때_삭제불가() {
        //given (init)

        //when
        ExtractableResponse<Response> 지하철_역_삭제하기_response = 지하철_역_삭제하기(ID_신분당선, 미금역_ID);

        //then
        assertThat(지하철_역_삭제하기_response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * given distance가 10인 지하철역 2개와, 그를 포함하는 line이 주어지고(init) 미금-정자
     * given 수지구청역을 생성만하고 line에 포함을 시키지 않은 뒤에
     * when 수지구청역을 삭제할시
     * then 에러가 반환된다.
     */
    @Test
    public void 노선_구간_삭제_등록되지않은_역은_삭제불가() {
        //given
        String 수지구청_ID = 지하철역_만들기("수지구청역").jsonPath().get("id").toString();

        //when
        ExtractableResponse<Response> 지하철_역_삭제하기_response = 지하철_역_삭제하기(ID_신분당선, 수지구청_ID);

        //then
        assertThat(지하철_역_삭제하기_response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
}