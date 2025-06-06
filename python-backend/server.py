# Install required libraries:
# pip install Flask google-generativeai

from flask import Flask, request, jsonify
import google.generativeai as genai
import os
import random # Import the random library

app = Flask(__name__)

# --- NEW: List of writing topics ---
WRITING_TOPICS = [
    "Beschreiben Sie Ihr Wochenende.", # Describe your weekend.
    "Was ist Ihr Lieblingsessen und warum?", # What is your favorite food and why?
    "Schreiben Sie eine E-Mail an einen Freund, um ein Treffen zu planen.", # Write an email to a friend to plan a meeting.
    "Was sind Ihre Pläne für den nächsten Urlaub?", # What are your plans for your next vacation?
    "Beschreiben Sie Ihr Lieblingsbuch oder Ihren Lieblingsfilm.", # Describe your favorite book or movie.
    "Was ist Ihr Traumberuf?", # What is your dream job?
    "Schreiben Sie über eine Person, die Sie bewundern.", # Write about a person you admire.
    "Was machen Sie gerne in Ihrer Freizeit?", # What do you like to do in your free time?
]

# --- Configure API Key ---
try:
    genai.configure(api_key=os.environ["GOOGLE_API_KEY"])
except KeyError:
    print("Error: GOOGLE_API_KEY environment variable not set.")
    exit()

model = genai.GenerativeModel('gemini-2.0-flash')

# --- NEW: Endpoint to get a random topic ---
@app.route('/topic', methods=['GET'])
def get_topic():
    """
    Returns a random writing topic from the list.
    """
    topic = random.choice(WRITING_TOPICS)
    return jsonify({'topic': topic})


@app.route('/feedback', methods=['POST'])
def get_feedback():
    """
    Receives text, gets feedback, and returns it.
    """
    if not request.json or 'text' not in request.json:
        return jsonify({'error': 'Invalid request. "text" field is required.'}), 400

    user_text = request.json['text']
    print(f"Received text from app: {user_text}") 

    prompt = f"""
    **Task:** Correct the following German sentence.
    **Provide output in this format:**
    1.  **Corrected:** [The corrected sentence]
    2.  **Explanation:** [A brief explanation of the errors]

    **German sentence to correct:**
    ---
    {user_text}
    ---
    """

    try:
        response = model.generate_content(prompt)
        
        if response and response.text:
            feedback_text = response.text
        else:
            feedback_text = "Sorry, could not generate feedback at this moment."

        return jsonify({'feedback': feedback_text})

    except Exception as e:
        print(f"An error occurred: {e}")
        return jsonify({'error': 'Failed to get feedback from the generative model.'}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)

