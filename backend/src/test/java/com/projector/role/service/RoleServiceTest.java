package com.projector.role.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.projector.role.model.Authority;
import com.projector.role.model.Role;
import com.projector.role.repository.RoleRepository;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class RoleServiceTest {

    @Mock private RoleRepository roleRepository;

    @InjectMocks private RoleService roleService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllRoles_Success() {
        Role role1 = createRole(1L, "Role1", Set.of(Authority.USER_VIEW.getName()));
        Role role2 = createRole(2L, "Role2", Set.of(Authority.ROLE_VIEW.getName()));

        when(roleRepository.findAll()).thenReturn(Flux.just(role1, role2));

        StepVerifier.create(roleService.getAllRoles()).expectNextCount(2).verifyComplete();

        verify(roleRepository, times(1)).findAll();
    }

    @Test
    public void testGetRoleById_Success() {
        Role role = createRole(1L, "TestRole", Set.of(Authority.USER_VIEW.getName()));

        when(roleRepository.findById(1L)).thenReturn(Mono.just(role));

        StepVerifier.create(roleService.getRoleById(1L))
                .expectNextMatches(r -> r.getId().equals(1L) && r.getName().equals("TestRole"))
                .verifyComplete();

        verify(roleRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetRoleById_NotFound() {
        when(roleRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(roleService.getRoleById(1L))
                .expectErrorMatches(
                        throwable -> {
                            if (throwable instanceof ServerWebInputException) {
                                String message = throwable.getMessage();
                                return message != null
                                        && (message.contains("Role not found")
                                                || message.contains("not found"));
                            }
                            return false;
                        })
                .verify();

        verify(roleRepository, times(1)).findById(1L);
    }

    @Test
    public void testCreateRole_Success() {
        Role role = new Role();
        role.setName("NewRole");
        role.setAuthorities(Set.of(Authority.USER_VIEW.getName(), Authority.USER_EDIT.getName()));
        Role savedRole =
                createRole(
                        1L,
                        "NewRole",
                        Set.of(Authority.USER_VIEW.getName(), Authority.USER_EDIT.getName()));

        when(roleRepository.existsByName("NewRole")).thenReturn(Mono.just(false));
        when(roleRepository.save(any(Role.class))).thenReturn(Mono.just(savedRole));

        StepVerifier.create(roleService.createRole(role))
                .expectNextMatches(r -> r.getId().equals(1L) && r.getName().equals("NewRole"))
                .verifyComplete();

        verify(roleRepository, times(1)).existsByName("NewRole");
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    public void testCreateRole_NameAlreadyExists() {
        Role role = new Role();
        role.setName("ExistingRole");
        role.setAuthorities(Set.of(Authority.USER_VIEW.getName()));

        when(roleRepository.existsByName("ExistingRole")).thenReturn(Mono.just(true));

        StepVerifier.create(roleService.createRole(role))
                .expectErrorMatches(
                        throwable -> {
                            if (throwable instanceof ServerWebInputException) {
                                String message = throwable.getMessage();
                                return message != null && message.contains("already exists");
                            }
                            return false;
                        })
                .verify();

        verify(roleRepository, times(1)).existsByName("ExistingRole");
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    public void testCreateRole_InvalidName() {
        Role role = new Role();
        role.setName("A"); // Too short

        StepVerifier.create(roleService.createRole(role))
                .expectErrorMatches(
                        throwable ->
                                throwable instanceof ServerWebInputException
                                        && throwable.getMessage().contains("at least 2 characters"))
                .verify();

        verify(roleRepository, never()).existsByName(anyString());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    public void testCreateRole_InvalidAuthority() {
        Role role = new Role();
        role.setName("NewRole");
        role.setAuthorities(Set.of("INVALID_AUTHORITY"));

        when(roleRepository.existsByName("NewRole")).thenReturn(Mono.just(false));

        StepVerifier.create(roleService.createRole(role))
                .expectErrorMatches(
                        throwable ->
                                throwable instanceof ServerWebInputException
                                        && throwable.getMessage() != null
                                        && throwable.getMessage().contains("Invalid authorities"))
                .verify();

        verify(roleRepository, times(1)).existsByName("NewRole");
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    public void testUpdateRole_Success() {
        Role existingRole = createRole(1L, "OldRole", Set.of(Authority.USER_VIEW.getName()));
        Role updateData = new Role();
        updateData.setName("UpdatedRole");
        updateData.setAuthorities(Set.of(Authority.USER_EDIT.getName()));
        Role updatedRole = createRole(1L, "UpdatedRole", Set.of(Authority.USER_EDIT.getName()));

        when(roleRepository.existsById(1L)).thenReturn(Mono.just(true));
        when(roleRepository.findByName("UpdatedRole")).thenReturn(Mono.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(Mono.just(updatedRole));

        StepVerifier.create(roleService.updateRole(1L, updateData))
                .expectNextMatches(r -> r.getId().equals(1L) && r.getName().equals("UpdatedRole"))
                .verifyComplete();

        verify(roleRepository, times(1)).existsById(1L);
        verify(roleRepository, times(1)).findByName("UpdatedRole");
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    public void testUpdateRole_NotFound() {
        Role updateData = new Role();
        updateData.setName("UpdatedRole");
        updateData.setAuthorities(Set.of(Authority.USER_VIEW.getName()));

        when(roleRepository.existsById(1L)).thenReturn(Mono.just(false));

        StepVerifier.create(roleService.updateRole(1L, updateData))
                .expectErrorMatches(
                        throwable -> {
                            if (throwable instanceof ServerWebInputException) {
                                String message = throwable.getMessage();
                                return message != null
                                        && (message.contains("Role not found")
                                                || message.contains("not found"));
                            }
                            return false;
                        })
                .verify();

        verify(roleRepository, times(1)).existsById(1L);
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    public void testDeleteRole_Success() {
        when(roleRepository.existsById(1L)).thenReturn(Mono.just(true));
        when(roleRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(roleService.deleteRole(1L)).verifyComplete();

        verify(roleRepository, times(1)).existsById(1L);
        verify(roleRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteRole_NotFound() {
        when(roleRepository.existsById(1L)).thenReturn(Mono.just(false));

        StepVerifier.create(roleService.deleteRole(1L))
                .expectErrorMatches(
                        throwable -> {
                            if (throwable instanceof ServerWebInputException) {
                                String message = throwable.getMessage();
                                return message != null
                                        && (message.contains("Role not found")
                                                || message.contains("not found"));
                            }
                            return false;
                        })
                .verify();

        verify(roleRepository, times(1)).existsById(1L);
        verify(roleRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testUpdateAuthorities_Success() {
        Role role = createRole(1L, "TestRole", Set.of(Authority.USER_VIEW.getName()));
        Set<String> newAuthorities =
                Set.of(Authority.USER_EDIT.getName(), Authority.ROLE_VIEW.getName());
        Role updatedRole = createRole(1L, "TestRole", newAuthorities);

        when(roleRepository.findById(1L)).thenReturn(Mono.just(role));
        when(roleRepository.save(any(Role.class))).thenReturn(Mono.just(updatedRole));

        StepVerifier.create(roleService.updateAuthorities(1L, newAuthorities))
                .expectNextMatches(r -> r.getAuthorities().equals(newAuthorities))
                .verifyComplete();

        verify(roleRepository, times(1)).findById(1L);
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    public void testUpdateAuthorities_RoleNotFound() {
        Set<String> newAuthorities = Set.of(Authority.USER_EDIT.getName());

        when(roleRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(roleService.updateAuthorities(1L, newAuthorities))
                .expectErrorMatches(
                        throwable -> {
                            if (throwable instanceof ServerWebInputException) {
                                String message = throwable.getMessage();
                                return message != null
                                        && (message.contains("Role not found")
                                                || message.contains("not found"));
                            }
                            return false;
                        })
                .verify();

        verify(roleRepository, times(1)).findById(1L);
        verify(roleRepository, never()).save(any(Role.class));
    }

    private Role createRole(Long id, String name, Set<String> authorities) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        role.setAuthorities(authorities != null ? new HashSet<>(authorities) : new HashSet<>());
        return role;
    }
}
