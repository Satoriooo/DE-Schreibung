import os
import json
import traceback
import google.generativeai as genai
from flask import Flask, request, jsonify
from dotenv import load_dotenv

print("--- SERVER STARTING WITH FULL DEBUG LOGGING ---")

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

# --- Prompt Templates (Unchanged) ---
EVALUATION_PROMPT_TEMPLATE = """...""" # Abbreviated for clarity
EXAMPLES_PROMPT_TEMPLATE = """...""" # Abbreviated for clarity


@app.route('/evaluate', methods=['POST'])
def evaluate_text_endpoint():
    try:
        request_data = request.get_json()
        user_text = request_data.get('text')
        print(f"Received text for evaluation: {user_text[:80]}...")
        
        full_prompt = EVALUATION_PROMPT_TEMPLATE.format(user_text=user_text)
        model = genai.GenerativeModel('gemini-1.5-flash')
        response = model.generate_content(full_prompt)

        # --- NEW: Print the entire response object for debugging ---
        print("--- Full Gemini Response Object (Evaluate) ---")
        print(response)
        print("---------------------------------------------")

        if not response.parts:
            # This handles cases where the response is blocked by safety filters
            print("Gemini response was blocked. Feedback:", response.prompt_feedback)
            return jsonify({"error": "The response was blocked by safety filters."}), 500

        cleaned_response_text = response.text.strip().replace('```json', '').replace('```', '').strip()
        response_json = json.loads(cleaned_response_text)
        response_json['originalText'] = user_text
        return jsonify(response_json), 200

    except Exception as e:
        print("--- An exception occurred during the /evaluate request ---")
        print(traceback.format_exc())
        return jsonify({"error": "Internal server error."}), 500


@app.route('/more_examples', methods=['POST'])
def more_examples_endpoint():
    try:
        request_data = request.get_json()
        german_word = request_data.get('word')
        print(f"Received request for examples for word: {german_word}")

        prompt = EXAMPLES_PROMPT_TEMPLATE.format(german_word=german_word)
        model = genai.GenerativeModel('gemini-1.5-flash')
        response = model.generate_content(prompt)
        
        # --- NEW: Print the entire response object for debugging ---
        print("--- Full Gemini Response Object (More Examples) ---")
        print(response)
        print("--------------------------------------------------")
        
        if not response.parts:
            print("Gemini response was blocked. Feedback:", response.prompt_feedback)
            return jsonify({"error": "The response was blocked by safety filters."}), 500

        cleaned_response_text = response.text.strip().replace('```json', '').replace('```', '').strip()
        response_json = json.loads(cleaned_response_text)
        return jsonify(response_json), 200
        
    except Exception as e:
        print("--- An exception occurred during the /more_examples request ---")
        print(traceback.format_exc())
        return jsonify({"error": "Internal server error."}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)