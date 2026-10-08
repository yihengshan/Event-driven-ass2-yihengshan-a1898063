import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class InterlockingImpl implements Interlocking {

    private final Map<Integer, String> sections;
    private final Map<String, Integer> trains;

    private final Set<Integer> southboundEntries;
    private final Set<Integer> southboundDestinations;

    private final Set<Integer> northboundEntries;
    private final Set<Integer> northboundDestinations;

    private final Map<String, int[]> passengerRoutes;

    public InterlockingImpl() {
        sections = new HashMap<>();
        trains = new HashMap<>();

        for (int i = 1; i <= 11; i++) {
            sections.put(i, null);
        }

        southboundEntries = new HashSet<>();
        southboundEntries.add(1);
        southboundEntries.add(3);

        southboundDestinations = new HashSet<>();
        southboundDestinations.add(4);
        southboundDestinations.add(8);
        southboundDestinations.add(9);
        southboundDestinations.add(11);

        northboundEntries = new HashSet<>();
        northboundEntries.add(4);
        northboundEntries.add(9);
        northboundEntries.add(10);
        northboundEntries.add(11);

        northboundDestinations = new HashSet<>();
        northboundDestinations.add(2);
        northboundDestinations.add(3);

        passengerRoutes = new HashMap<>();

        passengerRoutes.put("1-8", new int[] {1, 5, 8});
        passengerRoutes.put("1-9", new int[] {1, 5, 9});
        passengerRoutes.put("9-2", new int[] {9, 6, 2});
        passengerRoutes.put("10-2", new int[] {10, 6, 2});
    }

    private boolean isValidEntryDestination(int entryTrackSection,
                                            int destinationTrackSection) {

        boolean validSouthbound =
                southboundEntries.contains(entryTrackSection)
                && southboundDestinations.contains(destinationTrackSection);

        boolean validNorthbound =
                northboundEntries.contains(entryTrackSection)
                && northboundDestinations.contains(destinationTrackSection);

        return validSouthbound || validNorthbound;
    }

    private String routeKey(int entryTrackSection, int destinationTrackSection) {
        return entryTrackSection + "-" + destinationTrackSection;
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

        if (!trains.containsKey(trainName)) {
            throw new IllegalArgumentException("Train does not exist");
        }

        return trains.get(trainName);
    }
}
