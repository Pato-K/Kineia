import cv2
import mediapipe as mp
import numpy as np
#from tensorflow.keras.models import load_model
import tensorflow as tf



import pyttsx3
import time
from collections import deque

# --- Configuración ---
SEQ_LENGTH = 30  # tamaño de la secuencia para el modelo
model_path = "modelo.keras"
labels_path = "labels.npy"

# --- Cargar modelo y etiquetas ---
model = tf.keras.models.load_model(model_path)
#model = load_model(model_path)
labels = np.load(labels_path, allow_pickle=True)


# Inicializar MediaPipe Hands
mp_hands = mp.solutions.hands
hands = mp_hands.Hands(static_image_mode=False, max_num_hands=1)
mp_draw = mp.solutions.drawing_utils

# Inicializar síntesis de voz
engine = pyttsx3.init()

# Cola para guardar landmarks de frames recientes
seq_buffer = deque(maxlen=SEQ_LENGTH)

# Función para sintetizar texto (voz)
def speak(text):
    engine.say(text)
    engine.runAndWait()

# Abrir webcam
cap = cv2.VideoCapture(0)
#cap.set(cv2.CAP_PROP_FOURCC, cv2.VideoWriter_fourcc(*'MJPG'))


if not cap.isOpened():
    print("❌ Webcam could not be opened!")
    exit()

last_prediction = ""
last_spoken_time = 0

while True:
    ret, frame = cap.read()
    if not ret:
        break
    if not ret or frame is None:
        print("⚠️ Frame not captured!")
        continue

    image = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
    results = hands.process(image)

    frame_landmarks = []

    if results.multi_hand_landmarks:
        for hand_landmarks in results.multi_hand_landmarks:
            # Dibujar landmarks en el frame
            mp_draw.draw_landmarks(frame, hand_landmarks, mp_hands.HAND_CONNECTIONS)

            # Extraer coordenadas normalizadas
            for lm in hand_landmarks.landmark:
                frame_landmarks.extend([lm.x, lm.y, lm.z])

    else:
        # Si no detecta mano, agregar ceros para no romper la secuencia
        frame_landmarks = [0]*63  # 21 puntos * 3 coordenadas

    # Guardar frame_landmarks en buffer
    seq_buffer.append(frame_landmarks)

    # Cuando haya suficientes frames para una secuencia, predecir
    if len(seq_buffer) == SEQ_LENGTH:
        input_data = np.array(seq_buffer)
        input_data = np.expand_dims(input_data, axis=0)  # forma (1, 30, 63)

        prediction = model.predict(input_data)
        class_id = np.argmax(prediction)
        confidence = prediction[0][class_id]

        if confidence > 0.8:  # umbral de confianza para evitar ruido
            predicted_text = labels[class_id]
            cv2.putText(frame, f"{predicted_text} ({confidence:.2f})", (10, 40),
                        cv2.FONT_HERSHEY_SIMPLEX, 1.2, (0,255,0), 3)

            # Solo hablar si la predicción cambió o pasó un tiempo (2 seg)
            now = time.time()
            if predicted_text != last_prediction or (now - last_spoken_time) > 2:
                speak(predicted_text)
                last_spoken_time = now
                last_prediction = predicted_text
        else:
            cv2.putText(frame, "Detectando...", (10, 40),
                        cv2.FONT_HERSHEY_SIMPLEX, 1.2, (0,255,255), 2)

    else:
        cv2.putText(frame, "Esperando secuencia...", (10, 40),
                    cv2.FONT_HERSHEY_SIMPLEX, 1.2, (0,255,255), 2)

    # Mostrar frame
    cv2.imshow("Reconocimiento LSE", frame)

    if cv2.waitKey(1) & 0xFF == 27:  # tecla ESC para salir
        break

cap.release()
cv2.destroyAllWindows()
