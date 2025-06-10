from flask import Flask

# Create the Flask app instance
app = Flask(__name__)

# A simple "hello world" endpoint at the root URL
@app.route("/")
def hello():
    return "Hello, Render! The test server is running."

# A test endpoint to make sure routing works
@app.route("/test")
def test_route():
    return "The /test route is working."

# Note: The if __name__ == '__main__' block is not needed for Gunicorn