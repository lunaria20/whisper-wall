import os
import re
import shutil
from pathlib import Path

# File mapping: old_path -> (new_path, feature)
file_mapping = {
    # Auth Feature
    'controller/AuthController.java': ('features/auth/controller/AuthController.java', 'auth', 'com.sysintegg7.potane.whisperwall.features.auth.controller'),
    'service/AuthService.java': ('features/auth/service/AuthService.java', 'auth', 'com.sysintegg7.potane.whisperwall.features.auth.service'),
    'dto/AuthRequest.java': ('features/auth/dto/AuthRequest.java', 'auth', 'com.sysintegg7.potane.whisperwall.features.auth.dto'),
    'dto/AuthResponse.java': ('features/auth/dto/AuthResponse.java', 'auth', 'com.sysintegg7.potane.whisperwall.features.auth.dto'),
    'dto/RegisterRequest.java': ('features/auth/dto/RegisterRequest.java', 'auth', 'com.sysintegg7.potane.whisperwall.features.auth.dto'),
    'dto/ChangePasswordRequest.java': ('features/auth/dto/ChangePasswordRequest.java', 'auth', 'com.sysintegg7.potane.whisperwall.features.auth.dto'),
    
    # Users Feature
    'controller/UserController.java': ('features/users/controller/UserController.java', 'users', 'com.sysintegg7.potane.whisperwall.features.users.controller'),
    'service/UserService.java': ('features/users/service/UserService.java', 'users', 'com.sysintegg7.potane.whisperwall.features.users.service'),
    'dto/UserResponse.java': ('features/users/dto/UserResponse.java', 'users', 'com.sysintegg7.potane.whisperwall.features.users.dto'),
    'dto/UserProfileUpdateRequest.java': ('features/users/dto/UserProfileUpdateRequest.java', 'users', 'com.sysintegg7.potane.whisperwall.features.users.dto'),
    'dto/CreateUserRequest.java': ('features/users/dto/CreateUserRequest.java', 'users', 'com.sysintegg7.potane.whisperwall.features.users.dto'),
    'dto/UsageStatsResponse.java': ('features/users/dto/UsageStatsResponse.java', 'users', 'com.sysintegg7.potane.whisperwall.features.users.dto'),
    
    # Confessions Feature
    'controller/ConfessionController.java': ('features/confessions/controller/ConfessionController.java', 'confessions', 'com.sysintegg7.potane.whisperwall.features.confessions.controller'),
    'service/ConfessionService.java': ('features/confessions/service/ConfessionService.java', 'confessions', 'com.sysintegg7.potane.whisperwall.features.confessions.service'),
    'dto/ConfessionRequest.java': ('features/confessions/dto/ConfessionRequest.java', 'confessions', 'com.sysintegg7.potane.whisperwall.features.confessions.dto'),
    'dto/ConfessionResponse.java': ('features/confessions/dto/ConfessionResponse.java', 'confessions', 'com.sysintegg7.potane.whisperwall.features.confessions.dto'),
    'model/Confession.java': ('features/confessions/model/Confession.java', 'confessions', 'com.sysintegg7.potane.whisperwall.features.confessions.model'),
    'repository/ConfessionRepository.java': ('features/confessions/repository/ConfessionRepository.java', 'confessions', 'com.sysintegg7.potane.whisperwall.features.confessions.repository'),
    
    # Comments Feature
    'controller/CommentController.java': ('features/comments/controller/CommentController.java', 'comments', 'com.sysintegg7.potane.whisperwall.features.comments.controller'),
    'service/CommentService.java': ('features/comments/service/CommentService.java', 'comments', 'com.sysintegg7.potane.whisperwall.features.comments.service'),
    'dto/CommentRequest.java': ('features/comments/dto/CommentRequest.java', 'comments', 'com.sysintegg7.potane.whisperwall.features.comments.dto'),
    'dto/CommentResponse.java': ('features/comments/dto/CommentResponse.java', 'comments', 'com.sysintegg7.potane.whisperwall.features.comments.dto'),
    'model/Comment.java': ('features/comments/model/Comment.java', 'comments', 'com.sysintegg7.potane.whisperwall.features.comments.model'),
    'repository/CommentRepository.java': ('features/comments/repository/CommentRepository.java', 'comments', 'com.sysintegg7.potane.whisperwall.features.comments.repository'),
    
    # Reactions Feature
    'controller/ReactionController.java': ('features/reactions/controller/ReactionController.java', 'reactions', 'com.sysintegg7.potane.whisperwall.features.reactions.controller'),
    'service/ReactionService.java': ('features/reactions/service/ReactionService.java', 'reactions', 'com.sysintegg7.potane.whisperwall.features.reactions.service'),
    'dto/ReactionRequest.java': ('features/reactions/dto/ReactionRequest.java', 'reactions', 'com.sysintegg7.potane.whisperwall.features.reactions.dto'),
    'dto/ReactionResponse.java': ('features/reactions/dto/ReactionResponse.java', 'reactions', 'com.sysintegg7.potane.whisperwall.features.reactions.dto'),
    'model/Reaction.java': ('features/reactions/model/Reaction.java', 'reactions', 'com.sysintegg7.potane.whisperwall.features.reactions.model'),
    'repository/ReactionRepository.java': ('features/reactions/repository/ReactionRepository.java', 'reactions', 'com.sysintegg7.potane.whisperwall.features.reactions.repository'),
    
    # Reports Feature
    'controller/ReportController.java': ('features/reports/controller/ReportController.java', 'reports', 'com.sysintegg7.potane.whisperwall.features.reports.controller'),
    'service/ReportService.java': ('features/reports/service/ReportService.java', 'reports', 'com.sysintegg7.potane.whisperwall.features.reports.service'),
    'dto/ReportRequest.java': ('features/reports/dto/ReportRequest.java', 'reports', 'com.sysintegg7.potane.whisperwall.features.reports.dto'),
    'dto/ReportResponse.java': ('features/reports/dto/ReportResponse.java', 'reports', 'com.sysintegg7.potane.whisperwall.features.reports.dto'),
    'model/Report.java': ('features/reports/model/Report.java', 'reports', 'com.sysintegg7.potane.whisperwall.features.reports.model'),
    'repository/ReportRepository.java': ('features/reports/repository/ReportRepository.java', 'reports', 'com.sysintegg7.potane.whisperwall.features.reports.repository'),
    
    # Moderation Feature
    'controller/ModeratorController.java': ('features/moderation/controller/ModeratorController.java', 'moderation', 'com.sysintegg7.potane.whisperwall.features.moderation.controller'),
    'service/ModeratorService.java': ('features/moderation/service/ModeratorService.java', 'moderation', 'com.sysintegg7.potane.whisperwall.features.moderation.service'),
    
    # Admin Feature
    'controller/AdminController.java': ('features/admin/controller/AdminController.java', 'admin', 'com.sysintegg7.potane.whisperwall.features.admin.controller'),
    'service/AdminService.java': ('features/admin/service/AdminService.java', 'admin', 'com.sysintegg7.potane.whisperwall.features.admin.service'),
    
    # Restrictions Feature
    'dto/RestrictionRequestResponse.java': ('features/restrictions/dto/RestrictionRequestResponse.java', 'restrictions', 'com.sysintegg7.potane.whisperwall.features.restrictions.dto'),
    'dto/RestrictUserRequest.java': ('features/restrictions/dto/RestrictUserRequest.java', 'restrictions', 'com.sysintegg7.potane.whisperwall.features.restrictions.dto'),
    'dto/SendRestrictionRequestRequest.java': ('features/restrictions/dto/SendRestrictionRequestRequest.java', 'restrictions', 'com.sysintegg7.potane.whisperwall.features.restrictions.dto'),
    'model/RestrictionRequest.java': ('features/restrictions/model/RestrictionRequest.java', 'restrictions', 'com.sysintegg7.potane.whisperwall.features.restrictions.model'),
    'repository/RestrictionRequestRepository.java': ('features/restrictions/repository/RestrictionRequestRepository.java', 'restrictions', 'com.sysintegg7.potane.whisperwall.features.restrictions.repository'),
    
    # Shared - Config
    'config/SecurityConfig.java': ('shared/config/SecurityConfig.java', 'shared', 'com.sysintegg7.potane.whisperwall.shared.config'),
    'config/DataInitializer.java': ('shared/config/DataInitializer.java', 'shared', 'com.sysintegg7.potane.whisperwall.shared.config'),
    
    # Shared - Security
    'security/JwtTokenProvider.java': ('shared/security/JwtTokenProvider.java', 'shared', 'com.sysintegg7.potane.whisperwall.shared.security'),
    'security/JwtAuthenticationFilter.java': ('shared/security/JwtAuthenticationFilter.java', 'shared', 'com.sysintegg7.potane.whisperwall.shared.security'),
    'security/JwtAuthenticationEntryPoint.java': ('shared/security/JwtAuthenticationEntryPoint.java', 'shared', 'com.sysintegg7.potane.whisperwall.shared.security'),
    'security/JwtAccessDeniedHandler.java': ('shared/security/JwtAccessDeniedHandler.java', 'shared', 'com.sysintegg7.potane.whisperwall.shared.security'),
    'security/CustomUserDetailsService.java': ('shared/security/CustomUserDetailsService.java', 'shared', 'com.sysintegg7.potane.whisperwall.shared.security'),
    
    # Shared - Exception
    'exception/GlobalExceptionHandler.java': ('shared/exception/GlobalExceptionHandler.java', 'shared', 'com.sysintegg7.potane.whisperwall.shared.exception'),
    'exception/UnauthorizedException.java': ('shared/exception/UnauthorizedException.java', 'shared', 'com.sysintegg7.potane.whisperwall.shared.exception'),
    'exception/ResourceNotFoundException.java': ('shared/exception/ResourceNotFoundException.java', 'shared', 'com.sysintegg7.potane.whisperwall.shared.exception'),
    'exception/ResourceAlreadyExistsException.java': ('shared/exception/ResourceAlreadyExistsException.java', 'shared', 'com.sysintegg7.potane.whisperwall.shared.exception'),
    'exception/ErrorResponse.java': ('shared/exception/ErrorResponse.java', 'shared', 'com.sysintegg7.potane.whisperwall.shared.exception'),
    
    # Shared - Util
    'util/SecurityUtil.java': ('shared/util/SecurityUtil.java', 'shared', 'com.sysintegg7.potane.whisperwall.shared.util'),
    
    # Shared - Repository (core User/Role repos)
    'repository/UserRepository.java': ('shared/repository/UserRepository.java', 'shared', 'com.sysintegg7.potane.whisperwall.shared.repository'),
    'repository/RoleRepository.java': ('shared/repository/RoleRepository.java', 'shared', 'com.sysintegg7.potane.whisperwall.shared.repository'),
}

