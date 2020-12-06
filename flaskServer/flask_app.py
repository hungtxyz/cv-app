import os
from flask import Flask, request, flash, send_file
from werkzeug.utils import secure_filename
import cv2
from seam_carving import object_removal
import numpy as np
app = Flask(__name__)

@app.route('/')
def hello_world():
    return 'hung dep trai'


@app.route('/sendstr', methods=['GET','POST'])
def response():
    data = request.data
    return data





def preMask():
    img = cv2.imread('./mask.jpg', 0)
    img = np.array(img)
    img = np.where(img>50,255,0)
    for i in img:
        a = list(i)
        if 255 in a:
            l = a.index(255)
            r = len(a) - 1 - a[::-1].index(255)
            for j in range(l,r+1):
                i[j] = 255
    cv2.imwrite('mask.jpg', img)


@app.route('/sendimg', methods=['GET', 'POST'])
def upload_file():
    image_file = request.files['mask']
    file_name = secure_filename(image_file.filename)
    print(image_file.filename)
    image_file.save(file_name)

    image_file = request.files['origin']
    file_name = secure_filename(image_file.filename)
    print(image_file.filename)
    image_file.save(file_name)
    preMask()

    img = cv2.imread("./origin.jpg")
    mask = cv2.imread("./mask.jpg",0)
    result = object_removal(img,mask,vis=True, horizontal_removal=True)
    cv2.imwrite('result.jpg', result)
    return "success"    

@app.route('/request4img', methods=['GET', 'POST'])
def return_image():
    return send_file("./test.jpg", mimetype='image/jpg')










