import os
import json
import traceback
import google.generativeai as genai
from flask import Flask, request, jsonify
from dotenv import load_dotenv

print("--- SERVER STARTING WITH NORMALIZED VOCABULARY PROMPT V5 ---")

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

# --- UPDATED: Prompt for Text Evaluation ---
EVALUATION_PROMPT_TEMPLATE = """
You are 'Herr Schmidt', an extremely strict and meticulous German language teacher (ein strenger Prüfer) evaluating a student's writing for the B2 CEFR level. Your feedback must be precise, professional, and direct. Do not give high scores easily.

The user has provided the following text:
\"\"\"
{user_text}
\"\"\"

You MUST provide your response as a single, valid JSON object. Do not include any text or markdown formatting before or after the JSON object. The JSON object must have the following structure:
{{
  "originalText": "The user's original, unmodified text.",
  "correctedText": "The fully corrected version of the user's text. Ensure it is grammatically perfect and stylistically appropriate for a B2 level.",
  "feedbackComment": "A direct, professional comment on the overall quality. Justify the score based on the rubric below. Mention its primary strengths and weaknesses.",
  "score": <An integer score from 0 to 100 based on the STRICT rubric below>,
  "grammaticalExplanation": "A detailed but clear explanation of the 2-3 most important grammatical or stylistic mistakes in the text. Explain WHY it was wrong and what the correct form is.",
  "vocabularyList": [ {{ "germanWord": "<...>", "englishTranslation": "<...>", "exampleSentence": "<...>" }} ]
}}

**SCORING RUBRIC (BE VERY STRICT):**
- **95-100 (Exzellent):** Near-native fluency. Uses complex sentence structures (e.g., Konjunktiv II, Passiv, complex relative clauses) perfectly. No grammatical errors. Vocabulary is sophisticated and precise. This score should be exceptionally rare.
- **85-94 (Gut):** Solid B2 level. Clear, well-structured text with a good range of vocabulary. Only minor, isolated errors in grammar (e.g., an incorrect adjective ending) that do not hinder comprehension at all.
- **70-84 (Befriedigend):** Borderline B2. The meaning is clear, but there are noticeable errors in grammar (e.g., several wrong case endings, incorrect prepositions, verb position errors) or unnatural phrasing. The student is understandable but clearly not consistently at a B2 level. **Most of the user's texts will likely fall in this range.**
- **50-69 (Ausreichend):** Below B2 level. Contains frequent grammatical errors that sometimes make the text difficult to understand. Vocabulary is limited and repetitive. Sentence structure is simple and often incorrect.
- **0-49 (Nicht ausreichend):** Many fundamental errors in basic grammar. The text is largely incomprehensible.

**IMPORTANT RULES for the `vocabularyList`:**
- **All words in the `germanWord` field MUST be in their dictionary/base form.** For verbs, this is the infinitive (e.g., 'gehen', not 'gegangen'). For nouns, this is the singular nominative case and MUST include the article (e.g., 'der Apfel', not 'Äpfel'). For adjectives, use the uninflected base form (e.g., 'gut', not 'guter').
- Only extract nouns, verbs, adjectives, or adverbs that are at a B2 level or higher.
- EXCLUDE simple, common words (e.g., personal pronouns like 'ich', 'du'; articles like 'der', 'die', 'das'; common prepositions like 'in', 'auf'; and A1/A2 verbs like 'sein', 'haben', 'gehen').
- If the user made no relevant vocabulary mistakes, return an empty array: [].
- Provide a maximum of 3 words for the list.
"""

# --- UPDATED: Prompt for More Examples with Translations ---
EXAMPLES_PROMPT_TEMPLATE = """
You are a German language teacher. A student wants more example sentences for a specific German word to understand its usage better.
The student has requested examples for the word: "{german_word}"

Provide three diverse and useful example sentences for this word, appropriate for a B2 CEFR level.
The sentences must be returned as a single, valid JSON object. For each German example, you MUST provide an accurate English translation. Do not include any text or markdown formatting before or after the JSON object.
The JSON object must have a single key, "examples", which contains a list of objects. Each object must have two keys: "german" and "english".

Example format:
{{
  "examples": [
    {{ "german": "Das ist der erste Beispielsatz.", "english": "This is the first sentence." }},
    {{ "german": "Hier ist ein zweiter, anderer Satz.", "english": "Here is a second, different sentence." }},
    {{ "german": "Und ein dritter Satz, der eine andere Nuance zeigt.", "english": "And a third sentence that shows another nuance." }}
  ]
}}
"""

@app.route('/evaluate', methods=['POST'])
def evaluate_text_endpoint():
    # This function's logic remains the same
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
    # This function's logic also remains the same
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