import os

# Base directory
base_path = r"d:\User\Projects\whisper-wall\back-end\potane\src\main\java\com\sysintegg7\potane\whisperwall"

# Additional mapping for service imports that were missed
service_mapping = {
    "import com.sysintegg7.potane.whisperwall.service.AuthService;": "import com.sysintegg7.potane.whisperwall.features.auth.service.AuthService;",
    "import com.sysintegg7.potane.whisperwall.service.UserService;": "import com.sysintegg7.potane.whisperwall.features.users.service.UserService;",
    "import com.sysintegg7.potane.whisperwall.service.ConfessionService;": "import com.sysintegg7.potane.whisperwall.features.confessions.service.ConfessionService;",
    "import com.sysintegg7.potane.whisperwall.service.CommentService;": "import com.sysintegg7.potane.whisperwall.features.comments.service.CommentService;",
    "import com.sysintegg7.potane.whisperwall.service.ReactionService;": "import com.sysintegg7.potane.whisperwall.features.reactions.service.ReactionService;",
    "import com.sysintegg7.potane.whisperwall.service.ReportService;": "import com.sysintegg7.potane.whisperwall.features.reports.service.ReportService;",
    "import com.sysintegg7.potane.whisperwall.service.ModeratorService;": "import com.sysintegg7.potane.whisperwall.features.moderation.service.ModeratorService;",
    "import com.sysintegg7.potane.whisperwall.service.AdminService;": "import com.sysintegg7.potane.whisperwall.features.admin.service.AdminService;",
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
                
                # Apply service import fixes
                for old_import, new_import in service_mapping.items():
                    content = content.replace(old_import, new_import)
                
                # Write back if changed
                if content != original_content:
                    with open(filepath, 'w', encoding='utf-8') as f:
                        f.write(content)
                    fixed_count += 1
                    print(f"Fixed service imports: {filepath}")
            except Exception as e:
                print(f"Error: {filepath}: {e}")

print(f"\nTotal files fixed: {fixed_count}")
