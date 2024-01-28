package subway.station;

import subway.line.entity.Line;

import javax.persistence.*;
import java.util.List;

@Entity
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 20, nullable = false)
    private String name;

    @OneToMany(mappedBy = "upStation", cascade = CascadeType.ALL)
    private List<Line> linesAsUpStation;

    @OneToMany(mappedBy = "downStation", cascade = CascadeType.ALL)
    private List<Line> linesAsDownStation;

    protected Station() {
    }

    public Station(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Line> getLinesAsUpStation() {
        return linesAsUpStation;
    }

    public List<Line> getLinesAsDownStation() {
        return linesAsDownStation;
    }

    @Override
    public String toString() {
        return "Station{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
