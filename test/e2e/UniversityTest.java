package e2e;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class UniversityTest extends GrpcE2EBase {
    public UniversityTest() {
        super("./samples/json/EcdarUniversity/Components/");
    }

    @Test
    public void compositionOfAdminMachResIsConsistent() {
        assertTrue(consistency("consistency: (Administration || Machine || Researcher)"));
    }

    @Test
    public void researcherRefinesSelf() {
        assertTrue(refinement("refinement: Researcher <= Researcher"));
    }

    @Test
    public void specificationRefinesSelf() {
        assertTrue(refinement("refinement: Spec <= Spec"));
    }

    @Test
    public void administrationRefinesSelf() {
        assertTrue(refinement("refinement: Administration <= Administration"));
    }

    @Test
    public void machineRefinesSelf() {
        assertTrue(refinement("refinement: Machine <= Machine"));
    }

    @Test
    public void machine2RefinesSelf() {
        assertTrue(refinement("refinement: Machine2 <= Machine2"));
    }

    @Test
    public void machine3RefinesSelf() {
        assertTrue(refinement("refinement: Machine3 <= Machine3"));
    }

    @Test
    public void Adm2RefinesSelf() {
        assertTrue(refinement("refinement: Adm2 <= Adm2"));
    }

    @Test
    public void halfAdm1RefinesSelf() {
        assertTrue(refinement("refinement: HalfAdm1 <= HalfAdm1"));
    }

    @Test
    public void halfAdm2RefinesSelf() {
        assertTrue(refinement("refinement: HalfAdm2 <= HalfAdm2"));
    }

    @Test
    public void compositionOfAdminMachineResearcherRefinesSpec() {
        assertTrue(refinement("refinement: (Administration || Machine || Researcher) <= Spec"));
    }

    @Test
    public void generatedTest1() {
        assertTrue(refinement("refinement: ((HalfAdm1 && HalfAdm2) || Machine || Researcher) <= ((HalfAdm1 && HalfAdm2) || Machine || Researcher)"));
    }

    @Test
    public void generatedTest2() {
        assertTrue(refinement("refinement: ((HalfAdm1 && HalfAdm2) || Machine || Researcher) <= (Adm2 || Machine || Researcher)"));
    }

    @Test
    public void generatedTest3() {
        assertTrue(refinement("refinement: ((HalfAdm1 && HalfAdm2) || Researcher) <= ((HalfAdm1 && HalfAdm2) || Researcher)"));
    }

    @Test
    public void generatedTest4() {
        assertTrue(refinement("refinement: ((HalfAdm1 && HalfAdm2) || Researcher) <= (Adm2 || Researcher)"));
    }

    @Test
    public void generatedTest5() {
        assertTrue(refinement("refinement: (HalfAdm1 && HalfAdm2) <= (HalfAdm1 && HalfAdm2)"));
    }

    @Test
    public void generatedTest6() {
        assertTrue(refinement("refinement: (HalfAdm1 && HalfAdm2) <= HalfAdm2"));
    }

    @Test
    public void generatedTest7() {
        assertTrue(refinement("refinement: (HalfAdm1 && HalfAdm2) <= HalfAdm2"));
    }

    @Test
    public void generatedTest8() {
        assertTrue(refinement("refinement: (HalfAdm1 && HalfAdm2) <= Adm2"));
    }

    @Test
    public void generatedTest9() {
        assertTrue(refinement("refinement: (HalfAdm1 && HalfAdm2) <= Adm2"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest10() {
        assertTrue(refinement("refinement: (Administration || Machine || Researcher) <= (Administration || Machine || Researcher)"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest11() {
        assertTrue(refinement("refinement: (Administration || Machine || Researcher) <= Spec"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest12() {
        assertTrue(refinement("refinement: (Administration || Researcher) <= (Administration || Researcher)"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest13() {
        assertTrue(refinement("refinement: Researcher <= ((((Adm2 && HalfAdm1) || Researcher) && ((Adm2 && HalfAdm2) || Researcher) && ((HalfAdm1 && HalfAdm2) || Researcher) && (Adm2 || Researcher)) \\\\ (HalfAdm1 && HalfAdm2))"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest14() {
        assertTrue(refinement("refinement: Researcher <= ((((Adm2 && HalfAdm1 && HalfAdm2) || Researcher) && ((Adm2 && HalfAdm1) || Researcher) && ((HalfAdm1 && HalfAdm2) || Researcher)) \\\\ (Adm2 && HalfAdm1))"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest15() {
        assertTrue(refinement("refinement: ((((Adm2 && HalfAdm1 && HalfAdm2) || Researcher) && ((Adm2 && HalfAdm1) || Researcher) && ((HalfAdm1 && HalfAdm2) || Researcher)) \\\\ (Adm2 && HalfAdm1)) <= ((((Adm2 && HalfAdm1 && HalfAdm2) || Researcher) && ((Adm2 && HalfAdm1) || Researcher) && ((HalfAdm1 && HalfAdm2) || Researcher)) \\\\ (Adm2 && HalfAdm1))"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest16() {
        assertTrue(refinement("refinement: Researcher <= ((((Adm2 && HalfAdm1 && HalfAdm2) || Researcher) && ((Adm2 && HalfAdm1) || Researcher) && ((Adm2 && HalfAdm2) || Researcher) && ((HalfAdm1 && HalfAdm2) || Researcher)) \\\\ (Adm2 && HalfAdm1))"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest17() {
        assertTrue(refinement("refinement: ((((Adm2 && HalfAdm1 && HalfAdm2) || Researcher) && ((Adm2 && HalfAdm1) || Researcher) && ((Adm2 && HalfAdm2) || Researcher) && ((HalfAdm1 && HalfAdm2) || Researcher)) \\\\ (Adm2 && HalfAdm1)) <= ((((Adm2 && HalfAdm1 && HalfAdm2) || Researcher) && ((Adm2 && HalfAdm1) || Researcher) && ((Adm2 && HalfAdm2) || Researcher) && ((HalfAdm1 && HalfAdm2) || Researcher)) \\\\ (Adm2 && HalfAdm1))"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest18() {
        assertTrue(refinement("refinement: Researcher <= ((((Adm2 && HalfAdm1) || Researcher) && ((Adm2 && HalfAdm2) || Researcher) && (Adm2 || Researcher)) \\\\ (Adm2 && HalfAdm1))"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest19() {
        assertTrue(refinement("refinement: ((((Adm2 && HalfAdm1) || Researcher) && ((Adm2 && HalfAdm2) || Researcher) && (Adm2 || Researcher)) \\\\ (Adm2 && HalfAdm1)) <= ((((Adm2 && HalfAdm1) || Researcher) && ((Adm2 && HalfAdm2) || Researcher) && (Adm2 || Researcher)) \\\\ (Adm2 && HalfAdm1))"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest20() {
        assertTrue(refinement("refinement: Researcher <= ((((Adm2 && HalfAdm1 && HalfAdm2) || Researcher) && ((Adm2 && HalfAdm1) || Researcher) && (Adm2 || Researcher)) \\\\ (Adm2 && HalfAdm1))"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest21() {
        assertTrue(refinement("refinement: ((((Adm2 && HalfAdm1 && HalfAdm2) || Researcher) && ((Adm2 && HalfAdm1) || Researcher) && (Adm2 || Researcher)) \\\\ (Adm2 && HalfAdm1)) <= ((((Adm2 && HalfAdm1 && HalfAdm2) || Researcher) && ((Adm2 && HalfAdm1) || Researcher) && (Adm2 || Researcher)) \\\\ (Adm2 && HalfAdm1))"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest22() {
        assertTrue(refinement("refinement: Researcher <= ((((Adm2 && HalfAdm1) || Researcher) && ((Adm2 && HalfAdm2) || Researcher) && ((HalfAdm1 && HalfAdm2) || Researcher) && (Adm2 || Researcher)) \\\\ (Adm2 && HalfAdm1))"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTes23() {
        assertTrue(refinement("refinement: ((((Adm2 && HalfAdm1) || Researcher) && ((Adm2 && HalfAdm2) || Researcher) && ((HalfAdm1 && HalfAdm2) || Researcher) && (Adm2 || Researcher)) \\\\ (Adm2 && HalfAdm1)) <= ((((Adm2 && HalfAdm1) || Researcher) && ((Adm2 && HalfAdm2) || Researcher) && ((HalfAdm1 && HalfAdm2) || Researcher) && (Adm2 || Researcher)) \\\\ (Adm2 && HalfAdm1))"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest24() {
        assertTrue(refinement("refinement: Researcher <= ((((Adm2 && HalfAdm1 && HalfAdm2) || Researcher) && ((Adm2 && HalfAdm1) || Researcher) && ((Adm2 && HalfAdm2) || Researcher)) \\\\ (Adm2 && HalfAdm1))"));
    }

    @Test
    @Ignore // Causes memory errors (Persumably it passes)
    public void generatedTest25() {
        assertTrue(refinement("refinement: ((((Adm2 && HalfAdm1 && HalfAdm2) || Researcher) && ((Adm2 && HalfAdm1) || Researcher) && ((Adm2 && HalfAdm2) || Researcher)) \\\\ (Adm2 && HalfAdm1)) <= ((((Adm2 && HalfAdm1 && HalfAdm2) || Researcher) && ((Adm2 && HalfAdm1) || Researcher) && ((Adm2 && HalfAdm2) || Researcher)) \\\\ (Adm2 && HalfAdm1))"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest26() {
        assertTrue(refinement("refinement: Researcher <= ((((Adm2 && HalfAdm1 && HalfAdm2) || Researcher) && ((Adm2 && HalfAdm2) || Researcher) && (Adm2 || Researcher)) \\\\ (Adm2 && HalfAdm1))"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest27() {
        assertTrue(refinement("refinement: ((((Adm2 && HalfAdm1 && HalfAdm2) || Researcher) && ((Adm2 && HalfAdm2) || Researcher) && (Adm2 || Researcher)) \\\\ (Adm2 && HalfAdm1)) <= ((((Adm2 && HalfAdm1 && HalfAdm2) || Researcher) && ((Adm2 && HalfAdm2) || Researcher) && (Adm2 || Researcher)) \\\\ (Adm2 && HalfAdm1))"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest28() {
        assertTrue(refinement("refinement: Researcher <= ((((Adm2 && HalfAdm1 && HalfAdm2) || Researcher) && ((Adm2 && HalfAdm1) || Researcher) && ((HalfAdm1 && HalfAdm2) || Researcher) && (Adm2 || Researcher)) \\\\ (Adm2 && HalfAdm1))"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest29() {
        assertTrue(refinement("refinement: ((((Adm2 && HalfAdm1 && HalfAdm2) || Researcher) && ((Adm2 && HalfAdm1) || Researcher) && ((HalfAdm1 && HalfAdm2) || Researcher) && (Adm2 || Researcher)) \\\\ (Adm2 && HalfAdm1)) <= ((((Adm2 && HalfAdm1 && HalfAdm2) || Researcher) && ((Adm2 && HalfAdm1) || Researcher) && ((HalfAdm1 && HalfAdm2) || Researcher) && (Adm2 || Researcher)) \\\\ (Adm2 && HalfAdm1))"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest30() {
        assertTrue(refinement("refinement: Researcher <= ((((Adm2 && HalfAdm1 && HalfAdm2) || Researcher) && ((Adm2 && HalfAdm2) || Researcher) && ((HalfAdm1 && HalfAdm2) || Researcher)) \\\\ (Adm2 && HalfAdm1))"));
    }
}