base_dir = Path('d:/User/Projects/whisper-wall/back-end/potane/src/main/java/com/sysintegg7/potane/whisperwall')
target_base_dir = Path('d:/User/Projects/whisper-wall/back-end/potane/src/main/java/com/sysintegg7/potane/whisperwall')

def update_package_declaration(content, new_package):
    """Update the package declaration in a Java file"""
    pattern = r'^package\s+[^;]+;'
    replacement = f'package {new_package};'
    return re.sub(pattern, replacement, content, flags=re.MULTILINE)

def update_imports(content):
    """Update import statements from old package structure to new"""
    # Update various import patterns
    replacements = [
        (r'import com\.sysintegg7\.potane\.whisperwall\.model\.User;', 'import com.sysintegg7.potane.whisperwall.shared.model.User;'),
        (r'import com\.sysintegg7\.potane\.whisperwall\.model\.Role;', 'import com.sysintegg7.potane.whisperwall.shared.model.Role;'),
        (r'import com\.sysintegg7\.potane\.whisperwall\.model\.Confession;', 'import com.sysintegg7.potane.whisperwall.features.confessions.model.Confession;'),
        (r'import com\.sysintegg7\.potane\.whisperwall\.model\.Comment;', 'import com.sysintegg7.potane.whisperwall.features.comments.model.Comment;'),
        (r'import com\.sysintegg7\.potane\.whisperwall\.model\.Reaction;', 'import com.sysintegg7.potane.whisperwall.features.reactions.model.Reaction;'),
        (r'import com\.sysintegg7\.potane\.whisperwall\.model\.Report;', 'import com.sysintegg7.potane.whisperwall.features.reports.model.Report;'),
        (r'import com\.sysintegg7\.potane\.whisperwall\.model\.RestrictionRequest;', 'import com.sysintegg7.potane.whisperwall.features.restrictions.model.RestrictionRequest;'),
        (r'import com\.sysintegg7\.potane\.whisperwall\.exception\.', 'import com.sysintegg7.potane.whisperwall.shared.exception.'),
        (r'import com\.sysintegg7\.potane\.whisperwall\.security\.', 'import com.sysintegg7.potane.whisperwall.shared.security.'),
        (r'import com\.sysintegg7\.potane\.whisperwall\.util\.', 'import com.sysintegg7.potane.whisperwall.shared.util.'),
        (r'import com\.sysintegg7\.potane\.whisperwall\.repository\.UserRepository;', 'import com.sysintegg7.potane.whisperwall.shared.repository.UserRepository;'),
        (r'import com\.sysintegg7\.potane\.whisperwall\.repository\.RoleRepository;', 'import com.sysintegg7.potane.whisperwall.shared.repository.RoleRepository;'),
    ]
    
    for pattern, replacement in replacements:
        content = re.sub(pattern, replacement, content)
    
    return content

def process_file(old_relative_path, new_relative_path, new_package):
    """Read, update, and write a file to its new location"""
    old_path = base_dir / old_relative_path
    new_path = target_base_dir / new_relative_path
    
    if not old_path.exists():
        print(f"File not found: {old_path}")
        return False
    
    try:
        with open(old_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Update package and imports
        content = update_package_declaration(content, new_package)
        content = update_imports(content)
        
        # Ensure directory exists
        new_path.parent.mkdir(parents=True, exist_ok=True)
        
        # Write to new location
        with open(new_path, 'w', encoding='utf-8') as f:
            f.write(content)
        
        print(f"✓ Processed: {old_relative_path} -> {new_relative_path}")
        return True
    except Exception as e:
        print(f"✗ Error processing {old_relative_path}: {e}")
        return False

# Process all files
print("Starting migration...")
success_count = 0
for old_path, (new_path, feature, package) in file_mapping.items():
    if process_file(old_path, new_path, package):
        success_count += 1

print(f"\n✓ Migration complete: {success_count}/{len(file_mapping)} files processed")
