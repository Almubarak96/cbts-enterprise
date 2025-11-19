package com.almubaraksuleiman.cbts.security.authentication.service;

import com.almubaraksuleiman.cbts.security.authentication.entity.Role;
import com.almubaraksuleiman.cbts.security.authentication.entity.UserRole;
import com.almubaraksuleiman.cbts.security.authentication.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;

    /**
     * Returns role names (e.g. ROLE_ADMIN) for a username.
     */
    public Set<String> getRoleNames(String username) {
        return userRoleRepository.findAllByUsername(username).stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toSet());
    }

    /**
     * Returns permission names (e.g. CAN_CREATE_EXAM) aggregated for all roles of the user.
     */
    public Set<String> getPermissionNames(String username) {
        return userRoleRepository.findAllByUsername(username).stream()
                .map(UserRole::getRole)
                .flatMap((Role r) -> r.getPermissions().stream())
                .map(p -> p.getName())
                .collect(Collectors.toSet());
    }
}
