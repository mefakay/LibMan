package com.library.libman.repository;

import com.library.libman.entity.ProfileUpdateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProfileUpdateRequestRepository extends JpaRepository<ProfileUpdateRequest, Long> {
    List<ProfileUpdateRequest> findByStatus(ProfileUpdateRequest.RequestStatus status);
}