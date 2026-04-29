print("Test started...")

from services.groq_client import generate_text

result = generate_text("What is business continuity?")
print("Result:", result)