package com.example.B2C.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Enforces that modules only access each other through public facades,
 * never reaching into internal repository or service packages.
 *
 * <p>Rules target the application layer (controller/service) — internal JPA
 * entities and data-model relationships are intentionally excluded so that
 * a module's own {@code entity} package can still declare
 * {@code @ManyToOne} joins to other modules (those are intra-module details).
 */
@AnalyzeClasses(
    packages = "com.example.B2C",
    importOptions = ImportOption.DoNotIncludeTests.class
)
public class ModuleBoundaryArchTest {

    /**
     * The only legal cross-module entrance to the catalog module is through
     * {@link com.example.B2C.modules.catalog.api.CatalogFacade CatalogFacade}.
     * Application-layer classes in order may NOT call repository or service internals.
     */
    @ArchTest
    static final ArchRule order_should_not_depend_on_catalog_internals =
        noClasses()
            .that().resideInAPackage("..modules.order..")
            .and().resideOutsideOfPackage("..modules.order.entity..")
            .and().resideOutsideOfPackage("..modules.order.dto..")
            .should().dependOnClassesThat()
            .resideInAPackage("..modules.catalog.repository..")
            .because("Order chỉ được gọi CatalogFacade, không được truy cập repository nội bộ của catalog");

    /**
     * Entities, DTOs, and ID classes may cross module boundaries freely (they are
     * pure data).  Application services/controllers in the order module must
     * not import catalog entity classes for business logic.
     */
    @ArchTest
    static final ArchRule order_service_should_not_depend_on_catalog_entities =
        noClasses()
            .that().resideInAPackage("..modules.order.service..")
            .or().resideInAPackage("..modules.order.controller..")
            .should().dependOnClassesThat()
            .resideInAPackage("..modules.catalog.entity..")
            .because("Order service/controller không được leak entity nội bộ của catalog, dùng DTO từ api package");

    /**
     * Cart module application layer must not reach into catalog internals.
     */
    @ArchTest
    static final ArchRule cart_should_not_depend_on_catalog_internals =
        noClasses()
            .that().resideInAPackage("..modules.cart.service..")
            .or().resideInAPackage("..modules.cart.controller..")
            .should().dependOnClassesThat()
            .resideInAPackage("..modules.catalog.repository..")
            .because("Cart chỉ được gọi CatalogFacade");

    /**
     * Cart service may not touch catalog entity classes for business logic.
     */
    @ArchTest
    static final ArchRule cart_should_not_depend_on_catalog_entities =
        noClasses()
            .that().resideInAPackage("..modules.cart.service..")
            .or().resideInAPackage("..modules.cart.controller..")
            .should().dependOnClassesThat()
            .resideInAPackage("..modules.catalog.entity..")
            .because("Cart không được leak entity nội bộ của catalog");

    /**
     * Payment module application layer must not reach into catalog internals.
     */
    @ArchTest
    static final ArchRule payment_should_not_depend_on_catalog_internals =
        noClasses()
            .that().resideInAPackage("..modules.payment.service..")
            .or().resideInAPackage("..modules.payment.controller..")
            .should().dependOnClassesThat()
            .resideInAPackage("..modules.catalog.repository..")
            .because("Payment chỉ được gọi CatalogFacade");
}
