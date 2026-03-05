package com.sysintegg7.potane.whisperwall.service;

import com.sysintegg7.potane.whisperwall.dto.ConfessionRequest;
import com.sysintegg7.potane.whisperwall.dto.ConfessionResponse;
import com.sysintegg7.potane.whisperwall.exception.ResourceNotFoundException;
import com.sysintegg7.potane.whisperwall.exception.UnauthorizedException;
import com.sysintegg7.potane.whisperwall.model.Confession;
import com.sysintegg7.potane.whisperwall.model.User;
import com.sysintegg7.potane.whisperwall.repository.CommentRepository;
import com.sysintegg7.potane.whisperwall.repository.ConfessionRepository;
import com.sysintegg7.potane.whisperwall.repository.ReactionRepository;
import com.sysintegg7.potane.whisperwall.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ConfessionService {
    @Autowired
    private ConfessionRepository confessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReactionRepository reactionRepository;

    @Transactional
    public ConfessionResponse createConfession(Long userId, ConfessionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Confession confession = new Confession();
        confession.setContent(request.getContent());
        confession.setUser(user);
        confession.setCategory(request.getCategory());
        confession.setMood(request.getMood());
        confession.setIsApproved(false);
        confession.setIsVisible(true);

        Confession savedConfession = confessionRepository.save(confession);
        return convertToConfessionResponse(savedConfession);
    }

    public ConfessionResponse getConfession(Long id) {
        Confession confession = confessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Confession not found"));
        return convertToConfessionResponse(confession);
    }

    public Page<ConfessionResponse> getPublicConfessions(Pageable pageable) {
        return confessionRepository.findByIsVisibleAndIsApprovedOrderByCreatedAtDesc(true, true, pageable)
                .map(this::convertToConfessionResponse);
    }

    public Page<ConfessionResponse> getUserConfessions(Long userId, Pageable pageable) {
        return confessionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::convertToConfessionResponse);
    }

    @Transactional
    public ConfessionResponse updateConfession(Long id, Long userId, ConfessionRequest request) {
        Confession confession = confessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Confession not found"));

        if (!confession.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only update your own confessions");
        }

        confession.setContent(request.getContent());
        confession.setCategory(request.getCategory());
        confession.setMood(request.getMood());

        Confession updatedConfession = confessionRepository.save(confession);
        return convertToConfessionResponse(updatedConfession);
    }

    @Transactional
    public void deleteConfession(Long id, Long userId) {
        Confession confession = confessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Confession not found"));

        if (!confession.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own confessions");
        }

        confessionRepository.delete(confession);
    }

    @Transactional
    public void approveConfession(Long id) {
        Confession confession = confessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Confession not found"));
        confession.setIsApproved(true);
        confessionRepository.save(confession);
    }

    @Transactional
    public void hideConfession(Long id) {
        Confession confession = confessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Confession not found"));
        confession.setIsVisible(false);
        confessionRepository.save(confession);
    }

    private ConfessionResponse convertToConfessionResponse(Confession confession) {
        Long commentCount = commentRepository.countByConfessionId(confession.getId());
        Long reactionCount = reactionRepository.countByConfessionId(confession.getId());

        return new ConfessionResponse(
                confession.getId(),
                confession.getContent(),
                confession.getUser().getId(),
                confession.getUser().getUsername(),
                confession.getUser().getDisplayName(),
                confession.getUser().getProfilePicture(),
                confession.getIsApproved(),
                confession.getIsVisible(),
                confession.getCategory(),
                confession.getMood(),
                confession.getReportCount(),
                Math.toIntExact(commentCount),
                Math.toIntExact(reactionCount),
                confession.getCreatedAt(),
                confession.getUpdatedAt()
        );
    }
}
