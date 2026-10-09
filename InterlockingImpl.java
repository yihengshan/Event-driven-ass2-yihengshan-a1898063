import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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

        return currentSection == route[route.length - 1];
    }

    private String getJunctionForTransition(int currentSection,
                                            int nextSection) {

        if ((currentSection == 1 && nextSection == 5)
                || (currentSection == 6 && nextSection == 2)
                || (currentSection == 3 && nextSection == 4)
                || (currentSection == 4 && nextSection == 3)) {

            return "J1";
        }

        if ((currentSection == 5 && nextSection == 8)
                || (currentSection == 5 && nextSection == 9)
                || (currentSection == 9 && nextSection == 6)
                || (currentSection == 10 && nextSection == 6)) {

            return "J2";
        }

        return null;
    }

    private boolean isPassengerJ1Transition(int currentSection,
                                            int nextSection) {

        return (currentSection == 1 && nextSection == 5)
                || (currentSection == 6 && nextSection == 2);
    }

    private boolean isFreightJ1Transition(int currentSection,
                                          int nextSection) {

        return (currentSection == 3 && nextSection == 4)
                || (currentSection == 4 && nextSection == 3);
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
            throw new IllegalArgumentException(
                    "Train name already exists");
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

        trainRoutes.put(
                trainName,
                getDefinedRoute(entryTrackSection,
                                destinationTrackSection));
    }

    @Override
    public int moveTrains(String[] trainNames)
            throws IllegalArgumentException {

        if (trainNames == null) {
            throw new IllegalArgumentException(
                    "Train list cannot be null");
        }

        LinkedHashSet<String> requestedTrains =
                new LinkedHashSet<>();

        for (String trainName : trainNames) {

            if (trainName == null
                    || !trains.containsKey(trainName)) {
                throw new IllegalArgumentException(
                        "Train does not exist");
            }

            if (trains.get(trainName) == -1) {
                throw new IllegalArgumentException(
                        "Train is no longer in the rail corridor");
            }

            requestedTrains.add(trainName);
        }

        if (requestedTrains.isEmpty()) {
            return 0;
        }

        Map<Integer, String> originalSections =
                new HashMap<>(sections);

        Map<String, Integer> plannedNextSections =
                new HashMap<>();

        /*
         * Count how many requested trains want each
         * destination section.
         */
        Map<Integer, Integer> destinationCounts =
                new HashMap<>();

        for (String trainName : requestedTrains) {

            int nextSection;

            if (isAtDestination(trainName)) {
                nextSection = -1;
            } else {
                nextSection = getNextSection(trainName);
            }

            plannedNextSections.put(trainName, nextSection);

            if (nextSection != -1) {
                destinationCounts.put(
                        nextSection,
                        destinationCounts.getOrDefault(
                                nextSection, 0) + 1);
            }
        }

        /*
         * Trains that are already at their destination
         * will leave during this call.
         */
        Set<String> exitingTrains = new HashSet<>();

        for (String trainName : requestedTrains) {

            if (plannedNextSections.get(trainName) == -1) {
                exitingTrains.add(trainName);
            }
        }

        /*
         * First determine which normal movements are eligible.
         */
        Set<String> eligibleTrains = new LinkedHashSet<>();

        for (String trainName : requestedTrains) {

            int nextSection =
                    plannedNextSections.get(trainName);

            if (nextSection == -1) {
                continue;
            }

            /*
             * If two or more trains want the same section,
             * none of them may enter it.
             */
            if (destinationCounts.get(nextSection) > 1) {
                continue;
            }

            String occupyingTrain =
                    originalSections.get(nextSection);

            /*
             * An occupied section may only be entered if
             * its current train will directly EXIT during
             * this moveTrains call.
             *
             * We do not allow a chain such as:
             *
             * A: 6 -> 2
             * B: 9 -> 6
             *
             * during the same call.
             */
            if (occupyingTrain != null
                    && !exitingTrains.contains(occupyingTrain)) {
                continue;
            }

            eligibleTrains.add(trainName);
        }

        /*
         * Select transitions while protecting junctions.
         */
        Set<String> selectedTrains = new LinkedHashSet<>();
        Set<String> reservedJunctions = new HashSet<>();

        /*
         * Exiting trains always release their section.
         */
        selectedTrains.addAll(exitingTrains);

        /*
         * Passenger J1 movements first.
         */
        for (String trainName : requestedTrains) {

            if (!eligibleTrains.contains(trainName)) {
                continue;
            }

            int currentSection = trains.get(trainName);
            int nextSection =
                    plannedNextSections.get(trainName);

            if (!isPassengerJ1Transition(
                    currentSection,
                    nextSection)) {
                continue;
            }

            String junction =
                    getJunctionForTransition(
                            currentSection,
                            nextSection);

            if (junction != null
                    && reservedJunctions.contains(junction)) {
                continue;
            }

            selectedTrains.add(trainName);

            if (junction != null) {
                reservedJunctions.add(junction);
            }
        }

        /*
         * Normal non-freight-J1 movements.
         */
        for (String trainName : requestedTrains) {

            if (!eligibleTrains.contains(trainName)
                    || selectedTrains.contains(trainName)) {
                continue;
            }

            int currentSection = trains.get(trainName);
            int nextSection =
                    plannedNextSections.get(trainName);

            if (isFreightJ1Transition(
                    currentSection,
                    nextSection)) {
                continue;
            }

            String junction =
                    getJunctionForTransition(
                            currentSection,
                            nextSection);

            if (junction != null
                    && reservedJunctions.contains(junction)) {
                continue;
            }

            selectedTrains.add(trainName);

            if (junction != null) {
                reservedJunctions.add(junction);
            }
        }

        /*
         * Freight J1 movements last so passenger trains
         * keep priority.
         */
        for (String trainName : requestedTrains) {

            if (!eligibleTrains.contains(trainName)
                    || selectedTrains.contains(trainName)) {
                continue;
            }

            int currentSection = trains.get(trainName);
            int nextSection =
                    plannedNextSections.get(trainName);

            if (!isFreightJ1Transition(
                    currentSection,
                    nextSection)) {
                continue;
            }

            String junction =
                    getJunctionForTransition(
                            currentSection,
                            nextSection);

            if (junction != null
                    && reservedJunctions.contains(junction)) {
                continue;
            }

            selectedTrains.add(trainName);

            if (junction != null) {
                reservedJunctions.add(junction);
            }
        }

        /*
         * Apply all selected transitions together.
         *
         * First free old sections.
         */
        for (String trainName : selectedTrains) {

            int currentSection = trains.get(trainName);

            sections.put(currentSection, null);
        }

        /*
         * Then place moving trains in their new sections.
         */
        for (String trainName : selectedTrains) {

            int nextSection =
                    plannedNextSections.get(trainName);

            if (nextSection == -1) {
                trains.put(trainName, -1);
            } else {
                sections.put(nextSection, trainName);
                trains.put(trainName, nextSection);
            }
        }

        return selectedTrains.size();
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
