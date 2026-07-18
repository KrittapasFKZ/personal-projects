import sys
from yt_dlp import YoutubeDL

url = sys.argv[1]
file_name = sys.argv[2]

ydl_opts = {
    'format': 'bestaudio/best',
    'outtmpl': f'sounds/youtube/{file_name}.%(ext)s',
    'postprocessors': [{
        'key': 'FFmpegExtractAudio',
        'preferredcodec': 'mp3',
        'preferredquality': '192',
    }],
}

with YoutubeDL(ydl_opts) as ydl:
    ydl.download([url])

print("Downloaded audio successfully.")