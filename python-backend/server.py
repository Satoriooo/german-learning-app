# A safer server.py with better error logging

from flask import Flask, request, jsonify
import google.generativeai as genai
import os
import random
import json
import re
import sys # Import the sys module

app = Flask(__name__)

# --- SAFER API KEY CONFIGURATION ---
api_key = os.environ.get("GOOGLE_API_KEY")
if not api_key:
    # Print the error to standard error so it's more likely to appear in logs
    print("FATAL ERROR: GOOGLE_API_KEY environment variable not set.", file=sys.stderr)
    # Don't exit(), let the app run so we can see the error message.
    # The API calls will fail later, but at least the server starts.
else:
    try:
        genai.configure(api_key=api_key)
        print("Successfully configured Generative AI.")
    except Exception as e:
        print(f"FATAL ERROR: Failed to configure Generative AI: {e}", file=sys.stderr)

# Configure the generative model
model = genai.GenerativeModel('gemini-1.5-flash')

# List of writing topics
WRITING_TOPICS = [
    "Beschreiben Sie Ihr Wochenende.",
    "Was ist Ihr Lieblingsessen und warum?",
    "Schreiben Sie eine E-Mail an einen Freund, um ein Treffen zu planen.",
    "Was sind Ihre Pläne für den nächsten Urlaub?",
    "Beschreiben Sie Ihr Lieblingsbuch oder Ihren Lieblingsfilm.",
]

@app.route('/topic', methods=['GET'])
def get_topic():
    """Returns a random writing topic."""
    # Check if the model was configured
    if not api_key:
        return jsonify({'error': 'Server is missing API key configuration.'}), 500
    topic = random.choice(WRITING_TOPICS)
    return jsonify({'topic': topic})


@app.route('/feedback', methods=['POST'])
def get_feedback():
    """
    Receives text, evaluates it, and returns a structured JSON object.
    """
    if not api_key:
        return jsonify({'error': 'Server is missing API key configuration.'}), 500
        
    if not request.json or 'text' not in request.json or 'topic' not in request.json:
        return jsonify({'error': 'Invalid request. "text" and "topic" fields are required.'}), 400

    user_text = request.json['text']
    topic = request.json['topic']
    
    word_count = len(user_text.split())
    if word_count < 50:
        return jsonify({
            "score": random.randint(0, 15),
            "evaluation": "Der Text ist mit unter 50 Wörtern viel zu kurz für eine sinnvolle Bewertung.",
            "corrected_text": user_text,
            "explanation": "Bitte schreiben Sie mindestens 50 Wörter, um eine Bewertung zu erhalten.",
            "vocabulary_list": []
        })

    prompt = f"""
    You are a very strict German language professor evaluating a student's writing for the B2 CEFR level.

    **Analysis Steps:**

    1.  **Correct the Text:** Provide a corrected version. Wrap every single changed or added word in `<c>...</c>` tags. Example: "Ich bin <c>in den</c> Park gegangen."
    2.  **Extract Vocabulary:** From your corrections, identify the **single nouns, verbs, adjectives, or adverbs** that were corrected. Do NOT include pronouns, articles, or prepositions. Create a JSON list of these words.
    3.  **Generate Score (0-100):** Based on the number and severity of errors.
    4.  **Write Evaluation & Explanation:** Briefly evaluate the text and explain the most critical mistake in simple German.
    5.  **Translate & Create Sentences:** For each word in your vocabulary list, provide a simple English translation, an English example sentence, and a German example sentence.
    6.  **Format Final Output:** Return your entire response as a single, minified JSON object. It MUST contain these exact keys: "score", "evaluation", "corrected_text", "explanation", and "vocabulary_list". The "vocabulary_list" key must contain an array of objects, where each object has these keys: "german_word", "english_translation", "german_sentence", "english_sentence".

    **User's Topic:** {topic}
    **User's Text:** {user_text}
    """
    
    try:
        response = model.generate_content(prompt)
        raw_text = response.text
        match = re.search(r'\{.*\}', raw_text, re.DOTALL)
        if not match:
            print(f"Error: No JSON object found in AI response: {raw_text}", file=sys.stderr)
            return jsonify({'error': 'AI response did not contain a JSON object.'}), 500

        json_str = match.group(0)
        feedback_data = json.loads(json_str)
        if 'vocabulary_list' not in feedback_data:
            feedback_data['vocabulary_list'] = []
        return jsonify(feedback_data)

    except Exception as e:
        print(f"An error occurred during API call: {e}", file=sys.stderr)
        return jsonify({'error': 'Failed to get feedback from the generative model.'}), 500

# This part is intentionally left as is, assuming Render runs it with `python server.py`
if __name__ == '__main__':
    # Render provides the PORT environment variable.
    port = int(os.environ.get('PORT', 10000))
    app.run(host='0.0.0.0', port=port)