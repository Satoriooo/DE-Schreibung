import os
import json
import google.generativeai as genai
from flask import Flask, request, jsonify
from dotenv import load_dotenv

# --- Initial Setup ---

# Load environment variables from the .env file
load_dotenv()

# Initialize the Flask application
app = Flask(__name__)

# --- Configure Gemini API ---
try:
    # Retrieve the API key from environment variables
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        raise ValueError("Error: GEMINI_API_KEY not found. Please set it in your .env file.")
    
    # Configure the generativeai library with the API key
    genai.configure(api_key=api_key)
    print("Gemini API configured successfully.")

except Exception as e:
    # Print any configuration errors to the console
    print(e)


# --- AI Prompt Template ---
# This is the detailed instruction set for the "Strict German Teacher" AI persona.
# The {user_text} placeholder will be dynamically filled with the text from the Android app.
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
    },
    {
      "germanWord": "<The second key B2-level vocabulary word...>",
      "englishTranslation": "<...>",
      "exampleSentence": "<...>"
    }
  ]
}

IMPORTANT RULES for the `vocabularyList`:
- Only extract nouns, verbs, adjectives, or adverbs that are at a B2 level or higher.
- EXCLUDE simple, common words (e.g., personal pronouns like 'ich', 'du'; articles like 'der', 'die', 'das'; common prepositions like 'in', 'auf'; and A1/A2 verbs like 'sein', 'haben', 'gehen').
- If the user made no relevant vocabulary mistakes, return an empty array: [].
- Provide a maximum of 3 words for the list.
"""

# --- API Endpoint Definition ---
@app.route('/evaluate', methods=['POST'])
def evaluate_text_endpoint():
    """
    This function defines the /evaluate API endpoint.
    It receives text from the app, gets feedback from Gemini, and returns it.
    """
    # 1. Validate the incoming request
    if not request.is_json:
        return jsonify({"error": "Invalid request: Content-Type must be application/json"}), 400

    request_data = request.get_json()
    user_text = request_data.get('text')

    if not user_text or not isinstance(user_text, str) or not user_text.strip():
        return jsonify({"error": "Invalid request: 'text' field is missing or empty"}), 400

    print(f"Received text for evaluation: {user_text[:80]}...") # Log received text

    try:
        # 2. Prepare the prompt and call the Gemini API
        full_prompt = GEMINI_PROMPT_TEMPLATE.format(user_text=user_text)

        # Initialize the generative model. 'gemini-1.5-flash' is efficient for this use case.
        model = genai.GenerativeModel('gemini-1.5-flash')
        
        # Generate the content based on the detailed prompt
        response = model.generate_content(full_prompt)

        # 3. Clean and parse the API response
        # The API might wrap the JSON in markdown backticks. This removes them.
        cleaned_response_text = response.text.strip().replace('```json', '').replace('```', '').strip()
        
        # Convert the cleaned JSON string into a Python dictionary
        response_json = json.loads(cleaned_response_text)
        
        # Add the original text to the response, as the prompt requests
        response_json['originalText'] = user_text

        # 4. Send the structured JSON back to the Android client
        return jsonify(response_json), 200

    except json.JSONDecodeError:
        # Error if the AI response is not valid JSON
        print(f"JSONDecodeError from Gemini response: {cleaned_response_text}")
        return jsonify({"error": "Failed to parse response from AI service. The AI returned invalid JSON."}), 500
    except Exception as e:
        # Catch-all for other potential errors (e.g., API key issue, network problems)
        print(f"An unexpected error occurred: {e}")
        return jsonify({"error": "An internal server error occurred while contacting the AI service."}), 500

# --- Application Runner ---
if __name__ == '__main__':
    # This block runs the app with the Flask development server.
    # For production, a WSGI server like Gunicorn should be used instead.
    # The host '0.0.0.0' makes the server accessible on your local network.
    app.run(host='0.0.0.0', port=5000, debug=True)