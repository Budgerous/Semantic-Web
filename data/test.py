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
    try:
        request = Request("https://api.trakt.tv/shows/"+series['imdb_id']+"?extended=full", headers=headers)
        response_body = urlopen(request).read()
        tmp = json.loads(response_body)

        try:
            new_request = Request("http://www.omdbapi.com/?i="+series['imdb_id'])
            new_response_body = urlopen(new_request).read()
            new_tmp = json.loads(new_response_body)

            tmp['poster'] = new_tmp['Poster']

            new_request = Request("https://api.trakt.tv/shows/"+series['imdb_id']+"/people", headers=headers)
            new_response_body = urlopen(new_request).read()
            new_tmp = json.loads(new_response_body)

            tmp['cast'] = new_tmp['cast']
            tmp['crew'] = new_tmp['crew']

            new_final = new_final + json.dumps(tmp) + ","
        except Exception, Argument:
            print Argument
            new_final = new_final + json.dumps(tmp) + ","

    except Exception, Argument:
        print Argument
        print series['slug']

new_final = "[" + new_final[:-1] + "]"
new_final = json.loads(new_final)

with open("series.json","w") as file:
    file.write(json.dumps(new_final, indent=4))
