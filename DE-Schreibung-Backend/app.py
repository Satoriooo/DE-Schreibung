import os
import json
import traceback
import google.generativeai as genai
from flask import Flask, request, jsonify
from dotenv import load_dotenv

print("--- SERVER STARTING WITH STRUCTURED FEEDBACK V1 ---")

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

# --- UPDATED PROMPT: Removed 'positiveRemarks', changed 'grammaticalExplanation' to 'improvementSuggestions' ---
EVALUATION_PROMPT_TEMPLATE = """
You are 'Herr Schmidt', an extremely strict and meticulous German language teacher (ein strenger Prüfer) evaluating a student's writing for the B2 CEFR level. Your feedback must be precise, professional, and direct.

The user has provided the following text:
\"\"\"
{user_text}
\"\"\"

You MUST provide your response as a single, valid JSON object without any markdown formatting. The JSON object must have the following structure:
{{
  "originalText": "The user's original, unmodified text.",
  "correctedText": "The fully corrected version of the user's text.",
  "feedbackComment": "A direct, professional summary of the overall quality, referencing the four criteria (Grammar, Vocabulary, Cohesion, Expressiveness).",
  "score": <The integer SUM of the four scores from `detailedScore`>,
  "detailedScore": {{
      "grammar": <Score for Grammar, 35 max>,
      "vocabulary": <Score for Vocabulary, 25 max>,
      "cohesion": <Score for Cohesion, 20 max>,
      "expressiveness": <Score for Expressiveness, 20 max>
  }},
  "improvementSuggestions": [
    {{
      "category": "<'Stil' or 'Kohärenz'>",
      "originalSentence": "<The user's original sentence or phrase with the issue>",
      "suggestion": "<A concise explanation of why it can be improved (e.g., 'This connector is repetitive.' or 'This expression is too informal.')>",
      "rewrittenSentence": "<A better version of the sentence demonstrating the improvement>"
    }}
  ],
  "vocabularyList": [ {{ "germanWord": "<...>", "englishTranslation": "<...>", "exampleSentence": "<...>" }} ]
}}

**RULES FOR `improvementSuggestions`:**
- Provide 1 or 2 specific suggestions.
- The `category` must be either "Stil" (for style, word choice, and expressiveness) or "Kohärenz" (for structure, flow, and logical connectors).
- The `suggestion` should clearly explain the weakness.
- The `rewrittenSentence` provides a concrete example of what to write instead.
- If the text is nearly perfect, you may return an empty array: [].

**(The SCORING RUBRIC and other rules remain the same as before)**
"""

EXAMPLES_PROMPT_TEMPLATE = """
You are a helpful language assistant. A user wants to see more example sentences for a German word.
The word is: "{german_word}"

Please provide 3-4 diverse and clear example sentences for this word at a B1-B2 level.
You MUST provide your response as a single, valid JSON object with the following structure. Do not include any text before or after the JSON.
{{
  "examples": [
    {{ "german": "<Example sentence in German>", "english": "<The English translation>" }},
    {{ "german": "<Another German sentence>", "english": "<Its English translation>" }}
  ]
}}
"""

@app.route('/')
def health_check():
    return jsonify({"status": "ok", "message": "Server is healthy."}), 200

@app.route('/evaluate', methods=['POST'])
def evaluate_text_endpoint():
    try:
        request_data = request.get_json()
        user_text = request_data.get('text')
        print(f"Received text for evaluation: {user_text[:80]}...")
        full_prompt = EVALUATION_PROMPT_TEMPLATE.format(user_text=user_text)
        model = genai.GenerativeModel('gemini-1.5-flash')
        response = model.generate_content(full_prompt)
        print("--- Full Gemini Response Object (Evaluate) ---")
        print(response)
        if not response.parts:
            return jsonify({"error": "Response blocked by safety filters."}), 500
        cleaned_response_text = response.text.strip().replace('```json', '').replace('```', '').strip()
        response_json = json.loads(cleaned_response_text)
        response_json['originalText'] = user_text
        return jsonify(response_json), 200
    except Exception as e:
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
        print("--- Full Gemini Response Object (More Examples) ---")
        print(response)
        if not response.parts:
            return jsonify({"error": "Response blocked by safety filters."}), 500
        cleaned_response_text = response.text.strip().replace('```json', '').replace('```', '').strip()
        response_json = json.loads(cleaned_response_text)
        return jsonify(response_json), 200
    except Exception as e:
        print(traceback.format_exc())
        return jsonify({"error": "Internal server error."}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)