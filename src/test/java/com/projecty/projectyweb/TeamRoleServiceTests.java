package com.projecty.projectyweb;

import com.projecty.projectyweb.team.Team;
import com.projecty.projectyweb.team.TeamRepository;
import com.projecty.projectyweb.team.role.TeamRole;
import com.projecty.projectyweb.team.role.TeamRoleRepository;
import com.projecty.projectyweb.team.role.TeamRoleService;
import com.projecty.projectyweb.team.role.TeamRoles;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import com.projecty.projectyweb.user.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
public class TeamRoleServiceTests {

    @Autowired
    private TeamRoleService teamRoleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamRoleRepository teamRoleRepository;

    @MockBean
    private UserService userService;

    private User user;
    private User user1;

    @Before
    public void init() {
        user = new User();
        user.setUsername("user");
        userRepository.save(user);

        user1 = new User();
        user1.setUsername("user1");
        userRepository.save(user1);

        Mockito.when(userService.getCurrentUser())
                .thenReturn(user);
    }


    @Test
    public void whenAddTeamRolesByUsernames_shouldReturnTeamWithTeamRoles() {
        Team team = new Team();
        team.setName("Sample team");
        team = teamRepository.save(team);
        List<String> usernames = new ArrayList<>();
        usernames.add(user.getUsername());
        usernames.add(user1.getUsername());
        teamRoleService.addTeamRolesToTeamByUsernames(team, usernames, null);

        Optional<Team> optionalTeam = teamRepository.findById(team.getId());
        if (optionalTeam.isPresent()) {
            assertThat(teamRoleRepository.findByTeamAndAndUser(optionalTeam.get(), user), is(notNullValue()));
            assertThat(teamRoleRepository.findByTeamAndAndUser(optionalTeam.get(), user1), is(notNullValue()));
        } else {
            assert false;
        }
    }

    @Test
    public void whenAddCurrentUserAsTeamManager_shouldReturnTeamWithCurrentUserAsTeamManager() {
        Team team = new Team();
        team.setName("Sample");
        teamRoleService.addCurrentUserToTeamAsManager(team);
        teamRepository.save(team);

        Optional<Team> optionalTeam = teamRepository.findById(team.getId());
        if (optionalTeam.isPresent() && teamRoleRepository.findByTeamAndAndUser(optionalTeam.get(), user).isPresent()) {
            assertThat(
                    teamRoleRepository.findByTeamAndAndUser(optionalTeam.get(), user).get().getName(),
                    is(TeamRoles.MANAGER));
        } else {
            assert false;
        }
    }

    @Test
    public void whenCurrentUserIsProjectManager_shouldReturnTrue() {
        Team team = new Team();
        team.setName("Sample");
        TeamRole teamRole = new TeamRole();
        teamRole.setUser(user);
        teamRole.setTeam(team);
        teamRole.setName(TeamRoles.MANAGER);
        team.setTeamRoles(Collections.singletonList(teamRole));
        teamRepository.save(team);

        assertThat(teamRoleService.isCurrentUserTeamManager(team), is(true));
    }

    @Test
    public void whenCurrentUserIsNotProjectManager_shouldReturnFalse() {
        Team team = new Team();
        team.setName("Sample");
        TeamRole teamRole = new TeamRole();
        teamRole.setUser(user);
        teamRole.setTeam(team);
        teamRole.setName(TeamRoles.MEMBER);
        team.setTeamRoles(Collections.singletonList(teamRole));
        teamRepository.save(team);

        assertThat(teamRoleService.isCurrentUserTeamManager(team), is(false));
    }


}