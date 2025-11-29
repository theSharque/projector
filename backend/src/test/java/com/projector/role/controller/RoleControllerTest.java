package com.projector.role.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.projector.role.model.Authority;
import com.projector.role.model.Role;
import com.projector.role.service.RoleService;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class RoleControllerTest {

    @Mock private RoleService roleService;

    @InjectMocks private RoleController roleController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllRoles_Success() {
        Role role1 = createRole(1L, "Role1", Set.of(Authority.USER_VIEW.getName()));
        Role role2 = createRole(2L, "Role2", Set.of(Authority.ROLE_VIEW.getName()));

        when(roleService.getAllRoles()).thenReturn(Flux.just(role1, role2));

        StepVerifier.create(roleController.getAllRoles())
                .expectNext(role1)
                .expectNext(role2)
                .verifyComplete();

        verify(roleService, times(1)).getAllRoles();
    }

    @Test
    public void testGetRoleById_Success() {
        Role role = createRole(1L, "TestRole", Set.of(Authority.USER_VIEW.getName()));

        when(roleService.getRoleById(1L)).thenReturn(Mono.just(role));

        StepVerifier.create(roleController.getRoleById(1L))
                .expectNextMatches(
                        response ->
                                response.getStatusCode() == HttpStatus.OK
                                        && response.getBody().equals(role))
                .verifyComplete();

        verify(roleService, times(1)).getRoleById(1L);
    }

    @Test
    public void testGetRoleById_NotFound() {
        when(roleService.getRoleById(1L))
                .thenReturn(Mono.error(new ServerWebInputException("Role not found")));

        StepVerifier.create(roleController.getRoleById(1L))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();

        verify(roleService, times(1)).getRoleById(1L);
    }

    @Test
    public void testCreateRole_Success() {
        Role role = new Role();
        role.setName("NewRole");
        role.setAuthorities(Set.of(Authority.USER_VIEW.getName()));
        Role savedRole = createRole(1L, "NewRole", Set.of(Authority.USER_VIEW.getName()));

        when(roleService.createRole(any(Role.class))).thenReturn(Mono.just(savedRole));

        StepVerifier.create(roleController.createRole(role))
                .expectNextMatches(
                        response ->
                                response.getStatusCode() == HttpStatus.OK
                                        && response.getBody().equals(savedRole))
                .verifyComplete();

        verify(roleService, times(1)).createRole(any(Role.class));
    }

    @Test
    public void testCreateRole_Error() {
        Role role = new Role();
        role.setName("ExistingRole");
        role.setAuthorities(Set.of(Authority.USER_VIEW.getName()));

        when(roleService.createRole(any(Role.class)))
                .thenReturn(Mono.error(new ServerWebInputException("Role already exists")));

        StepVerifier.create(roleController.createRole(role))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();

        verify(roleService, times(1)).createRole(any(Role.class));
    }

    @Test
    public void testUpdateRole_Success() {
        Role role = createRole(1L, "UpdatedRole", Set.of(Authority.USER_EDIT.getName()));

        when(roleService.updateRole(anyLong(), any(Role.class))).thenReturn(Mono.just(role));

        StepVerifier.create(roleController.updateRole(1L, role))
                .expectNextMatches(
                        response ->
                                response.getStatusCode() == HttpStatus.OK
                                        && response.getBody().equals(role))
                .verifyComplete();

        verify(roleService, times(1)).updateRole(eq(1L), any(Role.class));
    }

    @Test
    public void testUpdateRole_Error() {
        Role role = createRole(1L, "UpdatedRole", Set.of(Authority.USER_VIEW.getName()));

        when(roleService.updateRole(anyLong(), any(Role.class)))
                .thenReturn(Mono.error(new ServerWebInputException("Role not found")));

        StepVerifier.create(roleController.updateRole(1L, role))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();

        verify(roleService, times(1)).updateRole(eq(1L), any(Role.class));
    }

    @Test
    public void testDeleteRole_Success() {
        when(roleService.deleteRole(1L)).thenReturn(Mono.empty());

        StepVerifier.create(roleController.deleteRole(1L))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NO_CONTENT)
                .verifyComplete();

        verify(roleService, times(1)).deleteRole(1L);
    }

    @Test
    public void testDeleteRole_Error() {
        when(roleService.deleteRole(1L))
                .thenReturn(Mono.error(new ServerWebInputException("Role not found")));

        StepVerifier.create(roleController.deleteRole(1L))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();

        verify(roleService, times(1)).deleteRole(1L);
    }

    @Test
    public void testUpdateAuthorities_Success() {
        Role role =
                createRole(
                        1L,
                        "TestRole",
                        Set.of(Authority.USER_EDIT.getName(), Authority.ROLE_VIEW.getName()));
        Set<String> newAuthorities =
                Set.of(Authority.USER_EDIT.getName(), Authority.ROLE_VIEW.getName());

        when(roleService.updateAuthorities(anyLong(), anySet())).thenReturn(Mono.just(role));

        StepVerifier.create(roleController.updateAuthorities(1L, newAuthorities))
                .expectNextMatches(
                        response ->
                                response.getStatusCode() == HttpStatus.OK
                                        && response.getBody().equals(role))
                .verifyComplete();

        verify(roleService, times(1)).updateAuthorities(eq(1L), anySet());
    }

    @Test
    public void testUpdateAuthorities_Error() {
        Set<String> newAuthorities = Set.of(Authority.USER_EDIT.getName());

        when(roleService.updateAuthorities(anyLong(), anySet()))
                .thenReturn(Mono.error(new ServerWebInputException("Role not found")));

        StepVerifier.create(roleController.updateAuthorities(1L, newAuthorities))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();

        verify(roleService, times(1)).updateAuthorities(eq(1L), anySet());
    }

    private Role createRole(Long id, String name, Set<String> authorities) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        role.setAuthorities(authorities != null ? new HashSet<>(authorities) : new HashSet<>());
        return role;
    }
}
