package com.library.libman.service;

import com.library.libman.entity.ProfileUpdateRequest;
import com.library.libman.entity.User;
import com.library.libman.repository.ProfileUpdateRequestRepository;
import com.library.libman.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class ProfileUpdateRequestService {

    @Autowired private ProfileUpdateRequestRepository requestRepository;
    @Autowired private UserRepository userRepository;

    public ProfileUpdateRequest createRequest(Long userId, String newUsername, String newEmail) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Kullanıcı yok"));

        userRepository.findByEmail(newEmail).ifPresent(u -> {
            if (!u.getId().equals(userId)) throw new RuntimeException("E-posta kullanımda.");
        });

        userRepository.findByUsername(newUsername).ifPresent(u -> {
            if (!u.getId().equals(userId)) throw new RuntimeException("Kullanıcı adı kullanımda.");
        });

        ProfileUpdateRequest req = new ProfileUpdateRequest();
        req.setUser(user);
        req.setNewUsername(newUsername);
        req.setNewEmail(newEmail);
        req.setRequestDate(LocalDate.now());
        req.setStatus(ProfileUpdateRequest.RequestStatus.PENDING);

        return requestRepository.save(req);
    }

    public void approveRequest(Long requestId) {
        ProfileUpdateRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("İstek bulunamadı"));

        if (req.getStatus() != ProfileUpdateRequest.RequestStatus.PENDING) return;

        User user = req.getUser();
        if (userRepository.findByEmail(req.getNewEmail()).isPresent() && !user.getEmail().equals(req.getNewEmail())) {
            req.setStatus(ProfileUpdateRequest.RequestStatus.REJECTED);
            requestRepository.save(req);
            throw new RuntimeException("E-posta artık müsait değil.");
        }

        user.setUsername(req.getNewUsername());
        user.setEmail(req.getNewEmail());
        userRepository.save(user);

        req.setStatus(ProfileUpdateRequest.RequestStatus.APPROVED);
        requestRepository.save(req);
    }

    public void rejectRequest(Long requestId) {
        ProfileUpdateRequest req = requestRepository.findById(requestId).orElseThrow();
        req.setStatus(ProfileUpdateRequest.RequestStatus.REJECTED);
        requestRepository.save(req);
    }

    public List<ProfileUpdateRequest> getPendingRequests() {
        return requestRepository.findByStatus(ProfileUpdateRequest.RequestStatus.PENDING);
    }
}