package dev.hoem.auth;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private static final JavaClasses AUTH_CLASSES =
            new ClassFileImporter().importPackages("dev.hoem.auth");

    @Test
    void domainShouldNotDependOnSpring() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("dev.hoem.auth.domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "org.hibernate..");
        rule.check(AUTH_CLASSES);
    }

    @Test
    void controllerShouldNotAccessPersistence() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("dev.hoem.auth.controller..")
                .should().dependOnClassesThat()
                .resideInAPackage("dev.hoem.auth.infrastructure.persistence..");
        rule.check(AUTH_CLASSES);
    }

    @Test
    void applicationShouldNotAccessInfrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("dev.hoem.auth.application..")
                .should().dependOnClassesThat()
                .resideInAPackage("dev.hoem.auth.infrastructure..");
        rule.check(AUTH_CLASSES);
    }
}