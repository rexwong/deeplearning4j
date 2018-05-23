package org.nd4j.linalg.api.iterator;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.nd4j.linalg.BaseNd4jTest;
import org.nd4j.linalg.api.iter.NdIndexIterator;
import org.nd4j.linalg.factory.Nd4jBackend;

import java.util.Iterator;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author Adam Gibson
 */
@RunWith(Parameterized.class)
public class NDIndexIteratorTest extends BaseNd4jTest {

    public NDIndexIteratorTest(Nd4jBackend backend) {
        super(backend);
    }

    @Test
    public void testIterate() {
        val shapeIter = new NdIndexIterator(2, 2);
        val possibleSolutions = new long[][] {{0, 0}, {0, 1}, {1, 0}, {1, 1},};

        for (int i = 0; i < 4; i++) {
            assertArrayEquals(possibleSolutions[i], shapeIter.next());
        }


    }

    @Override
    public char ordering() {
        return 'f';
    }
}
