import os
import re

# Base directory for all sources
base_path = r"d:\User\Projects\whisper-wall\back-end\potane\src\main\java\com\sysintegg7\potane\whisperwall"

# Mapping of old imports to new imports
import_fixes = {
    # DTO imports
    "import com.sysintegg7.potane.whisperwall.dto.CommentRequest;": "import com.sysintegg7.potane.whisperwall.features.comments.dto.CommentRequest;",
    "import com.sysintegg7.potane.whisperwall.dto.CommentResponse;": "import com.sysintegg7.potane.whisperwall.features.comments.dto.CommentResponse;",
    "import com.sysintegg7.potane.whisperwall.dto.ConfessionRequest;": "import com.sysintegg7.potane.whisperwall.features.confessions.dto.ConfessionRequest;",
    "import com.sysintegg7.potane.whisperwall.dto.ConfessionResponse;": "import com.sysintegg7.potane.whisperwall.features.confessions.dto.ConfessionResponse;",
    "import com.sysintegg7.potane.whisperwall.dto.ReactionRequest;": "import com.sysintegg7.potane.whisperwall.features.reactions.dto.ReactionRequest;",
    "import com.sysintegg7.potane.whisperwall.dto.ReactionResponse;": "import com.sysintegg7.potane.whisperwall.features.reactions.dto.ReactionResponse;",
    "import com.sysintegg7.potane.whisperwall.dto.ReportRequest;": "import com.sysintegg7.potane.whisperwall.features.reports.dto.ReportRequest;",
    "import com.sysintegg7.potane.whisperwall.dto.ReportResponse;": "import com.sysintegg7.potane.whisperwall.features.reports.dto.ReportResponse;",
    "import com.sysintegg7.potane.whisperwall.dto.AuthRequest;": "import com.sysintegg7.potane.whisperwall.features.auth.dto.AuthRequest;",
    "import com.sysintegg7.potane.whisperwall.dto.AuthResponse;": "import com.sysintegg7.potane.whisperwall.features.auth.dto.AuthResponse;",
    "import com.sysintegg7.potane.whisperwall.dto.RegisterRequest;": "import com.sysintegg7.potane.whisperwall.features.auth.dto.RegisterRequest;",
    "import com.sysintegg7.potane.whisperwall.dto.ChangePasswordRequest;": "import com.sysintegg7.potane.whisperwall.features.auth.dto.ChangePasswordRequest;",
    "import com.sysintegg7.potane.whisperwall.dto.CreateUserRequest;": "import com.sysintegg7.potane.whisperwall.features.users.dto.CreateUserRequest;",
    "import com.sysintegg7.potane.whisperwall.dto.UserResponse;": "import com.sysintegg7.potane.whisperwall.features.users.dto.UserResponse;",
    "import com.sysintegg7.potane.whisperwall.dto.UserProfileUpdateRequest;": "import com.sysintegg7.potane.whisperwall.features.users.dto.UserProfileUpdateRequest;",
    "import com.sysintegg7.potane.whisperwall.dto.UsageStatsResponse;": "import com.sysintegg7.potane.whisperwall.features.users.dto.UsageStatsResponse;",
    "import com.sysintegg7.potane.whisperwall.dto.SendRestrictionRequestRequest;": "import com.sysintegg7.potane.whisperwall.features.restrictions.dto.SendRestrictionRequestRequest;",
    "import com.sysintegg7.potane.whisperwall.dto.RestrictionRequestResponse;": "import com.sysintegg7.potane.whisperwall.features.restrictions.dto.RestrictionRequestResponse;",
    "import com.sysintegg7.potane.whisperwall.dto.RestrictUserRequest;": "import com.sysintegg7.potane.whisperwall.features.restrictions.dto.RestrictUserRequest;",
    
    # Repository imports
    "import com.sysintegg7.potane.whisperwall.repository.CommentRepository;": "import com.sysintegg7.potane.whisperwall.features.comments.repository.CommentRepository;",
    "import com.sysintegg7.potane.whisperwall.repository.ConfessionRepository;": "import com.sysintegg7.potane.whisperwall.features.confessions.repository.ConfessionRepository;",
    "import com.sysintegg7.potane.whisperwall.repository.ReactionRepository;": "import com.sysintegg7.potane.whisperwall.features.reactions.repository.ReactionRepository;",
    "import com.sysintegg7.potane.whisperwall.repository.ReportRepository;": "import com.sysintegg7.potane.whisperwall.features.reports.repository.ReportRepository;",
    "import com.sysintegg7.potane.whisperwall.repository.RestrictionRequestRepository;": "import com.sysintegg7.potane.whisperwall.features.restrictions.repository.RestrictionRequestRepository;",
    
    # Service imports
    "import com.sysintegg7.potane.whisperwall.service.ReportService;": "import com.sysintegg7.potane.whisperwall.features.reports.service.ReportService;",
    "import com.sysintegg7.potane.whisperwall.service.AdminService;": "import com.sysintegg7.potane.whisperwall.features.admin.service.AdminService;",
    "import com.sysintegg7.potane.whisperwall.service.ModeratorService;": "import com.sysintegg7.potane.whisperwall.features.moderation.service.ModeratorService;",
    
    # Model imports (old location)
    "import com.sysintegg7.potane.whisperwall.model.User;": "import com.sysintegg7.potane.whisperwall.shared.model.User;",
    "import com.sysintegg7.potane.whisperwall.model.Role;": "import com.sysintegg7.potane.whisperwall.shared.model.Role;",
    "import com.sysintegg7.potane.whisperwall.model.Confession;": "import com.sysintegg7.potane.whisperwall.features.confessions.model.Confession;",
    "import com.sysintegg7.potane.whisperwall.model.Comment;": "import com.sysintegg7.potane.whisperwall.features.comments.model.Comment;",
    "import com.sysintegg7.potane.whisperwall.model.Reaction;": "import com.sysintegg7.potane.whisperwall.features.reactions.model.Reaction;",
    "import com.sysintegg7.potane.whisperwall.model.Report;": "import com.sysintegg7.potane.whisperwall.features.reports.model.Report;",
    "import com.sysintegg7.potane.whisperwall.model.RestrictionRequest;": "import com.sysintegg7.potane.whisperwall.features.restrictions.model.RestrictionRequest;",
    
    # Old repository imports (not in features)
    "import com.sysintegg7.potane.whisperwall.repository.UserRepository;": "import com.sysintegg7.potane.whisperwall.shared.repository.UserRepository;",
    "import com.sysintegg7.potane.whisperwall.repository.RoleRepository;": "import com.sysintegg7.potane.whisperwall.shared.repository.RoleRepository;",
}

# Walk through all Java files
fixed_count = 0
for root, dirs, files in os.walk(base_path):
    for file in files:
        if file.endswith(".java"):
            filepath = os.path.join(root, file)
            try:
                with open(filepath, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                original_content = content
                
                # Apply all import fixes
                for old_import, new_import in import_fixes.items():
                    content = content.replace(old_import, new_import)
                
                # Write back if changed
                if content != original_content:
                    with open(filepath, 'w', encoding='utf-8') as f:
                        f.write(content)
                    fixed_count += 1
                    print(f"Fixed: {filepath}")
            except Exception as e:
                print(f"Error processing {filepath}: {e}")

print(f"\nTotal files fixed: {fixed_count}")
