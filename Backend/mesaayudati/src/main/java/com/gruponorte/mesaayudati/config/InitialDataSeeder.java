package com.gruponorte.mesaayudati.config;

import com.gruponorte.mesaayudati.entity.AppUser;
import com.gruponorte.mesaayudati.entity.ImpactLevel;
import com.gruponorte.mesaayudati.entity.Priority;
import com.gruponorte.mesaayudati.entity.PriorityRule;
import com.gruponorte.mesaayudati.entity.UserRole;
import com.gruponorte.mesaayudati.repository.PriorityRuleRepository;
import com.gruponorte.mesaayudati.repository.PriorityRepository;
import com.gruponorte.mesaayudati.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Crea catálogos base y cuentas de prueba cuando se configura su contraseña. */
@Configuration
public class InitialDataSeeder {

    @Bean
    CommandLineRunner seedInitialData(
            PriorityRepository priorityRepository,
            PriorityRuleRepository priorityRuleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.username}") String adminUsername,
            @Value("${app.bootstrap-admin.email}") String adminEmail,
            @Value("${app.bootstrap-admin.password}") String adminPassword,
            @Value("${app.bootstrap-test-users.password}") String testUsersPassword,
            @Value("${app.bootstrap-test-users.reset-existing}") boolean resetExistingUsers) {
        return args -> {
            createPriorityIfMissing(priorityRepository, "CRITICA", 4);
            createPriorityIfMissing(priorityRepository, "ALTA", 8);
            createPriorityIfMissing(priorityRepository, "MEDIA", 24);
            createPriorityIfMissing(priorityRepository, "BAJA", 72);

            // Matriz inicial sugerida; el administrador puede modificarla por la API de catálogos.
            createRuleIfMissing(priorityRuleRepository, priorityRepository, ImpactLevel.BAJO,
                    ImpactLevel.BAJO, "BAJA");
            createRuleIfMissing(priorityRuleRepository, priorityRepository, ImpactLevel.BAJO,
                    ImpactLevel.MEDIO, "BAJA");
            createRuleIfMissing(priorityRuleRepository, priorityRepository, ImpactLevel.BAJO,
                    ImpactLevel.ALTO, "MEDIA");
            createRuleIfMissing(priorityRuleRepository, priorityRepository, ImpactLevel.MEDIO,
                    ImpactLevel.BAJO, "BAJA");
            createRuleIfMissing(priorityRuleRepository, priorityRepository, ImpactLevel.MEDIO,
                    ImpactLevel.MEDIO, "MEDIA");
            createRuleIfMissing(priorityRuleRepository, priorityRepository, ImpactLevel.MEDIO,
                    ImpactLevel.ALTO, "ALTA");
            createRuleIfMissing(priorityRuleRepository, priorityRepository, ImpactLevel.ALTO,
                    ImpactLevel.BAJO, "MEDIA");
            createRuleIfMissing(priorityRuleRepository, priorityRepository, ImpactLevel.ALTO,
                    ImpactLevel.MEDIO, "ALTA");
            createRuleIfMissing(priorityRuleRepository, priorityRepository, ImpactLevel.ALTO,
                    ImpactLevel.ALTO, "CRITICA");

            if (resetExistingUsers && testUsersPassword.isBlank()) {
                throw new IllegalStateException(
                        "Configura INITIAL_TEST_USERS_PASSWORD antes de desactivar las cuentas existentes");
            }
            if (!testUsersPassword.isBlank()) {
                if (testUsersPassword.length() < 8) {
                    throw new IllegalStateException("INITIAL_TEST_USERS_PASSWORD debe tener al menos 8 caracteres");
                }
                if (resetExistingUsers) {
                    var existingUsers = userRepository.findAll();
                    existingUsers.forEach(account -> account.setEnabled(false));
                    userRepository.saveAll(existingUsers);
                }
                createOrUpdateTestUser(userRepository, passwordEncoder, "admin.prueba",
                        "admin.prueba@local.test", UserRole.ADMINISTRADOR, testUsersPassword);
                createOrUpdateTestUser(userRepository, passwordEncoder, "coordinador.prueba",
                        "coordinador.prueba@local.test", UserRole.COORDINADOR, testUsersPassword);
                createOrUpdateTestUser(userRepository, passwordEncoder, "tecnico.prueba",
                        "tecnico.prueba@local.test", UserRole.TECNICO, testUsersPassword);
                createOrUpdateTestUser(userRepository, passwordEncoder, "solicitante.prueba",
                        "solicitante.prueba@local.test", UserRole.SOLICITANTE, testUsersPassword);
            }

            // No se crea una cuenta administrativa con una contraseña por defecto insegura.
            if (!adminPassword.isBlank() && !userRepository.existsByUsernameIgnoreCase(adminUsername)) {
                AppUser admin = new AppUser();
                admin.setUsername(adminUsername);
                admin.setEmail(adminEmail);
                admin.setPasswordHash(passwordEncoder.encode(adminPassword));
                admin.setRole(UserRole.ADMINISTRADOR);
                admin.setEnabled(true);
                userRepository.save(admin);
            }
        };
    }

        private void createOrUpdateTestUser(UserRepository repository, PasswordEncoder passwordEncoder,
                                                                                String username, String email, UserRole role, String password) {
                AppUser account = repository.findByUsernameIgnoreCase(username).orElseGet(AppUser::new);
                account.setUsername(username);
                account.setEmail(email);
                account.setPasswordHash(passwordEncoder.encode(password));
                account.setRole(role);
                account.setEnabled(true);
                repository.save(account);
        }

    private void createPriorityIfMissing(PriorityRepository repository, String name, int slaHours) {
        if (repository.existsByNameIgnoreCase(name)) return;
        Priority priority = new Priority();
        priority.setName(name);
        priority.setSlaHours(slaHours);
        repository.save(priority);
    }

    private void createRuleIfMissing(PriorityRuleRepository rules, PriorityRepository priorities,
                                    ImpactLevel impact, ImpactLevel urgency, String priorityName) {
        if (rules.existsByImpactAndUrgency(impact, urgency)) return;
        Priority priority = priorities.findByNameIgnoreCase(priorityName).orElseThrow();
        PriorityRule rule = new PriorityRule();
        rule.setImpact(impact);
        rule.setUrgency(urgency);
        rule.setPriority(priority);
        rules.save(rule);
    }
}