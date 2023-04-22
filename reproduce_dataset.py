import os
from keras.preprocessing.image import ImageDataGenerator, img_to_array, load_img, array_to_img

source_folder = 'D:/Projects/artifactRecognition/images/classes'
destination_folder = 'D:/Projects/artifactRecognition/images/augmented'

# Create destination folder if it doesn't exist
if not os.path.exists(destination_folder):
    os.makedirs(destination_folder)

# Create the ImageDataGenerator with the desired augmentations
datagen = ImageDataGenerator(rescale=1.0/255,
                             rotation_range=40,
                             width_shift_range=0.2,
                             height_shift_range=0.2,
                             shear_range=0.2,
                             zoom_range=0.2,
                             horizontal_flip=True,
                             fill_mode='nearest')

# Number of augmented images to be created per original image
augmentation_multiplier = 5

class_folders = sorted(os.listdir(source_folder))
total_class_folders = 10

for idx, class_folder in enumerate(class_folders):
    if idx >= total_class_folders:
        break
    
    class_folder_path = os.path.join(source_folder, class_folder)
    if os.path.isdir(class_folder_path):
        augmented_class_folder_path = os.path.join(destination_folder, class_folder)
        if not os.path.exists(augmented_class_folder_path):
            os.makedirs(augmented_class_folder_path)
        
        for image_file in os.listdir(class_folder_path):
            if image_file.endswith(".jpg") or image_file.endswith(".jpeg") or image_file.endswith(".png"):
                image_file_path = os.path.join(class_folder_path, image_file)
                image = load_img(image_file_path)
                image_array = img_to_array(image)
                image_array = image_array.reshape((1,) + image_array.shape)
                
                # Save the augmented images to the destination folder
                i = 0
                for batch in datagen.flow(image_array, batch_size=1):
                    augmented_image = array_to_img(batch[0])
                    save_path = os.path.join(augmented_class_folder_path, f"aug_{class_folder}_{i}_{image_file}")
                    augmented_image.save(save_path)
                    i += 1
                    if i >= augmentation_multiplier:
                        break
