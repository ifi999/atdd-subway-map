package subway.line.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import subway.line.dto.LineRequest;
import subway.line.dto.LineResponse;
import subway.line.dto.LineUpdateRequest;
import subway.line.dto.SectionRequest;
import subway.line.entity.Line;
import subway.line.entity.Section;
import subway.line.entity.Sections;
import subway.line.repository.LineRepository;
import subway.line.repository.SectionRepository;
import subway.station.entity.Station;
import subway.station.repository.StationRepository;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LineService {

    private final LineRepository lineRepository;
    private final StationRepository stationRepository;
    private final SectionRepository sectionRepository;

    public LineService(final LineRepository lineRepository, final StationRepository stationRepository, final SectionRepository sectionRepository) {
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
        this.sectionRepository = sectionRepository;
    }

    @Transactional
    public LineResponse createSubwayLine(final LineRequest request) {
        final String lineName = request.getName();
        final String lineColor = request.getColor();
        final Station upStation = this.findStationById(request.getUpStationId());
        final Station downStation = this.findStationById(request.getDownStationId());
        final int lineDistance = request.getDistance();

        final Line newLine = new Line(lineName, lineColor, upStation, downStation, lineDistance);
        final Line savedLine = lineRepository.save(newLine);

        return LineResponse.convertToDto(savedLine);
    }

    public LineResponse getSubwayLine(final Long lindId) {
        final Line line = this.findLineById(lindId);

        return LineResponse.convertToDto(line);
    }

    public List<LineResponse> getSubwayLines() {
        return lineRepository.findAll().stream()
                .map(LineResponse::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateSubwayLine(final Long id, final LineUpdateRequest request) {
        final Line line = this.findLineById(id);

        final String name = request.getName();
        final String color = request.getColor();
        line.updateDetails(name, color);
    }

    @Transactional
    public void deleteSubwayLine(final Long lineId) {
        this.findLineById(lineId);
        lineRepository.deleteById(lineId);
    }

    private Station findStationById(final Long stationId) {
        return stationRepository.findById(stationId)
                .orElseThrow(() -> new EntityNotFoundException("Station not found. Station Id: " + stationId));
    }

    private Line findLineById(final Long lindId) {
        return lineRepository.findById(lindId)
                .orElseThrow(() -> new EntityNotFoundException("Line not found. Line Id: " + lindId));
    }

    @Transactional
    public LineResponse createLineSection(final Long lineId, final SectionRequest request) {
        final Line line = this.findLineById(lineId);

        Sections sections = line.getSections();
        Long upStationId = request.getUpStationId();
        Long downStationId = request.getDownStationId();

        if (sections.isSectionRegistered(upStationId, downStationId)) {
            throw new IllegalArgumentException();
        }

        this.validateNoDuplicateDownStation(request);

        final Station requestUpStation = this.findStationById(upStationId);
        final Station requestDownStation = this.findStationById(downStationId);

        final Section section = new Section(line, requestUpStation, requestDownStation, request.getDistance());
        sectionRepository.save(section);

        line.addSection(section);

        return LineResponse.convertToDto(line);
    }

    private void validateNoDuplicateDownStation(final SectionRequest request) {
        final List<Section> SectionListByDownStationId = sectionRepository.findByDownStationId(request.getDownStationId());
        for (Section section : SectionListByDownStationId) {
            if(this.isAlreadyRegistered(request, section)) {
                throw new IllegalArgumentException();
            }
        }
    }

    private boolean isAlreadyRegistered(final SectionRequest request, final Section section) {
        return request.getUpStationId().equals(section.getDownStation().getId());
    }

    @Transactional
    public void deleteLineSection(final Long lineId, final Long stationId) {
        Line line = this.findLineById(lineId);

        if (line.getSections().size() == 1) {
            throw new IllegalArgumentException();
        }

        List<Section> sections = line.getSections().getSections();
        for (Section section : sections) {
            if (section.getDownStation().getId().equals(stationId)) {
                line.subtractLineDistance(section.getDistance());
                break;
            }
        }

    }
}
