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
model = genai.GenerativeModel('gemini-1.5-flash') # Updated to a newer model for potentially better results

# Endpoint to get a random topic
@app.route('/topic', methods=['GET'])
def get_topic():
    """Returns a random writing topic."""
    topic = random.choice(WRITING_TOPICS)
    return jsonify({'topic': topic})


@app.route('/feedback', methods=['POST'])
def get_feedback():
    """
    Receives text and a topic, gets a detailed B2-level evaluation,
    and returns it as a structured JSON object.
    """
    if not request.json or 'text' not in request.json or 'topic' not in request.json:
        return jsonify({'error': 'Invalid request. "text" and "topic" fields are required.'}), 400

    user_text = request.json['text']
    topic = request.json['topic']
    print(f"Received text for topic '{topic}': {user_text}")

    # --- NEW: Pre-evaluation Check in Python ---
    word_count = len(user_text.split())
    if word_count < 50:
        print(f"Text is too short ({word_count} words). Returning immediate feedback.")
        return jsonify({
            "score": random.randint(0, 15), # Give a very low random score
            "evaluation": "Der Text ist mit unter 50 Wörtern viel zu kurz für eine sinnvolle Bewertung. Versuchen Sie, ausführlicher zu schreiben.",
            "corrected_text": user_text, # Return the original text as there's not enough to correct
            "explanation": "Der Text ist zu kurz. Bitte schreiben Sie mindestens 50 Wörter, um eine Bewertung nach dem B2-Niveau zu erhalten."
        })
    # --- END of new check ---

    # --- UPDATED PROMPT ---
    prompt = f"""
    You are a very strict German language professor evaluating a student's writing for the B2 CEFR level. Your feedback must be critical, precise, and adhere to a high standard.

    **Core Instructions:**

    1.  **Analyze the Text:** Scrutinize the text for relevance to the topic, logical structure, grammar, vocabulary, and style. Be particularly harsh on mistakes that a B2 learner should not be making (e.g., basic word order, common noun genders, verb conjugations).
    2.  **Generate a Score (0-100):**
        * **90-100:** Nearly flawless, native-like. (Extremely rare)
        * **75-89:** Excellent work, but with minor, infrequent errors.
        * **60-74:** Good, but with several noticeable errors in grammar or vocabulary.
        * **50-59:** Barely passes. Contains significant errors that hinder communication.
        * **0-49:** Fails the B2 standard. Poor grammar, limited vocabulary, or off-topic.
    3.  **Correct the Text:** Provide a fully corrected version. In this corrected version, wrap every single change (added, removed, or modified words) in `<c>...</c>` tags. For example, correcting "Ich habe zu die Park gegangen" to "Ich bin in den Park gegangen" must be formatted as "<c>Ich bin in den</c> Park <c>gegangen</c>". Do not bold the text.
    4.  **Explain Mistakes:** In simple German, explain the top 2-3 most critical mistakes. Focus on patterns of errors.
    5.  **Format Output:** Return your entire response as a single, minified JSON object with no extra text or line breaks before or after it. The JSON object must have these four keys and only these four: "score", "evaluation", "corrected_text", "explanation".

    **User's Topic:** {topic}
    **User's Text:** {user_text}
    """
    # --- END of updated prompt ---

    try:
        response = model.generate_content(prompt)
        
        if not response or not response.text:
             return jsonify({'error': 'AI returned an empty response.'}), 500

        raw_text = response.text
        print(f"Raw AI Response: {raw_text}")

        # The AI should now be better at returning only JSON, but we'll keep the regex as a safeguard
        match = re.search(r'\{.*\}', raw_text, re.DOTALL)
        if not match:
            print("Error: No JSON object found in AI response.")
            # Fallback: Try to wrap the response in a JSON structure if it's just a string
            return jsonify({'error': 'AI response did not contain a JSON object.'}), 500

        json_str = match.group(0)
        
        try:
            feedback_data = json.loads(json_str)
            return jsonify(feedback_data)
        except json.JSONDecodeError:
            print(f"Error: AI did not return valid JSON. Extracted string: {json_str}")
            return jsonify({'error': 'AI response was not valid JSON.'}), 500

    except Exception as e:
        print(f"An error occurred during API call: {e}")
        return jsonify({'error': 'Failed to get feedback from the generative model.'}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=os.environ.get("PORT", 10000))