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
}
