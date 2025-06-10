import os
import json
import traceback
import google.generativeai as genai
from flask import Flask, request, jsonify
from dotenv import load_dotenv

print("--- SERVER STARTING ---")

# --- Setup ---
load_dotenv()
app = Flask(__name__)

try:
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        raise ValueError("Error: GEMINI_API_KEY not found...")
    genai.configure(api_key=api_key)
    print("Gemini API configured successfully.")
except Exception as e:
    print(f"CRITICAL STARTUP ERROR: {e}")

# --- Prompt for Text Evaluation (No changes here) ---
EVALUATION_PROMPT_TEMPLATE = """
You are a strict but fair German language teacher...
{{
  ...
}}
...
""" # This prompt is long, so I'm omitting it for brevity. It has not been changed.

# --- NEW: Prompt for Getting More Example Sentences ---
EXAMPLES_PROMPT_TEMPLATE = """
You are a German language teacher. A student wants more example sentences for a specific German word to understand its usage better.
The student has requested examples for the word: "{german_word}"

Provide three diverse and useful example sentences for this word. The sentences should be appropriate for a B2 CEFR level.
The sentences must be returned as a single, valid JSON object. Do not include any text or markdown formatting before or after the JSON object.
The JSON object must have a single key, "examples", which contains a list of the three strings.

Example format:
{{
  "examples": [
    "Das ist der erste Beispielsatz.",
    "Hier ist ein zweiter, anderer Satz.",
    "Und ein dritter Satz, der eine andere Nuance zeigt."
  ]
}}
"""


@app.route('/evaluate', methods=['POST'])
def evaluate_text_endpoint():
    # This entire function has not been changed.
    try:
        if not request.is_json:
            return jsonify({"error": "Invalid request..."}), 400
        request_data = request.get_json()
        user_text = request_data.get('text')
        if not user_text:
            return jsonify({"error": "Invalid request..."}), 400
        
        print(f"Received text for evaluation: {user_text[:80]}...")
        
        full_prompt = EVALUATION_PROMPT_TEMPLATE.format(user_text=user_text)
        model = genai.GenerativeModel('gemini-1.5-flash')
        
        response = model.generate_content(full_prompt)

        if not response.parts:
            print("--- Gemini Response Blocked ---")
            print(response.prompt_feedback)
            return jsonify({"error": "Response blocked by safety filters."}), 500

        cleaned_response_text = response.text.strip().replace('```json', '').replace('```', '').strip()
        response_json = json.loads(cleaned_response_text)
        response_json['originalText'] = user_text
        return jsonify(response_json), 200

    except Exception as e:
        print("--- An exception occurred during the request ---")
        print(traceback.format_exc())
        return jsonify({"error": "Internal server error."}), 500


# --- NEW: Endpoint for More Examples ---
@app.route('/more_examples', methods=['POST'])
def more_examples_endpoint():
    try:
        if not request.is_json:
            return jsonify({"error": "Invalid request..."}), 400
        request_data = request.get_json()
        german_word = request_data.get('word')
        if not german_word:
            return jsonify({"error": "Invalid request, 'word' is missing."}), 400

        print(f"Received request for examples for word: {german_word}")

        prompt = EXAMPLES_PROMPT_TEMPLATE.format(german_word=german_word)
        model = genai.GenerativeModel('gemini-1.5-flash')
        response = model.generate_content(prompt)
        
        if not response.parts:
            return jsonify({"error": "Response blocked by safety filters."}), 500

        cleaned_response_text = response.text.strip().replace('```json', '').replace('```', '').strip()
        response_json = json.loads(cleaned_response_text)
        
        return jsonify(response_json), 200
        
    except Exception as e:
        print("--- An exception occurred during the more_examples request ---")
        print(traceback.format_exc())
        return jsonify({"error": "Internal server error."}), 500


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)