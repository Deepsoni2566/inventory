from flask import Flask, request, jsonify
import openai
import os

app = Flask(__name__)

# Set your OpenAI API key as an environment variable
openai.api_key = os.getenv("OPENAI_API_KEY")

@app.route('/analyze_product', methods=['POST'])
def analyze_product():
    data = request.json
    name = data.get("name")
    description = data.get("description")

    prompt = f"""
You are an AI product assistant. A customer is adding a product:
Name: {name}
Description: {description}

1. Identify possible allergies.
2. Suggest 2 similar recommended products.
Return result as JSON with keys: allergies, recommendations.
"""

    try:
        response = openai.ChatCompletion.create(
            model="gpt-4",
            messages=[{"role": "user", "content": prompt}],
            max_tokens=150
        )
        content = response['choices'][0]['message']['content']
        return jsonify({"response": content})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(port=5005)
