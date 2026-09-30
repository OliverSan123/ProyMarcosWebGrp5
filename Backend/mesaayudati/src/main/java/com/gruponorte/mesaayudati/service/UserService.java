package com.gruponorte.mesaayudati.service;

import com.gruponorte.mesaayudati.dto.UserRequest;
import com.gruponorte.mesaayudati.dto.UserResponse;
import com.gruponorte.mesaayudati.entity.AppUser;
import com.gruponorte.mesaayudati.entity.Area;
import com.gruponorte.mesaayudati.repository.AreaRepository;
import com.gruponorte.mesaayudati.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

/** Reglas de administración de cuentas y asignación de rol y área. */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final AreaRepository areaRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, AreaRepository areaRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.areaRepository = areaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listar() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse crear(UserRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.username())
                || userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El usuario o correo ya está registrado");
        }
        AppUser account = new AppUser();
        applyRequest(account, request);
        return toResponse(userRepository.save(account));
    }

    public UserResponse actualizar(Long id, UserRequest request) {
        AppUser account = getEntity(id);
        if (userRepository.existsByUsernameIgnoreCaseAndIdNot(request.username(), id)
                || userRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El usuario o correo ya está registrado");
        }
        applyRequest(account, request);
        return toResponse(userRepository.save(account));
    }

    public void eliminar(Long id) {
        userRepository.delete(getEntity(id));
    }

    private void applyRequest(AppUser account, UserRequest request) {
        account.setUsername(request.username().trim());
        account.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setRole(request.role());
        account.setEnabled(request.enabled());
        account.setArea(request.areaId() == null ? null : areaRepository.findById(request.areaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Área no encontrada")));
    }

    private AppUser getEntity(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private UserResponse toResponse(AppUser account) {
        Area area = account.getArea();
        return new UserResponse(account.getId(), account.getUsername(), account.getEmail(),
                account.getRole().name(), area == null ? null : area.getId(),
                area == null ? null : area.getName(), account.isEnabled());
    }
}