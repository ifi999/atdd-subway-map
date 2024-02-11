package subway.line.entity;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;

@Embeddable
public class Sections {

    @OrderBy("id ASC")
    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL)
    private List<Section> sections = new ArrayList<>();;

    protected Sections() {
    }

    private Sections(final List<Section> sections) {
        this.sections = sections;
    }

    public void addSection(final Section section) {
        this.sections.add(section);
    }

    public void removeSection(final Section section) {
        this.sections.remove(section);
    }

    public static Sections from(final List<Section> sections) {
        return new Sections(sections);
    }

    public int size() {
        return sections.size();
    }

    public boolean isSectionRegistered(final long upStationId, final long downStationId) {
        return this.getSections().stream()
                .anyMatch(section ->
                        section.getUpStation().getId().equals(upStationId) && section.getDownStation().getId().equals(downStationId)
                );
    }

    public void ensureNoDuplicateDownStation(final Section newSection) {
        final boolean hasDuplicateDownStation = this.getSections().stream()
                .anyMatch(section -> newSection.getDownStation().getId().equals(section.getUpStation().getId()));

        if (hasDuplicateDownStation) throw new IllegalArgumentException();
    }

    public List<Section> getSections() {
        return sections;
    }
}
