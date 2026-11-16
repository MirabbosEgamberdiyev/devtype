package com.devtype;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTests {

    private final ApplicationModules modules = ApplicationModules.of(DevTypeApplication.class);

    @Test
    @Tag("AC-BE-2")
    @DisplayName("AC-BE-2: Modullar chegaralari va ruxsat etilgan bog'liqliklar tekshiruvi")
    void verifyModularStructure() {
        modules.verify();
    }

    @Test
    @DisplayName("Modul hujjatlarini generatsiya qilish")
    void createDocumentation() {
        new Documenter(modules)
            .writeModulesAsPlantUml()
            .writeDocumentation();
    }
}
