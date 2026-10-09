"""Local explanation service. Production must run behind HTTPS and an authenticated,
rate-limited gateway. Provider keys live only in the server environment."""
import json, os, urllib.request, urllib.error
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
MODELS = json.loads(Path(__file__).with_name('models.json').read_text())
def explain(formula):
    if formula not in MODELS:
        raise ValueError('Unsupported molecule')
    key = os.environ.get('AI_API_KEY')
    if not key:
        raise RuntimeError('AI provider is not configured')
    facts = MODELS[formula]
    request = urllib.request.Request('https://api.openai.com/v1/chat/completions',
        data=json.dumps({'model': os.environ.get('AI_MODEL', 'gpt-4o-mini'),
        'messages': [{'role':'system','content':'Explain only the supplied validated molecular facts in plain language. Do not propose new diagrams, bond assignments, or other molecules. State that bond angles are approximate.'},
                     {'role':'user','content':json.dumps(facts)}], 'max_tokens':350}).encode(),
        headers={'Authorization':'Bearer '+key, 'Content-Type':'application/json'})
    with urllib.request.urlopen(request, timeout=25) as response:
        return json.load(response)['choices'][0]['message']['content']
class Handler(BaseHTTPRequestHandler):
    def respond(self, status, data):
        body=json.dumps(data).encode(); self.send_response(status)
        self.send_header('Content-Type','application/json'); self.send_header('Content-Length',str(len(body)))
        self.end_headers();self.wfile.write(body)
    def do_GET(self):
        self.respond(200 if self.path=='/health' else 404, {'status':'ok'} if self.path=='/health' else {'error':'Not found'})
    def do_POST(self):
        if self.path != '/explain': return self.respond(404, {'error':'Not found'})
        try:
            length=int(self.headers.get('Content-Length','0'))
            if not 0 < length <= 1024: return self.respond(413, {'error':'Invalid request size'})
            payload=json.loads(self.rfile.read(length));formula=payload.get('formula')
            if not isinstance(formula,str) or formula not in MODELS: return self.respond(422, {'error':'Unsupported molecule'})
            self.respond(200, {'explanation':explain(formula), 'facts':MODELS[formula]})
        except (ValueError,TypeError,AttributeError): self.respond(400, {'error':'Invalid request'})
        except RuntimeError: self.respond(503, {'error':'AI provider is not configured'})
        except Exception: self.respond(502, {'error':'AI provider unavailable'})
if __name__=='__main__':
    ThreadingHTTPServer(('127.0.0.1',int(os.environ.get('PORT','8080'))),Handler).serve_forever()
