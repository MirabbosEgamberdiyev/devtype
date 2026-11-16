package com.devtype;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class ArchitectureTests {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.devtype");
    }

    @Test
    @Tag("AC-QS-1")
    @DisplayName("AC-QS-1: Production kodida mock, fake, stub, dummy klasslar bo'lmasligi shart")
    void noMockClassesInMain() {
        ArchRule rule = noClasses().should().haveSimpleNameContaining("Mock")
            .orShould().haveSimpleNameContaining("Fake")
            .orShould().haveSimpleNameContaining("Stub")
            .orShould().haveSimpleNameContaining("Dummy")
            .because("Production kodi faqat haqiqiy implementatsiyalarni o'z ichiga olishi kerak (docs/18 AC-QS-1)")
            .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    @Test
    @Tag("AC-BE-2")
    @DisplayName("AC-BE-2: Training va Gamification domen qatlamlari Spring freymvorkiga bog'liq bo'lmasligi kerak")
    void pureCalculationDomain() {
        ArchRule rule = noClasses()
            .that().resideInAnyPackage("com.devtype.training.domain..", "com.devtype.gamification.domain..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework..")
            .because("Hisob-kitob domen mantig'i sof Java bo'lishi shart (docs/13 §2)")
            .allowEmptyShould(true);

        rule.check(importedClasses);
    }
}
