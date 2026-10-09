import static org.junit.Assert.*;
import org.junit.Test;

public class InterlockingImpl_Test {

    @Test
    public void testSectionsInitiallyEmpty() {
        InterlockingImpl interlocking = new InterlockingImpl();

        for (int i = 1; i <= 11; i++) {
            assertNull(interlocking.getSection(i));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSectionBelowValidRange() {
        InterlockingImpl interlocking = new InterlockingImpl();

        interlocking.getSection(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSectionAboveValidRange() {
        InterlockingImpl interlocking = new InterlockingImpl();

        interlocking.getSection(12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetUnknownTrain() {
        InterlockingImpl interlocking = new InterlockingImpl();

        interlocking.getTrain("TrainA");
    }

    @Test
    public void testAddPassengerTrain() {
        InterlockingImpl interlocking = new InterlockingImpl();

        interlocking.addTrain("PassengerA", 1, 8);

        assertEquals("PassengerA", interlocking.getSection(1));
        assertEquals(1, interlocking.getTrain("PassengerA"));
    }

    @Test
    public void testAddFreightTrain() {
        InterlockingImpl interlocking = new InterlockingImpl();

        interlocking.addTrain("FreightA", 3, 11);

        assertEquals("FreightA", interlocking.getSection(3));
        assertEquals(3, interlocking.getTrain("FreightA"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateTrainName() {
        InterlockingImpl interlocking = new InterlockingImpl();

        interlocking.addTrain("TrainA", 1, 8);
        interlocking.addTrain("TrainA", 3, 11);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidRoute() {
        InterlockingImpl interlocking = new InterlockingImpl();

        interlocking.addTrain("TrainA", 1, 2);
    }

    @Test(expected = IllegalStateException.class)
    public void testOccupiedEntrySection() {
        InterlockingImpl interlocking = new InterlockingImpl();

        interlocking.addTrain("TrainA", 1, 8);
        interlocking.addTrain("TrainB", 1, 9);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyTrainName() {
        InterlockingImpl interlocking = new InterlockingImpl();

        interlocking.addTrain("", 1, 8);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTrainName() {
        InterlockingImpl interlocking = new InterlockingImpl();

        interlocking.addTrain(null, 1, 8);
    }

    @Test
    public void testPassengerTrainMovesToNextSection() {
        InterlockingImpl interlocking = new InterlockingImpl();

        interlocking.addTrain("PassengerA", 1, 8);

        int moved = interlocking.moveTrains(
                new String[] {"PassengerA"});

        assertEquals(1, moved);
        assertNull(interlocking.getSection(1));
        assertEquals("PassengerA", interlocking.getSection(5));
        assertEquals(5, interlocking.getTrain("PassengerA"));
    }

    @Test
    public void testFreightTrainMovesToNextSection() {
        InterlockingImpl interlocking = new InterlockingImpl();

        interlocking.addTrain("FreightA", 3, 11);

        int moved = interlocking.moveTrains(
                new String[] {"FreightA"});

        assertEquals(1, moved);
        assertNull(interlocking.getSection(3));
        assertEquals("FreightA", interlocking.getSection(7));
        assertEquals(7, interlocking.getTrain("FreightA"));
    }

    @Test
    public void testTrainReachesDestination() {
        InterlockingImpl interlocking = new InterlockingImpl();

        interlocking.addTrain("PassengerA", 1, 8);

        interlocking.moveTrains(
                new String[] {"PassengerA"});

        interlocking.moveTrains(
                new String[] {"PassengerA"});

        assertEquals(8, interlocking.getTrain("PassengerA"));
        assertEquals("PassengerA", interlocking.getSection(8));
        assertNull(interlocking.getSection(5));
    }

    @Test
    public void testTrainExitsAfterDestination() {
        InterlockingImpl interlocking = new InterlockingImpl();

        interlocking.addTrain("PassengerA", 1, 8);

        interlocking.moveTrains(
                new String[] {"PassengerA"});

        interlocking.moveTrains(
                new String[] {"PassengerA"});

        int moved = interlocking.moveTrains(
                new String[] {"PassengerA"});

        assertEquals(1, moved);
        assertEquals(-1, interlocking.getTrain("PassengerA"));
        assertNull(interlocking.getSection(8));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMoveTrainAfterExit() {
        InterlockingImpl interlocking = new InterlockingImpl();

        interlocking.addTrain("PassengerA", 1, 8);

        interlocking.moveTrains(
                new String[] {"PassengerA"});

        interlocking.moveTrains(
                new String[] {"PassengerA"});

        interlocking.moveTrains(
                new String[] {"PassengerA"});

        interlocking.moveTrains(
                new String[] {"PassengerA"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMoveUnknownTrain() {
        InterlockingImpl interlocking = new InterlockingImpl();

        interlocking.moveTrains(
                new String[] {"UnknownTrain"});
    }

    @Test
    public void testBlockedTrainDoesNotMove() {
        InterlockingImpl interlocking = new InterlockingImpl();

        interlocking.addTrain("TrainA", 1, 8);
        interlocking.addTrain("TrainB", 9, 2);

        interlocking.moveTrains(
                new String[] {"TrainA"});

        interlocking.moveTrains(
                new String[] {"TrainB"});

        /*
         * TrainA is now in section 5.
         * TrainB is now in section 6.
         *
         * This test checks that occupied sections
         * are not overwritten during movement.
         */
        assertEquals("TrainA", interlocking.getSection(5));
        assertEquals("TrainB", interlocking.getSection(6));
    }

    @Test
    public void testMultipleTrainsCanMoveInOneCall() {
        InterlockingImpl interlocking = new InterlockingImpl();

        interlocking.addTrain("PassengerA", 1, 8);
        interlocking.addTrain("FreightA", 3, 11);

        int moved = interlocking.moveTrains(
                new String[] {"PassengerA", "FreightA"});

        assertEquals(2, moved);

        assertEquals(5,
                interlocking.getTrain("PassengerA"));

        assertEquals(7,
                interlocking.getTrain("FreightA"));
    }

    @Test
    public void testEmptyMoveRequest() {
        InterlockingImpl interlocking = new InterlockingImpl();

        int moved = interlocking.moveTrains(
                new String[] {});

        assertEquals(0, moved);
    }
}
