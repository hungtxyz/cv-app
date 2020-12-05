from flask import Flask, request

app = Flask(__name__)

@app.route('/')
def hello_world():
    return 'hung dep trai'


@app.route('/sendstr', methods=['GET','POST'])
def response():
    # data = request.data
    return "connected"
