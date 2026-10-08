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
}
