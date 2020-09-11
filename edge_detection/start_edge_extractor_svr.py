
import os
from flask import Flask, render_template, jsonify, request, send_file, send_from_directory, Response
from werkzeug.utils import secure_filename
from flask import Flask
from flask_restful import Api
import base64
from edge_detection import edge_detection
import cv2

app = Flask(__name__)

@app.route('/')
def hello_world():
    return render_template('index.html')

#이미지 업로드에 사용되는
@app.route('/uploadImage', methods = ['POST'])
def uploadImage():

    #파일 저장 이후,
    f = request.files['file']
    f.save(f.filename)


    processed_filename = "edge_{}.jpg".format(f.filename)
    #엣지 디텍션 한다.
    print("이미지 읽는중..")
    color_img = cv2.imread(f.filename, cv2.IMREAD_COLOR) # 이미지 읽기
    print("엣지 디텍션중..")
    edge = edge_detection(color_img) # 엣지 검출
    print("엣지 디텍션된 이미지 쓰는중중..")
    cv2.imwrite(processed_filename, edge) # 검출된 엣지 이미지 저장


    return send_file(processed_filename, as_attachment=True)


if __name__ == '__main__':
        app.run(host='0.0.0.0',port=20000, debug=True)
