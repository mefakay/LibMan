package com.library.libman.service;

import com.library.libman.entity.ProfileUpdateRequest;
import com.library.libman.entity.User;
import com.library.libman.repository.ProfileUpdateRequestRepository;
import com.library.libman.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileUpdateRequestServiceTest {

    @Mock
    private ProfileUpdateRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfileUpdateRequestService profileUpdateRequestService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("oldusername");
        testUser.setEmail("old@email.com");
        testUser.setFullName("Test User");
        testUser.setRole(User.UserRole.USER);
    }

    @Test
    void createRequest_success_savesNewRequest() {
        // GIVEN
        String newUsername = "newusername";
        String newEmail = "new@email.com";

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty());
        when(userRepository.findByUsername(newUsername)).thenReturn(Optional.empty());

        ProfileUpdateRequest savedRequest = new ProfileUpdateRequest();
        savedRequest.setId(10L);
        savedRequest.setUser(testUser);
        savedRequest.setNewUsername(newUsername);
        savedRequest.setNewEmail(newEmail);
        savedRequest.setStatus(ProfileUpdateRequest.RequestStatus.PENDING);
        savedRequest.setRequestDate(LocalDate.now());

        when(requestRepository.save(any(ProfileUpdateRequest.class))).thenReturn(savedRequest);

        // WHEN
        ProfileUpdateRequest result = profileUpdateRequestService.createRequest(1L, newUsername, newEmail);

        // THEN
        assertNotNull(result);
        assertEquals(newUsername, result.getNewUsername());
        assertEquals(newEmail, result.getNewEmail());
        assertEquals(ProfileUpdateRequest.RequestStatus.PENDING, result.getStatus());

        verify(requestRepository).save(any(ProfileUpdateRequest.class));
    }

    @Test
    void createRequest_throws_whenEmailAlreadyInUse() {
        // GIVEN
        String newUsername = "newusername";
        String newEmail = "existing@email.com";

        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail(newEmail);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.of(otherUser));

        // WHEN & THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> profileUpdateRequestService.createRequest(1L, newUsername, newEmail));

        assertTrue(ex.getMessage().contains("E-posta kullanımda"));
        verify(requestRepository, never()).save(any());
    }

    @Test
    void createRequest_throws_whenUsernameAlreadyInUse() {
        // GIVEN
        String newUsername = "existinguser";
        String newEmail = "new@email.com";

        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername(newUsername);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty());
        when(userRepository.findByUsername(newUsername)).thenReturn(Optional.of(otherUser));

        // WHEN & THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> profileUpdateRequestService.createRequest(1L, newUsername, newEmail));

        assertTrue(ex.getMessage().contains("Kullanıcı adı kullanımda"));
        verify(requestRepository, never()).save(any());
    }

    @Test
    void createRequest_allows_whenUserUpdatesOwnEmail() {
        // GIVEN - Kullanıcı kendi emailini tekrar girmişse hata vermemeli
        String newUsername = "newusername";
        String sameEmail = "old@email.com";

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(sameEmail)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername(newUsername)).thenReturn(Optional.empty());
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        ProfileUpdateRequest result = profileUpdateRequestService.createRequest(1L, newUsername, sameEmail);

        // THEN
        assertNotNull(result);
        verify(requestRepository).save(any());
    }

    @Test
    void approveRequest_success_updatesUserAndMarksApproved() {
        // GIVEN
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setId(10L);
        request.setUser(testUser);
        request.setNewUsername("approveduser");
        request.setNewEmail("approved@email.com");
        request.setStatus(ProfileUpdateRequest.RequestStatus.PENDING);

        when(requestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(userRepository.findByEmail("approved@email.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        profileUpdateRequestService.approveRequest(10L);

        // THEN
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User updatedUser = userCaptor.getValue();
        assertEquals("approveduser", updatedUser.getUsername());
        assertEquals("approved@email.com", updatedUser.getEmail());

        ArgumentCaptor<ProfileUpdateRequest> reqCaptor = ArgumentCaptor.forClass(ProfileUpdateRequest.class);
        verify(requestRepository).save(reqCaptor.capture());
        assertEquals(ProfileUpdateRequest.RequestStatus.APPROVED, reqCaptor.getValue().getStatus());
    }

    @Test
    void approveRequest_throws_whenEmailNoLongerAvailable() {
        // GIVEN
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setId(10L);
        request.setUser(testUser);
        request.setNewEmail("taken@email.com");
        request.setNewUsername("newuser");
        request.setStatus(ProfileUpdateRequest.RequestStatus.PENDING);

        User competitor = new User();
        competitor.setId(99L);
        competitor.setEmail("taken@email.com");

        when(requestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(userRepository.findByEmail("taken@email.com")).thenReturn(Optional.of(competitor));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // WHEN & THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> profileUpdateRequestService.approveRequest(10L));

        assertTrue(ex.getMessage().contains("E-posta artık müsait değil"));

        ArgumentCaptor<ProfileUpdateRequest> captor = ArgumentCaptor.forClass(ProfileUpdateRequest.class);
        verify(requestRepository).save(captor.capture());
        assertEquals(ProfileUpdateRequest.RequestStatus.REJECTED, captor.getValue().getStatus());
    }

    @Test
    void approveRequest_doesNothing_whenNotPending() {
        // GIVEN
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setId(10L);
        request.setStatus(ProfileUpdateRequest.RequestStatus.APPROVED);

        when(requestRepository.findById(10L)).thenReturn(Optional.of(request));

        // WHEN
        profileUpdateRequestService.approveRequest(10L);

        // THEN
        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectRequest_success_marksAsRejected() {
        // GIVEN
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setId(10L);
        request.setStatus(ProfileUpdateRequest.RequestStatus.PENDING);

        when(requestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        profileUpdateRequestService.rejectRequest(10L);

        // THEN
        ArgumentCaptor<ProfileUpdateRequest> captor = ArgumentCaptor.forClass(ProfileUpdateRequest.class);
        verify(requestRepository).save(captor.capture());
        assertEquals(ProfileUpdateRequest.RequestStatus.REJECTED, captor.getValue().getStatus());
    }

    @Test
    void getPendingRequests_returnsOnlyPending() {
        // GIVEN
        ProfileUpdateRequest pending1 = new ProfileUpdateRequest();
        pending1.setId(1L);
        pending1.setStatus(ProfileUpdateRequest.RequestStatus.PENDING);

        ProfileUpdateRequest pending2 = new ProfileUpdateRequest();
        pending2.setId(2L);
        pending2.setStatus(ProfileUpdateRequest.RequestStatus.PENDING);

        when(requestRepository.findByStatus(ProfileUpdateRequest.RequestStatus.PENDING))
                .thenReturn(List.of(pending1, pending2));

        // WHEN
        List<ProfileUpdateRequest> result = profileUpdateRequestService.getPendingRequests();

        // THEN
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(r -> r.getStatus() == ProfileUpdateRequest.RequestStatus.PENDING));
        verify(requestRepository).findByStatus(ProfileUpdateRequest.RequestStatus.PENDING);
    }

    @Test
    void createRequest_throws_whenUserNotFound() {
        // GIVEN
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> profileUpdateRequestService.createRequest(99L, "user", "email@test.com"));

        assertTrue(ex.getMessage().contains("Kullanıcı yok"));
    }
}