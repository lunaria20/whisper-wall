import os
import shutil
from pathlib import Path

# File mapping: old_path -> new_path
file_mapping = {
    'App.js': 'app/App.js',
    'App.css': 'app/App.css',
    'index.js': 'app/index.js',
    'index.css': 'app/index.css',
    'Profile.js': 'features/profile/pages/ProfilePage.js',
    'Profile.css': 'features/profile/styles/Profile.css',
    'Moderator.js': 'features/moderation/pages/ModerationDashboard.js',
    'Moderator.css': 'features/moderation/styles/Moderation.css',
    'Admin.js': 'features/admin/pages/AdminDashboard.js',
    'Admin.css': 'features/admin/styles/Admin.css',
    'imageUtils.js': 'shared/utils/imageUtils.js',
    'setupTests.js': 'app/setupTests.js',
    'reportWebVitals.js': 'app/reportWebVitals.js',
}

src_dir = Path('d:/User/Projects/whisper-wall/front-end/src')
target_base = Path('d:/User/Projects/whisper-wall/front-end/src')

def update_react_imports(content, old_filename):
    """Update import paths in React files"""
    replacements = {
        "from './Admin'": "from '../admin/pages/AdminDashboard'",
        "from './Moderator'": "from '../moderation/pages/ModerationDashboard'",
        "from './Profile'": "from '../profile/pages/ProfilePage'",
        "from './imageUtils'": "from '../../shared/utils/imageUtils'",
        "from './App'": "from '../../app/App'",
        "import './App.css'": "import '../../app/App.css'",
        "import './index.css'": "import '../../app/index.css'",
        "import './Profile.css'": "import '../styles/Profile.css'",
        "import './Moderator.css'": "import '../styles/Moderation.css'",
        "import './Admin.css'": "import '../styles/Admin.css'",
        "import reportWebVitals from './reportWebVitals'": "import reportWebVitals from '../../app/reportWebVitals'",
    }
    
    for old, new in replacements.items():
        content = content.replace(old, new)
    
    return content

def copy_and_update_file(old_relative_path, new_relative_path, is_react=True):
    """Copy file to new location and update imports if React"""
    old_path = src_dir / old_relative_path
    new_path = target_base / new_relative_path
    
    if not old_path.exists():
        print(f"✗ File not found: {old_path}")
        return False
    
    try:
        with open(old_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Update imports for React files
        if is_react:
            content = update_react_imports(content, old_relative_path)
        
        # Create directories
        new_path.parent.mkdir(parents=True, exist_ok=True)
        
        # Write to new location
        with open(new_path, 'w', encoding='utf-8') as f:
            f.write(content)
        
        print(f"✓ Migrated: {old_relative_path} -> {new_relative_path}")
        return True
    except Exception as e:
        print(f"✗ Error processing {old_relative_path}: {e}")
        return False

# Process all files
print("Starting frontend migration...")
success_count = 0
for old_path, new_path in file_mapping.items():
    if copy_and_update_file(old_path, new_path):
        success_count += 1

print(f"\n✓ Migration complete: {success_count}/{len(file_mapping)} files processed")
