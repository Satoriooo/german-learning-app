# server.py
from flask import Flask, request, jsonify
import google.generativeai as genai
import os
import random
import json

app = Flask(__name__)

# List of writing topics
WRITING_TOPICS = [
    "Beschreiben Sie Ihr Wochenende.", # Describe your weekend.
    "Was ist Ihr Lieblingsessen und warum?", # What is your favorite food and why?
    "Schreiben Sie eine E-Mail an einen Freund, um ein Treffen zu planen.", # Write an email to a friend to plan a meeting.
    "Was sind Ihre Pläne für den nächsten Urlaub?", # What are your plans for your next vacation?
    "Beschreiben Sie Ihr Lieblingsbuch oder Ihren Lieblingsfilm.", # Describe your favorite book or movie.
    "Was ist Ihr Traumberuf?", # What is your dream job?
    "Schreiben Sie über eine Person, die Sie bewundern.", # Write about a person you admire.
    "Was machen Sie gerne in Ihrer Freizeit?", # What do you like to do in your free time?
    "Was ist Ihr Lieblingshobby und warum?", # What is your favorite hobby and why?
    "Beschreiben Sie einen perfekten Tag.", # Describe a perfect day.
    "Was ist Ihre schönste Kindheitserinnerung?", # What is your fondest childhood memory?
    "Welches Land möchten Sie am liebsten bereisen und warum?", # Which country would you most like to visit and why?
    "Erzählen Sie von einem lustigen Vorfall, der Ihnen passiert ist.", # Tell a story about a funny incident that happened to you.
    "Was ist der beste Ratschlag, den Sie je erhalten haben?", # What is the best advice you have ever received?
    "Beschreiben Sie das Haus, in dem Sie aufgewachsen sind.", # Describe the house where you grew up.
    "Welche Superkraft hätten Sie gerne und warum?", # What superpower would you like to have and why?
    "Schreiben Sie über ein Thema, das Sie begeistert.", # Write about a topic that you are passionate about.
    "Was ist Ihr Lieblingslied und welche Erinnerungen verbinden Sie damit?", # What is your favorite song and what memories do you associate with it?
    "Beschreiben Sie eine Herausforderung, die Sie gemeistert haben, und was Sie daraus gelernt haben.", # Describe a challenge you have overcome and what you learned from it.
    "Wofür sind Sie im Leben am dankbarsten?", # What are you most grateful for in life?
    "Schreiben Sie eine Bewertung für ein Produkt, das Sie kürzlich verwendet haben.", # Write a review for a product you have used recently.
    "Was ist Ihre Meinung zu sozialen Medien?", # What is your opinion on social media?
    "Wie sieht Ihre typische Morgenroutine aus?", # What does your typical morning routine look like?
    "Welches historische Ereignis finden Sie am faszinierendsten?", # Which historical event do you find most fascinating?
    "Schreiben Sie einen Brief an Ihr zukünftiges Ich.", # Write a letter to your future self.
    "Was ist Ihnen in einer Freundschaft am wichtigsten?", # What is most important to you in a friendship?
    "Beschreiben Sie eine Fähigkeit, die Sie gerne erlernen würden.", # Describe a skill you would like to learn.
    "Was bedeutet Erfolg für Sie?", # What does success mean to you?
    ]

# Configure API Key
try:
    genai.configure(api_key=os.environ["GOOGLE_API_KEY"])
except KeyError:
    print("Error: GOOGLE_API_KEY environment variable not set.")
    exit()

# Configure the generative model
model = genai.GenerativeModel('gemini-2.0-flash')

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

    # --- NEW: Enhanced B2 Evaluation Prompt ---
    # This prompt instructs the model to return a JSON object, making it easy to parse in the app.
    prompt = f"""
    You are an expert German language tutor. Your task is to evaluate a German text submitted by a user in response to a specific topic. Your goal is to provide feedback consistent with B2 level expectations of the CEFR.

    **Instructions:**

    1.  **Evaluate the Text:** Assess the submitted text based on its relevance to the topic: "{topic}". Also evaluate its coherence, vocabulary, and grammar for the B2 level. Do NOT consider the length of the text.
    2.  **Generate a Score:** Based on your evaluation, provide a score from 0 to 100.
    3.  **Correct the Text:** Provide a fully corrected version. In this corrected version, wrap any word that you changed or added in `<c>...</c>` tags. For example, if the user wrote "Ich mag die Tee" and you correct it to "Ich mag den Tee", the output should be "Ich mag <c>den</c> Tee".
    4.  **Explain Mistakes:** Provide a simple explanation in German for the most important corrections.
    5.  **Format Output:** Return your entire response as a single, minified JSON object with no line breaks. The JSON object must have these exact keys: "score", "evaluation", "corrected_text", "explanation".

    **User's Topic:** {topic}
    **User's Text:** {user_text}

    **Example Output Format (minified JSON):**
    {{"score": 85, "evaluation": "Dein Text ist gut strukturiert und thematisch passend. Der Wortschatz ist angemessen, aber es gibt einige Grammatikfehler, besonders bei den Artikeln.", "corrected_text": "Ich mag <c>den</c> Tee.", "explanation": "* **Original:** 'die Tee'\\n* **Korrektur:** 'den Tee'\\n* **Erklärung:** 'Tee' ist ein maskulines Nomen, daher ist im Akkusativ der Artikel 'den' korrekt."}}
    """

    try:
        response = model.generate_content(prompt)
        
        if response and response.text:
            # The AI's response is the JSON string
            feedback_json_str = response.text
            # We don't need to wrap it in another JSON object, just return it directly
            # after ensuring it's valid.
            try:
                # Validate that the string is valid JSON
                json.loads(feedback_json_str)
                return feedback_json_str, 200, {'Content-Type': 'application/json; charset=utf-8'}
            except json.JSONDecodeError:
                print("Error: AI did not return valid JSON.")
                return jsonify({'error': 'AI response was not valid JSON.'}), 500
        else:
            return jsonify({'error': 'Could not generate feedback from AI.'}), 500

    except Exception as e:
        print(f"An error occurred: {e}")
        return jsonify({'error': 'Failed to get feedback from the generative model.'}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)

