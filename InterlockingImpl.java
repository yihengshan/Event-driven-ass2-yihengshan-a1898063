import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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

        int destinationSection = route[route.length - 1];

        return currentSection == destinationSection;
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

    /*
     * Checks whether the section occupied by another train
     * will eventually be released during this same move batch.
     */
    private boolean canEventuallyMove(
            String trainName,
            Map<String, Integer> plannedNextSections,
            Map<Integer, String> originalSections,
            Set<String> visiting) {

        if (visiting.contains(trainName)) {
            return false;
        }

        Integer nextSection = plannedNextSections.get(trainName);

        if (nextSection == null) {
            return false;
        }

        // -1 means that the train will leave the corridor.
        if (nextSection == -1) {
            return true;
        }

        String occupyingTrain = originalSections.get(nextSection);

        if (occupyingTrain == null) {
            return true;
        }

        if (!plannedNextSections.containsKey(occupyingTrain)) {
            return false;
        }

        visiting.add(trainName);

        boolean result = canEventuallyMove(
                occupyingTrain,
                plannedNextSections,
                originalSections,
                visiting);

        visiting.remove(trainName);

        return result;
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

        int[] route = getDefinedRoute(
                entryTrackSection,
                destinationTrackSection);

        trainRoutes.put(trainName, route);
    }

    @Override
    public int moveTrains(String[] trainNames)
            throws IllegalArgumentException {

        if (trainNames == null) {
            throw new IllegalArgumentException(
                    "Train list cannot be null");
        }

        /*
         * Remove duplicate names while preserving the
         * order supplied to moveTrains().
         */
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

        /*
         * Snapshot of the railway before this batch starts.
         */
        Map<Integer, String> originalSections =
                new HashMap<>(sections);

        /*
         * Store what each requested train wants to do.
         *
         * -1 = leave the rail corridor.
         *  1-11 = move to that track section.
         */
        Map<String, Integer> plannedNextSections =
                new HashMap<>();

        for (String trainName : requestedTrains) {

            if (isAtDestination(trainName)) {
                plannedNextSections.put(trainName, -1);
            } else {
                plannedNextSections.put(
                        trainName,
                        getNextSection(trainName));
            }
        }

        /*
         * Work out which trains are physically able to move.
         *
         * A destination section may currently contain another
         * train if that train will also move away during the
         * same batch.
         */
        Set<String> possibleTrains = new HashSet<>();

        for (String trainName : requestedTrains) {

            if (canEventuallyMove(
                    trainName,
                    plannedNextSections,
                    originalSections,
                    new HashSet<String>())) {

                possibleTrains.add(trainName);
            }
        }

        /*
         * Choose movements while respecting destination
         * section conflicts and junction conflicts.
         */
        Set<String> selectedTrains = new LinkedHashSet<>();

        Set<Integer> reservedSections = new HashSet<>();
        Set<String> reservedJunctions = new HashSet<>();

        /*
         * Trains that are exiting do not need a track section
         * or junction, so select them first.
         */
        for (String trainName : requestedTrains) {

            if (possibleTrains.contains(trainName)
                    && plannedNextSections.get(trainName) == -1) {

                selectedTrains.add(trainName);
            }
        }

        /*
         * Passenger movements through J1 are considered first
         * so passenger trains keep priority over freight trains.
         */
        for (String trainName : requestedTrains) {

            if (!possibleTrains.contains(trainName)) {
                continue;
            }

            int nextSection = plannedNextSections.get(trainName);

            if (nextSection == -1) {
                continue;
            }

            int currentSection = trains.get(trainName);

            if (!isPassengerJ1Transition(
                    currentSection,
                    nextSection)) {

                continue;
            }

            if (reservedSections.contains(nextSection)) {
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
            reservedSections.add(nextSection);

            if (junction != null) {
                reservedJunctions.add(junction);
            }
        }

        /*
         * Process normal movements that are not freight
         * movements through J1.
         */
        for (String trainName : requestedTrains) {

            if (!possibleTrains.contains(trainName)
                    || selectedTrains.contains(trainName)) {

                continue;
            }

            int nextSection = plannedNextSections.get(trainName);

            if (nextSection == -1) {
                continue;
            }

            int currentSection = trains.get(trainName);

            if (isFreightJ1Transition(
                    currentSection,
                    nextSection)) {

                continue;
            }

            if (reservedSections.contains(nextSection)) {
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
            reservedSections.add(nextSection);

            if (junction != null) {
                reservedJunctions.add(junction);
            }
        }

        /*
         * Freight movements through J1 are considered last.
         * This preserves passenger priority.
         */
        for (String trainName : requestedTrains) {

            if (!possibleTrains.contains(trainName)
                    || selectedTrains.contains(trainName)) {

                continue;
            }

            int nextSection = plannedNextSections.get(trainName);

            if (nextSection == -1) {
                continue;
            }

            int currentSection = trains.get(trainName);

            if (!isFreightJ1Transition(
                    currentSection,
                    nextSection)) {

                continue;
            }

            if (reservedSections.contains(nextSection)) {
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
            reservedSections.add(nextSection);

            if (junction != null) {
                reservedJunctions.add(junction);
            }
        }

        /*
         * If A wants to enter B's current section, B must
         * actually be one of the selected trains that will
         * leave that section.
         *
         * Repeat because removing B may also make A invalid.
         */
        boolean changed;

        do {
            changed = false;

            List<String> toRemove = new ArrayList<>();

            for (String trainName : selectedTrains) {

                int nextSection =
                        plannedNextSections.get(trainName);

                if (nextSection == -1) {
                    continue;
                }

                String occupyingTrain =
                        originalSections.get(nextSection);

                if (occupyingTrain != null
                        && !selectedTrains.contains(
                                occupyingTrain)) {

                    toRemove.add(trainName);
                }
            }

            if (!toRemove.isEmpty()) {
                selectedTrains.removeAll(toRemove);
                changed = true;
            }

        } while (changed);

        /*
         * Apply the selected transitions simultaneously.
         *
         * First release every old section.
         */
        for (String trainName : selectedTrains) {

            int currentSection = trains.get(trainName);

            sections.put(currentSection, null);
        }

        /*
         * Then place trains into their new sections or mark
         * them as having left the rail corridor.
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
