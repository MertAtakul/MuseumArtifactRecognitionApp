import os
from keras.preprocessing.image import ImageDataGenerator, load_img, img_to_array
import shutil

# girdi klasörü
input_dir = 'C:/Users/pc/Desktop/Projects/artifactRecognition/images/classes'

# çıktı klasörü
output_dir = 'C:/Users/pc/Desktop/Projects/artifactRecognition/images/augmented'

# classes klasöründe bulunan alt klasörlerin isimlerini bir listeye kaydedelim
class_names = os.listdir(input_dir)

# ImageDataGenerator nesnesi oluşturalım
datagen = ImageDataGenerator(rotation_range=45,
                             width_shift_range=0.2,
                             height_shift_range=0.2,
                             shear_range=0.2,
                             zoom_range=0.2,
                             horizontal_flip=True,
                             fill_mode='nearest')

# Her sınıf için ayrı ayrı augmentation uygulayalım
for class_name in class_names:
    # Çıktı klasörü
    class_output_dir = os.path.join(output_dir, class_name)
    if not os.path.exists(class_output_dir):
        os.makedirs(class_output_dir)

    # ImageDataGenerator nesnesi kullanarak augmentation uygulayalım
    class_input_dir = os.path.join(input_dir, class_name)
    for file_name in os.listdir(class_input_dir):
        file_path = os.path.join(class_input_dir, file_name)
        img = load_img(file_path)
        x = img_to_array(img)
        x = x.reshape((1,) + x.shape)
        i = 0
        for batch in datagen.flow(x, batch_size=16, save_to_dir=class_output_dir, save_prefix='aug', save_format='.jpg'):
            i += 1
            if i > 4: # her bir resimden 5 tane augment ediyoruz
                break
            
    # augment edilen resimleri ilgili sınıf klasörüne kopyalayalım
    for file_name in os.listdir(class_output_dir):
        file_path = os.path.join(class_output_dir, file_name)
        shutil.copy(file_path, class_input_dir)
