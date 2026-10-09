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
    private final Map<String, int[]> freightRoutes;

    private final Map<String, int[]> trainRoutes;

    public InterlockingImpl() {
        sections = new HashMap<>();
        trains = new HashMap<>();
        trainRoutes = new HashMap<>();

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

        freightRoutes = new HashMap<>();

        freightRoutes.put("3-4", new int[] {3, 4});
        freightRoutes.put("3-11", new int[] {3, 7, 11});
        freightRoutes.put("4-3", new int[] {4, 3});
        freightRoutes.put("11-3", new int[] {11, 7, 3});
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

    private String routeKey(int entryTrackSection,
                            int destinationTrackSection) {

        return entryTrackSection + "-" + destinationTrackSection;
    }

    private boolean hasDefinedRoute(int entryTrackSection,
                                    int destinationTrackSection) {

        String key = routeKey(entryTrackSection, destinationTrackSection);

        return passengerRoutes.containsKey(key)
                || freightRoutes.containsKey(key);
    }

    private int[] getDefinedRoute(int entryTrackSection,
                                  int destinationTrackSection) {

        String key = routeKey(entryTrackSection, destinationTrackSection);

        if (passengerRoutes.containsKey(key)) {
            return passengerRoutes.get(key);
        }

        return freightRoutes.get(key);
    }

    private int getNextSection(String trainName) {

        int currentSection = trains.get(trainName);
        int[] route = trainRoutes.get(trainName);

        for (int i = 0; i < route.length - 1; i++) {
            if (route[i] == currentSection) {
                return route[i + 1];
            }
        }

        return -1;
    }

    private boolean isAtDestination(String trainName) {

        int currentSection = trains.get(trainName);
        int[] route = trainRoutes.get(trainName);

        int destinationSection = route[route.length - 1];

        return currentSection == destinationSection;
    }

    private String getJunctionForTransition(int currentSection,
                                            int nextSection) {

        // Junction J1
        if ((currentSection == 1 && nextSection == 5)
                || (currentSection == 6 && nextSection == 2)
                || (currentSection == 3 && nextSection == 4)
                || (currentSection == 4 && nextSection == 3)) {

            return "J1";
        }

        // Junction J2
        if ((currentSection == 5 && nextSection == 8)
                || (currentSection == 5 && nextSection == 9)
                || (currentSection == 9 && nextSection == 6)
                || (currentSection == 10 && nextSection == 6)) {

            return "J2";
        }

        return null;
    }

    @Override
    public void addTrain(String trainName,
                         int entryTrackSection,
                         int destinationTrackSection)
            throws IllegalArgumentException, IllegalStateException {

        if (trainName == null || trainName.isEmpty()) {
            throw new IllegalArgumentException("Invalid train name");
        }

        if (trains.containsKey(trainName)) {
            throw new IllegalArgumentException("Train name already exists");
        }

        if (!isValidEntryDestination(entryTrackSection,
                                     destinationTrackSection)) {
            throw new IllegalArgumentException(
                    "Invalid entry or destination");
        }

        if (!hasDefinedRoute(entryTrackSection,
                             destinationTrackSection)) {
            throw new IllegalArgumentException("No valid route");
        }

        if (sections.get(entryTrackSection) != null) {
            throw new IllegalStateException(
                    "Entry track section is occupied");
        }

        sections.put(entryTrackSection, trainName);
        trains.put(trainName, entryTrackSection);

        int[] route = getDefinedRoute(entryTrackSection,
                                      destinationTrackSection);

        trainRoutes.put(trainName, route);
    }

    @Override
    public int moveTrains(String[] trainNames)
            throws IllegalArgumentException {

        int movedTrains = 0;

        // Save the state before this batch starts.
        Map<Integer, String> originalSections =
                new HashMap<>(sections);

        // Prevent two trains from moving to the same section.
        Set<Integer> reservedSections = new HashSet<>();

        // Prevent two trains from using the same junction
        // during one moveTrains call.
        Set<String> reservedJunctions = new HashSet<>();

        // Validate all train names before making any changes.
        for (String trainName : trainNames) {

            if (!trains.containsKey(trainName)) {
                throw new IllegalArgumentException(
                        "Train does not exist");
            }

            if (trains.get(trainName) == -1) {
                throw new IllegalArgumentException(
                        "Train is no longer in the rail corridor");
            }
        }

        for (String trainName : trainNames) {

            int currentSection = trains.get(trainName);

            // A train at its destination exits on its next move.
            if (isAtDestination(trainName)) {
                sections.put(currentSection, null);
                trains.put(trainName, -1);

                movedTrains++;
                continue;
            }

            int nextSection = getNextSection(trainName);

            if (nextSection == -1) {
                continue;
            }

            // The destination section must have been empty
            // before the whole batch started.
            if (originalSections.get(nextSection) != null) {
                continue;
            }

            // Another train in this batch must not already
            // have reserved the same destination section.
            if (reservedSections.contains(nextSection)) {
                continue;
            }

            String junction =
                    getJunctionForTransition(currentSection,
                                             nextSection);

            // Only one train can use a junction in this batch.
            if (junction != null
                    && reservedJunctions.contains(junction)) {
                continue;
            }

            reservedSections.add(nextSection);

            if (junction != null) {
                reservedJunctions.add(junction);
            }

            sections.put(currentSection, null);
            sections.put(nextSection, trainName);
            trains.put(trainName, nextSection);

            movedTrains++;
        }

        return movedTrains;
    }

    @Override
    public String getSection(int trackSection)
            throws IllegalArgumentException {

        if (trackSection < 1 || trackSection > 11) {
            throw new IllegalArgumentException(
                    "Invalid track section");
        }

        return sections.get(trackSection);
    }

    @Override
    public int getTrain(String trainName)
            throws IllegalArgumentException {

        if (!trains.containsKey(trainName)) {
            throw new IllegalArgumentException(
                    "Train does not exist");
        }

        return trains.get(trainName);
    }
}
