import os
from PIL import Image

def check_and_remove_image(image_path):
    try:
        img = Image.open(image_path)
        img.verify()
        img.close()
    except (IOError, SyntaxError) as e:
        print(f"Bozuk resim: {image_path} - {e}")
        os.remove(image_path)

images_folder = "D:/Projects/artifactRecognition/splitted_images/train"

for root, _, files in os.walk(images_folder):
    for file in files:
        if file.lower().endswith(('.jpg', '.jpeg', '.png')):
            file_path = os.path.join(root, file)
            check_and_remove_image(file_path)
