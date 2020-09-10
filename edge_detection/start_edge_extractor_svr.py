
import os
from flask import Flask, render_template, jsonify, request, send_file, send_from_directory, Response
from werkzeug.utils import secure_filename
from flask import Flask
from flask_restful import Api
import base64


app = Flask(__name__)

@app.route('/')
def hello_world():
    return render_template('index.html')

#이미지 업로드에 사용되는
@app.route('/uploadImage', methods = ['POST'])
def uploadImage():

    print("1111111111111")

    f = request.files['file']
    #filename = secure_filename(f.filename)
    f.save(f.filename)


    print("file size : {}".format(os.path.getsize(f.filename)))

    with open(f.filename, "rb") as image_file:
        encoded_string = base64.b64encode(image_file.read()).decode('utf8')

    #os.remove(filename)
    #result_dict = dict()
    #result_dict['result_code'] = 200
    #result_dict['message'] = "Hello"
    #resp = jsonify(result_dict)

    #return send_file(file_object, mimetype='image/PNG')
    #return send_file(f.filename, as_attachment=True)
    return encoded_string

if __name__ == '__main__':
        app.run(host='0.0.0.0',port=20000, debug=True)
