package subway.line;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
public class LineController {

    private final LineService lineService;

    public LineController(LineService lineService) {
        this.lineService = lineService;
    }

    @PostMapping("/lines")
    public ResponseEntity<LineResponse> createSubwayLine(final @RequestBody LineRequest request) {
        final LineResponse subwayLine = lineService.createSubwayLine(request);

        return ResponseEntity.created(URI.create("/line/"+subwayLine.getId())).body(subwayLine);
    }

    @GetMapping("/lines")
    public ResponseEntity<List<LineResponse>> getSubwayLines() {
        final List<LineResponse> subwayLines = lineService.getSubwayLines();

        return ResponseEntity.ok(subwayLines);
    }

    @GetMapping("/lines/{id}")
    public ResponseEntity<LineResponse> getSubwayLine(final @PathVariable Long id) {
        final LineResponse subwayLine = lineService.getSubwayLine(id);

        return ResponseEntity.ok(subwayLine);
    }

    @PutMapping("/lines/{id}")
    public ResponseEntity<Void> updateSubwayLine(final @PathVariable Long id, final @RequestBody LineUpdateRequest request) {
        lineService.updateSubwayLine(id, request);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/lines/{id}")
    public ResponseEntity<Void> deleteSubwayLine(final @PathVariable Long id) {
        lineService.deleteSubwayLine(id);

        return ResponseEntity.noContent().build();
    }

}
