from flask import Flask, request, jsonify
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address
from services.groq_client import generate_text
import re
import json

app = Flask(__name__)

# Rate Limiter
limiter = Limiter(key_func=get_remote_address)
limiter.init_app(app)


# Home route (for testing)
@app.route("/")
def home():
    return "Server is running!"


# Middleware (runs before every POST request)
@app.before_request
def sanitize_and_validate():
    if request.method == "POST":
        data = request.get_json()

        # 1. Check input
        if not data or "text" not in data:
            return jsonify({"error": "Missing input"}), 400

        text = data["text"]

        # 2. Remove HTML
        clean_text = re.sub(r"<.*?>", "", text)

        # 3. Detect prompt injection
        bad_words = [
            "ignore previous",
            "system prompt",
            "bypass",
            "hack",
            "admin"
        ]

        if any(word in clean_text.lower() for word in bad_words):
            return jsonify({"error": "Malicious input detected"}), 400

        # 4. Save cleaned text
        request.cleaned_text = clean_text


# Main API
@app.route("/describe", methods=["POST"])
@limiter.limit("30 per minute")
def describe():
    clean_text = request.cleaned_text

    result = generate_text(clean_text)

    return jsonify({
        "result": result
    })

@app.route("/recommend", methods=["POST"])
@limiter.limit("30 per minute")
def recommend():
    clean_text = request.cleaned_text

    prompt = f"""
    Give exactly 3 business continuity recommendations for this risk:

    {clean_text}

    Return ONLY valid JSON in this format:
    [
      {{
        "action_type": "PREVENTIVE",
        "description": "text",
        "priority": "HIGH"
      }}
    ]

    Do NOT include explanations or extra text.
    """

    result = generate_text(prompt)

    # 🔧 Clean unwanted formatting (like ```json)
    result = result.replace("```json", "").replace("```", "").strip()

    try:
        parsed = json.loads(result)
    except:
        parsed = []

    return jsonify({
        "recommendations": parsed
    })


# Run server
if __name__ == "__main__":
    app.run(host="127.0.0.1", port=5001, debug=True)