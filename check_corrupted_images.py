import PIL
from pathlib import Path
from PIL import UnidentifiedImageError
from PIL import Image


path = Path("C:/Users/pc/Desktop/Projects/artifactRecognition/images").rglob("*.webp")
for img_p in path:
    try:
        img = PIL.Image.open(img_p)
    except PIL.UnidentifiedImageError:
        print(img_p)