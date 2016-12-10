from urllib2 import Request, urlopen
import json


final = ""
for i in range(1,6):
    request = Request('https://popcornwvnbg7jev.onion.to/shows/' + str(i))

    response_body = urlopen(request).read()
    final = final + response_body[1:-1] + ","

final = "[" + final[:-1] + "]"
final = json.loads(final)


with open("popcorn.json","w") as file:
    file.write(json.dumps(final, indent=4))

headers = {
    'Content-Type': 'application/json',
    'trakt-api-version': '2',
    'trakt-api-key': 'b5ad09993dd34b980f0a997488e92d64e79d0fcf050efe8c03c914a9fca0b457'
}

new_final = ""
for series in final:
    request = Request("https://api.trakt.tv/shows/"+series['imdb_id']+"?extended=full", headers=headers)
    response_body = urlopen(request).read()
    new_final = new_final + response_body + ","

new_final = "[" + new_final[:-1] + "]"
new_final = json.loads(new_final)

with open("trakt_series.json","w") as file:
    file.write(json.dumps(new_final, indent=4))
