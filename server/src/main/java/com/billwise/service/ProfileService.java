package com.billwise.service;

import com.billwise.model.Profile;
import com.billwise.repository.ProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public List<Profile> getProfilesByUser(String searchQuery) {
        return profileRepository.findByUserId(searchQuery);
    }

    public Profile getProfileById(String id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));
    }

    public Profile createProfile(Profile profile) {
        return profileRepository.save(profile);
    }

    public Profile updateProfile(String id, Profile profileUpdate) {
        Profile existing = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));

        if (profileUpdate.getName() != null) existing.setName(profileUpdate.getName());
        if (profileUpdate.getEmail() != null) existing.setEmail(profileUpdate.getEmail());
        if (profileUpdate.getPhoneNumber() != null) existing.setPhoneNumber(profileUpdate.getPhoneNumber());
        if (profileUpdate.getBusinessName() != null) existing.setBusinessName(profileUpdate.getBusinessName());
        if (profileUpdate.getContactAddress() != null) existing.setContactAddress(profileUpdate.getContactAddress());
        if (profileUpdate.getPaymentDetails() != null) existing.setPaymentDetails(profileUpdate.getPaymentDetails());
        if (profileUpdate.getLogo() != null) existing.setLogo(profileUpdate.getLogo());
        if (profileUpdate.getWebsite() != null) existing.setWebsite(profileUpdate.getWebsite());
        if (profileUpdate.getUserId() != null) existing.setUserId(profileUpdate.getUserId());

        return profileRepository.save(existing);
    }

    public void deleteProfile(String id) {
        if (!profileRepository.existsById(id)) {
            throw new RuntimeException("Profile not found with id: " + id);
        }
        profileRepository.deleteById(id);
    }
}
