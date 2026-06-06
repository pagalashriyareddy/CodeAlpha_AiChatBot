import urllib.request
import urllib.error
import json

url='https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=AIzaSyC6DTfEidskLwTQLKO_VK1O5Upl5RnBl8E'
data=json.dumps({'contents':[{'parts':[{'text':'Hello'}]}]}).encode('utf-8')
req=urllib.request.Request(url, data=data, headers={'Content-Type': 'application/json'})
try:
    r=urllib.request.urlopen(req)
    print(r.read().decode())
except urllib.error.HTTPError as e:
    print(e.read().decode())
