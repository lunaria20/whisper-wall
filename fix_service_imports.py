import os
import re

# Base directory
base_path = r"d:\User\Projects\whisper-wall\back-end\potane\src\main\java\com\sysintegg7\potane\whisperwall\features"

# Mapping of import patterns to their correct locations
import_mappings = {
    r"import com\.sysintegg7\.potane\.whisperwall\.dto\.(\w+);": {
        "CommentRequest": "import com.sysintegg7.potane.whisperwall.features.comments.dto.CommentRequest;",
        "CommentResponse": "import com.sysintegg7.potane.whisperwall.features.comments.dto.CommentResponse;",
        "ConfessionRequest": "import com.sysintegg7.potane.whisperwall.features.confessions.dto.ConfessionRequest;",
        "ConfessionResponse": "import com.sysintegg7.potane.whisperwall.features.confessions.dto.ConfessionResponse;",
        "ReactionRequest": "import com.sysintegg7.potane.whisperwall.features.reactions.dto.ReactionRequest;",
        "ReactionResponse": "import com.sysintegg7.potane.whisperwall.features.reactions.dto.ReactionResponse;",
        "ReportRequest": "import com.sysintegg7.potane.whisperwall.features.reports.dto.ReportRequest;",
        "ReportResponse": "import com.sysintegg7.potane.whisperwall.features.reports.dto.ReportResponse;",
        "AuthRequest": "import com.sysintegg7.potane.whisperwall.features.auth.dto.AuthRequest;",
        "AuthResponse": "import com.sysintegg7.potane.whisperwall.features.auth.dto.AuthResponse;",
        "RegisterRequest": "import com.sysintegg7.potane.whisperwall.features.auth.dto.RegisterRequest;",
        "ChangePasswordRequest": "import com.sysintegg7.potane.whisperwall.features.auth.dto.ChangePasswordRequest;",
        "CreateUserRequest": "import com.sysintegg7.potane.whisperwall.features.users.dto.CreateUserRequest;",
        "UserResponse": "import com.sysintegg7.potane.whisperwall.features.users.dto.UserResponse;",
        "UserProfileUpdateRequest": "import com.sysintegg7.potane.whisperwall.features.users.dto.UserProfileUpdateRequest;",
        "UsageStatsResponse": "import com.sysintegg7.potane.whisperwall.features.users.dto.UsageStatsResponse;",
        "SendRestrictionRequestRequest": "import com.sysintegg7.potane.whisperwall.features.restrictions.dto.SendRestrictionRequestRequest;",
        "RestrictionRequestResponse": "import com.sysintegg7.potane.whisperwall.features.restrictions.dto.RestrictionRequestResponse;",
        "RestrictUserRequest": "import com.sysintegg7.potane.whisperwall.features.restrictions.dto.RestrictUserRequest;",
    },
    r"import com\.sysintegg7\.potane\.whisperwall\.repository\.(\w+);": {
        "CommentRepository": "import com.sysintegg7.potane.whisperwall.features.comments.repository.CommentRepository;",
        "ConfessionRepository": "import com.sysintegg7.potane.whisperwall.features.confessions.repository.ConfessionRepository;",
        "ReactionRepository": "import com.sysintegg7.potane.whisperwall.features.reactions.repository.ReactionRepository;",
        "ReportRepository": "import com.sysintegg7.potane.whisperwall.features.reports.repository.ReportRepository;",
        "RestrictionRequestRepository": "import com.sysintegg7.potane.whisperwall.features.restrictions.repository.RestrictionRequestRepository;",
    }
}

# Walk through all Java files
for root, dirs, files in os.walk(base_path):
    for file in files:
        if file.endswith(".java") and "service" in root:
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            original_content = content
            
            # Fix DTO imports
            for dto_name, correct_import in import_mappings[r"import com\.sysintegg7\.potane\.whisperwall\.dto\.(\w+);"]. items():
                old_import = f"import com.sysintegg7.potane.whisperwall.dto.{dto_name};"
                content = content.replace(old_import, correct_import)
            
            # Fix repository imports
            for repo_name, correct_import in import_mappings[r"import com\.sysintegg7\.potane\.whisperwall\.repository\.(\w+);"]. items():
                old_import = f"import com.sysintegg7.potane.whisperwall.repository.{repo_name};"
                content = content.replace(old_import, correct_import)
            
            # Write back if changed
            if content != original_content:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(content)
                print(f"Fixed imports in {filepath}")

print("Done!")
