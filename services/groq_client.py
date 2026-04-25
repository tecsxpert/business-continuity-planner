import os
import time
from groq import Groq
from dotenv import load_dotenv

load_dotenv()

client = Groq(api_key=os.getenv("GROQ_API_KEY"))

def generate_text(prompt):
    for i in range(3):  # try 3 times
        try:
            response = client.chat.completions.create(
                model="llama-3.3-70b-versatile",
                messages=[{"role": "user", "content": prompt}]
            )
            return response.choices[0].message.content
        
        except Exception as e:
            print("Error:", e)
            time.sleep(2)

    return "AI failed. Please try again."