from flask import Flask, request, jsonify
import base64
from io import BytesIO
from PIL import Image
import numpy as np
import tensorflow as tf
from keras.preprocessing.image import ImageDataGenerator



app = Flask(__name__)

# Load the model
model = tf.keras.models.load_model('artifact_recognition_model.h5')

IMG_WIDTH = 150
IMG_HEIGHT = 150
BATCH_SIZE=16

train_dataset_path = 'D:/Projects/artifactRecognition/splitted_images/train'

train_datagen = ImageDataGenerator(rescale=1.0/255)
train_generator = train_datagen.flow_from_directory(train_dataset_path, 
                                                    target_size=(IMG_WIDTH, IMG_HEIGHT), 
                                                    batch_size=BATCH_SIZE, 
                                                    class_mode='categorical')

labels = {value: key for key, value in train_generator.class_indices.items()}

@app.route('/predict', methods=['POST'])
def predict():

    if 'image' not in request.files:
        return jsonify({'error': 'image key is missing'}), 400

    # Get the image data from the request
    image_file = request.files['image']

    # Load the image and preprocess it
    img = Image.open(image_file).resize((IMG_WIDTH, IMG_HEIGHT))
    img_array = np.array(img) / 255.0
    img_array = img_array.reshape(1, IMG_WIDTH, IMG_HEIGHT, 3)

    # Make the prediction
    prediction = model.predict(img_array)
    predicted_class = np.argmax(prediction, axis=1)

    # Return the result
    return jsonify({'result': labels[predicted_class[0]]})

if __name__ == '__main__':
    app.run(debug=True)
