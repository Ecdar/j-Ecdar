package e2e;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class UniversityTest extends GrpcE2EBase {
    public UniversityTest() {
        super("./samples/json/EcdarUniversity/Components/");
    }

    @Test
    public void compositionOfAdminMachResIsConsistent() {
        boolean consistent = consistency("consistency: (Administration || Machine || Researcher)");

        assertTrue(consistent);
    }

    @Test
    public void researcherRefinesSelf() {
        boolean refines = refinement("refinement: Researcher <= Researcher");

        assertTrue(refines);
    }

    @Test
    public void specificationRefinesSelf() {
        boolean refines = refinement("refinement: Spec <= Spec");

        assertTrue(refines);
    }

    @Test
    public void administrationRefinesSelf() {
        boolean refines = refinement("refinement: Administration <= Administration");

        assertTrue(refines);
    }

    @Test
    public void machineRefinesSelf() {
        boolean refines = refinement("refinement: Machine <= Machine");

        assertTrue(refines);
    }

    @Test
    public void machine2RefinesSelf() {
        boolean refines = refinement("refinement: Machine2 <= Machine2");

        assertTrue(refines);
    }

    @Test
    public void machine3RefinesSelf() {
        boolean refines = refinement("refinement: Machine3 <= Machine3");

        assertTrue(refines);
    }

    @Test
    public void Adm2RefinesSelf() {
        boolean refines = refinement("refinement: Adm2 <= Adm2");

        assertTrue(refines);
    }

    @Test
    public void halfAdm1RefinesSelf() {
        boolean refines = refinement("refinement: HalfAdm1 <= HalfAdm1");

        assertTrue(refines);
    }

    @Test
    public void halfAdm2RefinesSelf() {
        boolean refines = refinement("refinement: HalfAdm2 <= HalfAdm2");

        assertTrue(refines);
    }

    @Test
    public void compositionOfAdminMachineResearcherRefinesSpec() {
        boolean refines = refinement("refinement: (Administration || Machine || Researcher) <= Spec");

        assertTrue(refines);
    }
}
