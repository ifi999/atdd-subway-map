package subway;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import subway.line.LineRepository;
import subway.line.LineRequest;
import subway.line.LineUpdateRequest;
import subway.station.Station;
import subway.station.StationRepository;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 노선 관련 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class LineAcceptanceTest {

    @Autowired
    private StationRepository stationRepository;
    @Autowired
    private LineRepository lineRepository;

    private Long 강남역_ID;
    private Long 역삼역_ID;
    private Long 지하철역_ID;

    @BeforeEach
    void setUp() {
        Station 강남역 = stationRepository.save(new Station("강남역"));
        강남역_ID = 강남역.getId();

        Station 역삼역 = stationRepository.save(new Station("역삼역"));
        역삼역_ID = 역삼역.getId();

        Station 지하철역 = stationRepository.save(new Station("지하철역"));
        지하철역_ID = 지하철역.getId();
    }

    @AfterEach
    void tearDown() {
        lineRepository.deleteAll();
        stationRepository.deleteAll();
    }

    /**
     * When 지하철 노선을 생성하면
     * Then 지하철 노선 목록 조회 시 생성한 노선을 찾을 수 있다
     */
    @DisplayName("지하철 노선을 생성한다.")
    @Test
    void 지하철_노선_생성() {
        // when
        final LineRequest request = new LineRequest("신분당선", "bg-red-600", 강남역_ID, 역삼역_ID, 10);
        final ExtractableResponse<Response> response = createSubwayLine(request);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        final JsonPath jsonPath = this.getSubwayLineList();

        final List<String> lineNames = jsonPath.getList("name", String.class);
        assertThat(lineNames).hasSize(1);
        assertThat(lineNames).containsAnyOf("신분당선");

        final List<Station> lineStations = jsonPath.getList("[0].stations", Station.class);
        assertThat(lineStations).hasSize(2);

        final List<String> lineStationNames = jsonPath.getList("[0].stations.name", String.class);
        assertThat(lineStationNames).containsExactlyInAnyOrder("강남역", "역삼역");
    }

    /**
     * Given 2개의 지하철 노선을 생성하고
     * When 지하철 노선 목록을 조회하면
     * Then 지하철 노선 목록 조회 시 2개의 노선을 조회할 수 있다.
     */
    @DisplayName("지하철 노선 목록을 조회한다.")
    @Test
    void 지하철_노선_목록_조회() {
        // given
        final LineRequest request1 = new LineRequest("신분당선", "bg-red-600", 강남역_ID, 역삼역_ID, 10);
        createSubwayLine(request1);

        final LineRequest request2 = new LineRequest("지하철노선", "bg-green-600", 강남역_ID, 지하철역_ID, 15);
        createSubwayLine(request2);

        // when
        final JsonPath jsonPath = this.getSubwayLineList();

        // then
        final List<String> lineNames = jsonPath.getList("name", String.class);
        assertThat(lineNames).hasSize(2);
        assertThat(lineNames).containsExactly("신분당선", "지하철노선");

        final List<String> lineStationNames = jsonPath.getList("[1].stations.name", String.class);
        assertThat(lineStationNames).doesNotContain("역삼역");
        assertThat(lineStationNames).containsExactly("강남역", "지하철역");
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 조회하면
     * Then 생성한 지하철 노선의 정보를 응답받을 수 있다.
     */
    @DisplayName("지하철 노선을 조회한다.")
    @Test
    void 지하철_노선_조회() {
        // given
        final LineRequest request = new LineRequest("신분당선", "bg-red-600", 강남역_ID, 역삼역_ID, 10);
        ExtractableResponse<Response> createSubwayLineResponse = createSubwayLine(request);

        final String location = createSubwayLineResponse.header("Location");
        final String subwayLineId = location.replaceAll(".*/(\\d+)$", "$1");

        // when
        JsonPath jsonPath = this.getSubwayLine(subwayLineId);

        // then
        String lineName = jsonPath.get("name");
        assertThat(lineName).isEqualTo("신분당선");

        String lineColor = jsonPath.get("color");
        assertThat(lineColor).isEqualTo("bg-red-600");

        List<Station> lineStations = jsonPath.getList("stations", Station.class);
        assertThat(lineStations).hasSize(2);

        List<String> lineStationNames = jsonPath.getList("stations.name", String.class);
        assertThat(lineStationNames).containsExactly("강남역", "역삼역");
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 수정하면
     * Then 해당 지하철 노선 정보는 수정된다
     */
    @DisplayName("지하철 노선을 수정한다.")
    @Test
    void 지하철_노선_수정() {
        // given
        final LineRequest createRequest = new LineRequest("신분당선", "bg-red-600", 강남역_ID, 역삼역_ID, 10);
        ExtractableResponse<Response> createSubwayLineResponse = createSubwayLine(createRequest);

        final String location = createSubwayLineResponse.header("Location");
        final String subwayLineId = location.replaceAll(".*/(\\d+)$", "$1");

        final LineUpdateRequest updateRequest = new LineUpdateRequest("2호선", "bg-yellow-600");

        // when
        given()
            .log().all()
            .body(updateRequest)
            .contentType(ContentType.JSON)
        .when()
            .put("lines/{id}", subwayLineId)
        .then()
            .statusCode(HttpStatus.OK.value())
            .log().all();

        // then
        JsonPath afterUpdatedSubwayLine = this.getSubwayLine(subwayLineId);

        String updatedName = afterUpdatedSubwayLine.get("name");
        assertThat(updatedName).isEqualTo("2호선");

        String updatedColor = afterUpdatedSubwayLine.get("color");
        assertThat(updatedColor).isEqualTo("bg-yellow-600");
    }

    private ExtractableResponse<Response> createSubwayLine(LineRequest request) {
        final ExtractableResponse<Response> response =
                given()
                    .log().all()
                    .body(request)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                    .post("/lines")
                .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .log().all()
                .extract();

        return response;
    }

    private JsonPath getSubwayLineList() {
        final JsonPath response =
                given()
                    .log().all()
                .when()
                    .get("/lines")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .log().all()
                .extract()
                    .jsonPath();

        return response;
    }

    private JsonPath getSubwayLine(String subwayLineId) {
        return given()
                .when()
                .get("/lines/{id}", subwayLineId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .log().all()
                .extract()
                .jsonPath();
    }

}
