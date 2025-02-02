package com.projecty.projectyweb.team;

import com.projecty.projectyweb.ProjectyWebApplication;
import com.projecty.projectyweb.message.MessageRepository;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectRepository;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
@AutoConfigureMockMvc
public class TeamControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    TeamRoleRepository teamRoleRepository;

    @MockBean
    private TeamRepository teamRepository;

    @MockBean
    private UserService userService;
    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ProjectRepository projectRepository;
    @Autowired
    private TeamRoleService teamRoleService;
    @MockBean
    private MessageRepository messageRepository;

    private Team team;
    private Project project;

    @Before
    public void init() {
        team = new Team();
        team.setId(1L);
        team.setName("team");

        User user = new User();
        user.setUsername("user");
        user.setId(2L);

        User user1 = new User();
        user1.setId(3L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setUsername("user2");

        TeamRole teamRole = new TeamRole();
        teamRole.setId(4L);
        teamRole.setUser(user);
        teamRole.setName(TeamRoles.MANAGER);
        teamRole.setTeam(team);

        List<TeamRole> teamRoles = new ArrayList<>();
        teamRoles.add(teamRole);

        team.setTeamRoles(teamRoles);
        user.setTeamRoles(teamRoles);
        project = new Project();
        project.setName("Sample");

        Mockito.when(teamRepository.findById(team.getId()))
                .thenReturn(java.util.Optional.ofNullable(team));

        Mockito.when(userService.getCurrentUser())
                .thenReturn(user);

        Mockito.when(userService.getUserSetByUsernamesWithoutCurrentUser(Collections.singletonList("user1")))
                .thenReturn(Collections.singleton(user1));

        Mockito.when(teamRepository.save(any(Team.class)))
                .thenReturn(team);

        Mockito.when(projectRepository.save(any(Project.class)))
                .thenReturn(project);

        Mockito.when(messageRepository.findByRecipientAndSeenDateIsNull(user))
                .thenReturn(new ArrayList<>());

        Mockito.when(userRepository.findById(user.getId()))
                .thenReturn(java.util.Optional.of(user));

        Mockito.when(userRepository.findById(user1.getId()))
                .thenReturn(java.util.Optional.of(user1));

        Mockito.when(userRepository.findByUsernameIn(Collections.singletonList(user1.getUsername())))
                .thenReturn(Collections.singleton(user1));

        Mockito.when(teamRoleRepository.findByTeamAndAndUser(team, user))
                .thenReturn(java.util.Optional.of(teamRole));

        Mockito.when(teamRoleRepository.findById(teamRole.getId()))
                .thenReturn(java.util.Optional.of(teamRole));

    }

    @Test
    @WithMockUser
    public void givenGetRequestOnMyTeams_shouldReturnMyTeamsView() throws Exception {
        mockMvc.perform(get("/team/myTeams"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("teamRoles"))
                .andExpect(view().name("fragments/team/my-teams"));
    }

    @Test
    @WithMockUser
    public void givenGetRequestOnAddTeam_shouldReturnAddTeamView() throws Exception {
        mockMvc.perform(get("/team/addTeam"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("team"))
                .andExpect(view().name("fragments/team/add-team"));
    }

    @Test
    @WithMockUser
    public void givenPostRequestOnAddTeam_shouldReturnMyTeamsView() throws Exception {
        mockMvc.perform(post("/team/addTeam")
                .flashAttr("team", team))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/team/myTeams"));
    }

    @Test
    @WithMockUser
    public void givenGetRequestOnAddTeamProject_shouldReturnAddTeamProjectView() throws Exception {
        mockMvc.perform(get("/team/addTeamProject"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("project"))
                .andExpect(view().name("fragments/team/add-project-team"));
    }

    @Test
    @WithMockUser
    public void givenGetRequestOnAddTeamProjectForSpecifiedTeam_shouldReturnAddTeamProjectViewForSpecifiedTeam() throws Exception {
        mockMvc.perform(get("/team/addTeamProject")
                .param("teamId", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("project"))
                .andExpect(view().name("fragments/team/add-project-specified-team"));
    }

    @Test
    @WithMockUser
    public void givenPostRequestOnAddTeamProject_shouldRedirectToMyTeams() throws Exception {
        mockMvc.perform(post("/team/addTeamProject")
                .flashAttr("project", project)
                .param("teamId", "1"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/team/myTeams"));
    }

    @Test
    @WithMockUser
    public void givenGetRequestOnManageTeam_shouldReturnManageTeamView() throws Exception {
        mockMvc.perform(get("/team/manageTeam")
                .param("teamId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/team/manage-team"));
    }

    @Test
    @WithMockUser
    public void givenPostRequestOnChangeName_shouldRedirectToManageTeam() throws Exception {
        mockMvc.perform(post("/team/changeName")
                .param("teamId", "1")
                .param("newName", "New name"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/team/manageTeam?teamId=1"));
    }

    // TODO: 6/28/19 Check if user has been added
    @Test
    @WithMockUser
    public void givenPostRequestOnAddUsersToTeam_shouldRedirectToManageTeam() throws Exception {
        mockMvc.perform(post("/team/addUsers")
                .param("teamId", "1")
                .param("usernames", "user1"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/team/manageTeam?teamId=1"));
    }

    @Test
    @WithMockUser
    public void givenRequestOnDeleteTeamRole_shouldRedirectToManageUsers() throws Exception {
        mockMvc.perform(post("/team/deleteTeamRole")
                .param("teamId", "1")
                .param("teamRoleId", "4"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/team/manageTeam?teamId=1"));
    }

    @Test
    @WithMockUser
    public void givenRequestOnChangeTeamRole_shouldRedirectToManageUsers() throws Exception {
        mockMvc.perform(post("/team/changeTeamRole")
                .param("teamId", "1")
                .param("teamRoleId", "4")
                .param("newRoleName", String.valueOf(TeamRoles.MEMBER)))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/team/manageTeam?teamId=1"));
    }

    @Test
    @WithMockUser
    public void givenRequestOnProjectList_shouldReturnTeamProjectList() throws Exception {
        mockMvc.perform(get("/team/projectList")
                .param("teamId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/team/project-list"));
    }

    @Test
    @WithMockUser
    public void givenRequestOnDeleteTeamConfirm_shouldReturnDeleteTeamConfirmView() throws Exception {
        mockMvc.perform(get("/team/deleteTeamConfirm")
                .param("teamId", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("team"))
                .andExpect(view().name("fragments/team/delete-team-confirm"));
    }

    @Test
    @WithMockUser
    public void givenRequestOnLeaveTeamConfirm_shouldReturnLeaveTeamView() throws Exception {
        mockMvc.perform(get("/team/leaveTeamConfirm")
                .param("teamId", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("team"))
                .andExpect(view().name("fragments/team/leave-team-confirm"));
    }

    @Test
    @WithMockUser
    public void givenRequestOnLeaveTeamPost_shouldRedirectToMyTeams() throws Exception {
        mockMvc.perform(post("/team/leaveTeam")
                .param("teamId", "1"))
                .andExpect(view().name("redirect:/team/myTeams"));
    }
}
