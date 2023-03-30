package models;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DiagonalConstraintRemoverTest {
    @Test
    public void testNonOverlappingDiagonalConstraints() {
        // Based on: https://www.geogebra.org/graphing/c6yuh7zm
        // Arrange
        Clock x = new Clock("x", "test");
        Clock y = new Clock("y", "test");
        List<Clock> clocks = new ArrayList<>();
        clocks.add(x);
        clocks.add(y);

        CDDRuntime.init(clocks, new ArrayList<>());

        // x>=2 ∧ x<5 ∧ y>1 ∧ y<4
        AndGuard a = new AndGuard(
                new ClockGuard(x, 2, Relation.GREATER_EQUAL), // x >= 2
                new ClockGuard(x, 5, Relation.LESS_THAN), // x < 5
                new ClockGuard(y, 1, Relation.GREATER_THAN), // y > 1
                new ClockGuard(y, 4, Relation.LESS_THAN) // y < 4
        );
        ClockGuard b = new ClockGuard(x, y, 4, Relation.LESS_THAN); // x-y<4
        ClockGuard c = new ClockGuard(y, x, 2, Relation.LESS_THAN); // y-x<2
        AndGuard abc = new AndGuard(a, b, c);

        CDD aCdd = CDDFactory.createFrom(a);

        DiagonalConstraintRemover remover = new DiagonalConstraintRemover(
                new GuardFactory(), new CDDFactory()
        );

        // Act
        Guard simplifiedGuard = abc.accept(remover);
        CDD simplifiedGuardCdd = CDDFactory.createFrom(simplifiedGuard);

        // Assert
        boolean equiv = simplifiedGuardCdd.equiv(aCdd);
        CDDRuntime.done();
        Assert.assertTrue(equiv);
    }
}
