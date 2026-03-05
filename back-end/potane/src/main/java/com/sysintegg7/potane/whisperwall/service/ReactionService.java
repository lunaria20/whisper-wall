package com.sysintegg7.potane.whisperwall.service;

import com.sysintegg7.potane.whisperwall.dto.ReactionRequest;
import com.sysintegg7.potane.whisperwall.dto.ReactionResponse;
import com.sysintegg7.potane.whisperwall.exception.ResourceNotFoundException;
import com.sysintegg7.potane.whisperwall.model.Confession;
import com.sysintegg7.potane.whisperwall.model.Reaction;
import com.sysintegg7.potane.whisperwall.model.User;
import com.sysintegg7.potane.whisperwall.repository.ConfessionRepository;
import com.sysintegg7.potane.whisperwall.repository.ReactionRepository;
import com.sysintegg7.potane.whisperwall.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ReactionService {
    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private ConfessionRepository confessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ReactionResponse addReaction(Long confessionId, Long userId, ReactionRequest request) {
        Confession confession = confessionRepository.findById(confessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Confession not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        var existingReaction = reactionRepository.findByConfessionIdAndUserId(confessionId, userId);
        if (existingReaction.isPresent()) {
            Reaction reaction = existingReaction.get();
            reaction.setReactionType(request.getReactionType());
            Reaction updatedReaction = reactionRepository.save(reaction);
            return convertToReactionResponse(updatedReaction);
        }

        Reaction reaction = new Reaction();
        reaction.setReactionType(request.getReactionType());
        reaction.setConfession(confession);
        reaction.setUser(user);

        Reaction savedReaction = reactionRepository.save(reaction);
        return convertToReactionResponse(savedReaction);
    }

    public List<ReactionResponse> getConfessionReactions(Long confessionId) {
        return reactionRepository.findByConfessionId(confessionId)
                .stream()
                .map(this::convertToReactionResponse)
                .toList();
    }

    @Transactional
    public void removeReaction(Long confessionId, Long userId) {
        var reaction = reactionRepository.findByConfessionIdAndUserId(confessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Reaction not found"));
        reactionRepository.delete(reaction);
    }

    private ReactionResponse convertToReactionResponse(Reaction reaction) {
        return new ReactionResponse(
                reaction.getId(),
                reaction.getReactionType(),
                reaction.getConfession().getId(),
                reaction.getUser().getId(),
                reaction.getUser().getUsername(),
                reaction.getCreatedAt()
        );
    }
}
