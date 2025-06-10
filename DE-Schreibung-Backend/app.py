import os
import json
import traceback  # <-- Import the traceback module
import google.generativeai as genai
from flask import Flask, request, jsonify
from dotenv import load_dotenv

# --- Setup (No changes here) ---
load_dotenv()
app = Flask(__name__)

try:
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        raise ValueError("Error: GEMINI_API_KEY not found. Please set it in your environment variables.")
    genai.configure(api_key=api_key)
    print("Gemini API configured successfully.")
except Exception as e:
    print(f"CRITICAL STARTUP ERROR: {e}")

# --- Prompt Template (No changes here) ---
GEMINI_PROMPT_TEMPLATE = """
You are a strict but fair German language teacher named 'Herr Schmidt'. Your student is studying for the B2 CEFR level. Your task is to evaluate the student's written text, providing feedback with the goal of helping them pass the B2 exam. Do not be overly friendly or encouraging; be direct, precise, and professional.

The user has provided the following text:
\"\"\"
{user_text}
\"\"\"

You MUST provide your response as a single, valid JSON object. Do not include any text or markdown formatting before or after the JSON object. The JSON object must have the following structure and content:

{
  "originalText": "The user's original, unmodified text.",
  "correctedText": "The fully corrected version of the user's text. Ensure it is grammatically perfect and stylistically appropriate for a B2 level.",
  "feedbackComment": "A direct, professional comment on the overall quality of the text. Mention its strengths and weaknesses in relation to the B2 level. Keep it concise.",
  "score": <An integer score from 0 to 100. Be a strict grader. A good B2 text would be 85-95. A perfect text is 100. A text with several basic errors should be below 70.>,
  "grammaticalExplanation": "A detailed but clear explanation of the 2-3 most important grammatical or stylistic mistakes in the text. Explain WHY it was wrong and what the correct form is.",
  "vocabularyList": [
    {
      "germanWord": "<The first key B2-level noun, verb, adjective, or adverb the user used incorrectly or could have used.>",
      "englishTranslation": "<The English translation of the German word.>",
      "exampleSentence": "<A simple, correct German sentence using this word.>"
    }
  ]
}

IMPORTANT RULES for the `vocabularyList`:
- Only extract nouns, verbs, adjectives, or adverbs that are at a B2 level or higher.
- EXCLUDE simple, common words (e.g., personal pronouns like 'ich', 'du'; articles like 'der', 'die', 'das'; common prepositions like 'in', 'auf'; and A1/A2 verbs like 'sein', 'haben', 'gehen').
- If the user made no relevant vocabulary mistakes, return an empty array: [].
- Provide a maximum of 3 words for the list.
"""

@app.route('/evaluate', methods=['POST'])
def evaluate_text_endpoint():
    # --- This entire 'try...except' block is new and more robust ---
    try:
        if not request.is_json:
            return jsonify({"error": "Invalid request: Content-Type must be application/json"}), 400
        request_data = request.get_json()
        user_text = request_data.get('text')
        if not user_text or not isinstance(user_text, str) or not user_text.strip():
            return jsonify({"error": "Invalid request: 'text' field is missing or empty"}), 400
        
        print(f"Received text for evaluation: {user_text[:80]}...")
        
        full_prompt = GEMINI_PROMPT_TEMPLATE.format(user_text=user_text)
        model = genai.GenerativeModel('gemini-1.5-flash')
        
        # This is the call we are debugging
        response = model.generate_content(full_prompt)

        # Check if the response was blocked by safety filters
        if not response.parts:
            print("--- Gemini Response Blocked ---")
            print(response.prompt_feedback)
            return jsonify({"error": "The response was blocked by Google's safety filters."}), 500

        # Proceed with parsing if not blocked
        cleaned_response_text = response.text.strip().replace('```json', '').replace('```', '').strip()
        response_json = json.loads(cleaned_response_text)
        response_json['originalText'] = user_text
        return jsonify(response_json), 200

    except Exception as e:
        # This is the most important part: it will print the full, detailed error.
        print("--- An exception occurred during the request ---")
        print(traceback.format_exc())
        print("----------------------------------------------------")
        return jsonify({"error": "An internal server error occurred. See server logs for details."}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)