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

/** Crea prioridades SLA base y un administrador inicial solo si se configura su contraseña. */
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
            @Value("${app.bootstrap-admin.password}") String adminPassword) {
        return args -> {
            createPriorityIfMissing(priorityRepository, "CRITICA", 4);
            createPriorityIfMissing(priorityRepository, "ALTA", 8);
            createPriorityIfMissing(priorityRepository, "MEDIA", 24);
            createPriorityIfMissing(priorityRepository, "BAJA", 72);

            // Matriz inicial sugerida; el administrador puede modificarla por la API de catálogos.
            createRuleIfMissing(priorityRuleRepository, priorityRepository, ImpactLevel.BAJO,
                    ImpactLevel.BAJO, "BAJA");
            createRuleIfMissing(priorityRuleRepository, priorityRepository, ImpactLevel.BAJO,
                    ImpactLevel.MEDIO, "MEDIA");
            createRuleIfMissing(priorityRuleRepository, priorityRepository, ImpactLevel.BAJO,
                    ImpactLevel.ALTO, "MEDIA");
            createRuleIfMissing(priorityRuleRepository, priorityRepository, ImpactLevel.MEDIO,
                    ImpactLevel.BAJO, "MEDIA");
            createRuleIfMissing(priorityRuleRepository, priorityRepository, ImpactLevel.MEDIO,
                    ImpactLevel.MEDIO, "ALTA");
            createRuleIfMissing(priorityRuleRepository, priorityRepository, ImpactLevel.MEDIO,
                    ImpactLevel.ALTO, "ALTA");
            createRuleIfMissing(priorityRuleRepository, priorityRepository, ImpactLevel.ALTO,
                    ImpactLevel.BAJO, "ALTA");
            createRuleIfMissing(priorityRuleRepository, priorityRepository, ImpactLevel.ALTO,
                    ImpactLevel.MEDIO, "CRITICA");
            createRuleIfMissing(priorityRuleRepository, priorityRepository, ImpactLevel.ALTO,
                    ImpactLevel.ALTO, "CRITICA");

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