package e2e;

import log.Log;
import log.Urgency;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UniversityTest extends GrpcE2EBase {
    public UniversityTest() {
        super("./samples/json/EcdarUniversity/Components/");
    }

    @Test
    public void compositionOfTheConjoinedHalfAdministrationsResearcherMachineDoesNotRefineSpecification() {
        assertFalse(refinement("refinement: (HalfAdm1 && HalfAdm2) || Researcher || Machine <= Spec"));
    }

    @Test
    public void CompositionOfAdministrationResearcherMachineRefinesSelf() {
        assertTrue(refinement("refinement: Administration || Researcher || Machine <=  Administration || Researcher || Machine"));
    }

    @Test
    public void conjunctionOfHalfAdministration1And2RefinesAdministration2() {
        assertTrue(refinement("refinement: HalfAdm1 && HalfAdm2 <= Adm2"));
    }

    @Test
    public void administration2RefinesConjunctionOfHalfAdministration1And2() {
        assertTrue(refinement("refinement: Adm2 <= HalfAdm1 && HalfAdm2"));
    }

    @Test
    public void administration2RefinesSelf() {
        assertTrue(refinement("refinement: Adm2 <= Adm2"));
    }

    @Test
    public void HalfAdm1RefinesSelf() {
        assertTrue(refinement("refinement: HalfAdm1 <= HalfAdm1"));
    }

    @Test
    public void HalfAdm2RefinesSelf() {
        assertTrue(refinement("refinement: HalfAdm2 <= HalfAdm2"));
    }

    @Test
    public void AdministrationRefinesSelf() {
        assertTrue(refinement("refinement: Administration <= Administration"));
    }

    @Test
    public void MachineRefinesSelf() {
        assertTrue(refinement("refinement: Machine <= Machine"));
    }

    @Test
    public void ResearcherRefinesSelf() {
        assertTrue(refinement("refinement: Researcher <= Researcher"));
    }

    @Test
    public void SpecificationRefinesSelf() {
        assertTrue(refinement("refinement: Spec <= Spec"));
    }

    @Test
    public void Machine3RefinesSelf() {
        assertTrue(refinement("refinement: Machine3 <= Machine3"));
    }

    @Test
    public void administrationDoesNotRefineMachine() {
        assertFalse(refinement("refinement: Administration <= Machine"));
    }

    @Test
    public void administrationDoesNotRefineResearcher() {
        assertFalse(refinement("refinement: Administration <= Researcher"));
    }

    @Test
    public void administrationDoesNotRefineSpecification() {
        assertFalse(refinement("refinement: Administration <= Spec"));
    }

    @Test
    public void administrationDoesNotRefineMachine3() {
        assertFalse(refinement("refinement: Administration <= Machine3"));
    }

    @Test
    public void machineDoesNotRefinesAdministration() {
        assertFalse(refinement("refinement: Machine <= Administration"));
    }

    @Test
    public void machineDoesNotRefinesResearcher() {
        assertFalse(refinement("refinement: Machine <= Researcher"));
    }

    @Test
    public void machineDoesNotRefinesSpecification() {
        assertFalse(refinement("refinement: Machine <= Spec"));
    }

    @Test
    public void machineDoesNotRefinesMachine3() {
        assertFalse(refinement("refinement: Machine <= Machine3"));
    }

    @Test
    public void researcherDoesNotRefineAdministration() {
        assertFalse(refinement("refinement: Researcher <= Administration"));
    }

    @Test
    public void researcherDoesNotRefineMachine() {
        assertFalse(refinement("refinement: Researcher <= Machine"));
    }

    @Test
    public void researcherDoesNotRefineSpecification() {
        assertFalse(refinement("refinement: Researcher <= Spec"));
    }

    @Test
    public void researcherDoesNotRefineMachine3() {
        assertFalse(refinement("refinement: Researcher <= Machine3"));
    }

    @Test
    public void specificationDoesNotRefineAdministration() {
        assertFalse(refinement("refinement: Spec <= Administration"));
    }

    @Test
    public void specificationDoesNotRefineMachine() {
        assertFalse(refinement("refinement: Spec <= Machine"));
    }

    @Test
    public void specificationDoesNotRefineResearcher() {
        assertFalse(refinement("refinement: Spec <= Researcher"));
    }

    @Test
    public void specificationDoesNotRefineMachine3() {
        assertFalse(refinement("refinement: Spec <= Machine3"));
    }

    @Test
    public void machine3DoesNotRefineAdministration() {
        assertFalse(refinement("refinement: Machine3 <= Administration"));
    }

    @Test
    public void machine3DoesNotRefineResearcher() {
        assertFalse(refinement("refinement: Machine3 <= Researcher"));
    }

    @Test
    public void machine3DoesNotRefineSpecification() {
        assertFalse(refinement("refinement: Machine3 <= Spec"));
    }

    @Test
    public void machine3DoesNotRefineMachine() {
        assertTrue(refinement("refinement: Machine3 <= Machine"));
    }

    @Test
    public void compositionOfAdministrationMachineResearcherIsConsistent() {
        assertTrue(consistency("consistency: (Administration || Machine || Researcher)"));
    }

    @Test
    public void compositionOfAdministrationMachineResearcherRefinesSpecification() {
        assertTrue(refinement("refinement: (Administration || Machine || Researcher) <= Spec"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest1() {
        assertTrue(refinement("refinement: ((HalfAdm1 && HalfAdm2) || Machine || Researcher) <= ((HalfAdm1 && HalfAdm2) || Machine || Researcher)"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest2() {
        assertTrue(refinement("refinement: ((HalfAdm1 && HalfAdm2) || Machine || Researcher) <= (Adm2 || Machine || Researcher)"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest3() {
        assertTrue(refinement("refinement: ((HalfAdm1 && HalfAdm2) || Researcher) <= ((HalfAdm1 && HalfAdm2) || Researcher)"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest4() {
        assertTrue(refinement("refinement: ((HalfAdm1 && HalfAdm2) || Researcher) <= (Adm2 || Researcher)"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest5() {
        assertTrue(refinement("refinement: (HalfAdm1 && HalfAdm2) <= (HalfAdm1 && HalfAdm2)"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest6() {
        assertTrue(refinement("refinement: (HalfAdm1 && HalfAdm2) <= HalfAdm2"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest7() {
        assertTrue(refinement("refinement: (HalfAdm1 && HalfAdm2) <= HalfAdm2"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
    public void generatedTest8() {
        assertTrue(refinement("refinement: (HalfAdm1 && HalfAdm2) <= Adm2"));
    }

    @Test
    @Ignore // Uses a lot of memory but does not cause any errors (It passes Sep 18 2022)
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
    @Ignore // Causes memory errors (presumably it passes)
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
