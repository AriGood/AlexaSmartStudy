import json
import openai
import os
import sys
import webbrowser
     
# Load your API key from an environment variable or secret store
openai.api_key = "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"

# Get the file path from the command line argument
file_path = sys.argv[1]

audio_file= open(file_path, "rb")
transcript = openai.Audio.transcribe("whisper-1", audio_file, language="en")

response=openai.ChatCompletion.create(
  model="gpt-3.5-turbo",
  messages=[
        {"role": "system", "content": "you are an ai program that when given a transcript of a class writes complex study notes with bullet points based on the transcript from said class they will be formatted with HTML at the top and you put a <h2> with the title at the top of the page you also link the CSS page styles.css so the page will look nice and have examples if there is math involved in the notes"},
        {"role": "user", "content": "Can you turn the following into study notes remember to include examples if it is math also add practice questions at the bottom: "+ transcript['text']}
  ]
)

reply = response["choices"][0]["message"]["content"]
with open('output.html', 'w') as file:
        file.write(reply)
print(reply)
# webbrowser.open('output.html')



