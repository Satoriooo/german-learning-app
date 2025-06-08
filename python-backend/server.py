# server.py
from flask import Flask, request, jsonify
import google.generativeai as genai
import os
import random
import json
import re

app = Flask(__name__)

# List of writing topics
WRITING_TOPICS = [
    "Beschreiben Sie Ihr Wochenende.",
    "Was ist Ihr Lieblingsessen und warum?",
    "Schreiben Sie eine E-Mail an einen Freund, um ein Treffen zu planen.",
    "Was sind Ihre Pläne für den nächsten Urlaub?",
    "Beschreiben Sie Ihr Lieblingsbuch oder Ihren Lieblingsfilm.",
    "Was ist Ihr Traumberuf?",
    "Schreiben Sie über eine Person, die Sie bewundern.",
    "Was machen Sie gerne in Ihrer Freizeit?",
]

# Configure API Key
try:
    genai.configure(api_key=os.environ["GOOGLE_API_KEY"])
except KeyError:
    print("Error: GOOGLE_API_KEY environment variable not set.")
    exit()

# Configure the generative model
model = genai.GenerativeModel('gemini-1.5-flash')

@app.route('/topic', methods=['GET'])
def get_topic():
    """Returns a random writing topic."""
    topic = random.choice(WRITING_TOPICS)
    return jsonify({'topic': topic})


@app.route('/feedback', methods=['POST'])
def get_feedback():
    """
    Receives text, evaluates it, and returns a structured JSON object
    including a list of vocabulary words from the corrections.
    """
    if not request.json or 'text' not in request.json or 'topic' not in request.json:
        return jsonify({'error': 'Invalid request. "text" and "topic" fields are required.'}), 400

    user_text = request.json['text']
    topic = request.json['topic']
    print(f"Received text for topic '{topic}': {user_text}")

    word_count = len(user_text.split())
    if word_count < 50:
        print(f"Text is too short ({word_count} words). Returning immediate feedback.")
        # Return an empty vocabulary list for short texts
        return jsonify({
            "score": random.randint(0, 15),
            "evaluation": "Der Text ist mit unter 50 Wörtern viel zu kurz für eine sinnvolle Bewertung.",
            "corrected_text": user_text,
            "explanation": "Bitte schreiben Sie mindestens 50 Wörter, um eine Bewertung zu erhalten.",
            "vocabulary_list": []
        })

    # --- NEW, IMPROVED PROMPT ---
    prompt = f"""
    You are a very strict German language professor evaluating a student's writing for the B2 CEFR level.

    **Analysis Steps:**

    1.  **Correct the Text:** Provide a corrected version. Wrap every single changed or added word in `<c>...</c>` tags. Example: "Ich bin <c>in den</c> Park gegangen."
    2.  **Extract Vocabulary:** From your corrections, identify the **single nouns, verbs, adjectives, or adverbs** that were corrected. Do NOT include pronouns, articles, or prepositions. Create a JSON list of these words. For example, if you corrected "große Haus" to "<c>großes</c> Haus", you should extract "großes". If you corrected "Ich habe...gegeht" to "<c>bin</c>...<c>gegangen</c>", you should extract "gegangen".
    3.  **Generate Score (0-100):** Based on the number and severity of errors.
    4.  **Write Evaluation & Explanation:** Briefly evaluate the text and explain the most critical mistake in simple German.
    5.  **Translate & Create Sentences:** For each word in your vocabulary list, provide a simple English translation, an English example sentence, and a German example sentence.
    6.  **Format Final Output:** Return your entire response as a single, minified JSON object. It MUST contain these exact keys: "score", "evaluation", "corrected_text", "explanation", and "vocabulary_list". The "vocabulary_list" key must contain an array of objects, where each object has these keys: "german_word", "english_translation", "german_sentence", "english_sentence".

    **User's Topic:** {topic}
    **User's Text:** {user_text}
    """
    # --- END OF PROMPT ---

    try:
        response = model.generate_content(prompt)
        
        if not response or not response.text:
             return jsonify({'error': 'AI returned an empty response.'}), 500

        raw_text = response.text
        print(f"Raw AI Response: {raw_text}")

        match = re.search(r'\{.*\}', raw_text, re.DOTALL)
        if not match:
            print("Error: No JSON object found in AI response.")
            return jsonify({'error': 'AI response did not contain a JSON object.'}), 500

        json_str = match.group(0)
        
        try:
            feedback_data = json.loads(json_str)
            # Ensure the vocabulary list key exists, even if it's empty
            if 'vocabulary_list' not in feedback_data:
                feedback_data['vocabulary_list'] = []
            return jsonify(feedback_data)
        except json.JSONDecodeError:
            print(f"Error: AI did not return valid JSON. Extracted string: {json_str}")
            return jsonify({'error': 'AI response was not valid JSON.'}), 500

    except Exception as e:
        print(f"An error occurred during API call: {e}")
        return jsonify({'error': 'Failed to get feedback from the generative model.'}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=os.environ.get("PORT", 10000))