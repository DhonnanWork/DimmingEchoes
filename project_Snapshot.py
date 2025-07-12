import os

# --- Configuration ---
# The name of the output file.
output_filename = 'project_snapshot.txt'
# A list of files and directories to ignore.
# We ignore the script itself, its output, and common large/binary directories.
ignore_list = [
    output_filename,
    os.path.basename(__file__),  # The script's own name
    '.git',
    '.gradle',
    'build',
    '__pycache__',
    '.idea'
]
# List of common non-text file extensions to handle gracefully.
# You can extend this list with more extensions if needed.
NON_TEXT_EXTENSIONS = {
    # Images
    '.png', '.jpg', '.jpeg', '.gif', '.bmp', '.tiff', '.webp',
    # Unity specific (kept for general utility)
    '.unity', '.asset', '.mat', '.prefab', '.shader', '.anim', '.controller',
    # Audio
    '.wav', '.mp3', '.ogg', '.flac',
    # Fonts
    '.ttf', '.otf', '.woff', '.woff2',
    # 3D Models
    '.fbx', '.obj', '.blend', '.dae',
    # Binaries/Executables
    '.dll', '.exe', '.so', '.dylib', '.jar', '.class',
    # Archives
    '.unitypackage', '.zip', '.rar', '.7z', '.tar', '.gz',
    # Documents
    '.pdf', '.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx',
    # Design files
    '.psd', '.ai', '.eps', '.ico', '.icns',
    # Videos
    '.mp4', '.mov', '.avi', '.mkv',
    # Data files that might be large or have complex structures
    '.json', '.xml', '.yaml', '.yml', '.atlas',
    # Gradle and IDE specific
    '.bin', '.lock', '.iml',
    # Map files
    '.tiled-project', '.tiled-session'
}

# --- End Configuration ---

def is_text_file(filepath):
    """
    Checks if a file is likely a text file based on its extension.
    Returns True if it's likely text, False otherwise.
    """
    _, ext = os.path.splitext(filepath)
    # Treat files with no extension as potentially text
    if not ext:
        return True
    return ext.lower() not in NON_TEXT_EXTENSIONS

def create_project_snapshot():
    """
    Walks through the current directory and its subdirectories,
    reads the content of each file, and writes it to a single output file.
    Handles non-text files more gracefully.
    """
    # Open the output file for writing with UTF-8 encoding.
    with open(output_filename, 'w', encoding='utf-8', errors='replace') as outfile:
        # Get the starting directory path.
        start_dir = '.'
        outfile.write(f"--- Project Snapshot of directory: {os.path.abspath(start_dir)} ---\n\n")

        # os.walk() is perfect for this. It goes through every directory and file.
        for dirpath, dirnames, filenames in os.walk(start_dir, topdown=True):

            # --- Filtering Logic ---
            # We want to skip ignored directories entirely.
            # We must modify dirnames in place to prevent os.walk from entering them.
            dirnames[:] = [d for d in dirnames if d not in ignore_list]

            # --- File Processing ---
            for filename in filenames:
                # Skip any explicitly ignored files.
                if filename in ignore_list:
                    continue

                # Get the full path to the file.
                file_path = os.path.join(dirpath, filename)

                # Write a clear header for each file.
                header = f"--- File: {file_path} ---\n"
                print(f"Processing: {file_path}")  # Log progress to the console.
                outfile.write(header)

                if is_text_file(file_path):
                    try:
                        # Open and read the content of the current file.
                        with open(file_path, 'r', encoding='utf-8', errors='ignore') as infile:
                            content = infile.read()
                            outfile.write(content)
                    except Exception as e:
                        # If reading fails (e.g., permission errors, encoding issues not caught by 'ignore')
                        error_message = f"*** Could not read file content. Reason: {e} ***\n"
                        outfile.write(error_message)
                else:
                    # For non-text files, write a placeholder message.
                    file_ext = os.path.splitext(file_path)[1]
                    outfile.write(f"*** Non-text file ({file_ext} extension). Content not displayed. ***\n")

                # Add spacing between files for better readability.
                outfile.write("\n" + "=" * 80 + "\n\n")

    print(f"\n✅ Success! All code and file summaries have been written to '{output_filename}'")


if __name__ == "__main__":
    create_project_snapshot()