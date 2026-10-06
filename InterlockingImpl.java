import java.util.HashMap;
import java.util.Map;

public class InterlockingImpl implements Interlocking {

    private final Map<Integer, String> sections;
    private final Map<String, Integer> trains;

    public InterlockingImpl() {
        sections = new HashMap<>();
        trains = new HashMap<>();

        for (int i = 1; i <= 11; i++) {
            sections.put(i, null);
        }
    }

    @Override
    public void addTrain(String trainName,
                         int entryTrackSection,
                         int destinationTrackSection)
            throws IllegalArgumentException, IllegalStateException {

        // To be implemented later
    }

    @Override
    public int moveTrains(String[] trainNames)
            throws IllegalArgumentException {

        // To be implemented later
        return 0;
    }

    @Override
    public String getSection(int trackSection)
            throws IllegalArgumentException {

        if (trackSection < 1 || trackSection > 11) {
            throw new IllegalArgumentException("Invalid track section");
        }

        return sections.get(trackSection);
    }

    @Override
    public int getTrain(String trainName)
            throws IllegalArgumentException {

        // To be implemented later
        return -1;
    }
}
