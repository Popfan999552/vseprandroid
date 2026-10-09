import unittest
from unittest.mock import patch
import server
class Tests(unittest.TestCase):
 def test_reject_unsupported_before_provider(self):
  with self.assertRaises(ValueError): server.explain('C6H6')
 def test_missing_key(self):
  with patch.dict('os.environ',{},clear=True):
   with self.assertRaises(RuntimeError): server.explain('ClF3')
 def test_resonance_facts(self):
  self.assertEqual(server.MODELS['NO2-']['electrons'],18)
  self.assertIn('resonance',server.MODELS['NO2-']['note'])
if __name__=='__main__': unittest.main()

class ProviderTests(unittest.TestCase):
 def test_provider_only_receives_curated_facts(self):
  import io, json
  response=io.BytesIO(json.dumps({'choices':[{'message':{'content':'Validated explanation'}}]}).encode())
  with patch.dict('os.environ',{'AI_API_KEY':'test-only-key'}), patch('server.urllib.request.urlopen',return_value=response) as call:
   self.assertEqual(server.explain('IF5'),'Validated explanation')
   request=call.call_args.args[0]
   payload=json.loads(request.data)
   self.assertEqual(json.loads(payload['messages'][1]['content'])['geometry'],'Square pyramidal')
   self.assertNotIn('test-only-key',request.data.decode())
