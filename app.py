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

# Cache 
cache = {}


# Home route
@app.route("/")
def home():
    return "Server is running!"


# Health endpoint
@app.route("/health", methods=["GET"])
def health():
    return jsonify({
        "status": "ok",
        "service": "ai-service"
    })


# JWT Verification (Day 9)
def verify_jwt():
    token = request.headers.get("Authorization")
    if not token:
        return False
    return True


# Middleware
@app.before_request
def sanitize_and_validate():
    if request.method == "POST":
        data = request.get_json()

        if not data or "text" not in data:
            return jsonify({"error": "Missing input"}), 400

        text = data["text"]

        clean_text = re.sub(r"<.*?>", "", text)

        bad_words = [
            "ignore previous",
            "system prompt",
            "bypass",
            "hack",
            "admin"
        ]

        if any(word in clean_text.lower() for word in bad_words):
            return jsonify({"error": "Malicious input detected"}), 400

        request.cleaned_text = clean_text


# Security headers
@app.after_request
def add_security_headers(response):
    response.headers["X-Content-Type-Options"] = "nosniff"
    response.headers["X-Frame-Options"] = "DENY"
    response.headers["X-XSS-Protection"] = "1; mode=block"
    return response


# Describe endpoint
@app.route("/describe", methods=["POST"])
@limiter.limit("30 per minute")
def describe():
    # JWT check
    if not verify_jwt():
        return jsonify({"error": "Unauthorized"}), 401

    clean_text = request.cleaned_text
    result = generate_text(clean_text)

    return jsonify({"result": result})


# Recommend endpoint
@app.route("/recommend", methods=["POST"])
@limiter.limit("30 per minute")
def recommend():
    # JWT check
    if not verify_jwt():
        return jsonify({"error": "Unauthorized"}), 401

    clean_text = request.cleaned_text

    # Cache check
    if clean_text in cache:
        return jsonify({
            "recommendations": cache[clean_text],
            "cached": True
        })

    prompt = f"""
    You are an expert in business continuity planning.

    Analyze the following risk:
    {clean_text}

    Provide exactly 3 high-quality recommendations.

    Each recommendation must include:
    - action_type (PREVENTIVE / MITIGATION / RECOVERY)
    - description (clear, specific, and actionable)
    - priority (HIGH / MEDIUM / LOW)

    Ensure:
    - Recommendations are practical and realistic
    - Avoid generic or vague statements
    - Use professional language
    - Be specific and include real-world actionable steps

    Return ONLY valid JSON in this format:
    [
      {{
        "action_type": "PREVENTIVE",
        "description": "text",
        "priority": "HIGH"
      }}
    ]
    """

    result = generate_text(prompt)

    result = result.replace("```json", "").replace("```", "").strip()

    try:
        parsed = json.loads(result)
    except:
        parsed = []

    cache[clean_text] = parsed

    return jsonify({
        "recommendations": parsed,
        "cached": False
    })



# Run server
if __name__ == "__main__":
    app.run(host="127.0.0.1", port=5005, debug=False)