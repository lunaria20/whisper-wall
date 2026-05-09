from PIL import Image
import os

# Source image
src = r"C:\Users\Kate Rielle\Downloads\whisperwall-logo.png"
basedir = r"d:\User\Projects\whisper-wall\mobile\app\src\main\res"

# Android densities: (folder_name, scale_factor, size)
densities = [
    ("mipmap-mdpi", 1.0, 192),
    ("mipmap-hdpi", 1.5, 192),
    ("mipmap-xhdpi", 2.0, 192),
    ("mipmap-xxhdpi", 3.0, 192),
    ("mipmap-xxxhdpi", 4.0, 192),
]

# Open source image
img = Image.open(src).convert("RGBA")

# Generate scaled versions
for folder, scale, base_size in densities:
    new_size = int(base_size * scale)
    scaled = img.resize((new_size, new_size), Image.Resampling.LANCZOS)
    
    # Save as ic_launcher.webp and ic_launcher_round.webp
    for fname in ["ic_launcher.webp", "ic_launcher_round.webp"]:
        path = os.path.join(basedir, folder, fname)
        scaled.save(path, "WEBP", quality=95)
        print(f"Created {folder}/{fname} ({new_size}x{new_size})")

print("Done!")
