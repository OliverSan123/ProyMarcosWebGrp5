package com.gruponorte.mesaayudati;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import com.gruponorte.mesaayudati.dto.LoginRequest;
import com.gruponorte.mesaayudati.entity.AppUser;
import com.gruponorte.mesaayudati.entity.UserRole;
import com.gruponorte.mesaayudati.repository.UserRepository;
import com.gruponorte.mesaayudati.service.AuthService;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:mesaayudati-test;DB_CLOSE_DELAY=-1",
		"app.bootstrap-test-users.password=prueba-local-123",
		"app.bootstrap-test-users.reset-existing=true"
})
class MesaayudatiApplicationTests {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AuthService authService;

	@Autowired
	private JwtDecoder jwtDecoder;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private CommandLineRunner seedInitialData;

	@Test
	void createsOneEnabledAccountForEachTestRole() {
		assertEquals(4, userRepository.findAll().size());
		var admin = userRepository.findByUsernameIgnoreCase("admin.prueba").orElseThrow();
		var coordinator = userRepository.findByUsernameIgnoreCase("coordinador.prueba").orElseThrow();
		var technician = userRepository.findByUsernameIgnoreCase("tecnico.prueba").orElseThrow();
		var requester = userRepository.findByUsernameIgnoreCase("solicitante.prueba").orElseThrow();
		assertEquals(UserRole.ADMINISTRADOR, admin.getRole());
		assertEquals(UserRole.COORDINADOR, coordinator.getRole());
		assertEquals(UserRole.TECNICO, technician.getRole());
		assertEquals(UserRole.SOLICITANTE, requester.getRole());
		assertTrue(admin.isEnabled());
		assertTrue(coordinator.isEnabled());
		assertTrue(technician.isEnabled());
		assertTrue(requester.isEnabled());
	}

	@Test
	void resetDisablesAnAccountAlreadyInTheDatabase() throws Exception {
		AppUser existingAccount = new AppUser();
		existingAccount.setUsername("usuario.anterior");
		existingAccount.setEmail("usuario.anterior@local.test");
		existingAccount.setPasswordHash(passwordEncoder.encode("clave-anterior"));
		existingAccount.setRole(UserRole.SOLICITANTE);
		existingAccount.setEnabled(true);
		userRepository.save(existingAccount);

		seedInitialData.run();

		var deactivatedAccount = userRepository.findByUsernameIgnoreCase("usuario.anterior").orElseThrow();
		assertFalse(deactivatedAccount.isEnabled());
		userRepository.delete(deactivatedAccount);
	}

	@Test
	void deactivatingAccountRevokesItsExistingToken() {
		var login = authService.login(new LoginRequest("solicitante.prueba", "prueba-local-123"));
		assertNotNull(jwtDecoder.decode(login.accessToken()));

		var account = userRepository.findByUsernameIgnoreCase("solicitante.prueba").orElseThrow();
		account.setEnabled(false);
		userRepository.save(account);

		var validationException = assertThrows(JwtValidationException.class,
				() -> jwtDecoder.decode(login.accessToken()));
		assertNotNull(validationException);

		account.setEnabled(true);
		userRepository.save(account);
	}

}
